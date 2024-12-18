package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Technology extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private String remark;

    // One-to-Many relationship with ArchitectureSolutionQualityRequirementTechnology
    @OneToMany(mappedBy = "technology", fetch = FetchType.EAGER)
    private List<ArchitectureSolutionQualityRequirementTechnology> architectureSolutionQualityRequirementTechnologies;

    /***
     * Get all QualityRequirements of this Technology
     *
     * @return List<QualityRequirement>
     */
    public List<QualityRequirement> getQualityRequirements() {
        List<QualityRequirement> qualityRequirements = new ArrayList<>();
        this.architectureSolutionQualityRequirementTechnologies.forEach(qualityRequirement -> {
            qualityRequirements.add(qualityRequirement.getQualityRequirement());
        });

        return qualityRequirements;
    }

    /***
     * Get all ArchitectureSolutions of this Technology
     *
     * @return List<ArchitectureSolution>
     */
    public List<ArchitectureSolution> getArchitectureSolutions() {
        List<ArchitectureSolution> architectureSolutions = new ArrayList<>();
        this.architectureSolutionQualityRequirementTechnologies.forEach(arch -> {
            architectureSolutions.add(arch.getArchitectureSolution());
        });

        return architectureSolutions;
    }

    @Override
    public String toString() {
        return description;
    }
}
