package eu.coatrack.admin.service;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2020 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;
import eu.coatrack.api.Proxy;
import eu.coatrack.api.ServiceApi;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.registry.DeleteException;

/**
 *
 * @author perezdf, ChristophBaier
 */
@Component
public class GitService {

    private static final Logger log = LoggerFactory.getLogger(GitService.class);
    private static final String tmpDirStr = System.getProperty("java.io.tmpdir");

    @Value("${ygg.admin.api-base-url-for-gateway}")
    private String adminApiBaseUrlForGateway;

    @PostConstruct
    public void makeInitializationChecks() throws IOException {
        if (tmpDirStr == null) {
            throw new IOException(
                    "System property 'java.io.tmpdir' does not specify a tmp dir");
        }
    }

    public void addProxy(Proxy proxy) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(tmpDirStr + "/ygg-proxy-" + proxy.getId() + ".yml", "UTF-8");
        writer.println("proxy-id: " + proxy.getId());
        writer.println("ygg.admin.api-base-url: " + adminApiBaseUrlForGateway);
        if (proxy.getPort() != null) {
            writer.println("server.port: " + proxy.getPort());
        }
        for (ServiceApi service : proxy.getServiceApis()) {
            writer.println("zuul.routes." + service.getUriIdentifier() + ".url : " + service.getLocalUrl());
        }
        writer.println("zuul.host.connect-timeout-millis: 150000");
        writer.println("zuul.host.socket-timeout-millis: 150000");
        writer.close();
    }

    public void deleteProxy(Proxy proxy) throws FileNotFoundException, FileCouldNotBeDeletedException {
        File proxyConfigToBeDeleted = new File(tmpDirStr + "/ygg-proxy-" + proxy.getId() + ".yml", "UTF-8");
        if (!proxyConfigToBeDeleted.exists())
            throw new FileNotFoundException("Tried to delete the configuration for proxy " + proxy.getId()
                    + ", but there is no according file.");
        else {
            if (proxyConfigToBeDeleted.delete())
                log.debug("Proxy {} was successfully deleted.", proxy.getId());
            else
                throw new FileCouldNotBeDeletedException("Configuration file of proxy " + proxy.getId() + " could not be deleted.");
        }
    }

}
