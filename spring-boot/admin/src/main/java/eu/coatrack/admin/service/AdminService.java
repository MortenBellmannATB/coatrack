package eu.coatrack.admin.service;

import be.ceau.chart.DoughnutChart;
import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.DoughnutData;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.DoughnutDataset;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.enums.PointStyle;
import be.ceau.chart.options.LineOptions;
import be.ceau.chart.options.scales.LinearScale;
import be.ceau.chart.options.scales.LinearScales;
import be.ceau.chart.options.ticks.LinearTicks;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coatrack.admin.UserSessionSettings;
import eu.coatrack.admin.components.WebUI;
import eu.coatrack.admin.controllers.AdminController;
import eu.coatrack.admin.controllers.ReportController;
import eu.coatrack.admin.controllers.UserController;
import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.logic.CreateProxyAction;
import eu.coatrack.admin.logic.CreateServiceAction;
import eu.coatrack.admin.model.repository.*;
import eu.coatrack.admin.model.vo.*;
import eu.coatrack.admin.service.GatewayHealthMonitorService;
import eu.coatrack.api.*;
import eu.coatrack.config.github.GithubEmail;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Service
public class AdminService {
    private static final String ADMIN_HOME_VIEW = "admin/dashboard";

    @Value("${ygg.admin.gettingStarted.consumer.testService.provider.username}")
    private String gettingStartedTestServiceProvider;

    @Value("${ygg.proxy.server.port.defaultValue}")
    private int proxyServerDefaultPort;

    @Value("${ygg.admin.gettingStarted.consumer.testService.uriIdentifier}")
    private String gettingStartedTestServiceIdentifier;

    private static final String ADMIN_CONSUMER_HOME_VIEW = "admin/consumer_dashboard";
    private static final String ADMIN_WIZARD_VIEW = "admin/wizard/wizard";
    private static final String ADMIN_STARTPAGE = "admin/startpage";
    private static final String ADMIN_CONSUMER_WIZARD = "admin/consumer_wizard/wizard";
    private static final String ADMIN_PROFILE = "admin/profile/profile";
    private static final String GITHUB_API_USER = "https://api.github.com/user";
    private static final String GITHUB_API_EMAIL = GITHUB_API_USER + "/emails";
    private static final String GATEWAY_HEALTH_MONITOR_FRAGMENT = "admin/fragments/gateway_health_monitor :: gateway-health-monitor";
    private static final Map<Integer, Color> chartColorsPerHttpResponseCode;

    static {
        Map<Integer, Color> colorMap = new HashMap<>();
        colorMap.put(400, Color.ORANGE);
        colorMap.put(401, Color.SALMON);
        colorMap.put(403, Color.LIGHT_YELLOW);
        colorMap.put(404, new Color(255, 255, 102)); // yellow
        colorMap.put(500, Color.RED);
        colorMap.put(503, Color.ORANGE_RED);
        colorMap.put(504, Color.DARK_RED);
        chartColorsPerHttpResponseCode = Collections.unmodifiableMap(colorMap);
    }

    /* REPOSITORIES */
    @Autowired
    MetricsAggregationCustomRepository metricsAggregationCustomRepository;

    @Autowired
    MetricRepository metricRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ServiceApiRepository serviceApiRepository;

    @Autowired
    private CreateProxyAction createProxyAction;

    @Autowired
    private CreateServiceAction createServiceAction;

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    @Autowired
    UserController userController;

    @Autowired
    private ReportService reportService;

    @Autowired
    WebUI webUI;

    @Autowired
    UserSessionSettings session;

    @Autowired
    GatewayHealthMonitorService gatewayHealthMonitorService;

