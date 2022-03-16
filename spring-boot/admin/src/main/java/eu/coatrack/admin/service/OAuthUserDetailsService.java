package eu.coatrack.admin.service;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2022 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import eu.coatrack.config.github.GithubEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OAuthUserDetailsService {

    private static final String GITHUB_API_EMAIL = "https://api.github.com/user/emails";

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    private OAuth2User getLoggedInUser() {
        return (OAuth2User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public String getLoginNameFromLoggedInUser() {
        return getLoggedInUser().getAttribute("login");
    }

    public String getNameFromLoggedInUser() {
        return getLoggedInUser().getAttribute("name");
    }

    public String getCompanyFromLoggedInUser() {
        return getLoggedInUser().getAttribute("company");
    }

    public String getEmailFromLoggedInUser() throws JsonProcessingException {
        String email = getLoggedInUser().getAttribute("email");

        if (email == null || email.isEmpty()) {
            ResponseEntity<String> emailListFromGithub = getEmailsListFromGithub();
            email = getPrimaryEmailFromLoggedInUser(emailListFromGithub);
        }
        return email;
    }

    private ResponseEntity<String> getEmailsListFromGithub() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + getTokenFromLoggedInUser());
        HttpEntity<String> githubRequest = new HttpEntity(headers);

        return restTemplate.exchange(GITHUB_API_EMAIL, HttpMethod.GET,
                githubRequest, String.class);
    }

    private String getPrimaryEmailFromLoggedInUser(ResponseEntity<String> userEmailsRequest) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CollectionType mapResponseWithEmailsListToAListOfGithubEmails = objectMapper.getTypeFactory().constructCollectionType(List.class, GithubEmail.class);
        List<GithubEmail> emailsList = objectMapper.readValue(userEmailsRequest.getBody(), mapResponseWithEmailsListToAListOfGithubEmails);

        return emailsList.stream()
                .filter(email -> (email.isMailAddressVerified() && email.isUsersPrimaryMailAddress()))
                .map(GithubEmail::getEmail)
                .findFirst()
                .orElse(null);
    }

    public String getTokenFromLoggedInUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId,
                oauthToken.getName());
        return client.getAccessToken().getTokenValue();
    }
}