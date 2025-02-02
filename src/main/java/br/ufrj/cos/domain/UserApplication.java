package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String userName;

    @Enumerated(EnumType.STRING)
    private Role role; // Enum for USER or ADMIN

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfCreation;

    @Lob
    @Column(columnDefinition = "CLOB")
    @Basic(fetch = FetchType.LAZY)
    private String notes;

    public enum Role {
        USER, ADMIN
    }

    @Override
    public String toString() {
        return name;
    }
}
