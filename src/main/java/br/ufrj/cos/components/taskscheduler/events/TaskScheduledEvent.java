package br.ufrj.cos.components.taskscheduler.events;

import br.ufrj.cos.components.taskscheduler.TaskConfig;
import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.service.TaskScheduleConfigService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

public class TaskScheduledEvent extends ApplicationEvent {
    private static final Logger log = LoggerFactory.getLogger(TaskScheduledEvent.class);

    @Getter
    private final TaskConfig config;
    private final TaskScheduleConfigService taskScheduleConfigService;

    public TaskScheduledEvent(Object source, TaskConfig config, TaskScheduleConfigService taskScheduleConfigService) {
        super(source);
        this.config = config;
        this.taskScheduleConfigService = taskScheduleConfigService;

        this.updateTaskInfo(this.config);
    }

    private void updateTaskInfo(TaskConfig config) {
        TaskScheduleConfig task = this.taskScheduleConfigService.findByName(config.getTaskName());
        task.setActive(Boolean.TRUE);
        task.setFixedRateMilliseconds(config.getFixedRateMilliseconds());
        task.setInitialDelayMilliseconds(config.getInitialDelayMilliseconds());
        taskScheduleConfigService.save(task);
        log.info("Updated status task schedule config (active): {}", task.getTaskName());

    }
}