package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"qrs", "technologies"}, callSuper = false)
public class ArchitectureSolution extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true, columnDefinition = "CLOB")
    private String description;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "paper_reference_id", unique = false, nullable = true)
    private PaperReference paperReference;

    @OneToMany(mappedBy = "architectureSolution", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<QualityRequirement> qrs;

    @OneToMany(mappedBy = "architectureSolution", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Technology> technologies;

    @Override
    public String toString() {
        return this.getName();
    }

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private IoTDomain ioTDomain;
}
