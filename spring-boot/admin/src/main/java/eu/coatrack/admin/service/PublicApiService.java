package eu.coatrack.admin.service;

import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.model.repository.ServiceApiRepository;
import eu.coatrack.admin.model.repository.UserRepository;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.ServiceApiDTO;
import eu.coatrack.api.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static org.modelmapper.convention.MatchingStrategies.STRICT;

@AllArgsConstructor
@Slf4j
@Service
public class PublicApiService implements InitializingBean {

    @Autowired
    private ServiceApiRepository serviceApiRepository;

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    private static ModelMapper modelMapper;

    @Override
    public void afterPropertiesSet() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(STRICT)
                .setPropertyCondition(Conditions.isNotNull());
    }

    public void subscribeToService(ServiceApi serviceToSubscribeTo, User userWhoSubscribes) {
        createApiKeyAction.setServiceApi(serviceToSubscribeTo);
        createApiKeyAction.setUser(userWhoSubscribes);
        createApiKeyAction.execute();
    }

    // TODO should be in ServiceApiService
    @Deprecated
    public ServiceApiDTO findByServiceOwnerAndUriIdentifier(String uriIdentifier, String serviceOwnerUsername) {
        return toDTO(serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier));
    }

    // TODO should be in ServiceApiService
    @Deprecated
    public List<ServiceApiDTO> findByServiceOwner(String authenticatedUserName) {
        return toListOfDTOs(serviceApiRepository.findByOwnerUsername(authenticatedUserName));
    }

    // TODO should be in ServiceApiService
    @Deprecated
    private static List<ServiceApiDTO> toListOfDTOs(List<ServiceApi> entity) {
        List<ServiceApiDTO> serviceApiDTOList = new ArrayList<>();

        for (ServiceApi singleEntity : entity) {
            serviceApiDTOList.add(modelMapper.map(singleEntity, ServiceApiDTO.class));
        }
        return serviceApiDTOList;
    }

    // TODO should be in ServiceApiService
    @Deprecated
    private static ServiceApiDTO toDTO(ServiceApi entity) {
        ServiceApiDTO serviceDTO = modelMapper.map(entity, ServiceApiDTO.class);
        serviceDTO.setServiceOwnerUsername(entity.getOwner().getUsername());
        return serviceDTO;
    }

}
