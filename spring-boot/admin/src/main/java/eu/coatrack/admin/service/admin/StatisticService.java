package eu.coatrack.admin.service.admin;

import eu.coatrack.admin.model.repository.MetricsAggregationCustomRepository;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.admin.service.report.ReportService;
import eu.coatrack.api.ServiceApi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Service
public class StatisticService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private MetricsAggregationCustomRepository metricsAggregationCustomRepository;

    public GeneralStats prepareStatsForApiProviderUsername(
            String apiProviderUsername, LocalDate from, LocalDate until
    ) {
        long timePeriodDurationInDays = ChronoUnit.DAYS.between(from, until);

        LocalDate previousTimePeriodEnd = from.minusDays(1);
        LocalDate previousTimePeriodStart = previousTimePeriodEnd.minusDays(timePeriodDurationInDays);

        int callsThisPeriod = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls
                (from, until, apiProviderUsername);

        int callsPreviousPeriod = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls
                (previousTimePeriodStart, previousTimePeriodEnd, apiProviderUsername);

        int callsTotal = metricsAggregationCustomRepository.getTotalNumberOfLoggedApiCalls
                (from, until, apiProviderUsername);

        int errorsTotal = metricsAggregationCustomRepository.getNumberOfErroneousApiCalls
                (previousTimePeriodStart, previousTimePeriodEnd, apiProviderUsername);

        int callsDiff = callsThisPeriod - callsPreviousPeriod;

        int errorsThisPeriod = metricsAggregationCustomRepository
                .getNumberOfErroneousApiCalls(from, until, apiProviderUsername);


        long userCount = metricsAggregationCustomRepository.getNumberOfApiCallers(from, until, apiProviderUsername);

        List<ServiceApi> offeredServices = serviceApiService.findByOwnerUsername(apiProviderUsername);
        double revenueTotal = reportService.reportTotalRevenueForApiProvider(offeredServices, from, until);

        GeneralStats stats = new GeneralStats(
                from,
                until,
                callsTotal,
                errorsTotal,
                revenueTotal,
                callsThisPeriod,
                errorsThisPeriod,
                callsDiff,
                userCount
        );
        return stats;
    }



}
