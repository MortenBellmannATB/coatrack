package eu.coatrack.admin.controllers.mvc;

import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.api.ServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import static eu.coatrack.admin.utils.PathProvider.ADMIN_BASE_PATH;
import static eu.coatrack.admin.utils.PathProvider.ADMIN_CONSUMER_WIZARD;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller(ADMIN_BASE_PATH + "/consumer")
public class ConsumerController {
    @Autowired
    private ServiceApiService serviceApiService;


    @RequestMapping(value = "/gettingstarted", method = GET)
    @ResponseBody
    public ModelAndView gettingStartedWizardForConsumer() {
        ServiceApi gettingStartedTestService = serviceApiService.getTestServiceForConsumerWizard();

        return new ModelAndView(ADMIN_CONSUMER_WIZARD)
                .addObject("testService", gettingStartedTestService)
                .addObject("testApiKeys", null);
    }
}
