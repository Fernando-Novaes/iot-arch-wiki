package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaperReference extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String doi;

    private String link;

    private int publishYear;

    // ArchitectureSolution Relationship (One-to-One)
    @OneToOne(mappedBy = "paperReference", fetch = FetchType.EAGER)
    private ArchitectureSolution architectureSolution;

    @Override
    public String toString() {
        return title;
    }
}
