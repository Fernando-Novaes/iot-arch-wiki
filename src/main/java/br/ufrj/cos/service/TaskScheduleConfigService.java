package br.ufrj.cos.service;

import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.repository.TaskScheduleConfigRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskScheduleConfigService {

    private final TaskScheduleConfigRepository taskScheduleConfigRepository;

    public TaskScheduleConfigService(TaskScheduleConfigRepository taskScheduleConfigRepository) {
        this.taskScheduleConfigRepository = taskScheduleConfigRepository;
    }

    public TaskScheduleConfig save(TaskScheduleConfig taskScheduleConfig) {
        return taskScheduleConfigRepository.save(taskScheduleConfig);
    }

    public List<TaskScheduleConfig> findAll() {
        return taskScheduleConfigRepository.findAll(Sort.by("taskName"));
    }

    public TaskScheduleConfig findByName(String taskName) {
        return this.taskScheduleConfigRepository.findById(taskName).orElse(null);
    }
}
