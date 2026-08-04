package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "annotations")
public class UserApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String userName;

    @Enumerated(EnumType.STRING)
    private Role role; // Enum for USER or ADMIN

    //Bcrypt Hash
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfCreation;

    @Lob
    @Column(columnDefinition = "CLOB")
    @Basic(fetch = FetchType.LAZY)
    private String notes;

    private Boolean allowExternalContext;

    @OneToMany(mappedBy = "userApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Annotation> annotations;

    public enum Role {
        USER, ADMIN
    }

    @Override
    public String toString() {
        return name;
    }
}
