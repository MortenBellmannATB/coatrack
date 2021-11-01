package eu.coatrack.system_integration_testing.configuration;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2021 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class PageConfiguration {

    public static final String username, password, host, startpageUrl;

    static {
        File configFile = new File("config.properties");

        try {
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            username = props.getProperty("username");
            password = props.getProperty("password");
            String protocol = props.getProperty("protocol");
            host = props.getProperty("host");
            String port = props.getProperty("port");

            if (host.equals("localhost") || host.equals("host.docker.internal"))
                startpageUrl = protocol + "://" + host + ":" + port;
            else
                startpageUrl = protocol + "://" + host;

            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("The reading of the page configuration failed.", e);
        }
    }

    public static final String
            serviceProviderDashboardUrl      = startpageUrl + "/admin",
            serviceProviderTutorialUrl       = startpageUrl + "/admin/gettingstarted",
            serviceProviderServicesUrl       = startpageUrl + "/admin/services",
            serviceProviderGatewaysUrl       = startpageUrl + "/admin/proxies",
            serviceProviderApiKeysUrl        = startpageUrl + "/admin/api-keys",
            serviceProviderReportsUrl        = startpageUrl + "/admin/reports",

            serviceConsumerDashboardUrl      = startpageUrl + "/admin/consumer",
            serviceConsumerTutorialUrl       = startpageUrl + "/admin/consumer/gettingstarted",
            serviceConsumerApiKeyListUrl     = startpageUrl + "/admin/api-keys/consumer/list",
            serviceConsumerServiceListUrl    = startpageUrl + "/admin/services/consumer/list",
            serviceConsumerReportsUrl        = startpageUrl + "/admin/reports/consumer",

            exampleServiceUrl                = "http://example.org/",
            localGatewayAccessUrl            = host.equals("host.docker.internal") ? "http://localhost:8088" : "http://selenium-test-executor:8088",
            seleniumServerHostName           = host.equals("host.docker.internal") ? "localhost" : "selenium-server";

    public static final int seleniumServerPort = 4444;

}
