package eu.coatrack.admin.controllers.rest;

import eu.coatrack.admin.config.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;

import static eu.coatrack.admin.utils.PathProvider.ADMIN_HOME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@WebMvcTest
public class AdminRestControllerTest {
    private final AdminRestController adminRestController;

    private final MockMvc mvc;

    public AdminRestControllerTest() {
        adminRestController = new AdminRestController();

        mvc = MockMvcBuilders.standaloneSetup(adminRestController).build();
    }

    @Test
    public void generateUserStatisticsDoughnutChart() throws Exception {

        String query = String.format("%s/%s/%s", "http://localhost:8080", ADMIN_HOME, "userStatsDoughnutChart?dateFrom=2022-09-17&dateUntil=2022-09-23");

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void generateHttpResponseStatisticsDoughnutChart() throws Exception {
        String query = String.format("%s/%s/%s", "http://localhost:8080", ADMIN_HOME, "httpResponseStatsChart?dateFrom=2022-09-17&dateUntil=2022-09-23");

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
