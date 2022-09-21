package eu.coatrack.admin.controllers.mvc;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coatrack.admin.UserSessionSettings;
import eu.coatrack.admin.components.WebUI;
import eu.coatrack.admin.model.GeneralStats;
import eu.coatrack.admin.model.vo.*;
import eu.coatrack.admin.service.*;
import eu.coatrack.admin.service.user.UserService;
import eu.coatrack.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static eu.coatrack.admin.utils.PathProvider.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Timon Veenstra <tveenstra@bebr.nl>
 * @author Bruno Silva <silva@atb-bremen.de>
 */
@Slf4j
@Controller
@RequestMapping(value = ADMIN_BASE_PATH)
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private GatewayHealthMonitorService gatewayHealthMonitorService;

    @RequestMapping(value = "/profiles", method = GET)
    public ModelAndView goProfiles() {
        return new ModelAndView(ADMIN_PROFILE);
    }

    @RequestMapping(value = "/gettingstarted", method = GET)
    public ModelAndView gettingStartedWizard() {
        return new ModelAndView(ADMIN_WIZARD_VIEW);
    }

    @RequestMapping(value = "/serviceWizard", method = POST)
    public ModelAndView serviceWizard(ServiceWizardForm wizard) {
        User authenticatedUser = userService.getAuthenticatedUser();
        log.debug("got user");
        ServiceApi service = serviceApiService.create(wizard, authenticatedUser);
        log.debug("got service");
        Proxy proxy = proxyService.create(authenticatedUser, service);
        log.debug("got proxy");
        ApiKey apiKey = apiKeyService.create(authenticatedUser, service);
        log.debug("got apikey");

        authenticatedUser.setInitialized(Boolean.TRUE);
        authenticatedUser = userService.save(authenticatedUser);

        ServiceWizardResponse response = new ServiceWizardResponse(
                service.getName(),
                service.getUriIdentifier(),
                service.getLocalUrl(),
                wizard.getServiceForFree(),
                wizard.getMonthlyCharge(),
                wizard.getPercallCharge(),
                wizard.getServiceCost(),
                authenticatedUser.getUsername(),
                apiKey.getKeyValue(),
                proxy.getPublicUrl()
        );
        return new ModelAndView(ADMIN_WIZARD_RESULT)
                .addObject("wizardResponse", response);
    }

    @RequestMapping(value = "/dashboard/gateway-health-monitor", method = GET)
    @ResponseBody
    public ModelAndView getGatewayHealthMonitorGuiFragment() {
        log.debug("client request for Gateway Health Monitor Data");

        GatewayHealthMonitorService.DataForGatewayHealthMonitor dataForGatewayHealthMonitor = gatewayHealthMonitorService
                .getGatewayHealthMonitorData();

        return new ModelAndView(GATEWAY_HEALTH_MONITOR_FRAGMENT)
                .addObject("gatewayHealthMonitorProxyData", dataForGatewayHealthMonitor);
    }
}
