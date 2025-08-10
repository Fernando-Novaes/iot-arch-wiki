package br.ufrj.cos.components.taskscheduler;

import br.ufrj.cos.components.taskscheduler.events.TaskCancelledEvent;
import br.ufrj.cos.components.taskscheduler.events.TaskScheduledEvent;
import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.TaskScheduleConfigService;
import br.ufrj.cos.tasks.RAGDataUpdate;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class TaskRegister {

    private static final Logger log = LoggerFactory.getLogger(TaskRegister.class);

    private Runnable task;
    private final TaskConfig taskConfig;
    private AppConfigService appConfigService;
    private RAGDataUpdate ragDataUpdate;
    private final ApplicationEventPublisher eventPublisher; // For publishing events
    private final TaskScheduleConfigService taskScheduleConfigService;
    private final TaskScheduler taskScheduler;
    private final ApplicationContext context;

    // A map to hold the handles of all scheduled tasks
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public TaskRegister(TaskConfig taskConfig, AppConfigService appConfigService, RAGDataUpdate ragDataUpdate, ApplicationEventPublisher eventPublisher, TaskScheduleConfigService taskScheduleConfigService, TaskScheduler taskScheduler, ApplicationContext context) {
        this.taskConfig = taskConfig;
        this.appConfigService = appConfigService;
        this.ragDataUpdate = ragDataUpdate;
        this.eventPublisher = eventPublisher;
        this.taskScheduleConfigService = taskScheduleConfigService;
        this.taskScheduler = taskScheduler;
        this.context = context;
    }

    @PostConstruct
    private void init() {
        startAllTasks();
    }

    private void startAllTasks() {
        List<TaskScheduleConfig> tasks = this.appConfigService.getTaskConfigs();

        tasks.forEach(task -> {
            if (task.isActive()) {
                if (task.getTaskName().equals(RAGDataUpdate.SERVICE_NAME)) {
                    ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                            ragDataUpdate,
                            Duration.ofMillis(task.getFixedRateMilliseconds())
                    );
                    // Store the handle so we can cancel it later
                    scheduledTasks.put(task.getTaskName(), future);
                }
            }
        });
    }

    /**
     * Schedules a task based on the provided configuration.
     * If a task with the same name is already scheduled, it will be cancelled and rescheduled.
     * @param config The configuration object for the task.
     */
    public void scheduleTask(Runnable task, TaskConfig config) {
        this.task = task;

        // First, cancel any existing task with this name
        if (taskConfig.isActive()) {
            cancelTask(taskConfig.getTaskName());
        }

//        if (!config.isActive()) {
//            log.info("Task '{}' is disabled and will not be scheduled.", config.getTaskName());
//            return;
//        }

        // Look up the Runnable bean by its name from the ApplicationContext
        if (this.task == null) {
            throw new RuntimeException("No task registered for task '" + config.getTaskName() + "'");
        }

        log.info("Scheduling task '{}' with fixed rate of {} ms and initial delay of {} ms.",
                config.getTaskName(), config.getFixedRateMilliseconds(), config.getInitialDelayMilliseconds());

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                this.task,
                Duration.ofMillis(config.getFixedRateMilliseconds())
        );

        // Store the handle so we can cancel it later
        scheduledTasks.put(config.getTaskName(), future);

        // *** PUBLISH THE SCHEDULED EVENT ***
        eventPublisher.publishEvent(new TaskScheduledEvent(this, config, this.taskScheduleConfigService));

    }

    /**
     * Cancels a scheduled task by its name.
     * @param taskName The name of the task to cancel.
     */
    public void cancelTask(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.get(taskName);
        if (future != null && !future.isCancelled()) {
            log.info("Cancelling scheduled task '{}'.", taskName);
            future.cancel(false); // false = don't interrupt if running
            scheduledTasks.remove(taskName);
            // *** PUBLISH THE CANCELLED EVENT ***
            eventPublisher.publishEvent(new TaskCancelledEvent(this, taskName, this.taskScheduleConfigService));
        }
    }

}
