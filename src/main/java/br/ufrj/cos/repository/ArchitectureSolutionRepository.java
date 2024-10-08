package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchitectureSolutionRepository extends JpaRepository<ArchitectureSolution, Long> {

    @Query(value = "select a from ArchitectureSolution a")
    List<ArchitectureSolution> findAll();

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord(i.name, COUNT(i), (SELECT COUNT(*) FROM ArchitectureSolution i2)) FROM ArchitectureSolution i GROUP BY i.name")
    List<ArchitectureSolutionRecord> countArchitectureSolutionGroupedByName();

    @Query("select a from ArchitectureSolution a join a.qrs q where q.id = :id")
    List<ArchitectureSolution> findByQualityRequirementId(@Param("id") Long id);

    List<ArchitectureSolution> findByNameContainingIgnoreCase(String name);
}
