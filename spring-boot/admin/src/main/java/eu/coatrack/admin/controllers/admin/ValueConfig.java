package eu.coatrack.admin.controllers.admin;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ValueConfig {

    @Value("${ygg.admin.gettingStarted.consumer.testService.provider.username}")
    private String gettingStartedTestServiceProvider;

    @Value("${ygg.proxy.server.port.defaultValue}")
    private int proxyServerDefaultPort;

    @Value("${ygg.admin.gettingStarted.consumer.testService.uriIdentifier}")
    private String gettingStartedTestServiceIdentifier;


    @Value("${ygg.admin.server.url}")
    private String coatrackAdminPublicServerURL;
}
