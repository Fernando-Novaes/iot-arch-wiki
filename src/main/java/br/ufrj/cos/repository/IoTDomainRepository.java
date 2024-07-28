package br.ufrj.cos.repository;

import br.ufrj.cos.components.chart.data.IoTDomainRecord;
import br.ufrj.cos.domain.IoTDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IoTDomainRepository extends JpaRepository<IoTDomain, Long> {

    @Query(value = "SELECT new br.ufrj.cos.components.chart.data.IoTDomainRecord(i.name, COUNT(i), (SELECT COUNT(*) FROM IoTDomain i2)) FROM IoTDomain i GROUP BY i.name")
    List<IoTDomainRecord> countIoTDomainsGroupedByName();

    @Query("SELECT new br.ufrj.cos.components.chart.data.IoTDomainRecord(d.name, COUNT(d), (SELECT COUNT(*) FROM IoTDomain i2)) FROM IoTDomain d JOIN d.archs a GROUP BY d")
    List<IoTDomainRecord> countIoTDomainByArchitectureSolution();
}