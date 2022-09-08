package eu.coatrack.admin.service;

import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class PublicApiService {

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    public void subscribeToService(ServiceApi serviceToSubscribeTo, User userWhoSubscribes) {
        createApiKeyAction.setServiceApi(serviceToSubscribeTo);
        createApiKeyAction.setUser(userWhoSubscribes);
        createApiKeyAction.execute();
    }
}
