package br.ufrj.cos.service;

import br.ufrj.cos.components.treeview.*;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.DomainBase;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
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
    private final QualityRequirementService qualityRequirementService;
    private final QualityRequirementTreeBuilder qualityRequirementTreeBuilder;

    @Getter
    private List<? extends DomainBase> treeViewData;

    @Autowired
    public TreeViewService(IoTDomainTreeBuilder treeBuilder, ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder, IoTDomainService ioTDomainService, ArchitectureSolutionService architectureSolutionService, QualityRequirementService qualityRequirementService, QualityRequirementTreeBuilder qualityRequirementTreeBuilder) {
        this.iotDomainTreeBuilder = treeBuilder;
        this.architectureSolutionTreeBuilder = architectureSolutionTreeBuilder;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.qualityRequirementTreeBuilder = qualityRequirementTreeBuilder;
    }

    private void setTreeViewData(List<? extends DomainBase> treeViewData) {
        this.treeViewData = treeViewData;
    }

    public TreeNode<Object> getTree(TreeViewType treeViewType) {

        return switch (treeViewType) {
            case IoTDomain -> {
                List<IoTDomain> list = this.ioTDomainService.findAllOrderByName();
                this.setTreeViewData(list);
                yield iotDomainTreeBuilder.setNodeAsRoot(list);
            }

            case ArchitectureSolution -> {
                List<ArchitectureSolution> list = this.architectureSolutionService.findAll();
                this.setTreeViewData(list);
                yield architectureSolutionTreeBuilder.setNodeAsRoot(list);
            }

            case QualityRequirement, Technology -> {
                List<QualityRequirement> list = this.qualityRequirementService.findAll();
                this.setTreeViewData(list);
                yield qualityRequirementTreeBuilder.setNodeAsRoot(list);
            }

            case Filtered -> {
                List<IoTDomain> list = (List<IoTDomain>) this.getTreeViewData();
                yield iotDomainTreeBuilder.setNodeAsRoot(list);
            }
        };

    }

}
