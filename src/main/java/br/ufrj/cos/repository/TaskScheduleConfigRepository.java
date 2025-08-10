package br.ufrj.cos.repository;

import br.ufrj.cos.domain.TaskScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskScheduleConfigRepository extends JpaRepository<TaskScheduleConfig, String> { }
