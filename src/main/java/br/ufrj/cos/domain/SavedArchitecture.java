package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SavedArchitecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String description;

    private String domain;
    private String pattern;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String layersJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String aiEvaluationReport;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserApplication userApplication;
}
