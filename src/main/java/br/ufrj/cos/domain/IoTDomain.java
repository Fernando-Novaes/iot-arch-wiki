package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "architectureSolutions")
public class IoTDomain extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Lob
    @Column(columnDefinition = "CLOB")
    private String description;

    @OneToMany(mappedBy = "ioTDomain", fetch = FetchType.EAGER)
    private List<ArchitectureSolution> architectureSolutions;

    @Override
    public String toString() {
        return this.name;
    }
}