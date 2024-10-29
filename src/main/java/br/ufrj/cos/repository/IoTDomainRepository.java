package br.ufrj.cos.repository;

import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.views.record.IoTDomainRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IoTDomainRepository extends JpaRepository<IoTDomain, Long> {

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.IoTDomainChartRecord(i.name, COUNT(i), (SELECT COUNT(*) FROM IoTDomain i2)) FROM IoTDomain i GROUP BY i.name")
    List<IoTDomainChartRecord> countIoTDomainsGroupedByName();

    @Query(value = "SELECT new br.ufrj.cos.views.record.IoTDomainRecord(i.name) FROM IoTDomain i GROUP BY i.name")
    List<IoTDomainRecord> findAllIoTDomainsGroupedByName();

    @Query("SELECT new br.ufrj.cos.components.chart.data.IoTDomainChartRecord(d.name, COUNT(d), (SELECT COUNT(*) FROM IoTDomain i2)) FROM IoTDomain d JOIN d.archs a GROUP BY d")
    List<IoTDomainChartRecord> countIoTDomainByArchitectureSolution();
}