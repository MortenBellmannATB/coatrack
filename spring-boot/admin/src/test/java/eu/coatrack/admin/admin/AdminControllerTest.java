package eu.coatrack.admin.admin;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2020 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import eu.coatrack.admin.components.WebUI;
import eu.coatrack.admin.config.TestConfiguration;
import eu.coatrack.admin.controllers.admin.AdminConfig;
import eu.coatrack.admin.controllers.admin.AdminController;
import eu.coatrack.admin.model.repository.*;
import eu.coatrack.admin.service.GatewayHealthMonitorService;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.admin.service.admin.StatisticService;
import eu.coatrack.admin.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static eu.coatrack.admin.controllers.admin.AdminController.ADMIN_CONSUMER_WIZARD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static eu.coatrack.admin.admin.AdminDataFactory.*;
import static eu.coatrack.admin.controllers.admin.AdminController.BASE_PATH;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = TestConfiguration.class)
@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    private final AdminController adminController;

    private UserService userService;
    private StatisticService statisticService;
    private MetricsAggregationCustomRepository metricsAggregationCustomRepository;
    private ServiceApiService serviceApiService;
    private WebUI webUI;
    private GatewayHealthMonitorService gatewayHealthMonitorService;

    private MockMvc mvc;

    public AdminControllerTest() {
        userService = mock(UserService.class);
        webUI = mock(WebUI.class);
        serviceApiService = mock(ServiceApiService.class);
        statisticService = mock(StatisticService.class);
        metricsAggregationCustomRepository = mock(MetricsAggregationCustomRepository.class);
        gatewayHealthMonitorService = mock(GatewayHealthMonitorService.class);

        adminController = new AdminController();
        adminController.setUserService(userService);
        adminController.setWebUI(webUI);
        adminController.setServiceApiService(serviceApiService);
        adminController.setStatisticService(statisticService);
        adminController.setMetricsAggregationCustomRepository(metricsAggregationCustomRepository);
        adminController.setGatewayHealthMonitorService(gatewayHealthMonitorService);

        mvc = MockMvcBuilders.standaloneSetup(adminController).build();

    }

    @Test
    public void home() throws Exception {
        doReturn(user).when(userService).getAuthenticatedUser();
        doReturn(springVersion).when(webUI).parameterizedMessage(anyString(), anyList());

        mvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("springVersion", "1.0"));
    }


    @Test
    public void gettingStartedWizardForCustomer() throws Exception {
        doReturn(testService).when(serviceApiService).getTestServiceForConsumerWizard(any(AdminConfig.class));

        String query = String.format("%s/consumer/gettingstarted", BASE_PATH);

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andExpect(view().name(ADMIN_CONSUMER_WIZARD))
                .andExpect(model().attribute("testService", testService))
                .andExpect(model().attribute("testApiKeys", testApiKey));

    }

    @Test
    public void getGatewayHealthMonitorGuiFragment() throws Exception {
        doReturn(dataForHealthMonitor).when(gatewayHealthMonitorService).getGatewayHealthMonitorData();

        String query = String.format("%s/dashboard/gateway-health-monitor", BASE_PATH);

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andExpect(model().attribute("gatewayHealthMonitorProxyData", dataForHealthMonitor));
    }

    @Test
    public void gettingStatedWizard() {
        // mvc.perform(...)
    }

    @Test
    public void goProfiles() {
        // mvc.perform(...)
    }

    @Test
    public void serviceWizard() {
        // mvc.perform(...)
    }
}
