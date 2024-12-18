package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ArchitectureSolutionQualityRequirementTechnology extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ArchitectureSolution Relationship (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "architecture_solution_id", nullable = false)
    private ArchitectureSolution architectureSolution;

    // QualityRequirement Relationship (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "quality_requirement_id", nullable = false)
    private QualityRequirement qualityRequirement;

    // Technology Relationship (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology;

    // Additional context or notes for this specific combination
    private String notes;

    @Override
    public String toString() {
        return String.format("%s - %s", this.getQualityRequirement(), this.getTechnology());
    }
}
