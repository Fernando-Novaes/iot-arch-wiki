package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DialectOverride;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Technology extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String description;

    @Column(nullable = true, columnDefinition = "CLOB")
    private String remark;

    @ManyToOne(fetch = FetchType.EAGER)
    private ArchitectureSolution architectureSolution;

    @OneToOne(fetch = FetchType.EAGER)
    private QualityRequirement qualityRequirement;

    public IoTDomain getIoTDomain() {
        if (this.architectureSolution != null) {
            return this.architectureSolution.getIoTDomain();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
