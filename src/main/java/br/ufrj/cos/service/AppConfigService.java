package br.ufrj.cos.service;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.api.TextToRagStoreRequest;
import br.ufrj.cos.components.taskscheduler.TaskConfig;
import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.ServiceName;
import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.repository.AppConfigRepository;
import br.ufrj.cos.repository.ServiceNameRepository;
import br.ufrj.cos.repository.TaskScheduleConfigRepository;
import br.ufrj.cos.utils.NotificationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AppConfigService {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);

    private final AppConfigRepository appConfigRepository;
    private final ServiceNameRepository serviceNameRepository;
    private RAGService ragService;
    private AppConfig appConfig;
    private final APIServiceConnection apiServiceConnection;
    private final TaskScheduleConfigRepository taskConfigRepository;

    public AppConfigService(AppConfigRepository appConfigRepository, ServiceNameRepository serviceNameRepository, RAGService ragService, APIServiceConnection apiServiceConnection, TaskScheduleConfigRepository taskConfigRepository) {
        this.appConfigRepository = appConfigRepository;
        this.serviceNameRepository = serviceNameRepository;
        this.ragService = ragService;
        this.apiServiceConnection = apiServiceConnection;
        this.taskConfigRepository = taskConfigRepository;
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
        List<AppConfig> all = appConfigRepository.findAll();
        if (all.isEmpty()) {
            AppConfig config = new AppConfig();
            return appConfigRepository.saveAndFlush(config);
        }
        return all.get(0);
    }

    public ServiceName getServiceNameByType(APIServiceType serviceType) {
        return this.getAppConfig().getServiceNames().stream()
                .filter(serviceName -> serviceName.getType().equals(serviceType))
                .findFirst().orElse(null);
    }

    public TaskScheduleConfig getTaskConfig(String taskName) {
        return this.taskConfigRepository.findById(taskName).orElse(new TaskScheduleConfig());
    }

    public List<TaskScheduleConfig> getTaskConfigs() {
        return this.taskConfigRepository.findAll(Sort.by(Sort.Direction.DESC, "taskName"));
    }

}

