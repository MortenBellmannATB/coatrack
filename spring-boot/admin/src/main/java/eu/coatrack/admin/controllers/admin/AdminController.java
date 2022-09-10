package eu.coatrack.admin.controllers.admin;

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

import be.ceau.chart.DoughnutChart;
import be.ceau.chart.LineChart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coatrack.admin.UserSessionSettings;
import eu.coatrack.admin.components.WebUI;
import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.logic.CreateProxyAction;
import eu.coatrack.admin.logic.CreateServiceAction;
import eu.coatrack.admin.model.repository.*;
import eu.coatrack.admin.model.vo.*;
import eu.coatrack.admin.service.GatewayHealthMonitorService;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.admin.service.admin.ChartService;
import eu.coatrack.admin.service.admin.GeneralStats;
import eu.coatrack.admin.service.admin.StatisticService;
import eu.coatrack.admin.service.user.UserService;
import eu.coatrack.api.*;
import eu.coatrack.config.github.GithubEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Timon Veenstra <tveenstra@bebr.nl>
 * @author Bruno Silva <silva@atb-bremen.de>
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private static final String ADMIN_HOME_VIEW = "admin/dashboard";
    private static final String ADMIN_CONSUMER_HOME_VIEW = "admin/consumer_dashboard";
    private static final String ADMIN_WIZARD_VIEW = "admin/wizard/wizard";
    private static final String ADMIN_STARTPAGE = "admin/startpage";
    private static final String ADMIN_CONSUMER_WIZARD = "admin/consumer_wizard/wizard";
    private static final String ADMIN_PROFILE = "admin/profile/profile";
    private static final String GITHUB_API_USER = "https://api.github.com/user";
    private static final String GITHUB_API_EMAIL = GITHUB_API_USER + "/emails";
    private static final String GATEWAY_HEALTH_MONITOR_FRAGMENT = "admin/fragments/gateway_health_monitor :: gateway-health-monitor";

    @Autowired
    private UserService userService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private MetricsAggregationCustomRepository metricsAggregationCustomRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private CreateProxyAction createProxyAction;

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    @Autowired
    private WebUI webUI;

    @Autowired
    private UserSessionSettings session;

    @Autowired
    private GatewayHealthMonitorService gatewayHealthMonitorService;


    @RequestMapping(value = "", method = GET)
    public ModelAndView home() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView mav;

        if (auth != null) {
            User exisitingUser = userService.getAuthenticatedUser();

            if (exisitingUser != null)
                mav = prepareModelAndViewByUser(exisitingUser);
            else
                mav = prepareModelAndViewByAuthentication(auth);
        } else {
            // User is not authenticated
            String springVersion = webUI.parameterizedMessage
                    ("home.spring.version", SpringBootVersion.getVersion(), SpringVersion.getVersion());

            mav = new ModelAndView("home")
                    .addObject("springVersion", springVersion);
        }
        return mav;
    }

    private ModelAndView prepareModelAndViewByUser(User user) {
        List<ServiceApi> services = serviceApiService.findFromActiveUser();
        ModelAndView mav = new ModelAndView();

        if (services != null && !services.isEmpty()) {
            mav.setViewName(ADMIN_HOME_VIEW);
            // The user is already stored in our database
            mav.addObject("stats", loadGeneralStatistics(
                    session.getDashboardDateRangeStart(),
                    session.getDashboardDateRangeEnd())
            );

            mav.addObject("userStatistics", getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                    session.getDashboardDateRangeStart(),
                    session.getDashboardDateRangeEnd())
            );
        } else {
            if (!user.getInitialized()) {
                // temporalFirstTimeFlag = false;
                mav.setViewName(ADMIN_STARTPAGE);
            } else {
                // IT IS A CONSUMER USER
                mav.setViewName(ADMIN_CONSUMER_HOME_VIEW);
            }
        }
        return mav;
    }

    private ModelAndView prepareModelAndViewByAuthentication(Authentication auth) throws JsonProcessingException {
        // The user is new for our database therefore we try to retrieve as much user
        // info is possible from Github
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + details.getTokenValue());
        HttpEntity<String> githubRequest = new HttpEntity<String>(headers);

        ResponseEntity<String> userInfoResponse = restTemplate.exchange(GITHUB_API_USER, HttpMethod.GET,
                githubRequest, String.class);
        String userInfo = userInfoResponse.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Map<String, Object> userMap = objectMapper.readValue(userInfo,
                new TypeReference<Map<String, Object>>() {
                });
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

        return new ModelAndView("register")
                .addObject("user", user);
    }

    @RequestMapping(value = "/consumer/gettingstarted", method = GET)
    @ResponseBody
    public ModelAndView gettingStartedWizardForConsumer() {
        ApiKey newApiKey = null;
        ServiceApi gettingStartedTestService = serviceApiService.getTestServiceForConsumerWizard(adminConfig);

        return new ModelAndView(ADMIN_CONSUMER_WIZARD)
                .addObject("testService", gettingStartedTestService)
                .addObject("testApiKeys", newApiKey);
    }

    @RequestMapping(value = "/dashboard/gateway-health-monitor", method = GET)
    @ResponseBody
    public ModelAndView getGatewayHealthMonitorGuiFragment() {
        log.debug("client request for Gateway Health Monitor Data");

        GatewayHealthMonitorService.DataForGatewayHealthMonitor dataForGatewayHealthMonitor = gatewayHealthMonitorService
                .getGatewayHealthMonitorData();

        return new ModelAndView(GATEWAY_HEALTH_MONITOR_FRAGMENT)
                .addObject("gatewayHealthMonitorProxyData", dataForGatewayHealthMonitor);
    }

    @RequestMapping(value = "/gettingstarted", method = GET)
    public ModelAndView gettingStartedWizard() {
        return new ModelAndView(ADMIN_WIZARD_VIEW);
    }

    @RequestMapping(value = "/profiles", method = GET)
    public ModelAndView goProfiles() {
        return new ModelAndView(ADMIN_PROFILE);
    }

    @PostMapping(value = "/serviceWizard")
    public ModelAndView serviceWizard(ServiceWizardForm wizard) {
        ServiceWizardResponse response = new ServiceWizardResponse();

        User authenticatedUser = userService.getAuthenticatedUser();
        if (authenticatedUser != null) {
            ServiceApi serviceApi = serviceApiService.createServiceApi(wizard, authenticatedUser);

            // TODO should be in dedicated Services
            Proxy proxy = createProxy(authenticatedUser, serviceApi, adminConfig.getProxyServerDefaultPort());
            ApiKey apiKey = createApiKey(authenticatedUser, serviceApi);

            userService.initializeUser(authenticatedUser);

            response = new ServiceWizardResponse(
                    wizard.getServiceName(),
                    serviceApi.getUriIdentifier(),
                    wizard.getServiceUrl(),
                    wizard.isServiceForFree(),
                    wizard.isMonthlyCharged(),
                    wizard.isChargedPerCall(),
                    wizard.getServiceCost(),
                    authenticatedUser.getUsername(),
                    apiKey.getKeyValue(),
                    getDownloadLink(proxy.getId())
            );
        }
        return new ModelAndView("admin/wizard/serviceWizardResult")
                .addObject("wizardResponse", response);
    }

    private static String getDownloadLink(String id) {
        //Path.of("admin", "proxies", proxy.getId(), "download").toString();
        return String.format("/admin/proxies/%s/download", id);
    }


    @RequestMapping(value = "/dashboard/statisticsPerApiConsumerInDescendingOrderByNoOfCalls", method = GET, produces = "application/json")
    @ResponseBody
    public List<StatisticsPerApiUser> getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<StatisticsPerApiUser> userStatistics = new ArrayList<>();
        if (auth != null) {
            userStatistics = metricsAggregationCustomRepository.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                    selectedTimePeriodStart,
                    selectedTimePeriodEnd,
                    auth.getName()
            );
        }
        return userStatistics;
    }

    @RequestMapping(value = "/dashboard/userStatsDoughnutChart", method = GET, produces = "application/json")
    @ResponseBody
    public DoughnutChart generateUserStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        DoughnutChart chart = new DoughnutChart();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            List<StatisticsPerApiUser> userStatsList = metricsAggregationCustomRepository.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                    selectedTimePeriodStart,
                    selectedTimePeriodEnd,
                    auth.getName()
            );

            chart = chartService.createDoughnutChartFromUserStatistics(userStatsList);
        }
        return chart;
    }

    @RequestMapping(value = "/dashboard/httpResponseStatsChart", method = GET, produces = "application/json")
    @ResponseBody
    public DoughnutChart generateHttpResponseStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        DoughnutChart chart = new DoughnutChart();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            List<StatisticsPerHttpStatusCode> statisticsPerHttpStatusCodeList = metricsAggregationCustomRepository.getNoOfCallsPerHttpResponseCode(
                    from,
                    until,
                    auth.getName()
            );

            chart = chartService.createDoughnutChartFromHttpStatistics(statisticsPerHttpStatusCodeList);
        }
        return chart;
    }

    @RequestMapping(value = "/dashboard/statsPerDayLineChart", method = GET, produces = "application/json")
    @ResponseBody
    public LineChart generateStatsPerDayLineChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until)
    {
        LineChart chart = new LineChart();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            List<StatisticsPerDay> statisticsPerDays = metricsAggregationCustomRepository.getNoOfCallsPerDayForDateRange
                    (from, until, auth.getName());

            chart = chartService.createLineChartFromStatisticsPerDays(statisticsPerDays, from, until);
        }
        return chart;
    }

    @RequestMapping(value = "/dashboard/metricsByLoggedUserStatistics", method = GET, produces = "application/json")
    @ResponseBody
    private Iterable<Metric> loadCallsStatistics(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd
    ) {
        String logMessage = String.format("List of Calls by the user during the date range from %s and %s", selectedTimePeriodStart, selectedTimePeriodEnd);
        log.debug(logMessage);

        List<Metric> metrics = new ArrayList<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            metrics = metricRepository.retrieveByUserConsumer(
                    auth.getName(),
                    Date.valueOf(selectedTimePeriodStart),
                    Date.valueOf(selectedTimePeriodEnd)
            );
        }
        return metrics;
    }

    @RequestMapping(value = "/dashboard/generalStatistics", method = GET, produces = "application/json")
    @ResponseBody
    private GeneralStats loadGeneralStatistics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("until") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        log.debug("session before update is : {}", session);
        session.setDashboardDateRangeStart(from);
        session.setDashboardDateRangeEnd(until);
        log.debug("session after update is : {}", session);

        GeneralStats stats = new GeneralStats();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            stats = statisticService.prepareStatsForApiProviderUsername(auth.getName(), from, until);
        }
        return stats;
    }


    // Refresh the Test Api Key and the URL to call the service in the getting
    // started wizard for consumer
    @RequestMapping(value = "/consumer/gettingstarted/refreshApiKeys/{whichFragmentToLoad}", method = GET)
    public String refreshApiKeys(@PathVariable("whichFragmentToLoad") String whichFragmentToLoad, Model model) {
        ServiceApi gettingStartedTestService = serviceApiService.getTestServiceForConsumerWizard(adminConfig);
        List<ApiKey> apiKeysForTestService = apiKeyRepository.findByLoggedInAPIConsumerAndServiceId
                (gettingStartedTestService.getId());

        // Gets the last Getting Started Test Service APIKey, as it can be used several times
        ApiKey newApiKey = apiKeysForTestService.get(apiKeysForTestService.size() - 1);

        // get the URL(s) for the test service proxy(s)
        List<String> proxiesUrlList = proxyRepository.customSearchForAllProxiesForGivenServiceApiId(newApiKey.getServiceApi().getId())
                .stream().map(Proxy::getPublicUrl).filter(Objects::nonNull)
                .filter(publicUrl -> !publicUrl.isEmpty()).collect(Collectors.toList());

        if (proxiesUrlList.size() > 1) {
            log.info("Please notice the list of proxies is larger than one, so for test consumer we will take into account only first one");
        }

        List<String> defaultProxyList = new ArrayList<>();
        if (!proxiesUrlList.isEmpty()) {
            defaultProxyList.add(proxiesUrlList.get(0));
        }

        Map<String, List<String>> proxyURLPerNewApiKey = new TreeMap<>();
        proxyURLPerNewApiKey.putIfAbsent(newApiKey.getKeyValue(), defaultProxyList);

        model.addAttribute("testService", gettingStartedTestService);
        model.addAttribute("proxiesPerApiKey", proxyURLPerNewApiKey);
        model.addAttribute("apiKeys", newApiKey);

        // decide about which fragment to return
        String returnedFragment = (whichFragmentToLoad.equals("table"))
                // returns the ApiKeyTable Fragment
                ? "admin/fragments/api-keys/consumer/list :: apiKeyTable"
                // returns the Url Fragment
                : "admin/fragments/consumer_wizard/wizard :: gatewayCallURL";

        return returnedFragment;
    }



    @RequestMapping(value = "/dashboard/gateway-health-monitor/notification-status", method = POST)
    @ResponseBody
    public void updateNotificationStatusOnGatewayHealthMonitor(@RequestParam String proxyId, @RequestParam boolean isMonitoringEnabled) {
        Proxy proxy = proxyRepository.findById(proxyId).orElse(null);
        if(proxy != null) {
            proxy.setHealthMonitoringEnabled(isMonitoringEnabled);
            log.debug("Changing the monitoring status of proxy {} to {}", proxy.getName(), proxy.isHealthMonitoringEnabled());
            proxyRepository.save(proxy);
        } else {
            log.debug("Requested proxy could not be found! Please check your data!");
        }
    }


    //TODO should be shifted to apiKeyService
    @Deprecated
    private ApiKey createApiKey(User authenticatedUser, ServiceApi serviceApi) {
        createApiKeyAction.setServiceApi(serviceApi);
        createApiKeyAction.setUser(authenticatedUser);
        createApiKeyAction.execute();
        return createApiKeyAction.getApiKey();
    }

    //TODO should be shifted to proxyService
    @Deprecated
    private Proxy createProxy(User authenticatedUser, ServiceApi service, int proxyServerDefaultPort) {
        Proxy proxy = new Proxy();
        proxy.setPort(proxyServerDefaultPort);
        proxy.setName("Gateway for " + service.getName());
        proxy.setOwner(authenticatedUser);
        proxy.setDescription("Gateway generated by the getting started wizard");

        createProxyAction.setProxy(proxy);
        createProxyAction.setUser(authenticatedUser);
        createProxyAction.setSelectedServices(Collections.singletonList(service.getId()));
        createProxyAction.execute();
        return createProxyAction.getProxy();
    }

}
