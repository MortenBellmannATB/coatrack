package eu.coatrack.admin.controllers.rest;

import eu.coatrack.admin.config.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    public void test() {

    }
}
