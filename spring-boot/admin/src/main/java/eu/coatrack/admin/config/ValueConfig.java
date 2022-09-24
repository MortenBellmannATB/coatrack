package eu.coatrack.admin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
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
