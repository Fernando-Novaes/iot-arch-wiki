package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.repository.IoTDomainRepository;
import br.ufrj.cos.views.record.IoTDomainRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IoTDomainService {

    private final IoTDomainRepository ioTDomainRepository;

    @Autowired
    public IoTDomainService(IoTDomainRepository ioTDomainRepository) {
        this.ioTDomainRepository = ioTDomainRepository;
    }

    public List<IoTDomainChartRecord> getIoTDomainCountGroupedByName() {
        return ioTDomainRepository.countIoTDomainsGroupedByName();
    }

    public List<IoTDomainRecord> findAllIoTDomainGroupedByName() {
        return ioTDomainRepository.findAllIoTDomainsGroupedByName();
    }

    public List<IoTDomain> findAll() {
        return ioTDomainRepository.findAll();
    }

    public List<IoTDomain> findAllOrderByName() {
        return ioTDomainRepository.findAll(Sort.by("name"));
    }

    public IoTDomain saveAndFlush(IoTDomain domain) {
        return ioTDomainRepository.saveAndFlush(domain);
    }

    public IoTDomain saveAndUpdate(IoTDomain domain) {
        return ioTDomainRepository.saveAndFlush(domain);
    }

    public List<IoTDomainChartRecord> countIoTDomainByArchitectureSolution() {
        return this.ioTDomainRepository.countIoTDomainByArchitectureSolution();
    }
}
