package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QualityRequirement extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // One-to-Many relationship with ArchitectureSolutionQualityRequirementTechnology
    @OneToMany(mappedBy = "qualityRequirement", fetch = FetchType.EAGER)
    private List<ArchitectureSolutionQualityRequirementTechnology> architectureSolutionQualityRequirementTechnologies;

    /***
     * Get all Technologies of this QualityRequirement
     *
     * @return List<Technology>
     */
    public List<Technology> getTechnologies() {
        List<Technology> technologies = new ArrayList<>();
        this.architectureSolutionQualityRequirementTechnologies.forEach(technology -> {
            technologies.add(technology.getTechnology());
        });

        return technologies;
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
        return this.name;
    }
}