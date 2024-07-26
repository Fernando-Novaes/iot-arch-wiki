package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"qrs", "technologies"})
public class ArchitectureSolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "paper_reference_id", nullable = true)
    private PaperReference paperReference;

    @OneToMany(mappedBy = "architectureSolution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<QualityRequirement> qrs;

    @OneToMany(mappedBy = "architectureSolution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Technology> technologies;

    @Override
    public String toString() {
        return this.getName();
    }
}
