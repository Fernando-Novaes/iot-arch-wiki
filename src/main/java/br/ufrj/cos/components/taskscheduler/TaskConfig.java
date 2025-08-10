package br.ufrj.cos.components.taskscheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component("taskConfig")
public class TaskConfig {

    private String taskName;
    private long fixedRateMilliseconds;
    private long initialDelayMilliseconds;
    private boolean active;

}