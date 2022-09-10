package eu.coatrack.admin.report;

import eu.coatrack.admin.service.report.ApiUsageCalculator;
import eu.coatrack.admin.service.report.ApiUsageDTO;
import eu.coatrack.admin.service.report.ReportService;
import eu.coatrack.api.ApiUsageReport;
import eu.coatrack.api.DataTableView;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static eu.coatrack.admin.report.ReportDataFactory.*;
import static eu.coatrack.admin.utils.DateUtils.getTodayLastMonthAsString;
import static eu.coatrack.api.ServiceAccessPaymentPolicy.WELL_DEFINED_PRICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ReportServiceTest {

    private final ApiUsageCalculator apiUsageCalculator;

    private final ReportService reportService;

    public ReportServiceTest() {
        apiUsageCalculator = mock(ApiUsageCalculator.class);

        doReturn(apiUsageReports).when(apiUsageCalculator).calculateForSpecificService(any(ApiUsageDTO.class));

        reportService = new ReportService(apiUsageCalculator);
    }

    @Test
    public void reportApiUsage() {
        ApiUsageDTO apiUsageDTO = getApiUsageDTO(getTodayLastMonthAsString(), WELL_DEFINED_PRICE);
        DataTableView<ApiUsageReport> tableView = reportService.reportApiUsage(apiUsageDTO);

        assertEquals(3, tableView.getData().size());
    }

    @Test
    public void reportTotalRevenueForApiProvider() {
        double res = reportService.reportTotalRevenueForApiProvider(serviceApis, LocalDate.now(), LocalDate.now());

        assertEquals(600.0, res);
    }
}
