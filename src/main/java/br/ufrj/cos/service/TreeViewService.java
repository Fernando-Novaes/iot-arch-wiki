package br.ufrj.cos.service;

import br.ufrj.cos.components.treeview.ArchitectureSolutionTreeBuilder;
import br.ufrj.cos.components.treeview.IoTDomainTreeBuilder;
import br.ufrj.cos.components.treeview.TreeNode;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.domain.IoTDomain;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreeViewService {

    private final IoTDomainTreeBuilder iotDomainTreeBuilder;
    private final ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder;
    private final IoTDomainService ioTDomainService;
    private final ArchitectureSolutionService architectureSolutionService;

    @Autowired
    public TreeViewService(IoTDomainTreeBuilder treeBuilder, ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder, IoTDomainService ioTDomainService, ArchitectureSolutionService architectureSolutionService) {
        this.iotDomainTreeBuilder = treeBuilder;
        this.architectureSolutionTreeBuilder = architectureSolutionTreeBuilder;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
    }

    public TreeNode<Object> getTree(TreeViewType treeViewType) {

        return switch (treeViewType) {
            case IoTDomain ->
                    iotDomainTreeBuilder.buildTreeIoTDomain(this.ioTDomainService.findAll());
            case ArchitectureSolution ->
                    architectureSolutionTreeBuilder.buildTreeArchitectureSolution(this.architectureSolutionService.findAllOrderedByName());
            default ->
                    null;
        };

    }

}
