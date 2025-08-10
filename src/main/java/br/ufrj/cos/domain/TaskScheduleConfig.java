package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, exclude = "appConfig")
public class TaskScheduleConfig extends DomainBase {

    @Id
    private String taskName; // e.g., "data-processing-task"

    private long fixedRateMilliseconds;
    private long initialDelayMilliseconds;
    private boolean active;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_config_id")
    private AppConfig appConfig;

    @Override
    public String toString() {
        return taskName;
    }
}