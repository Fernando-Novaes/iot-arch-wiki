package br.ufrj.cos.repository;


import br.ufrj.cos.components.chart.data.TechnologyChartRecord;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.views.record.TechnologyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    @Query(value = "select t from Technology as t")
    List<Technology> searchAll();

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.TechnologyChartRecord(i.description, COUNT(i), (SELECT COUNT(*) FROM Technology i2)) FROM Technology i GROUP BY i.description")
    List<TechnologyChartRecord> countTechnologyGroupedByName();

    @Query(value = "SELECT new br.ufrj.cos.views.record.TechnologyRecord(i.description) FROM Technology i GROUP BY i.description")
    List<TechnologyRecord> findAllTechnologyGroupedByName();

    List<Technology> findByAssociations_ArchitectureSolution_Architecture_NameContainingIgnoreCase(String name);


    List<Technology> findByDescriptionContainingIgnoreCase(String description);

}
