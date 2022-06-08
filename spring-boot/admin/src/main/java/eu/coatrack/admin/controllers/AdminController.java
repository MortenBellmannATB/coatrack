package eu.coatrack.admin.controllers;

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

import be.ceau.chart.DoughnutChart;
import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.DoughnutData;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.DoughnutDataset;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.enums.PointStyle;
import be.ceau.chart.options.LineOptions;
import be.ceau.chart.options.scales.LinearScale;
import be.ceau.chart.options.scales.LinearScales;
import be.ceau.chart.options.ticks.LinearTicks;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coatrack.admin.UserSessionSettings;
import eu.coatrack.admin.components.WebUI;
import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.logic.CreateProxyAction;
import eu.coatrack.admin.logic.CreateServiceAction;
import eu.coatrack.admin.model.repository.*;
import eu.coatrack.admin.model.vo.*;
import eu.coatrack.admin.service.AdminService;
import eu.coatrack.admin.service.GatewayHealthMonitorService;
import eu.coatrack.api.*;
import eu.coatrack.config.github.GithubEmail;
import javassist.NotFoundException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Timon Veenstra <tveenstra@bebr.nl>
 * @author Bruno Silva <silva@atb-bremen.de>
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    public static class GeneralStats {

        public LocalDate dateUntil;
        public LocalDate dateFrom;

        public int callsTotal;
        public int errorsTotal;
        public double revenueTotal;

        public int callsThisPeriod;
        public int errorsThisPeriod;
        public int callsDiff;
        public long users;
    }

    @RequestMapping(value = "/profiles", method = GET)
    public ModelAndView goProfiles(Model model) {
        return adminService.goProfiles(model);
    }

    @RequestMapping(value = "", method = GET)
    public ModelAndView home(Model model, HttpServletRequest request) throws IOException {
        return adminService.home(model, request);
    }

    @RequestMapping(value = "/gettingstarted", method = GET)
    public ModelAndView gettingStartedWizard(Model model) {
        return adminService.gettingStartedWizard(model);
    }

    @RequestMapping(value = "/dashboard/statisticsPerApiConsumerInDescendingOrderByNoOfCalls", method = GET, produces = "application/json")
    @ResponseBody
    public List<StatisticsPerApiUser> getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.getStatisticsPerApiConsumerInDescendingOrderByNoOfCalls(selectedTimePeriodStart, selectedTimePeriodEnd);
    }

    @RequestMapping(value = "/dashboard/userStatsDoughnutChart", method = GET, produces = "application/json")
    @ResponseBody
    public DoughnutChart generateUserStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.generateUserStatisticsDoughnutChart(selectedTimePeriodStart, selectedTimePeriodEnd);
    }

    @RequestMapping(value = "/dashboard/httpResponseStatsChart", method = GET, produces = "application/json")
    @ResponseBody
    public DoughnutChart generateHttpResponseStatisticsDoughnutChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.generateHttpResponseStatisticsDoughnutChart(selectedTimePeriodStart, selectedTimePeriodEnd);
    }

    @RequestMapping(value = "/dashboard/statsPerDayLineChart", method = GET, produces = "application/json")
    @ResponseBody
    public LineChart generateStatsPerDayLineChart(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.generateStatsPerDayLineChart(selectedTimePeriodStart, selectedTimePeriodEnd);
    }

    @RequestMapping(value = "/dashboard/metricsByLoggedUserStatistics", method = GET, produces = "application/json")
    @ResponseBody
    private Iterable<Metric> loadCallsStatistics(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.loadCallsStatistics(selectedTimePeriodStart, selectedTimePeriodEnd);
    }

    @RequestMapping(value = "/dashboard/generalStatistics", method = GET, produces = "application/json")
    @ResponseBody
    private GeneralStats loadGeneralStatistics(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodStart,
            @RequestParam("dateUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedTimePeriodEnd) {

        return adminService.loadGeneralStatistics(selectedTimePeriodStart, selectedTimePeriodEnd);
    }


    @PostMapping(value = "/serviceWizard")
    public ModelAndView serviceWizard(ServiceWizardForm wizard, BindingResult bindingResult, Model model) {
        return adminService.serviceWizard(wizard, bindingResult, model);
    }

    // This is the controller for the Consumer Getting Started Wizard
    @RequestMapping(value = "/consumer/gettingstarted", method = GET)
    @ResponseBody
    public ModelAndView gettingStartedWizardForConsumer(Model model) {
        return adminService.gettingStartedWizardForConsumer(model);
    }

    // Refresh the Test Api Key and the URL to call the service in the getting
    // started wizard for consumer
    @RequestMapping(value = "/consumer/gettingstarted/refreshApiKeys/{whichFragmentToLoad}", method = GET)
    public String refreshApiKeys(@PathVariable("whichFragmentToLoad") String whichFragmentToLoad, Model model) {
        return adminService.refreshApiKeys(whichFragmentToLoad, model);
    }

    @RequestMapping(value = "/dashboard/gateway-health-monitor", method = GET)
    @ResponseBody
    public ModelAndView getGatewayHealthMonitorGuiFragment() {
        return adminService.getGatewayHealthMonitorGuiFragment();
    }

    @RequestMapping(value = "/dashboard/gateway-health-monitor/notification-status", method = POST)
    @ResponseBody
    public void updateNotificationStatusOnGatewayHealthMonitor(@RequestParam String proxyId, @RequestParam boolean isMonitoringEnabled) {
        boolean proxyFound = adminService.updateNotificationStatusOnGatewayHealthMonitor(proxyId, isMonitoringEnabled);
        if(!proxyFound) {
            // TODO Prepare Response
        }
    }
}
