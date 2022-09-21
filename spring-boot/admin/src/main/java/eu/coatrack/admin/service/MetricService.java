package eu.coatrack.admin.service;

import eu.coatrack.admin.model.GeneralStats;
import eu.coatrack.admin.model.repository.MetricRepository;
import eu.coatrack.admin.model.repository.MetricsAggregationCustomRepository;
import eu.coatrack.admin.model.vo.StatisticsPerApiUser;
import eu.coatrack.admin.model.vo.StatisticsPerDay;
import eu.coatrack.admin.model.vo.StatisticsPerHttpStatusCode;
import eu.coatrack.admin.service.report.ReportService;
import eu.coatrack.api.Metric;
import eu.coatrack.api.ServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
public class MetricService {
    @Autowired
    private MetricsAggregationCustomRepository metricsAggregationCustomRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private ReportService reportService;

    public List<StatisticsPerDay> getNoOfCallsPerDayForDateRange(LocalDate from, LocalDate until, String apiProviderUsername) {
        return metricsAggregationCustomRepository.getNoOfCallsPerDayForDateRange(from, until, apiProviderUsername);
    }

    public List<StatisticsPerApiUser> getStatisticsPerApiConsumer
            (LocalDate from, LocalDate until, String apiProviderUsername) {
        return metricsAggregationCustomRepository.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(from, until, apiProviderUsername);
    }

    public List<Metric> getByUserCustomer(LocalDate from, LocalDate until, String apiProviderUsername) {
        return metricRepository.retrieveByUserConsumer(apiProviderUsername, Date.valueOf(from), Date.valueOf(until));
    }

    public GeneralStats getGeneralStats(LocalDate from, LocalDate until, String apiProviderUsername, List<ServiceApi> offeredServices) {
        long timePeriodDurationInDays = ChronoUnit.DAYS.between(from, until);

        LocalDate previousTimePeriodEnd = from.minusDays(1);
        LocalDate previousTimePeriodStart = previousTimePeriodEnd.minusDays(timePeriodDurationInDays);

        int callsThisPeriod = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls(from,
                until, apiProviderUsername);
        int callsPreviousPeriod = metricsAggregationCustomRepository
                .getTotalNumberOfLoggedApiCalls(previousTimePeriodStart, previousTimePeriodEnd, apiProviderUsername);

        GeneralStats stats = new GeneralStats();
        stats.dateFrom = from;
        stats.dateUntil = until;

        stats.callsThisPeriod = callsThisPeriod;
        stats.callsDiff = callsThisPeriod - callsPreviousPeriod;

        stats.errorsThisPeriod = metricsAggregationCustomRepository
                .getNumberOfErroneousApiCalls(from, until, apiProviderUsername);
        stats.errorsTotal = metricsAggregationCustomRepository.getNumberOfErroneousApiCalls(previousTimePeriodStart,
                previousTimePeriodEnd, apiProviderUsername);

        stats.users = metricsAggregationCustomRepository.getNumberOfApiCallers(from,
                until, apiProviderUsername);
        stats.callsTotal = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls(from,
                until, apiProviderUsername);

        stats.revenueTotal = reportService.reportTotalRevenueForApiProvider(offeredServices, from, until);

        return stats;
    }

    public List<StatisticsPerHttpStatusCode> getNoOfCallsPerHttpResponseCode(LocalDate from, LocalDate until, String apiProviderUsername) {
        return metricsAggregationCustomRepository.getNoOfCallsPerHttpResponseCode(from, until, apiProviderUsername);
    }

}
