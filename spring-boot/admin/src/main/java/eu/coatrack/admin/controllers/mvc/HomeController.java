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
import eu.coatrack.admin.model.repository.CoverRepository;
import eu.coatrack.admin.model.repository.ErrorRepository;
import eu.coatrack.admin.model.vo.StatisticsPerApiUser;
import eu.coatrack.admin.service.GithubService;
import eu.coatrack.admin.service.MetricService;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.admin.service.user.UserService;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.ServiceCover;
import eu.coatrack.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static eu.coatrack.admin.utils.PathProvider.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author Timon Veenstra <tveenstra@bebr.nl>
 */
@CrossOrigin(origins = "*")
@Controller
public class HomeController {
    @Autowired
    private WebUI webUI;

    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private CoverRepository coverRepository;

    @Autowired
    private MetricService metricService;
    @Autowired
    private UserSessionSettings session;

    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private UserService userService;

    @Autowired
    private GithubService githubService;

    @RequestMapping("/")
    public ModelAndView home() {
        String springVersion = webUI.parameterizedMessage("home.spring.version",
                SpringBootVersion.getVersion(), SpringVersion.getVersion());
        return new ModelAndView(HOME_VIEW)
                .addObject("springVersion", springVersion);
    }

    @PostMapping(value = "/errors", produces = "application/json")
    @ResponseBody
    public eu.coatrack.api.Error saveErrors(@RequestBody eu.coatrack.api.Error error) {
        return errorRepository.save(error);
    }

    @RequestMapping(value = "/covers", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Iterable<ServiceCover> serviceCoversListPageRest(){
        return coverRepository.findAll();
    }

    @RequestMapping(value = ADMIN_BASE_PATH, method = GET)
    public ModelAndView admin() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            return prepareHomeByAuthentication(auth);
        else
            return prepareHomeForFailedAuthentication();

    }

    private ModelAndView prepareHomeByAuthentication(Authentication auth) throws JsonProcessingException {
        ModelAndView mav;
        User user = userService.getAuthenticatedUser();
        if (user != null) {
            List<ServiceApi> offeredServices = serviceApiService.findFromActiveUser();
            mav = prepareHomeByUser(user, offeredServices);
        } else
            mav = prepareHomeByGithub(auth);
        return mav;
    }

    private ModelAndView prepareHomeForFailedAuthentication() {
        String springVersion = webUI.parameterizedMessage
                ("home.spring.version", SpringBootVersion.getVersion(), SpringVersion.getVersion());

        return new ModelAndView("home")
                .addObject("springVersion", springVersion);
    }

    private ModelAndView prepareHomeByGithub(Authentication auth) throws JsonProcessingException {
        User user = githubService.getUserInfoViaGithub(auth.getDetails());
        return new ModelAndView(REGISTER)
                .addObject("user", user);
    }

    private ModelAndView prepareHomeByUser(User user, List<ServiceApi> offeredServices) {
        ModelAndView mav = new ModelAndView();
        List<ServiceApi> services = serviceApiService.findFromActiveUser();

        if (services != null && !services.isEmpty()) {
            LocalDate from = session.getDashboardDateRangeStart();
            LocalDate until = session.getDashboardDateRangeEnd();
            GeneralStats generalStatistics = metricService.getGeneralStats(from, until, user.getUsername(), offeredServices);
            List<StatisticsPerApiUser> statisticsPerConsumer = metricService.getStatisticsPerApiConsumer
                    (from, until, user.getUsername());

            mav.setViewName(ADMIN_HOME);
            mav.addObject("stats", generalStatistics);
            mav.addObject("userStatistics", statisticsPerConsumer);
        } else {
            if (!user.getInitialized()) {
                mav.setViewName(ADMIN_STARTPAGE);
            } else {
                mav.setViewName(ADMIN_CONSUMER_HOME_VIEW);
            }
        }
        return mav;
    }


}
