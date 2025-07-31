package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.QualityRequirementChartRecord;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.views.record.QualityRequirementRecord;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface QualityRequirementRepository extends JpaRepository<QualityRequirement, Long> {

    @Query("SELECT DISTINCT qr FROM QualityRequirement qr " +
            "JOIN QualityRequirementTechnology qrt ON qrt.qualityRequirement = qr " +
            "JOIN ArchitectureSolution a ON qrt.architectureSolution = a " +
            "WHERE a.ioTDomain.name = :domainName " +
            "AND a.architecture.name = :architectureName")
    @QueryHints({@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "false")})
    List<QualityRequirement> findByIoTDomainAndArchitecture(
            @Param("domainName") String domainName,
            @Param("architectureName") String architectureName
    );

    @Query(value = "select q from QualityRequirement as q")
    List<QualityRequirement> searchAll();

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.QualityRequirementChartRecord(qr.name, COUNT(DISTINCT ast), " +
            "(SELECT COUNT(qr2) FROM QualityRequirement qr2)) " +
            "FROM QualityRequirement qr " +
            "JOIN QualityRequirementTechnology qrt ON qrt.qualityRequirement = qr " +
            "JOIN qrt.architectureSolution ast " +
            "GROUP BY qr.name")
    List<QualityRequirementChartRecord> countQualityRequirementGroupedByName();

    @Query(value = "SELECT new br.ufrj.cos.views.record.QualityRequirementRecord(i.name) FROM QualityRequirement i GROUP BY i.name")
    List<QualityRequirementRecord> findAllQualityRequirementGroupedByName();

    @Query("select q from QualityRequirement q join q.associations a where a.id = :id")
    List<QualityRequirement> findByArchitectureSolutionId(@Param("id") Long id);

    List<QualityRequirement> findAllByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT qr.name FROM QualityRequirement qr")
    List<String> findDistinctNames();

    List<QualityRequirement> findByAssociations_ArchitectureSolution_Architecture_Name_ContainingIgnoreCaseOrderByNameAsc(String name);

}
