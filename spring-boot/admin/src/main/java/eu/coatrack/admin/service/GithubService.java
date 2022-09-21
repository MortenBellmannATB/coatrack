package eu.coatrack.admin.service;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2020 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.coatrack.api.User;
import eu.coatrack.config.github.GithubEmail;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import eu.coatrack.config.github.GithubUser;
import eu.coatrack.config.github.GithubUserList;
import eu.coatrack.config.github.GithubUserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static eu.coatrack.admin.utils.PathProvider.GITHUB_API_EMAIL;
import static eu.coatrack.admin.utils.PathProvider.GITHUB_API_USER;

/**
 *
 * @author perezdf
 */
@Component
public class GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubService.class);

    private static CacheManager cacheManager;

    private static final String GITHUB_API_SEARCH_USERS = "https://api.github.com/search/users";

    private final ObjectMapper objectMapper;

    public GithubService() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        cacheManager = CacheManager.create();
        if (cacheManager.getCache("githubUsersCache") == null) {
            Cache githubUserCache = new net.sf.ehcache.Cache(
                    new CacheConfiguration("githubUsersCache", 1000)
                            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                            .eternal(false)
                            .timeToLiveSeconds(600)
                            .timeToIdleSeconds(300)
                            .diskExpiryThreadIntervalSeconds(0));
            cacheManager.addCache(githubUserCache);

        }

        if (cacheManager.getCache("githubQueryCache") == null) {

            Cache githubQueryCache = new net.sf.ehcache.Cache(
                    new CacheConfiguration("githubQueryCache", 1000)
                            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                            .eternal(false)
                            .timeToLiveSeconds(600)
                            .timeToIdleSeconds(300)
                            .diskExpiryThreadIntervalSeconds(0));
            cacheManager.addCache(githubQueryCache);
        }
    }

    public List<GithubUserProfile> findGithubUserProfileByCriteria(String criteria) throws IOException {

        List<GithubUserProfile> githubUserProfileList = null;
        if (criteria != null && !criteria.isEmpty()) {

            GithubUserList githubUserListObject = findGithubUserByCriteria(criteria);

            List<GithubUser> githubUserList = githubUserListObject.getItems();

            githubUserProfileList = new ArrayList<>();
            for (GithubUser item : githubUserList) {
                githubUserProfileList.add(findGithubUserProfile(item));
            }

        }

        return githubUserProfileList;
    }

    public List<GithubUserProfile> findGithubUserProfileByUsername(String username) throws IOException {

        List<GithubUserProfile> githubUserProfileList = null;
        if (username != null && !username.isEmpty()) {

            GithubUserList githubUserListObject = findGithubUserByUsername(username);

            List<GithubUser> githubUserList = githubUserListObject.getItems();

            githubUserProfileList = new ArrayList<>();
            for (GithubUser item : githubUserList) {
                // We retrieve exact matches
                if (item.getLogin().equals(username)) {
                    githubUserProfileList.add(findGithubUserProfile(item));
                }
            }

        }

        return githubUserProfileList;
    }

    public GithubUserProfile findGithubUserProfile(GithubUser githubUser) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        Cache githubUserCache = cacheManager.getCache("githubUsersCache");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();

        GithubUserProfile githubUserProfile = null;
        Element githubUserProfileWrapperCached = githubUserCache.get(githubUser.getId());
        if (githubUserProfileWrapperCached == null) {

            try {
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + details.getTokenValue());
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                ResponseEntity<String> response = restTemplate.exchange(URI.create(githubUser.getUrl()), HttpMethod.GET, request, String.class);
                githubUserProfile = objectMapper.readValue(response.getBody(), GithubUserProfile.class);
                Element githubUserProfileWrapper = new Element(githubUserProfile.getId(), githubUserProfile);
                githubUserCache.put(githubUserProfileWrapper);

            } catch (HttpClientErrorException e) {
                log.error("Error during the call : " + githubUser.getUrl());
            }
        } else {
            githubUserProfile = (GithubUserProfile) githubUserProfileWrapperCached.getObjectValue();
        }

        return githubUserProfile;
    }

    public GithubUserList findGithubUserByCriteria(String criteriaArg) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String criteria = URLEncoder.encode(criteriaArg, "UTF-8");
        Cache githubQueryCache = cacheManager.getCache("githubQueryCache");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();

        Element githubQueeyWrapperCached = githubQueryCache.get(criteria);
        String queryResult = null;
        if (githubQueeyWrapperCached == null) {
            try {
                URI uri = URI.create(GITHUB_API_SEARCH_USERS + "?q=" + criteria + "+in:login+" + criteria + "+in:email+" + criteria + "+in:name");
                log.debug("Attempting: " + uri.toString());

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + details.getTokenValue());
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

                queryResult = response.getBody();
                if (queryResult != null) {
                    Element githubUserProfileWrapper = new Element(criteria, queryResult);
                    githubQueryCache.put(githubUserProfileWrapper);
                }

            } catch (HttpClientErrorException e) {
                log.error("Error during the call : " + GITHUB_API_SEARCH_USERS + "?q=" + criteria + "\n" + e.getMessage());
            }
        } else {
            queryResult = (String) githubQueeyWrapperCached.getObjectValue();
        }
        GithubUserList rawQueryList = null;
        if (queryResult != null && !queryResult.isEmpty()) {
            rawQueryList = objectMapper.readValue(queryResult, GithubUserList.class);
        }

        return rawQueryList;
    }

    public GithubUserList findGithubUserByUsername(String username) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String criteria = URLEncoder.encode(username, "UTF-8");
        Cache githubQueryCache = cacheManager.getCache("githubQueryCache");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();

        Element githubQueeyWrapperCached = githubQueryCache.get(criteria);
        String queryResult = null;
        if (githubQueeyWrapperCached == null) {
            try {
                URI uri = URI.create(GITHUB_API_SEARCH_USERS + "?q=" + criteria + "+in:login");
                log.debug("Attempting: " + uri.toString());

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + details.getTokenValue());
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

                queryResult = response.getBody();
                if (queryResult != null) {
                    Element githubUserProfileWrapper = new Element(criteria, queryResult);
                    githubQueryCache.put(githubUserProfileWrapper);
                }

            } catch (HttpClientErrorException e) {
                log.error("Error during the call : " + GITHUB_API_SEARCH_USERS + "?q=" + criteria + "\n" + e.getMessage());
            }
        } else {
            queryResult = (String) githubQueeyWrapperCached.getObjectValue();
        }
        GithubUserList rawQueryList = null;
        if (queryResult != null && !queryResult.isEmpty()) {
            rawQueryList = objectMapper.readValue(queryResult, GithubUserList.class);
        }

        return rawQueryList;
    }

    public User getUserInfoViaGithub(Object authenticationDetails) throws JsonProcessingException {
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authenticationDetails;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + details.getTokenValue());
        HttpEntity<String> githubRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse =  restTemplate.exchange(GITHUB_API_USER, HttpMethod.GET, githubRequest, String.class);
        String userInfo = userInfoResponse.getBody();


        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> userMap = objectMapper.readValue(userInfo, typeRef);

        String email = (String) userMap.get("email");
        if (email == null || email.isEmpty()) {

            ResponseEntity<String> userEmailsResponse = restTemplate.exchange(GITHUB_API_EMAIL, HttpMethod.GET,
                    githubRequest, String.class);
            String userEmails = userEmailsResponse.getBody();
            List<GithubEmail> emailsList = objectMapper.readValue(userEmails,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GithubEmail.class));

            Iterator<GithubEmail> it = emailsList.iterator();
            boolean found = false;
            if (emailsList.size() > 0) {
                while (!found && it.hasNext()) {
                    GithubEmail githubEmail = it.next();
                    if (githubEmail.getVerified()) {
                        email = githubEmail.getEmail();
                        found = true;
                    }
                }
                if (!found) {
                    email = emailsList.get(0).getEmail();
                }
            }
        }

        User user = new User();
        user.setUsername((String) userMap.get("login"));
        user.setFirstname((String) userMap.get("name"));
        user.setCompany((String) userMap.get("company"));

        if (email != null) {
            user.setEmail(email);
        }
        return user;
    }

}
