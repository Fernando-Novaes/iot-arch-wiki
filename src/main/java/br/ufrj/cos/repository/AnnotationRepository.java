package br.ufrj.cos.repository;

import br.ufrj.cos.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository  extends JpaRepository<Annotation, Long> {

    // 01 - Find all by userApplication
    List<Annotation> findByUserApplication(UserApplication userApplication);

    @Query("SELECT a FROM Annotation a " +
            "WHERE a.userApplication = :userApplication AND :iotDomain MEMBER OF a.ioTDomains " +
            "ORDER BY a.lastUpdate DESC")
    List<Annotation> findFirstByUserApplicationAndIoTDomainOrderByLastUpdateDesc(
            UserApplication userApplication,
            IoTDomain iotDomain);

    // 03 - Find all by userApplication and architecture
    List<Annotation> findByUserApplicationAndArchitecturesContainingOrderByLastUpdateDesc(UserApplication userApplication, Architecture architecture);

    // 04 - Find all by userApplication and qualityRequirement
    List<Annotation> findByUserApplicationAndQualityRequirementsContainingOrderByLastUpdateDesc(UserApplication userApplication, QualityRequirement qualityRequirement);

    // 05 - Find all by userApplication and technology
    List<Annotation> findByUserApplicationAndTechnologiesContainingOrderByLastUpdateDesc(UserApplication userApplication, Technology technology);

}
