package eu.coatrack.admin.service;

import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.model.repository.ApiKeyRepository;
import eu.coatrack.api.ApiKey;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiKeyService {
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    public ApiKey create(User authenticatedUser, ServiceApi service) {
        createApiKeyAction.setServiceApi(service);
        createApiKeyAction.setUser(authenticatedUser);
        createApiKeyAction.execute();
        return createApiKeyAction.getApiKey();
    }

    public List<ApiKey> findByActiveUserAndServiceId(long id) {
        return apiKeyRepository.findByLoggedInAPIConsumerAndServiceId(id);
    }
}
