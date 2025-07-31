package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.repository.IoTDomainRepository;
import br.ufrj.cos.views.record.IoTDomainRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<IoTDomain> findByNameContainingIgnoreCase(String name) {
        return ioTDomainRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<IoTDomainRecord> findAllIoTDomainGroupedByName() {
        ioTDomainRepository.flush();
        return ioTDomainRepository.findAllIoTDomainsGroupedByName();
    }

    @Transactional(readOnly = true)
    public List<IoTDomain> findAll() {
        ioTDomainRepository.flush();
        return ioTDomainRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<IoTDomain> findAllOrderByName() {
        ioTDomainRepository.flush();
        return ioTDomainRepository.findAll(Sort.by("name"));
    }

    public IoTDomain saveAndFlush(IoTDomain domain) {
        return ioTDomainRepository.saveAndFlush(domain);
    }

    public IoTDomain saveOrUpdate(IoTDomain domain) {
        return ioTDomainRepository.save(domain);
    }

    public List<IoTDomainChartRecord> countIoTDomainByArchitectureSolution() {
        return ioTDomainRepository.countIoTDomainByArchitectureSolution();
    }

    public void delete(IoTDomain domain) {
        ioTDomainRepository.delete(domain);
    }

    public List<IoTDomain> findAllByDomainName(List<String> domainNames) {
        return this.ioTDomainRepository.findIoTDomainsByNameInOrderByNameAsc(domainNames);
    }
}
