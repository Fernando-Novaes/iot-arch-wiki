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
@EqualsAndHashCode(callSuper = false)
public class PaperReference extends DomainBase {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;
        private String doi;
        private String link;
        @Column(nullable = true)
        private Integer publishYear;

        @OneToOne(mappedBy = "paperReference", cascade = CascadeType.ALL, orphanRemoval = true)
        private ArchitectureSolution architectureSolution;


        @Override
        public String toString() {
            return title;
        }
}
