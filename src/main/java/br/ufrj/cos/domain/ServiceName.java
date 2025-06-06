package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ServiceName extends DomainBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private APIServiceType type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_config_id")
    private AppConfig appConfig;
}