package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.IoTDomainRecord;
import br.ufrj.cos.components.treeview.TreeBuilder;
import br.ufrj.cos.components.treeview.TreeNode;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.PaperReference;
import br.ufrj.cos.repository.IoTDomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IoTDomainService {

    private final IoTDomainRepository ioTDomainRepository;
    private final TreeBuilder treeBuilder;

    @Autowired
    public IoTDomainService(IoTDomainRepository ioTDomainRepository) {
        this.ioTDomainRepository = ioTDomainRepository;
        this.treeBuilder = new TreeBuilder();
    }

    public TreeNode<Object> getTree() {
        List<IoTDomain> domains = ioTDomainRepository.findAll();
        return treeBuilder.buildTree(domains);
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
}
