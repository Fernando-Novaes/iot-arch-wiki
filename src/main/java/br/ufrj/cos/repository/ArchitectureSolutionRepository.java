package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface ArchitectureSolutionRepository extends JpaRepository<ArchitectureSolution, Long> {

    @Query("SELECT DISTINCT a FROM ArchitectureSolution a WHERE a.ioTDomain.name = :domainName")
    @QueryHints({@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "false")})
    List<ArchitectureSolution> findByIoTDomainName(@Param("domainName") String domainName);

    @Query("SELECT a FROM ArchitectureSolution a")
    List<ArchitectureSolution> findAll();

    @Query("SELECT new br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord(a.architecture.name, COUNT(a), (SELECT COUNT(b) FROM ArchitectureSolution b)) FROM ArchitectureSolution a GROUP BY a.architecture.name")
    List<ArchitectureSolutionChartRecord> countArchitectureSolutionGroupedByName();

    @Query("SELECT new br.ufrj.cos.views.record.ArchitectureSolutionRecord(a.architecture.name) FROM ArchitectureSolution a GROUP BY a.architecture.name")
    List<ArchitectureSolutionRecord> findAllArchitectureSolutionGroupedByName();

    @Query("SELECT a FROM ArchitectureSolution a JOIN a.qualityRequirementTechnologies q WHERE q.qualityRequirement.id = :id")
    List<ArchitectureSolution> findByQualityRequirementId(@Param("id") Long id);

    List<ArchitectureSolution> findByArchitecture_NameContainingIgnoreCase(String name);

    List<ArchitectureSolution> findArchitectureSolutionByIoTDomain(IoTDomain domain);

    // Encontra soluções baseadas em um conjunto de nomes de domínio.
    List<ArchitectureSolution> findByIoTDomainNameIn(Collection<String> domainNames);
}
