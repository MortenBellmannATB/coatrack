package eu.coatrack.admin.publicapi;

import eu.coatrack.admin.config.TestConfiguration;
import eu.coatrack.admin.controllers.mvc.PublicApiController;
import eu.coatrack.admin.service.PublicApiService;
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

import java.util.Collections;

import static eu.coatrack.admin.publicapi.PublicApiDataFactory.serviceOwner;
import static eu.coatrack.admin.publicapi.PublicApiDataFactory.uriIdentifier;
import static eu.coatrack.admin.report.ReportDataFactory.consumer;
import static eu.coatrack.admin.utils.DateUtils.getTodayAsString;
import static eu.coatrack.admin.utils.DateUtils.getTodayLastMonthAsString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@WebMvcTest(PublicApiController.class)
public class PublicApiControllerTest {

    private final PublicApiController publicApiController;
    private final ReportService reportService;
    private final UserService userService;
    private final ServiceApiService serviceApiService;

    private final PublicApiService publicApiService;
    private final MockMvc mvc;
    private final String basePath = "/public-api";


    public PublicApiControllerTest() {
        publicApiService = mock(PublicApiService.class);
        reportService = mock(ReportService.class);
        userService = mock(UserService.class);
        serviceApiService = mock(ServiceApiService.class);

        publicApiController = new PublicApiController(publicApiService, serviceApiService, reportService, userService);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        Authentication authentication = new UsernamePasswordAuthenticationToken(consumer.getUsername(), "PetesPassword", Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mvc = MockMvcBuilders.standaloneSetup(publicApiController).build();
    }

    @Test
    public void findByServiceOwnerAndUriIdentifier() throws Exception {
        String query = String.format("%s/services/%s/%s",
                basePath,
                uriIdentifier,
                serviceOwner.getUsername()
        );

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void findByServiceOwner() throws Exception {
        String query = basePath + "/services";

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }

    @Test
    public void subscribeToService() throws Exception {
        String query = String.format("%s/services/%s/%s/subscriptions",
                basePath,
                uriIdentifier,
                serviceOwner.getUsername()
        );

        mvc.perform(post(query))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andDo(print());
    }

    @Test
    public void getServiceUsageStatistics() throws Exception {
        String query = String.format("%s/services/%s/%s/usageStatistics?dateFrom=%s&dateUntil=%s",
                basePath,
                uriIdentifier,
                serviceOwner.getUsername(),
                getTodayLastMonthAsString(),
                getTodayAsString()
        );

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
