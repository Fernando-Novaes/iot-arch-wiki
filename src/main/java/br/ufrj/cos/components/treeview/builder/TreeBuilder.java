package br.ufrj.cos.components.treeview.builder;

import br.ufrj.cos.components.treeview.TreeNode;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class TreeBuilder implements IoTDomainTreeBuilder, ArchitectureSolutionTreeBuilder, QualityRequirementTreeBuilder {

    private TreeNode<IoTDomain> buildIoTDomainNode(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        domain.getArchitectureSolutions().stream()
                .sorted(Comparator.comparing(solution -> solution.getArchitecture().getName()))
                .forEach(solution -> {
                    TreeNode<ArchitectureSolution> solutionNode = buildArchitectureSolutionNode(solution);
                    domainNode.addChild(solutionNode);
                });

        return domainNode;
    }

    private TreeNode<ArchitectureSolution> buildArchitectureSolutionNode(ArchitectureSolution solution) {
        TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);

        solution.getQualityRequirementTechnologies().stream()
                .sorted(Comparator.comparing(qrt -> qrt.getQualityRequirement().getName()))
                .forEach(qrt -> {
                    TreeNode<QualityRequirement> reqNode = new TreeNode<>(qrt.getQualityRequirement());
                    TreeNode<Technology> techNode = new TreeNode<>(qrt.getTechnology());

                    techNode.addChild(new TreeNode<>(solution.getIoTDomain()));
                    reqNode.addChild(techNode);
                    solutionNode.addChild(reqNode);
                });

        return solutionNode;
    }

    private TreeNode<QualityRequirement> buildQualityRequirementNode(QualityRequirement requirement) {
        TreeNode<QualityRequirement> reqNode = new TreeNode<>(requirement);

        requirement.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getDescription))
                .forEach(tech -> {
                    TreeNode<Technology> techNode = new TreeNode<>(tech);

                    tech.getArchitectureSolutions().stream()
                            .filter(solution -> solution.getQualityRequirements().contains(requirement))
                            .sorted(Comparator.comparing(solution -> solution.getArchitecture().getName()))
                            .forEach(solution -> {
                                TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
                                TreeNode<IoTDomain> domainNode = new TreeNode<>(solution.getIoTDomain());

                                solutionNode.addChild(domainNode);
                                techNode.addChild(solutionNode);
                            });

                    reqNode.addChild(techNode);
                });

        return reqNode;
    }

    @Override
    public TreeNode<Object> setNodeAsRoot(List<?> list) {
        if (list == null || list.isEmpty()) {
            return new TreeNode<>(null);
        }

        TreeNode<Object> root = new TreeNode<>(null);
        Object firstItem = list.getFirst();

        if (firstItem instanceof IoTDomain domain) {
            list.stream()
                    .map(d -> (IoTDomain) d)
                    .sorted(Comparator.comparing(IoTDomain::getName))
                    .forEach(d -> root.addChild(buildIoTDomainNode(d)));
        }
        else if (firstItem instanceof ArchitectureSolution solution) {
            list.stream()
                    .map(s -> (ArchitectureSolution) s)
                    .sorted(Comparator.comparing(s -> s.getArchitecture().getName()))
                    .forEach(s -> {
                        Optional<TreeNode<?>> existingNode = root.getChildren().stream()
                                .filter(node -> ((ArchitectureSolution)node.getData()).getArchitecture().equals(s.getArchitecture()))
                                .findAny();

                        if (existingNode.isPresent()) {
                            @SuppressWarnings("unchecked")
                            TreeNode<ArchitectureSolution> architectureNode = (TreeNode<ArchitectureSolution>) existingNode.get();
                            addQualityRequirementsToNode(s, architectureNode);
                        } else {
                            root.addChild(buildArchitectureSolutionNode(s));
                        }
                    });
        }
        else if (firstItem instanceof QualityRequirement requirement) {
            list.stream()
                    .map(r -> (QualityRequirement) r)
                    .sorted(Comparator.comparing(QualityRequirement::getName))
                    .forEach(r -> {
                        Optional<TreeNode<?>> existingRootNode = root.getChildren().stream()
                                .filter(qr -> ((QualityRequirement)qr.getData()).getId().equals(r.getId()))
                                .findAny();

                        if (existingRootNode.isPresent()) {
                            @SuppressWarnings("unchecked")
                            TreeNode<QualityRequirement> qrNode = (TreeNode<QualityRequirement>) existingRootNode.get();
                            buildQualityRequirementNode(r);
                        } else {
                            root.addChild(buildQualityRequirementNode(r));
                        }
                    });
//
//            Optional<TreeNode<?>> existingRootNode =
//                    root.getChildren().stream().filter(qr -> ((QualityRequirement)qr.getData()).getId().equals(requirement.getId())).findAny();
//
//            if (existingRootNode.isPresent()) {
//                existingRootNode.get().getChildren().add(buildQualityRequirementNode(requirement));
//            } else {
//                list.stream()
//                        .map(r -> (QualityRequirement) r)
//                        .sorted(Comparator.comparing(QualityRequirement::getId))
//                        .forEach(r -> root.addChild(buildQualityRequirementNode(r)));
//            }
        }

        return root;
    }

    private void addQualityRequirementsToNode(ArchitectureSolution solution, TreeNode<ArchitectureSolution> solutionNode) {
        solution.getQualityRequirementTechnologies().stream()
                .sorted(Comparator.comparing(qrt -> qrt.getQualityRequirement().getName()))
                .forEach(qrt -> {
                    TreeNode<QualityRequirement> reqNode = findOrCreateQualityRequirementNode(qrt.getQualityRequirement(), solutionNode);
                    TreeNode<Technology> techNode = new TreeNode<>(qrt.getTechnology());
                    techNode.addChild(new TreeNode<>(solution.getIoTDomain()));
                    reqNode.addChild(techNode);
                });
    }

    private TreeNode<QualityRequirement> findOrCreateQualityRequirementNode(
            QualityRequirement requirement, TreeNode<ArchitectureSolution> solutionNode) {
        return solutionNode.getChildren().stream()
                .filter(node -> node.getData() instanceof QualityRequirement)
                .map(node -> (TreeNode<QualityRequirement>) node)
                .filter(node -> node.getData().getName().equals(requirement.getName()))
                .findFirst()
                .orElseGet(() -> {
                    TreeNode<QualityRequirement> newNode = new TreeNode<>(requirement);
                    solutionNode.addChild(newNode);
                    return newNode;
                });
    }
}

