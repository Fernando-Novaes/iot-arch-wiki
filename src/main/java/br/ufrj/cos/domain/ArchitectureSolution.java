package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ArchitectureSolution extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    // iotDomain Relationship (Many-to-One)
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "iot_domain_id", nullable = true)
    private IoTDomain iotDomain;

    // QualityRequirement and Technology Combination Relationship
    @OneToMany(mappedBy = "architectureSolution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ArchitectureSolutionQualityRequirementTechnology> qualityRequirementTechnologies;

    // PaperReference Relationship (One-to-One)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "paper_reference_id", unique = true)
    private PaperReference paperReference;

    /***
     * Get all QualityRequirements of this ArchitectureSolution
     *
     * @return List<QualityRequirement>
     */
    public List<QualityRequirement> getQualityRequirements() {
        List<QualityRequirement> qualityRequirements = new ArrayList<>();
        this.qualityRequirementTechnologies.forEach(qualityRequirement -> {
            qualityRequirements.add(qualityRequirement.getQualityRequirement());
        });

        return qualityRequirements;
    }

    /***
     * Get all Technologies of this ArchitectureSolution
     *
     * @return List<Technology>
     */
    public List<Technology> getTechnologies() {
        List<Technology> technologies = new ArrayList<>();
        this.qualityRequirementTechnologies.forEach(technology -> {
            technologies.add(technology.getTechnology());
        });

        return technologies;
    }

    @Override
    public String toString() {
        return name;
    }
}
