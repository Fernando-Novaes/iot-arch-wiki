package br.ufrj.cos.service;

import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.ServiceName;
import br.ufrj.cos.repository.AppConfigRepository;
import br.ufrj.cos.repository.ServiceNameRepository;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;
    private final ServiceNameRepository serviceNameRepository;

    public AppConfigService(AppConfigRepository appConfigRepository, ServiceNameRepository serviceNameRepository) {
        this.appConfigRepository = appConfigRepository;
        this.serviceNameRepository = serviceNameRepository;
    }

    public void save(AppConfig appConfig) {
        appConfigRepository.saveAndFlush(appConfig);
    }

    public void deleteServiceName(ServiceName serviceName) {
        serviceNameRepository.delete(serviceName);
    }

    public void saveServiceName(ServiceName serviceName) {
        serviceNameRepository.save(serviceName);
    }

    public AppConfig getAppConfig() {
        return
                (appConfigRepository.findAll().isEmpty())? new AppConfig() : appConfigRepository.findAll().getFirst();
    }

    public ServiceName getServiceNameByType(APIServiceType serviceType) {
        return this.getAppConfig().getServiceNames().stream()
                .filter(serviceName -> serviceName.getType().equals(serviceType))
                .findFirst().orElse(null);
    }
}