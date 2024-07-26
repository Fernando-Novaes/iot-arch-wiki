package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.TechnologyRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    @Query(value = "select t from Technology as t")
    List<Technology> searchAll();

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.TechnologyRecord(i.description, COUNT(i), (SELECT COUNT(*) FROM Technology i2)) FROM Technology i GROUP BY i.description")
    List<TechnologyRecord> countTechnologyGroupedByName();

    List<Technology> findByArchitectureSolutionNameContainingIgnoreCase(String name);

}
