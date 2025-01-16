package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TreeBuilder implements IoTDomainTreeBuilder, ArchitectureSolutionTreeBuilder, QualityRequirementTreeBuilder {

    private TreeNode<IoTDomain> buildIoTDomainNode(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        domain.getArchitectureSolutions().stream()
                .sorted(Comparator.comparing(solution -> solution.getArchitecture().getName()))
                .forEach(solution -> {
                    TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
                    buildQualityRequirementNodes(solution, solutionNode);
                    domainNode.addChild(solutionNode);
                });

        return domainNode;
    }

    private TreeNode<ArchitectureSolution> buildArchitectureSolutionNode(ArchitectureSolution solution) {
        TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);

        solution.getQualityRequirementTechnologies()
                //.sorted(Comparator.comparing(QualityRequirement::getName))
                .forEach(requirement -> {
                    TreeNode<QualityRequirement> reqNode = new TreeNode<QualityRequirement>(requirement.getQualityRequirement());
                    // Adding Technology node
                    TreeNode<Technology> techNode = new TreeNode<>(requirement.getTechnology());
                    reqNode.addChild(techNode);
                    //Adding the Quality Req node
                    techNode.addChild(new TreeNode<IoTDomain>(requirement.getArchitectureSolution().getIoTDomain()));
                    solutionNode.addChild(reqNode);
                });

        return solutionNode;
    }

    private TreeNode<QualityRequirement> buildQualityRequirementNode(QualityRequirement requirement) {
        TreeNode<QualityRequirement> reqNode = new TreeNode<>(requirement);

        // Get technologies and create nodes
        requirement.getTechnologies().stream()
                .sorted(Comparator.comparing(Technology::getDescription))
                .forEach(tech -> {
                    TreeNode<Technology> techNode = new TreeNode<>(tech);

                    // For each technology, get associated architecture solutions through the requirement
                    tech.getArchitectureSolutions().stream()
                            .filter(solution -> solution.getQualityRequirements().contains(requirement))
                            .sorted(Comparator.comparing(solution -> solution.getArchitecture().getName()))
                            .forEach(solution -> {
                                // Create architecture solution node
                                TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
                                // Create IoT domain node
                                TreeNode<IoTDomain> domainNode = new TreeNode<>(solution.getIoTDomain());
                                //Adding the node (ArchitectureSolution) and children (IoT Domain)
                                solutionNode.addChild(domainNode);
                                techNode.addChild(solutionNode);
                            });

                    reqNode.addChild(techNode);
                });

        return reqNode;
    }

    private void buildQualityRequirementNodes(ArchitectureSolution solution, TreeNode<ArchitectureSolution> solutionNode) {
        solution.getQualityRequirementTechnologies().stream()
                //.sorted(Comparator.comparing(QualityRequirement::getName))
                .forEach(requirement -> {
                    TreeNode<QualityRequirement> reqNode = findOrCreateQualityRequirementNode(requirement.getQualityRequirement(), solutionNode);
                    reqNode.addChild(buildTechnologyNode(requirement.getTechnology()));
                });
    }

    private TreeNode<QualityRequirement> findOrCreateQualityRequirementNode(QualityRequirement requirement,
                                                                            TreeNode<ArchitectureSolution> solutionNode) {
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

    private TreeNode<Technology> buildTechnologyNode(Technology technology) {
        return new TreeNode<>(technology);
    }

    @Override
    public TreeNode<Object> setNodeAsRoot(List<?> list) {
        if (list == null || list.isEmpty()) {
            return new TreeNode<>(null);
        }

        TreeNode<Object> root = new TreeNode<>(null);
        Object firstItem = list.getFirst();

        if (firstItem instanceof IoTDomain) {
            @SuppressWarnings("unchecked")
            List<IoTDomain> domains = (List<IoTDomain>) list;
            domains.stream()
                    .sorted(Comparator.comparing(IoTDomain::getName))
                    .forEach(domain -> root.addChild(buildIoTDomainNode(domain)));
        }
        else if (firstItem instanceof ArchitectureSolution) {
            @SuppressWarnings("unchecked")
            List<ArchitectureSolution> solutions = (List<ArchitectureSolution>) list;
            solutions.stream()
                    .sorted(Comparator.comparing(solution -> solution.getArchitecture().getName()))
                    .forEach(solution -> {
                        root.addChild(buildArchitectureSolutionNode(solution));
                    });
        }
        else if (firstItem instanceof QualityRequirement) {
            @SuppressWarnings("unchecked")
            List<QualityRequirement> requirements = (List<QualityRequirement>) list;
            requirements.stream()
                    .sorted(Comparator.comparing(QualityRequirement::getName))
                    .forEach(requirement -> root.addChild(buildQualityRequirementNode(requirement)));
        }

        return root;
    }
}

