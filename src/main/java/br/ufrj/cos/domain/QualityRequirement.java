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
@EqualsAndHashCode(exclude = {"architectureSolution", "technology"})
public class QualityRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(mappedBy = "qualityRequirement")
    private Technology technology;

    @ManyToOne
    private ArchitectureSolution architectureSolution;

    @Override
    public String toString() {
        return this.getName();
    }
}