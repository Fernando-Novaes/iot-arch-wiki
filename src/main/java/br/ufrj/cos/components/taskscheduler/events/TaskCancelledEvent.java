package br.ufrj.cos.components.taskscheduler.events;

import br.ufrj.cos.components.taskscheduler.TaskConfig;
import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.service.TaskScheduleConfigService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

public class TaskCancelledEvent extends ApplicationEvent {
    private static final Logger log = LoggerFactory.getLogger(TaskScheduledEvent.class);

    @Getter
    private final String taskName;
    private final TaskScheduleConfigService taskScheduleConfigService;


    public TaskCancelledEvent(Object source, String taskName, TaskScheduleConfigService taskScheduleConfigService) {
        super(source);
        this.taskName = taskName;
        this.taskScheduleConfigService = taskScheduleConfigService;

        this.updateTaskInfo(taskName);
    }

    private void updateTaskInfo(String taskName) {
        TaskScheduleConfig task = this.taskScheduleConfigService.findByName(taskName);
        task.setActive(Boolean.FALSE);
        taskScheduleConfigService.save(task);
        log.info("Updated status task schedule config (cancel): {}", task.getTaskName());

    }
}