package eu.coatrack.admin.service.report;

import eu.coatrack.admin.model.repository.ServiceApiRepository;
import eu.coatrack.api.*;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.coatrack.admin.utils.DateUtils.getDateFromString;


@Slf4j
@Service
@AllArgsConstructor
@Setter
public class ReportService {
    // TODO serviceApi dependency can be moved up by 1 layer, there is no other usage
    @Deprecated
    @Autowired
    private final ServiceApiRepository serviceApiRepository;

    @Autowired
    private ApiUsageCalculator apiUsageCalculator;

    public DataTableView<ApiUsageReport> reportApiUsage(ApiUsageDTO apiUsageDTO) {
        List<ApiUsageReport> apiUsageReports;

        if (apiUsageDTO != null && apiUsageDTO.getService() != null) {
           apiUsageReports = apiUsageCalculator.calculateForSpecificService(apiUsageDTO);
        } else {
            apiUsageReports = new ArrayList<>();
        }

        DataTableView<ApiUsageReport> table = new DataTableView<>();
        table.setData(apiUsageReports);
        return table;
    }

    public double reportTotalRevenueForApiProvider(List<ServiceApi> offeredServices, Date from, Date until) {
        List<ApiUsageReport> apiUsageReportsForAllOfferedServices = new ArrayList<>();

        for (ServiceApi service : offeredServices) {
            ApiUsageDTO apiUsageDTO = new ApiUsageDTO(service, null, from, until, true, false);
            List<ApiUsageReport> calculatedApiUsage = calculateApiUsageReportForSpecificService(apiUsageDTO);
            apiUsageReportsForAllOfferedServices.addAll(calculatedApiUsage);
        }

        double total = apiUsageReportsForAllOfferedServices.stream().mapToDouble(ApiUsageReport::getTotal).sum();
        return total;
    }

    public List<ApiUsageReport> calculateApiUsageReportForSpecificService(ApiUsageDTO apiUsageDTO) {
        return apiUsageCalculator.calculateForSpecificService(apiUsageDTO);
    }

    // TODO this has to be changed with AdminController refactoring
    @Deprecated
    public double reportTotalRevenueForApiProvider(String apiProviderUsername, LocalDate timePeriodStart, LocalDate timePeriodEnd) {
        Date from = java.sql.Date.valueOf(timePeriodStart);
        Date until = java.sql.Date.valueOf(timePeriodEnd);

        // TODO move to ServiceApiService after refactoring admin controller, serviceApiRepository can be moved to AdminController
        List<ServiceApi> offeredServices = serviceApiRepository.findByOwnerUsername(apiProviderUsername);
        return reportTotalRevenueForApiProvider(offeredServices, from, until);
    }


    public ServiceUsageStatisticsDTO getServiceUsageStatistics(
            String uriIdentifier, String serviceOwnerUsername, String dateFromString, String dateUntilString, User consumer
    ) {

        String authenticatedUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        ServiceApi service = serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier);
        Date from = getDateFromString(dateFromString);
        Date until = getDateFromString(dateUntilString);

        ApiUsageDTO apiUsageDTO = new ApiUsageDTO(service, null, from, until, true, false);

        if (!serviceOwnerUsername.equals(authenticatedUserName)) {
            apiUsageDTO.setConsumer(consumer);
        }

        List<ApiUsageReport> apiUsageReports = calculateApiUsageReportForSpecificService(apiUsageDTO);

        long numberOfCalls = apiUsageReports.stream().mapToLong(ApiUsageReport::getCalls).sum();

        ServiceUsageStatisticsDTO serviceUsageStatisticsDTO = new ServiceUsageStatisticsDTO(
                numberOfCalls,
                dateFromString,
                dateUntilString,
                uriIdentifier,
                service.getOwner().getUsername()
        );

        return serviceUsageStatisticsDTO;
    }
}
