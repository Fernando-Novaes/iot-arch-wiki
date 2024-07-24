package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(exclude = {"qrs", "paperReferences"})
public class ArchitectureSolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PaperReference> paperReferences;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "architecture_solution_quality_requirement",
            joinColumns = @JoinColumn(name = "architecture_solution_id"),
            inverseJoinColumns = @JoinColumn(name = "quality_requirement_id")
    )
    private Set<QualityRequirement> qrs;

    @Override
    public String toString() {
        return this.getName();
    }
}
