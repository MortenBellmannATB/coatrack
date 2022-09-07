package eu.coatrack.admin.publicapi;

import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

public class PublicApiDataFactory {
    public static final String uriIdentifier = "1234567890";
    public static final User serviceOwner = getUser(1L, "Pete");
    public static final ServiceApi serviceApi = getServiceApi(1L, "SuperDuperService");
    public static final List<ServiceApi> serviceApiList = Arrays.asList(
            serviceApi,
            getServiceApi(2L, "NochEinService"),
            getServiceApi(3L, "AnotherOne")
    );

    private static User getUser(long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        return user;
    }

    private static ServiceApi getServiceApi(long id, String name) {
        ServiceApi service = new ServiceApi();
        service.setId(id);
        service.setName(name);
        return service;
    }
}
