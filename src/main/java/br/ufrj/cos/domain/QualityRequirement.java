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
@EqualsAndHashCode(exclude = {"techs", "architectureSolutions"})
@ToString(exclude = {"techs", "architectureSolutions"})
public class QualityRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Technology> techs;

    @ManyToMany(mappedBy = "qrs", fetch = FetchType.EAGER)
    private Set<ArchitectureSolution> architectureSolutions;
}