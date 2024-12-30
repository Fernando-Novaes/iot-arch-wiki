package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "paperReference")
public class ArchitectureSolution extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "architecture_id", nullable = false)
    private Architecture architecture;

    @OneToOne
    private PaperReference paperReference;

    @ManyToOne
    private IoTDomain ioTDomain;

    @OneToMany(mappedBy = "architectureSolution", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<QualityRequirementTechnology> qualityRequirementTechnologies;

    public List<QualityRequirement> getQualityRequirements() {
        return qualityRequirementTechnologies.stream()
                .map(QualityRequirementTechnology::getQualityRequirement)
                .distinct()
                .toList();
    }

    public List<Technology> getTechnologies() {
        return qualityRequirementTechnologies.stream()
                .map(QualityRequirementTechnology::getTechnology)
                .distinct()
                .toList();
    }

    @Override
    public String toString() {
        if (this.getArchitecture() != null) {
            return this.architecture.getName();
        } else {
            return "No Architecture associated.";
        }
    }
}
