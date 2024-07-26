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

    @Column(nullable = false, columnDefinition = "CLOB")
    private String description;

    @ManyToOne
    private ArchitectureSolution architectureSolution;

    @OneToOne
    private QualityRequirement qualityRequirement;

    @Override
    public String toString() {
        return getDescription();
    }
}
