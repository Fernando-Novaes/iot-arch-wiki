package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude = {"ioTDomains", "architectures", "qualityRequirements", "technologies"})
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String text;

    private Date lastUpdate;

    @ManyToOne(fetch = FetchType.EAGER) // Define the relationship
    @JoinColumn(name = "user_application_id") // Specify the foreign key column
    private UserApplication userApplication;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "annotation_iot_domain",
            joinColumns = @JoinColumn(name = "annotation_id", nullable = true),
            inverseJoinColumns = @JoinColumn(name = "iot_domain_id", nullable = true)
    )
    private List<IoTDomain> ioTDomains;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "annotation_architecture",
            joinColumns = @JoinColumn(name = "annotation_id", nullable = true),
            inverseJoinColumns = @JoinColumn(name = "architecture_id", nullable = true)
    )
    private List<Architecture> architectures;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "annotation_quality_requirement",
            joinColumns = @JoinColumn(name = "annotation_id", nullable = true),
            inverseJoinColumns = @JoinColumn(name = "quality_requirement_id", nullable = true)
    )
    private List<QualityRequirement> qualityRequirements;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "annotation_technology",
            joinColumns = @JoinColumn(name = "annotation_id", nullable = true),
            inverseJoinColumns = @JoinColumn(name = "technology_id", nullable = true)
    )
    private List<Technology> technologies;

    @Override
    public String toString() {
        return text;
    }
}