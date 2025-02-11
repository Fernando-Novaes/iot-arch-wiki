package br.ufrj.cos.repository;

import br.ufrj.cos.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRepository  extends JpaRepository<Annotation, Long> {

    // 01 - Find all by userApplication
    List<Annotation> findByUserApplication(UserApplication userApplication);

    // 02 - Find all by userApplication and ioTDomain
    List<Annotation> findByUserApplicationAndIoTDomainsContainingOrderByLastUpdateDesc(UserApplication userApplication, IoTDomain ioTDomain);

    // 03 - Find all by userApplication and architecture
    List<Annotation> findByUserApplicationAndArchitecturesContainingOrderByLastUpdateDesc(UserApplication userApplication, Architecture architecture);

    // 04 - Find all by userApplication and qualityRequirement
    List<Annotation> findByUserApplicationAndQualityRequirementsContainingOrderByLastUpdateDesc(UserApplication userApplication, QualityRequirement qualityRequirement);

    // 05 - Find all by userApplication and technology
    List<Annotation> findByUserApplicationAndTechnologiesContainingOrderByLastUpdateDesc(UserApplication userApplication, Technology technology);

}
