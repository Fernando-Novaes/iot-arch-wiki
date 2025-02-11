package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude = "annotations")
public class Architecture extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "architecture", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ArchitectureSolution> architectureSolutions;

    @ManyToMany(mappedBy = "architectures")
    private List<Annotation> annotations;

    @Override
    public String toString() {
        return name;
    }
}
