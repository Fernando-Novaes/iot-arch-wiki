package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AnnotationDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    @ManyToOne(fetch = FetchType.EAGER, optional = true) // Make optional
    @JoinColumn(name = "iot_domain_id")
    private IoTDomain ioTDomain;

    @ManyToOne(fetch = FetchType.EAGER, optional = true) // Make optional
    @JoinColumn(name = "architecture_id")
    private Architecture architecture;

    @ManyToOne(fetch = FetchType.EAGER, optional = true) // Make optional
    @JoinColumn(name = "technology_id")
    private Technology technology;

    @ManyToOne(fetch = FetchType.EAGER, optional = true) // Make optional
    @JoinColumn(name = "quality_requirement_id")
    private QualityRequirement qualityRequirement;
}
