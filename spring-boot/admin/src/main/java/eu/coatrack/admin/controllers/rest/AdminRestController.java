package eu.coatrack.admin.controllers.rest;

import be.ceau.chart.DoughnutChart;
import be.ceau.chart.LineChart;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coatrack.admin.UserSessionSettings;
import eu.coatrack.admin.model.GeneralStats;
import eu.coatrack.admin.model.vo.StatisticsPerApiUser;
import eu.coatrack.admin.service.ChartService;
import eu.coatrack.admin.service.MetricService;
import eu.coatrack.admin.service.ProxyService;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.api.Metric;
import eu.coatrack.api.Proxy;
import eu.coatrack.api.ServiceApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static eu.coatrack.admin.utils.PathProvider.ADMIN_BASE_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@RequestMapping(value = ADMIN_BASE_PATH + "/dashboard")
public class AdminRestController {
    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private UserSessionSettings session;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private ProxyService proxyService;

    private static ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping(value = "/generalStatistics", produces = "application/json")
    @ResponseBody
    private GeneralStats loadGeneralStatistics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("until") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        session.setDashboardDateRangeStart(from);
        session.setDashboardDateRangeEnd(until);

        GeneralStats stats = new GeneralStats();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            List<ServiceApi> offeredServices = serviceApiService.findFromActiveUser();
            stats = metricService.getGeneralStats(from, until, auth.getName(), offeredServices);
        }
        return stats;
    }

    @GetMapping(value = "/statisticsPerApiConsumerInDescendingOrderByNoOfCalls", produces = "application/json")
    @ResponseBody
    public List<StatisticsPerApiUser> getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<StatisticsPerApiUser> userStatistics = new ArrayList<>();
        if (auth != null) {
            userStatistics = metricService.getStatisticsPerApiConsumer
                    (selectedTimePeriodStart, selectedTimePeriodEnd, auth.getName());
        }
        return userStatistics;
    }

    @GetMapping(value = "/userStatsDoughnutChart", produces = "application/json")
    @ResponseBody
    public DoughnutChart generateUserStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        DoughnutChart chart = new DoughnutChart();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            chart = chartService.generateUserStatisticsDoughnutChart(from, until, auth.getName());
        }
        return chart;
    }

    @GetMapping(value = "/statsPerDayLineChart", produces = "application/json")
    @ResponseBody
    public LineChart generateStatsPerDayLineChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LineChart chart = new LineChart();
        if (auth != null)
            chart = chartService.generateStatsPerDayLineChart(from, until, auth.getName());
        return chart;
    }

    @PostMapping(value = "/gateway-health-monitor/notification-status")
    @ResponseBody
    public void updateNotificationStatusOnGatewayHealthMonitor(
            @RequestParam String proxyId,
            @RequestParam boolean isMonitoringEnabled
    ) {
        Proxy proxy = proxyService.findById(proxyId);
        proxy.setHealthMonitoringEnabled(isMonitoringEnabled);
        log.debug("Changing the monitoring status of proxy {} to {}", proxy.getName(), proxy.isHealthMonitoringEnabled());
        proxyService.save(proxy);
    }

    @GetMapping(value = "/metricsByLoggedUserStatistics", produces = "application/json")
    @ResponseBody
    private Iterable<Metric> loadCallsStatistics(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        log.debug("List of Calls by the user during the date range from " + from + " and "
                + until);

        List<Metric> callStatistics = new ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            callStatistics = metricService.getByUserCustomer(from, until, auth.getName());
        }

        return callStatistics;
    }

    @GetMapping(value = "/httpResponseStatsChart", produces = "application/json")
    @ResponseBody
    public DoughnutChart generateHttpResponseStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until
    ) {
        DoughnutChart chart = new DoughnutChart();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            chart = chartService.generateHttpResponseStatisticsDoughnutChart(from, until, auth.getName());
        return chart;
    }
}
