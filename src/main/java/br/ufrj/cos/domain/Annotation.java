package br.ufrj.cos.domain;

import com.vaadin.flow.server.PwaIcon;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false, exclude = {"annotationDomains"})
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String text;

    private Date lastUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_application_id")
    private UserApplication userApplication;

    @OneToMany(mappedBy = "annotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AnnotationDomain> annotationDomains;

    public DomainBase getAnnotationDomainType(AnnotationDomain annotationDomain) {
        if ((annotationDomain.getIoTDomain() != null) && (annotationDomain.getArchitecture() == null) && (annotationDomain.getQualityRequirement() == null) && (annotationDomain.getTechnology() == null)) {
            return annotationDomain.getIoTDomain();
        } else if ((annotationDomain.getIoTDomain() != null) && (annotationDomain.getArchitecture() != null) && (annotationDomain.getQualityRequirement() == null) && (annotationDomain.getTechnology() == null)) {
            return annotationDomain.getArchitecture();
        } else if ((annotationDomain.getIoTDomain() != null) && (annotationDomain.getArchitecture() != null) && (annotationDomain.getQualityRequirement() != null) && (annotationDomain.getTechnology() == null)) {
            return annotationDomain.getQualityRequirement();
        } else if ((annotationDomain.getIoTDomain() != null) && (annotationDomain.getArchitecture() != null) && (annotationDomain.getQualityRequirement() != null) && (annotationDomain.getTechnology() != null)) {
            return annotationDomain.getTechnology();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}