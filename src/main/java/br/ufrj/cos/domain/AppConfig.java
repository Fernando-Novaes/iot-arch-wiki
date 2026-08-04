package br.ufrj.cos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = "tasks")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppConfig extends DomainBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apiAddress;

    @Column(length = 500)
    private String googleApiKey;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScrapWebSite> scrapWebSites;

    @OneToMany(mappedBy = "appConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ServiceName> serviceNames;

    private Instant knowledgeDatabaseLastUpdate;

    private Instant aiRagDocumentsLastUpdate;

    @Builder.Default
    private Boolean allowExternalContext = true;

    @Builder.Default
    private Boolean allowUserContextOverride = true;

    @ToString.Exclude
    @OneToMany(mappedBy = "appConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TaskScheduleConfig> tasks;
}