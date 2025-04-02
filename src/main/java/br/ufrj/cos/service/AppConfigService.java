package br.ufrj.cos.service;

import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.ServiceName;
import br.ufrj.cos.repository.AppConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigService(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    public void save(AppConfig appConfig) {
        appConfigRepository.save(appConfig);
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