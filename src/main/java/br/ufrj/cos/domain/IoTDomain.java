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
@EqualsAndHashCode(callSuper = false)
public class IoTDomain extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    // ArchitectureSolution Relationship (One-to-Many)
    @OneToMany(mappedBy = "iotDomain", fetch = FetchType.EAGER)
    private List<ArchitectureSolution> architectureSolutions;

    @Override
    public String toString() {
        return this.name;
    }
}