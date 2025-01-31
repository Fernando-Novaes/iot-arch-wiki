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
@EqualsAndHashCode(callSuper = false, exclude = "architectureSolution")
public class QualityRequirementTechnology extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String notes;

    @ManyToOne
    private ArchitectureSolution architectureSolution;

    @ManyToOne
    private QualityRequirement qualityRequirement;

    @ManyToOne
    private Technology technology;

    @Override
    public String toString() {
        return String.format("%s - %s", this.getQualityRequirement(), this.getTechnology());
    }
}
