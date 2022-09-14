package eu.coatrack.admin.admin;

import eu.coatrack.admin.model.vo.StatisticsPerApiUser;
import eu.coatrack.admin.service.GatewayHealthMonitorService;
import eu.coatrack.admin.service.admin.GeneralStats;
import eu.coatrack.api.ApiKey;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;

import java.util.Arrays;
import java.util.List;

public class AdminDataFactory {
    public final static User user = getUser(1L, "Pete");
    public final static String springVersion = "1.0";
    public final static GatewayHealthMonitorService.DataForGatewayHealthMonitor dataForHealthMonitor = null;
    public final static List<ServiceApi> serviceApis = Arrays.asList(

    );

    public final static GeneralStats generalStats = new GeneralStats(

    );

    public final static List<StatisticsPerApiUser> statisticsPerUser = Arrays.asList(

    );

    public final static ServiceApi testService = new ServiceApi();

    public final static ApiKey testApiKey = getApiKey(1L, "abc");

    private static User getUser(long id, String name) {
        User newUser = new User();
        newUser.setId(id);
        newUser.setUsername(name);
        return newUser;
    }

    private static ApiKey getApiKey(long id, String keyValue) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(id);
        apiKey.setKeyValue(keyValue);
        return apiKey;
    }


}
