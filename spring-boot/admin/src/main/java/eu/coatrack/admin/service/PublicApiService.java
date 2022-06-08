package eu.coatrack.admin.service;

import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.model.repository.ServiceApiRepository;
import eu.coatrack.admin.model.repository.UserRepository;
import eu.coatrack.api.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.modelmapper.convention.MatchingStrategies.STRICT;

@Slf4j
@Service
public class PublicApiService implements InitializingBean {

    @Value("${ygg.admin.server.url}")
    private String coatrackAdminPublicServerURL;

    @Autowired
    private ServiceApiRepository serviceApiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreateApiKeyAction createApiKeyAction;

    @Autowired
    private ReportService reportService;

    private ModelMapper modelMapper;


    public void afterPropertiesSet() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(STRICT);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }


    public ServiceApiDTO findByServiceOwnerAndUriIdentifier(String uriIdentifier, String serviceOwnerUsername) {
        return toDTO(serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier));
    }

    public List<ServiceApiDTO> findByServiceOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return toListOfDTOs(serviceApiRepository.findByOwnerUsername(auth.getName()));
    }

    public String subscribeToService(String uriIdentifier, String serviceOwnerUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userWhoSubscribes = userRepository.findByUsername(auth.getName());
        ServiceApi serviceToSubscribeTo = serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier);
        createApiKeyAction.setServiceApi(serviceToSubscribeTo);
        createApiKeyAction.setUser(userWhoSubscribes);
        createApiKeyAction.execute();
        // return baseURL
        return coatrackAdminPublicServerURL + "/admin/api-keys/consumer/list";
    }

    public ServiceUsageStatisticsDTO getServiceUsageStatistics(String uriIdentifier, String serviceOwnerUsername, String dateFrom, String dateUntil) throws ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ServiceApi service = serviceApiRepository.findServiceApiByServiceOwnerAndUriIdentifier(serviceOwnerUsername, uriIdentifier);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFromParsedToDate = formatter.parse(dateFrom);
        Date dateUntilParsedToDate = formatter.parse(dateUntil);
        List<ApiUsageReport> apiUsageReports;
        // if user is the owner of the service
        if (serviceOwnerUsername.equals(auth.getName())) {
            List<ApiUsageReport> calculatedApiUsage = reportService.calculateApiUsageReportForSpecificService(
                    service, -1L, // for all consumers
                    java.sql.Date.valueOf(dateFromParsedToDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                    java.sql.Date.valueOf(dateUntilParsedToDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                    true);

            apiUsageReports = new ArrayList<>(calculatedApiUsage);
        } else {
            Long userId = userRepository.findByUsername(auth.getName()).getId();
            apiUsageReports = reportService.calculateApiUsageReportForSpecificService(service, userId, dateFromParsedToDate, dateUntilParsedToDate, false);
        }

        ServiceUsageStatisticsDTO serviceUsageStatisticsDTO = new ServiceUsageStatisticsDTO();
        serviceUsageStatisticsDTO.setNumberOfCalls(apiUsageReports.stream().mapToLong(ApiUsageReport::getCalls).sum());
        serviceUsageStatisticsDTO.setDateFrom(dateFrom);
        serviceUsageStatisticsDTO.setDateUntil(dateUntil);
        serviceUsageStatisticsDTO.setUriIdentifier(uriIdentifier);
        serviceUsageStatisticsDTO.setOwnerUserName(service.getOwner().getUsername());

        return serviceUsageStatisticsDTO;
    }

    private List<ServiceApiDTO> toListOfDTOs(List<ServiceApi> entity) {

        List<ServiceApiDTO> serviceApiDTOList = new ArrayList<>();

        for (ServiceApi singleEntity : entity) {
            serviceApiDTOList.add(modelMapper.map(singleEntity, ServiceApiDTO.class));
        }
        return serviceApiDTOList;
    }

    private ServiceApiDTO toDTO(ServiceApi entity) {
        ServiceApiDTO serviceDTO = modelMapper.map(entity, ServiceApiDTO.class);
        serviceDTO.setServiceOwnerUsername(entity.getOwner().getUsername());
        return serviceDTO;
    }

}
