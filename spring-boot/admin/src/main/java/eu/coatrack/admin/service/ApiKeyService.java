package eu.coatrack.admin.service;

import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.model.repository.ApiKeyRepository;
import eu.coatrack.api.ApiKey;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ApiKeyService {
    @Autowired
    private CreateApiKeyAction createApiKeyAction;
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public ApiKey create(User authenticatedUser, ServiceApi serviceApi) {
        createApiKeyAction.setServiceApi(serviceApi);
        createApiKeyAction.setUser(authenticatedUser);
        createApiKeyAction.execute();
        return createApiKeyAction.getApiKey();
    }

    public List<ApiKey> findByLoggedInAPIConsumerAndServiceId(long id) {
        return apiKeyRepository.findByLoggedInAPIConsumerAndServiceId(id);
    }


}
