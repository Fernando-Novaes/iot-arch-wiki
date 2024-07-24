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
@EqualsAndHashCode
public class PaperReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)")
    private String paperTitle;

    @Column(columnDefinition = "VARCHAR(255)")
    private String paperDoi;

    @Column(columnDefinition = "VARCHAR(255)")
    private String paperLink;

    @Override
    public String toString() {
        return this.paperTitle;
    }
}
