package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.QualityRequirementRecord;
import br.ufrj.cos.domain.QualityRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityRequirementRepository extends JpaRepository<QualityRequirement, Long> {

    @Query(value = "select q from QualityRequirement as q")
    List<QualityRequirement> searchAll();

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.QualityRequirementRecord(i.name, COUNT(i), (SELECT COUNT(*) FROM QualityRequirement i2)) FROM QualityRequirement i GROUP BY i.name")
    List<QualityRequirementRecord> countQualityRequirementGroupedByName();
}
