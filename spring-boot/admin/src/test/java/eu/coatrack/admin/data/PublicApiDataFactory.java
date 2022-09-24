package eu.coatrack.admin.data;

import eu.coatrack.api.ApiUsageReport;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

public class PublicApiDataFactory {
    public static final String uriIdentifier = "1234567890";
    public static final User serviceOwner = getUser(1L, "Pete");
    public static final User consumer = getUser(2L, "Connie, Consumer");
    public static final ServiceApi serviceApi = getServiceApi(1L, "SuperDuperService", serviceOwner);
    public static final List<ServiceApi> serviceApiList = Arrays.asList(
            serviceApi,
            getServiceApi(2L, "NochEinService", getUser(2L, "Jessica"))
    );

    public static final List<ApiUsageReport> apiUsageReports = Arrays.asList(
            new ApiUsageReport("ReportA", 1L, 100.0, 100.0),
            new ApiUsageReport("ReportB", 1L, 100.0, 100.0),
            new ApiUsageReport("ReportC", 1L, 100.0, 100.0)
    );

    private static User getUser(long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        return user;
    }

    private static ServiceApi getServiceApi(long id, String name, User owner) {
        ServiceApi service = new ServiceApi();
        service.setId(id);
        service.setName(name);
        service.setOwner(owner);
        return service;
    }
}
