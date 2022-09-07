package eu.coatrack.admin.publicapi;


import eu.coatrack.admin.logic.CreateApiKeyAction;
import eu.coatrack.admin.model.repository.ServiceApiRepository;
import eu.coatrack.admin.model.repository.UserRepository;
import eu.coatrack.admin.service.PublicApiService;
import eu.coatrack.admin.service.report.ReportService;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.ServiceApiDTO;
import eu.coatrack.api.ServiceUsageStatisticsDTO;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static eu.coatrack.admin.publicapi.PublicApiDataFactory.*;
import static eu.coatrack.admin.report.ReportDataFactory.apiUsageReports;
import static eu.coatrack.admin.utils.DateUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PublicApiServiceTest {
    private PublicApiService publicApiService;

    private final String coatrackAdminPublicServerURL = "http://localhost:8080";
    private final ServiceApiRepository serviceApiRepository;
    private final UserRepository userRepository;
    private final CreateApiKeyAction createApiKeyAction;
    private final ReportService reportService;
    private final ModelMapper modelMapper;


    public PublicApiServiceTest() {
        serviceApiRepository = mock(ServiceApiRepository.class);
        userRepository = mock(UserRepository.class);
        // hat keine Auswirkungen auf etwas zwischen Anwender und API
        createApiKeyAction = mock(CreateApiKeyAction.class);
        reportService = mock(ReportService.class);
        modelMapper = mock(ModelMapper.class);


        publicApiService = new PublicApiService(
                coatrackAdminPublicServerURL,
                serviceApiRepository,
                userRepository,
                createApiKeyAction,
                reportService,
                modelMapper
        );
    }

    @Test
    @Deprecated
    public void findByServiceOwnerAndUriIdentifier() {
        doReturn(serviceApi).when(serviceApiRepository).findServiceApiByServiceOwnerAndUriIdentifier(
                anyString(), anyString()
        );

        ServiceApiDTO serviceApiDTO = publicApiService.findByServiceOwnerAndUriIdentifier(serviceOwner.getUsername(), uriIdentifier);

        assertEquals(serviceApi.getName(), serviceApiDTO.getName());
        assertEquals(serviceApi.getUriIdentifier(), serviceApiDTO.getUriIdentifier());
        assertEquals(serviceOwner.getUsername(), serviceApiDTO.getServiceOwnerUsername());
    }

    @Test
    public void findByServiceOwner() {
        doReturn(serviceApiList).when(serviceApiRepository).findByOwnerUsername(anyString());

        List<ServiceApiDTO> resultList = publicApiService.findByServiceOwner(serviceOwner.getUsername());

        assertEquals(3, resultList.size());
    }

    @Test
    public void subscribeToService() {
        String redirectionPath = publicApiService.subscribeToService(uriIdentifier, serviceOwner.getUsername());
        String expectedPath = coatrackAdminPublicServerURL + "/admin/api-keys/consumer/list";

        assertEquals(expectedPath, redirectionPath);
    }

    @Test
    public void getServiceUsageStatistics() throws ParseException {
        doReturn(serviceApi).when(serviceApiRepository).findServiceApiByServiceOwnerAndUriIdentifier(
                anyString(), anyString()
        );

        doReturn(apiUsageReports).when(reportService).calculateApiUsageReportForSpecificService(
                any(ServiceApi.class), anyLong(), any(Date.class), any(Date.class), anyBoolean()
        );

        ServiceUsageStatisticsDTO serviceUsageDTO = publicApiService.getServiceUsageStatistics(
                uriIdentifier,
                serviceOwner.getUsername(),
                getTodayLastMonthAsString(),
                getTodayAsString()
        );

        assertEquals(1L, serviceUsageDTO.getNumberOfCalls());
        assertEquals(getTodayLastMonthAsString(), serviceUsageDTO.getDateFrom());
        assertEquals(getTodayAsString(), serviceUsageDTO.getDateUntil());
        assertEquals(uriIdentifier, serviceUsageDTO.getUriIdentifier());
        assertEquals(serviceOwner.getUsername(), serviceUsageDTO.getOwnerUserName());

    }

}
