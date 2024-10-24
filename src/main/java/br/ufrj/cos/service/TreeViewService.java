package br.ufrj.cos.service;

import br.ufrj.cos.components.treeview.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TreeViewService {

    private final IoTDomainTreeBuilder iotDomainTreeBuilder;
    private final ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder;
    private final IoTDomainService ioTDomainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final QualityRequirementTreeBuilder qualityRequirementTreeBuilder;

    @Autowired
    public TreeViewService(IoTDomainTreeBuilder treeBuilder, ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder, IoTDomainService ioTDomainService, ArchitectureSolutionService architectureSolutionService, QualityRequirementService qualityRequirementService, QualityRequirementTreeBuilder qualityRequirementTreeBuilder) {
        this.iotDomainTreeBuilder = treeBuilder;
        this.architectureSolutionTreeBuilder = architectureSolutionTreeBuilder;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.qualityRequirementTreeBuilder = qualityRequirementTreeBuilder;
    }

    public TreeNode<Object> getTree(TreeViewType treeViewType) {

        return switch (treeViewType) {
            case IoTDomain ->
                    iotDomainTreeBuilder.setNodeAsRoot(this.ioTDomainService.findAllOrderByName());
            case ArchitectureSolution ->
                    architectureSolutionTreeBuilder.setNodeAsRoot(this.architectureSolutionService.findAllOrderedByName());
            case QualityRequirement, Technology ->
                    qualityRequirementTreeBuilder.setNodeAsRoot(this.qualityRequirementService.findAllOrderedByName());
        };

    }

}
