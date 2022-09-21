package eu.coatrack.admin.service;

import eu.coatrack.admin.config.ValueConfig;
import eu.coatrack.admin.logic.CreateProxyAction;
import eu.coatrack.admin.model.repository.ProxyRepository;
import eu.coatrack.admin.model.vo.ServiceWizardForm;
import eu.coatrack.api.Proxy;
import eu.coatrack.api.ServiceApi;
import eu.coatrack.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProxyService {
    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ValueConfig config;

    @Autowired
    private CreateProxyAction createProxyAction;

    public Proxy create(User authenticatedUser, ServiceApi service) {
        Proxy proxy = new Proxy();
        proxy.setPort(config.getProxyServerDefaultPort());
        proxy.setName("Gateway for " + service.getName());
        proxy.setOwner(authenticatedUser);
        proxy.setDescription("Gateway generated by the getting started wizard");

        createProxyAction.setProxy(proxy);
        createProxyAction.setUser(authenticatedUser);
        createProxyAction.setSelectedServices(Collections.singletonList(service.getId()));
        createProxyAction.execute();
        return createProxyAction.getProxy();
    }

    public List<String> findByServiceId(long serviceId) {
        return proxyRepository.customSearchForAllProxiesForGivenServiceApiId(serviceId).stream()
                .map(Proxy::getPublicUrl).filter(Objects::nonNull)
                .filter(publicUrl -> !publicUrl.equals("")).collect(Collectors.toList());
    }

    public Proxy findById(String id) {
        return proxyRepository.findById(id).orElse(null);
    }

    public Proxy save(Proxy proxy)  {
        return proxyRepository.save(proxy);
    }
}
