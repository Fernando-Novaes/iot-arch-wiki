package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private String notes;

    @OneToMany(mappedBy = "technology", fetch = FetchType.EAGER)
    private List<QualityRequirementTechnology> associations;

    public List<QualityRequirement> getQualityRequirements() {
        return associations.stream()
                .map(QualityRequirementTechnology::getQualityRequirement)
                .distinct()
                .toList();
    }

    public List<ArchitectureSolution> getArchitectureSolutions() {
        return associations.stream()
                .map(QualityRequirementTechnology::getArchitectureSolution)
                .distinct()
                .toList();
    }

    @Override
    public String toString() {
        return description;
    }
}
