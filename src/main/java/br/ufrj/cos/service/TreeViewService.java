package br.ufrj.cos.service;

import br.ufrj.cos.components.treeview.*;
import br.ufrj.cos.components.treeview.builder.ArchitectureSolutionTreeBuilder;
import br.ufrj.cos.components.treeview.builder.IoTDomainTreeBuilder;
import br.ufrj.cos.components.treeview.builder.QualityRequirementTreeBuilder;
import br.ufrj.cos.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TreeViewService {

    private final IoTDomainTreeBuilder iotDomainTreeBuilder;
    private final ArchitectureSolutionTreeBuilder architectureSolutionTreeBuilder;
    private final IoTDomainService ioTDomainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final QualityRequirementTreeBuilder qualityRequirementTreeBuilder;

    @Getter @Setter
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

    public TreeNode<Object> getTree(TreeViewType treeViewType) {

        return switch (treeViewType) {
            case IoTDomain -> {
                List<IoTDomain> list = this.ioTDomainService.findAllOrderByName();
                list.sort(Comparator.comparing(IoTDomain::getName));
                this.setTreeViewData(list);
                yield iotDomainTreeBuilder.setNodeAsRoot(list);
            }

            case ArchitectureSolution -> {
                List<ArchitectureSolution> list = this.architectureSolutionService.findAll();
                list.sort(Comparator.comparing(architectureSolution -> architectureSolution.getArchitecture().getName()));
                this.setTreeViewData(list);
                yield architectureSolutionTreeBuilder.setNodeAsRoot(list);
            }

            case QualityRequirement, Technology -> {
                List<QualityRequirement> list = this.qualityRequirementService.findAll();
                list.sort(Comparator.comparing(QualityRequirement::getName));
                this.setTreeViewData(list);
                yield qualityRequirementTreeBuilder.setNodeAsRoot(list);
            }

            case IoTDomain_Filtered -> {
                List<IoTDomain> list = (List<IoTDomain>) this.getTreeViewData();
                yield iotDomainTreeBuilder.setNodeAsRoot(list);
            }

            case ArchitectureSolution_Filtered -> {
                List<ArchitectureSolution> list = new ArrayList<>();
                ((List<IoTDomain>) this.getTreeViewData()).forEach(domain -> {
                    list.addAll(domain.getArchitectureSolutions());
                });

                yield architectureSolutionTreeBuilder.setNodeAsRoot(list);
            }

            case QualityRequirement_Filtered, Technology_Filtered -> {
                List<QualityRequirement> list = new ArrayList<>();
                ((List<IoTDomain>) this.getTreeViewData()).forEach(domain -> {
                    domain.getArchitectureSolutions().forEach(solution -> {
                        solution.getQualityRequirementTechnologies().forEach(qrTech -> {
                            list.add(qrTech.getQualityRequirement());
                        });
                    });
                });
                yield qualityRequirementTreeBuilder.setNodeAsRoot(list);
            }

            case Filtered -> {
                List<IoTDomain> list = (List<IoTDomain>) this.getTreeViewData();
                yield iotDomainTreeBuilder.setNodeAsRoot(list);
            }
        };

    }

}
