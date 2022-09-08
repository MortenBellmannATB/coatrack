package eu.coatrack.admin.service;

import eu.coatrack.admin.model.repository.ApiKeyRepository;
import eu.coatrack.admin.model.repository.ServiceApiRepository;
import eu.coatrack.api.ApiKey;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.ServiceApiDTO;
import eu.coatrack.api.User;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.coatrack.api.ServiceAccessPaymentPolicy.WELL_DEFINED_PRICE;
import static org.modelmapper.convention.MatchingStrategies.STRICT;

@Service
public class ServiceApiService  implements InitializingBean {

    @Autowired
    private ServiceApiRepository serviceApiRepository;
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private static ModelMapper modelMapper;

    @Override
    public void afterPropertiesSet() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(STRICT)
                .setPropertyCondition(Conditions.isNotNull());
    }


    public List<String> getPayPerCallServicesIds(List<ServiceApi> serviceApis) {
        List<String> payPerCallServicesIds = new ArrayList<>();
        if (!serviceApis.isEmpty()) {
            payPerCallServicesIds = serviceApis.stream().filter(serviceApi -> serviceApi.getServiceAccessPaymentPolicy().equals(WELL_DEFINED_PRICE)).map(ServiceApi::getId).map(String::valueOf).collect(Collectors.toList());
        }
        return payPerCallServicesIds;
    }

    public List<User> getServiceConsumers(List<ServiceApi> servicesProvidedByUser) {
        return servicesProvidedByUser.stream()
                .flatMap(api -> api.getApiKeys().stream())
                .map(ApiKey::getUser)
                .distinct()
                .collect(Collectors.toList());
    }

    public ServiceApi findById(long id) {
        return serviceApiRepository.findById(id).orElse(null);
    }

    public List<ServiceApi> findFromActiveUser() {
       return serviceApiRepository.findByApiKeyList(apiKeyRepository.findByLoggedInAPIConsumer());
    }

    public List<ServiceApi> findServicesByOwnerUsername(String name) {
        return serviceApiRepository.findByOwnerUsername(name);
    }

    public List<ServiceApi> findIfNotDeleted() {
        return serviceApiRepository.findByDeletedWhen(null);
    }

    public ServiceApiDTO findByServiceOwnerAndUriIdentifier(String uriIdentifier, String serviceOwnerUsername) {
        return toDTO(serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier));
    }

    public List<ServiceApiDTO> findByServiceOwner(String authenticatedUserName) {
        return toListOfDTOs(serviceApiRepository.findByOwnerUsername(authenticatedUserName));
    }

    public ServiceApi findServiceApiByServiceOwnerAndUriIdentifier(String serviceOwnerUsername, String uriIdentifier) {
        return serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier);
    }


    private static List<ServiceApiDTO> toListOfDTOs(List<ServiceApi> entity) {
        List<ServiceApiDTO> serviceApiDTOList = new ArrayList<>();

        for (ServiceApi singleEntity : entity) {
            serviceApiDTOList.add(modelMapper.map(singleEntity, ServiceApiDTO.class));
        }
        return serviceApiDTOList;
    }


    private static ServiceApiDTO toDTO(ServiceApi entity) {
        ServiceApiDTO serviceDTO = modelMapper.map(entity, ServiceApiDTO.class);
        serviceDTO.setServiceOwnerUsername(entity.getOwner().getUsername());
        return serviceDTO;
    }
}
