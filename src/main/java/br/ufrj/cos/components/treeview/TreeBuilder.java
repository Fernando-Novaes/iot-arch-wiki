package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.utils.ClassTypeUtils;
import br.ufrj.cos.views.record.IoTDomainRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class TreeBuilder implements IoTDomainTreeBuilder, ArchitectureSolutionTreeBuilder, QualityRequirementTreeBuilder {

    private TreeNode<Object> root;

    private TreeNode<Object> getRootTreeNode() {
        root = new TreeNode<>(null); // Create a root node
        return root;
    }

    public TreeNode<Object> setNodeAsRoot(List<?> list) {
        if (!list.isEmpty()) {
            root = this.getRootTreeNode();

            if (list.get(0) instanceof IoTDomain) {
                ((List<IoTDomain>) list).sort(Comparator.comparing(IoTDomain::getName));
                for (IoTDomain domain : (List<IoTDomain>) list) {
                    root.addChild(buildTreeIoTDomain(domain));
                }
            } else if (list.get(0) instanceof ArchitectureSolution) {
                ((List<ArchitectureSolution>) list).sort(Comparator.comparing(architectureSolution -> architectureSolution.getArchitecture().getName()));
                for (ArchitectureSolution arch : (List<ArchitectureSolution>) list) {
                    root.addChild(buildTreeArchitectureSolution(arch));
                }
            } else if (list.get(0) instanceof QualityRequirement) {
                List<QualityRequirement> listOrdered = ((List<QualityRequirement>) list).stream().sorted(Comparator.comparing(QualityRequirement::getName)).toList();
                for (QualityRequirement qr : listOrdered) {
                    root.addChild(buildTreeQualityRequirement(qr));
                }
            }
        }
        return root;
    }

    private TreeNode<IoTDomain> buildTreeIoTDomain(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        List<ArchitectureSolution> archsSorted = domain.getArchitectureSolutions();
        archsSorted.sort(Comparator.comparing(architectureSolution -> architectureSolution.getArchitecture().getName()));

        for (ArchitectureSolution solution : archsSorted) {
            TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
            buildTreeQualityRequirement(solution, solutionNode);
            domainNode.addChild(solutionNode);
        }

        return domainNode;
    }

    private TreeNode<ArchitectureSolution> buildTreeArchitectureSolution(ArchitectureSolution arch) {
        TreeNode<ArchitectureSolution> archNode = new TreeNode<>(arch);
        buildTreeQualityRequirement(arch, archNode);

        return archNode;
    }

    private TreeNode<QualityRequirement> buildTreeQualityRequirement(QualityRequirement qr) {
        TreeNode<QualityRequirement> qrNode = new TreeNode<>(qr);
        for (Technology tech : qr.getTechnologies()) {
            buildTreeTechnology(tech, qrNode);
        }
        return qrNode;
    }

    private void buildTreeQualityRequirement(ArchitectureSolution arch, TreeNode<ArchitectureSolution> archNode) {
        List<QualityRequirement> list = new ArrayList<>(arch.getQualityRequirements());
        list.sort(Comparator.comparing(QualityRequirement::getName));

        for (QualityRequirement qr : list) {
            TreeNode<QualityRequirement> qrNode = archNode.getChildren().stream()
                    .filter(treeNode -> treeNode.getData() instanceof QualityRequirement)
                    .map(treeNode -> (TreeNode<QualityRequirement>) treeNode)
                    .filter(treeNode -> treeNode.getData().getName().equals(qr.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        TreeNode<QualityRequirement> newQrNode = new TreeNode<>(qr);
                        archNode.addChild(newQrNode);
                        return newQrNode;
                    });

            for (Technology tech : qr.getTechnologies()) {
                buildTreeTechnology(tech, qrNode);
            }
        }
    }

    private void buildTreeTechnology(Technology tech, TreeNode<QualityRequirement> qrNode) {
        if (tech != null) {
            TreeNode<Technology> techNode = new TreeNode<>(tech);
            qrNode.addChild(techNode);
        }
    }
}

