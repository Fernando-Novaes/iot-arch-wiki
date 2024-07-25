package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"architectureSolutions"})
public class QualityRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "technology_id")
    private Technology technology;

    @ManyToMany(mappedBy = "qrs", fetch = FetchType.EAGER)
    private Set<ArchitectureSolution> architectureSolutions;

    @Override
    public String toString() {
        return this.getName();
    }
}