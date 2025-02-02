package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "associations")
public class QualityRequirement extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String description;

    @OneToMany(mappedBy = "qualityRequirement", fetch = FetchType.EAGER)
    private List<QualityRequirementTechnology> associations;

    public List<Technology> getTechnologies() {
        return associations.stream()
                .map(QualityRequirementTechnology::getTechnology)
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
        return this.name;
    }
}