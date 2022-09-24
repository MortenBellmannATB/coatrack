package eu.coatrack.admin.controllers.rest;

import eu.coatrack.admin.service.ApiKeyService;
import eu.coatrack.admin.service.ProxyService;
import eu.coatrack.admin.service.ServiceApiService;
import eu.coatrack.api.ApiKey;
import eu.coatrack.api.ServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static eu.coatrack.admin.utils.PathProvider.ADMIN_BASE_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController()
@RequestMapping(value = ADMIN_BASE_PATH + "/consumer")
public class ConsumerRestController {

    @Autowired
    private ServiceApiService serviceApiService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ProxyService proxyService;

    @GetMapping(value = "/gettingstarted/refreshApiKeys/{whichFragmentToLoad}")
    public String refreshApiKeys(
            @PathVariable("whichFragmentToLoad") String whichFragmentToLoad,
            Model model
    ) {
        ServiceApi gettingStartedTestService = serviceApiService.getTestServiceForConsumerWizard();
        List<ApiKey> apiKeysForTestService = apiKeyService.findByActiveUserAndServiceId(gettingStartedTestService.getId());
        ApiKey newApiKey = apiKeysForTestService.get(apiKeysForTestService.size() - 1);

        List<String> proxiesUrlList = proxyService.findByServiceId(newApiKey.getServiceApi().getId());
        List<String> defaultProxyList = proxiesUrlList.isEmpty() ?
                new ArrayList<>() :
                Collections.singletonList(proxiesUrlList.get(0));

        // TODO remove this, why would i need the map if it is 1 Entry every time?
        Map<String, List<String>> proxyURLPerNewApiKey = new TreeMap<>();
        proxyURLPerNewApiKey.put(newApiKey.getKeyValue(), defaultProxyList);

        model.addAttribute("testService", gettingStartedTestService);
        model.addAttribute("proxiesPerApiKey", proxyURLPerNewApiKey);
        model.addAttribute("apiKeys", newApiKey);

        return whichFragmentToLoad.equals("table") ?
                "admin/fragments/api-keys/consumer/list :: apiKeyTable" :
                "admin/fragments/consumer_wizard/wizard :: gatewayCallURL";
    }

}
