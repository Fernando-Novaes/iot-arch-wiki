package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "architecture_solution_id")
    private ArchitectureSolution architectureSolution;

    @OneToOne(mappedBy = "technology", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private QualityRequirement qualityRequirement;

    @Override
    public String toString() {
        return getDescription();
    }
}
