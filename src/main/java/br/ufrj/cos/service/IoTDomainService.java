package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.IoTDomainRecord;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.repository.IoTDomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IoTDomainService {

    private final IoTDomainRepository ioTDomainRepository;

    @Autowired
    public IoTDomainService(IoTDomainRepository ioTDomainRepository) {
        this.ioTDomainRepository = ioTDomainRepository;
    }

    public List<IoTDomainRecord> getIoTDomainCountGroupedByName() {
        return ioTDomainRepository.countIoTDomainsGroupedByName();
    }

    public List<IoTDomain> findAll() {
        return ioTDomainRepository.findAll();
    }

    public IoTDomain saveAndFlush(IoTDomain domain) {
        return ioTDomainRepository.saveAndFlush(domain);
    }

    public IoTDomain saveAndUpdate(IoTDomain domain) {
        return ioTDomainRepository.saveAndFlush(domain);
    }

    public List<IoTDomainRecord> countIoTDomainByArchitectureSolution() {
        return this.ioTDomainRepository.countIoTDomainByArchitectureSolution();
    }
}