    public ModelAndView goProfiles(Model model) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(ADMIN_PROFILE);
        return mav;
    }

    //TODO check with Christoph if this needs to be broken down into smaller methods
    public ModelAndView home(Model model, HttpServletRequest request) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView mav = new ModelAndView();

        if (auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName());

            if (user != null) {

                List<ServiceApi> services = serviceApiRepository.findByOwnerUsername(auth.getName());
                if (services != null && !services.isEmpty()) {
                    mav.setViewName(ADMIN_HOME_VIEW);
                    // The user is already stored in our database
                    mav.addObject("stats", loadGeneralStatistics(session.getDashboardDateRangeStart(),
                            session.getDashboardDateRangeEnd()));

                    mav.addObject("userStatistics", getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                            session.getDashboardDateRangeStart(),
                            session.getDashboardDateRangeEnd()));
                } else {
                    if (!user.getInitialized())
                        mav.setViewName(ADMIN_STARTPAGE);
                    else
                        mav.setViewName(ADMIN_CONSUMER_HOME_VIEW);
                }
            } else {

                // The user is new for our database therefore we try to retrieve as much user info as possible from Github
                OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "token " + details.getTokenValue());
                HttpEntity<String> githubRequest = new HttpEntity<>(headers);

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

                user = new User();
                user.setUsername((String) userMap.get("login"));
                user.setFirstname((String) userMap.get("name"));
                user.setCompany((String) userMap.get("company"));

                if (email != null) {
                    user.setEmail(email);
                }

                mav.addObject("user", user);

                mav.setViewName("register");
            }

        } else {
            // User is not authenticated, it is quite weird according to the Github login page but I prefer to prevent the case
            String springVersion = webUI.parameterizedMessage("home.spring.version", SpringBootVersion.getVersion(),
                    SpringVersion.getVersion());
            model.addAttribute("springVersion", springVersion);
            mav.setViewName("home");
        }
        return mav;
    }

    public ModelAndView gettingStartedWizard(Model model) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(ADMIN_WIZARD_VIEW);
        return mav;
    }

    public List<StatisticsPerApiUser> getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<StatisticsPerApiUser> userStatistics = metricsAggregationCustomRepository.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                selectedTimePeriodStart, selectedTimePeriodEnd, auth.getName());
        return userStatistics;
    }

    public DoughnutChart generateUserStatisticsDoughnutChart(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<StatisticsPerApiUser> userStatsList = metricsAggregationCustomRepository.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
                selectedTimePeriodStart, selectedTimePeriodEnd, auth.getName());

        DoughnutDataset dataset = new DoughnutDataset()
                .setLabel("API calls")
                .addBackgroundColors(Color.AQUA_MARINE, Color.LIGHT_BLUE, Color.LIGHT_SALMON, Color.LIGHT_BLUE, Color.GRAY)
                .setBorderWidth(2);

        DoughnutChart generatedChart;

        if (userStatsList.size() > 0) {
            userStatsList.forEach(stats -> dataset.addData(stats.getNoOfCalls()));
            DoughnutData data = new DoughnutData().addDataset(dataset);
            userStatsList.forEach(stats -> data.addLabel(stats.getUserName()));
            generatedChart = new DoughnutChart(data);
        } else
            generatedChart = new DoughnutChart();
        return generatedChart;
    }

    public DoughnutChart generateHttpResponseStatisticsDoughnutChart(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<StatisticsPerHttpStatusCode> statisticsPerHttpStatusCodeList = metricsAggregationCustomRepository.getNoOfCallsPerHttpResponseCode(
                selectedTimePeriodStart, selectedTimePeriodEnd, auth.getName());
        DoughnutChart generatedChart;

        if (statisticsPerHttpStatusCodeList.size() > 0) {
            List<Color> chartColors = new ArrayList<>();
            for (StatisticsPerHttpStatusCode statisticsPerHttpStatusCode : statisticsPerHttpStatusCodeList) {
                int statusCode = statisticsPerHttpStatusCode.getStatusCode();
                Color colorForStatusCode = getColorByStatusCode(statusCode);
                chartColors.add(colorForStatusCode);
            }

            DoughnutDataset dataset = new DoughnutDataset()
                    .setLabel("HTTP response codes")
                    .addBackgroundColors(chartColors.toArray(new Color[0]))
                    .setBorderWidth(2);

            statisticsPerHttpStatusCodeList.forEach(stats -> dataset.addData(stats.getNoOfCalls()));
            DoughnutData data = new DoughnutData().addDataset(dataset);
            statisticsPerHttpStatusCodeList.forEach(stats -> data.addLabel(stats.getStatusCode().toString()));
            generatedChart = new DoughnutChart(data);
        } else
            generatedChart = new DoughnutChart();
        return generatedChart;
    }

    private Color getColorByStatusCode(int statusCode) {
        Color color = chartColorsPerHttpResponseCode.get(statusCode);

        if (color == null) {
            // there is no fixed color defined for this status code, set it based on the range
            if (statusCode >= 200 && statusCode < 300)
                color = new Color(0, 204, 0); // lighter green
            else if (statusCode >= 300 && statusCode < 400)
                color = Color.LIGHT_BLUE;
            else if (statusCode >= 404 && statusCode < 500)
                color = Color.DARK_ORANGE;
            else if (statusCode >= 500 && statusCode < 600)
                color = new Color(255, 51, 51); // red
            else
                color = Color.LIGHT_GRAY;
        }
        return color;
    }

    public LineChart generateStatsPerDayLineChart(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<StatisticsPerDay> statsList = metricsAggregationCustomRepository.getNoOfCallsPerDayForDateRange(
                selectedTimePeriodStart, selectedTimePeriodEnd, auth.getName());

        // create a map with entries for all days in the given date range
        Map<LocalDate, Long> callsPerDay = new TreeMap<>();
        long timePeriodDurationInDays = ChronoUnit.DAYS.between(selectedTimePeriodStart, selectedTimePeriodEnd);
        for (int i = 0; i <= timePeriodDurationInDays; i++) {
            // put "0" as default, in case no calls are registered in database
            callsPerDay.put(selectedTimePeriodStart.plusDays(i), 0L);
        }

        // add numbers from database, if any
        statsList.forEach(statisticsPerDay -> callsPerDay.put(statisticsPerDay.getLocalDate(), statisticsPerDay.getNoOfCalls()));

        // create actual chart
        LineDataset dataset = new LineDataset()
                .setLabel("Total number of API calls per day")
                .setBackgroundColor(Color.LIGHT_YELLOW)
                .setBorderWidth(3);

        LineData data = new LineData().addDataset(dataset);

        callsPerDay.forEach((date, noOfCalls) -> {
            data.addLabel(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            dataset.addData(noOfCalls)
                    .addPointStyle(PointStyle.CIRCLE)
                    .addPointBorderWidth(2)
                    .setLineTension(0f)
                    .setSteppedLine(false)
                    .addPointBackgroundColor(Color.LIGHT_YELLOW)
                    .addPointBorderColor(Color.LIGHT_GRAY);
        });

        LinearTicks ticks = new LinearTicks().setBeginAtZero(true);
        LinearScales scales = new LinearScales().addyAxis(new LinearScale().setTicks(ticks));
        LineOptions options = new LineOptions().setScales(scales);
        return new LineChart(data, options);
    }

    public Iterable<Metric> loadCallsStatistics(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        log.debug("List of Calls by the user during the date range from {} and {}", selectedTimePeriodStart, selectedTimePeriodEnd);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return metricRepository.retrieveByUserConsumer(auth.getName(), Date.valueOf(selectedTimePeriodStart), Date.valueOf(selectedTimePeriodEnd));
    }

    public AdminController.GeneralStats loadGeneralStatistics(LocalDate selectedTimePeriodStart, LocalDate selectedTimePeriodEnd) {
        log.debug("session before update is : {}", session);
        // store new selected time period in user session settings
        session.setDashboardDateRangeStart(selectedTimePeriodStart);
        session.setDashboardDateRangeEnd(selectedTimePeriodEnd);
        log.debug("session after update is : {}", session);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long timePeriodDurationInDays = ChronoUnit.DAYS.between(selectedTimePeriodStart, selectedTimePeriodEnd);

        String apiProviderUsername = auth.getName();
        int callsThisPeriod = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls(
                selectedTimePeriodStart, selectedTimePeriodEnd, apiProviderUsername);

        LocalDate previousTimePeriodEnd = selectedTimePeriodStart.minusDays(1);
        LocalDate previousTimePeriodStart = previousTimePeriodEnd.minusDays(timePeriodDurationInDays);

        int callsPreviousPeriod = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls(
                previousTimePeriodStart, previousTimePeriodEnd, apiProviderUsername);

        AdminController.GeneralStats stats = new AdminController.GeneralStats();
        stats.dateFrom = selectedTimePeriodStart;
        stats.dateUntil = selectedTimePeriodEnd;
        stats.callsThisPeriod = callsThisPeriod;
        stats.callsDiff = callsThisPeriod - callsPreviousPeriod;
        stats.errorsThisPeriod = metricsAggregationCustomRepository.getNumberOfErroneousApiCalls(
                selectedTimePeriodStart, selectedTimePeriodEnd, apiProviderUsername);

        stats.errorsTotal = metricsAggregationCustomRepository.getNumberOfErroneousApiCalls(
                previousTimePeriodStart, previousTimePeriodEnd, apiProviderUsername);

        stats.users = metricsAggregationCustomRepository.getNumberOfApiCallers(selectedTimePeriodStart,
                selectedTimePeriodEnd, apiProviderUsername);

        stats.callsTotal = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls(selectedTimePeriodStart,
                selectedTimePeriodEnd, apiProviderUsername);

        stats.revenueTotal = reportService.calculateTotalRevenueForApiProvider(auth.getName(),
                selectedTimePeriodStart, selectedTimePeriodEnd);

        return stats;
    }

    public ModelAndView serviceWizard(ServiceWizardForm wizard, BindingResult bindingResult, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());

        // Create Service
        ServiceApi serviceApi = new ServiceApi();
        serviceApi.setName(wizard.getServiceName());
        serviceApi.setLocalUrl(wizard.getServiceUrl());
        serviceApi.setOwner(user);
        serviceApi.setServiceAccessPermissionPolicy(ServiceAccessPermissionPolicy.PERMISSION_NECESSARY);
        serviceApi.setDescription("Generated by the getting started wizard");

        if (wizard.getServiceForFree())
            serviceApi.setServiceAccessPaymentPolicy(ServiceAccessPaymentPolicy.FOR_FREE);
        else if (wizard.getMonthlyCharge()) {
            serviceApi.setServiceAccessPaymentPolicy(ServiceAccessPaymentPolicy.MONTHLY_FEE);
            serviceApi.setMonthlyFee(Double.parseDouble(wizard.getServiceCost()));
        } else {
            serviceApi.setServiceAccessPaymentPolicy(ServiceAccessPaymentPolicy.WELL_DEFINED_PRICE);
            List<EntryPoint> entryPoints = new ArrayList<>();

            EntryPoint entryPoint = new EntryPoint();
            entryPoint.setHttpMethod("GET");
            entryPoint.setName("/");
            entryPoint.setPathPattern("/");
            entryPoint.setPricePerCall(Double.parseDouble(wizard.getServiceCost()));

            entryPoints.add(entryPoint);
            serviceApi.setEntryPoints(entryPoints);
        }

        createServiceAction.setUser(user);
        createServiceAction.setServiceApi(serviceApi);
        createServiceAction.execute();

        ServiceApi newServiceApi = createServiceAction.getServiceApi();

        // Create Proxy
        Proxy proxy = new Proxy();
        proxy.setPort(proxyServerDefaultPort);
        proxy.setName("Gateway for " + wizard.getServiceName());
        proxy.setOwner(user);
        proxy.setDescription("Gateway generated by the getting started wizard");
        List<Long> serviceIdList = new ArrayList<>();
        serviceIdList.add(newServiceApi.getId());
        createProxyAction.setProxy(proxy);
        createProxyAction.setUser(user);
        createProxyAction.setSelectedServices(serviceIdList);
        createProxyAction.execute();

        // Create an Api
        createApiKeyAction.setServiceApi(newServiceApi);
        createApiKeyAction.setUser(user);
        createApiKeyAction.execute();
        ApiKey apiKey = createApiKeyAction.getApiKey();

        // Update the User Flag
        user.setInitialized(Boolean.TRUE);
        userRepository.save(user);

        // Prepare response
        ServiceWizardResponse response = new ServiceWizardResponse();
        response.setApiKey(apiKey.getKeyValue());
        response.setMonthlyCharge(wizard.getMonthlyCharge());
        response.setPercallCharge(wizard.getPercallCharge());
        response.setProxyUrl("/admin/proxies/" + proxy.getId() + "/download");
        response.setServiceCost(wizard.getServiceCost());
        response.setServiceForFree(wizard.getServiceForFree());
        response.setServiceName(wizard.getServiceName());
        response.setUriIdentifier(newServiceApi.getUriIdentifier());
        response.setServiceUrl(wizard.getServiceUrl());
        response.setUserName(user.getUsername());

        ModelAndView mav = new ModelAndView();
        mav.addObject("wizardResponse", response);
        mav.setViewName("admin/wizard/serviceWizardResult");
        return mav;
    }

    public ModelAndView gettingStartedWizardForConsumer(Model model) {
        ApiKey dummyApiKey = null;
        ServiceApi gettingStartedTestService = loadTestServiceForConsumerWizard();
        ModelAndView mav = new ModelAndView();
        mav.setViewName(ADMIN_CONSUMER_WIZARD);
        mav.addObject("testService", gettingStartedTestService);
        model.addAttribute("testApiKeys", dummyApiKey);
        return mav;
    }

    public String refreshApiKeys(String whichFragmentToLoad, Model model) {
        ServiceApi gettingStartedTestService = loadTestServiceForConsumerWizard();
        List<ApiKey> apiKeysForTestService = apiKeyRepository.findByLoggedInAPIConsumerAndServiceId(gettingStartedTestService.getId());

        // Gets the last Getting Started Test Service APIKey, as the Getting Started for Consumer can be used several times
        ApiKey newApiKey = apiKeysForTestService.get(apiKeysForTestService.size() - 1);

        // get the URL(s) for the test service proxy(s)
        Map<String, List<String>> proxyURLPerNewApiKey = new TreeMap<>();
        //TODO check if still working as expected
        List<String> proxiesUrlList = proxyRepository
                .customSearchForAllProxiesForGivenServiceApiId(newApiKey.getServiceApi().getId()).stream()
                .map(Proxy::getPublicUrl).filter(Objects::nonNull)
                .filter(publicUrl -> publicUrl.equals("")).collect(Collectors.toList());

        if (proxiesUrlList.size() > 1)
            log.info("Please notice the list of proxies is larger than one, so for test consumer we will take into account only first one");

        List<String> defaultProxyList = new ArrayList<>();
        if (!proxiesUrlList.isEmpty())
            defaultProxyList.add(proxiesUrlList.get(0));

        proxyURLPerNewApiKey.putIfAbsent(newApiKey.getKeyValue(), defaultProxyList);

        model.addAttribute("testService", gettingStartedTestService);
        model.addAttribute("proxiesPerApiKey", proxyURLPerNewApiKey);
        model.addAttribute("apiKeys", newApiKey);

        // decide about which fragment to return
        String returnedFragment = (whichFragmentToLoad.equals("table"))
                ? "admin/fragments/api-keys/consumer/list :: apiKeyTable" // ApiKeyTable Fragment
                : "admin/fragments/consumer_wizard/wizard :: gatewayCallURL"; // Url Fragment
        return returnedFragment;
    }

    private ServiceApi loadTestServiceForConsumerWizard() {
        // Getting the test service
        log.debug("trying to load test service for provider '{}' and uriIdentifier '{}'",
                gettingStartedTestServiceProvider, gettingStartedTestServiceIdentifier);

        ServiceApi testServiceApi = serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(
                gettingStartedTestServiceProvider, gettingStartedTestServiceIdentifier);

        log.debug("test service loaded: {}", testServiceApi);
        return testServiceApi;
    }

    public ModelAndView getGatewayHealthMonitorGuiFragment() {
        ModelAndView mav = new ModelAndView();
        log.debug("client request for Gateway Health Monitor Data");
        GatewayHealthMonitorService.DataForGatewayHealthMonitor dataForGatewayHealthMonitor = gatewayHealthMonitorService.getGatewayHealthMonitorData();
        mav.addObject("gatewayHealthMonitorProxyData", dataForGatewayHealthMonitor);
        mav.setViewName(GATEWAY_HEALTH_MONITOR_FRAGMENT);
        return mav;
    }

    public boolean updateNotificationStatusOnGatewayHealthMonitor(String proxyId, boolean isMonitoringEnabled) {
        Proxy proxy = proxyRepository.findById(proxyId).orElse(null);
        boolean found = false;
        if(proxy != null) {
            proxy.setHealthMonitoringEnabled(isMonitoringEnabled);
            log.debug("Changing the monitoring status of proxy {} to {}", proxy.getName(), proxy.isHealthMonitoringEnabled());
            proxyRepository.save(proxy);
            found = true;
        }
        return found;
    }
}
