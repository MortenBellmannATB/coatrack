package eu.coatrack.admin.controllers.mvc.report;

import eu.coatrack.admin.controllers.mvc.ReportController;
import eu.coatrack.admin.config.TestConfiguration;
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

import static eu.coatrack.admin.data.ReportDataFactory.*;
import static eu.coatrack.admin.utils.DateUtils.*;
import static eu.coatrack.admin.utils.PathProvider.REPORT_BASE_PATH;
import static eu.coatrack.admin.utils.PathProvider.REPORT_VIEW;
import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
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

    public ReportControllerTest() {
        userService = mock(UserService.class);
        serviceApiService = mock(ServiceApiService.class);
        reportService = mock(ReportService.class);

        doReturn(consumer).when(userService).findByUsername(anyString());

        reportController = new ReportController(userService, serviceApiService, reportService);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        Authentication authentication = new UsernamePasswordAuthenticationToken(consumer.getUsername(), "PetesPassword", Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    public void reportWithoutParam() throws Exception {
        doReturn(consumer).when(userService).getAuthenticatedUser();
        doReturn(serviceApis).when(serviceApiService).findIfNotDeleted();
        doReturn(serviceApis.get(0)).when(serviceApiService).findById(anyLong());
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());

        mvc.perform(get(REPORT_BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(REPORT_VIEW))
                .andExpect(model().attribute("users", is(consumers)))
                .andExpect(model().attribute("selectedServiceId", -1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", -1L))
                .andExpect(model().attribute("services", serviceApis))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("isOnlyPaidCalls", false))
                .andExpect(model().attribute("isReportForConsumer", false))
                .andExpect(model().attribute("dateFrom", LocalDate.now()))
                .andExpect(model().attribute("dateUntil", LocalDate.now()))
                .andExpect(model().attribute("serviceApiSelectedForReport", serviceApis.get(0)))
                .andExpect(model().attribute("consumerUserSelectedForReport", is(nullValue())));
    }

    @Test
    public void reportWithParam() throws Exception {
        doReturn(consumer).when(userService).getAuthenticatedUser();
        doReturn(serviceApis).when(serviceApiService).findIfNotDeleted();
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());

        String query = String.format("%s/%s/%s/%d/%d/%b",
                REPORT_BASE_PATH,
                getTodayLastMonthAsString(),
                getTodayAsString(),
                selectedServiceId,
                selectedApiConsumerUserId,
                considerOnlyPaidCalls
        );

        mvc.perform(get(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(REPORT_VIEW))
                .andExpect(model().attribute("users", is(consumers)))
                .andExpect(model().attribute("selectedServiceId", selectedServiceId))
                .andExpect(model().attribute("selectedApiConsumerUserId", selectedApiConsumerUserId))
                .andExpect(model().attribute("services", is(serviceApis)))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("isOnlyPaidCalls", false))
                .andExpect(model().attribute("isReportForConsumer", false))
                .andExpect(model().attribute("dateFrom", LocalDate.now().minusMonths(1)))
                .andExpect(model().attribute("dateUntil", LocalDate.now()))
                .andExpect(model().attribute("serviceApiSelectedForReport", nullValue()))
                .andExpect(model().attribute("consumerUserSelectedForReport", nullValue()));
    }

    @Test
    public void showGenerateReportPageForServiceConsumer() throws Exception {
        doReturn(consumer).when(userService).getAuthenticatedUser();
        doReturn(serviceApis).when(serviceApiService).findFromActiveUser();
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());

        mvc.perform(get(REPORT_BASE_PATH + "/consumer"))
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
                .andExpect(model().attribute("serviceApiSelectedForReport", nullValue())); // TODO delete
    }

    @Test
    public void searchReportsByServicesConsumed() throws Exception {
        doReturn(consumer).when(userService).getAuthenticatedUser();
        doReturn(serviceApis).when(serviceApiService).findFromActiveUser();
        doReturn(serviceApis.get(0)).when(serviceApiService).findById(anyLong());
        doReturn(payPerCallServiceIds).when(serviceApiService).getPayPerCallServicesIds(anyList());
        doReturn(consumers).when(serviceApiService).getServiceConsumers(anyList());

        String query = String.format("%s/consumer/%s/%s/%d/%b",
                REPORT_BASE_PATH,
                getTodayLastMonthAsString(),
                getTodayAsString(),
                selectedServiceId,
                considerOnlyPaidCalls
        );

        mvc.perform(get(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedServiceId", 1L))
                .andExpect(model().attribute("selectedApiConsumerUserId", consumer.getId()))
                .andExpect(model().attribute("services", is(serviceApis)))
                .andExpect(model().attribute("payPerCallServicesIds", payPerCallServiceIds))
                .andExpect(model().attribute("exportUser", is(consumer)))
                .andExpect(model().attribute("users", consumers))
                .andExpect(model().attribute("isOnlyPaidCalls", false))
                .andExpect(model().attribute("isReportForConsumer", false))
                .andExpect(model().attribute("dateFrom", LocalDate.now().minusMonths(1)))
                .andExpect(model().attribute("dateUntil", LocalDate.now()))
                .andExpect(model().attribute("serviceApiSelectedForReport", serviceApis.get(0)));
    }
}
