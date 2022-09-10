package eu.coatrack.admin.report;

import eu.coatrack.admin.config.TestConfiguration;
import eu.coatrack.admin.controllers.ReportController;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.admin.service.report.ReportService;
import eu.coatrack.admin.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.Collections;
import static eu.coatrack.admin.report.ReportDataFactory.*;
import static eu.coatrack.admin.utils.DateUtils.getTodayAsString;
import static eu.coatrack.admin.utils.DateUtils.getTodayLastMonthAsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = TestConfiguration.class)
@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    private final ReportController reportController;

    private final UserService userService;
    private final ServiceApiService serviceApiService;
    private final ReportService reportService;

    private final MockMvc mvc;
    private final String basePath = "/admin/reports";

    public ReportControllerTest() {
        userService = mock(UserService.class);
        serviceApiService = mock(ServiceApiService.class);
        reportService = mock(ReportService.class);

        doReturn(consumer).when(userService).getAuthenticatedUser();

        reportController = new ReportController(userService, serviceApiService, reportService);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        Authentication authentication = new UsernamePasswordAuthenticationToken(consumer.getUsername(), "PetesPassword", Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    public void reportWithoutParam() throws Exception {
        doReturn(serviceApis).when(serviceApiService).findIfNotDeleted();
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());

        mvc.perform(get(basePath))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(ReportController.REPORT_VIEW))
                .andExpect(model().attribute("users", is(consumers)))
                .andExpect(model().attribute("selectedServiceId", -1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", -1L))
                .andExpect(model().attribute("services", serviceApis))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("isOnlyPaidCalls", false)) // TODO delete
                .andExpect(model().attribute("isReportForConsumer", false)) // TODO delete
                .andExpect(model().attribute("dateFrom", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("dateUntil", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("serviceApiSelectedForReport", is(nullValue()))) // TODO delete
                .andExpect(model().attribute("consumerUserSelectedForReport", is(nullValue()))); // TODO delete
    }

    @Test
    public void reportWithParam() throws Exception {
        doReturn(serviceApis).when(serviceApiService).findIfNotDeleted();
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());

        String query = String.format("%s/%s/%s/%d/%d/%b",
                basePath,
                getTodayLastMonthAsString(),
                getTodayAsString(),
                selectedServiceId,
                selectedApiConsumerUserId,
                considerOnlyPaidCalls
        );

        mvc.perform(get(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(ReportController.REPORT_VIEW))
                .andExpect(model().attribute("users", is(consumers)))
                .andExpect(model().attribute("selectedServiceId", -1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", -1L))
                .andExpect(model().attribute("services", is(serviceApis)))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("isOnlyPaidCalls", false)) // TODO delete
                .andExpect(model().attribute("isReportForConsumer", false)) // TODO delete
                .andExpect(model().attribute("dateFrom", LocalDate.now().minusMonths(1))) // TODO delete
                .andExpect(model().attribute("dateUntil", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("serviceApiSelectedForReport", nullValue())) // TODO delete
                .andExpect(model().attribute("consumerUserSelectedForReport", nullValue())); // TODO delete
    }

    @Test
    public void showGenerateReportPageForServiceConsumer() throws Exception {
        doReturn(serviceApis).when(serviceApiService).findFromActiveUser();
        doReturn(serviceApis.get(0)).when(serviceApiService).findById(anyLong());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());

        mvc.perform(get(basePath + "/consumer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedServiceId", -1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", consumer.getId()))
                .andExpect(model().attribute("services", is(serviceApis)))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("users", consumers))
                .andExpect(model().attribute("isOnlyPaidCalls", false)) // TODO to delete
                .andExpect(model().attribute("isReportForConsumer", false)) // TODO to delete
                .andExpect(model().attribute("dateFrom", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("dateUntil", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("serviceApiSelectedForReport", is(serviceApis.get(0)))); // TODO delete
    }

    @Test
    public void searchReportsByServicesConsumed() throws Exception {
        doReturn(serviceApis).when(serviceApiService).findFromActiveUser();
        doReturn(serviceApis.get(0)).when(serviceApiService).findById(anyLong());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());

        String query = String.format("%s/consumer/%s/%s/%d/%b",
                basePath,
                getTodayLastMonthAsString(),
                getTodayAsString(),
                selectedServiceId,
                considerOnlyPaidCalls
        );

        mvc.perform(get(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedServiceId", -1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", consumer.getId()))
                .andExpect(model().attribute("services", is(serviceApis)))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("users", consumers))
                .andExpect(model().attribute("isOnlyPaidCalls", false)) // TODO to delete
                .andExpect(model().attribute("isReportForConsumer", false)) // TODO to delete
                .andExpect(model().attribute("dateFrom", LocalDate.now().minusMonths(1))) // TODO delete
                .andExpect(model().attribute("dateUntil", LocalDate.now())) // TODO delete
                .andExpect(model().attribute("serviceApiSelectedForReport", is(serviceApis.get(0)))); // TODO delete
    }
}
