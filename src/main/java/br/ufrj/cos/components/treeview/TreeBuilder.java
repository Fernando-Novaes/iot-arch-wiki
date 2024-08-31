package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TreeBuilder implements IoTDomainTreeBuilder, ArchitectureSolutionTreeBuilder {

    private TreeNode<IoTDomain> buildTreeIoTDomain(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        for (ArchitectureSolution solution : domain.getArchs()) {
            TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
            domainNode.addChild(solutionNode);

            for (QualityRequirement qr : solution.getQrs()) {
                TreeNode<QualityRequirement> qrNode = new TreeNode<>(qr);
                solutionNode.addChild(qrNode);

                if (qr.getTechnology() !=null) {
                    TreeNode<Technology> techNode = new TreeNode<>(qr.getTechnology());
                    qrNode.addChild(techNode);
                }
            }
        }

        return domainNode;
    }

    public TreeNode<Object> buildTreeIoTDomain(List<IoTDomain> domains) {
        TreeNode<Object> root = new TreeNode<>(null); // Create a root node

        for (IoTDomain domain : domains) {
            root.addChild(buildTreeIoTDomain(domain));
        }

        return root;
    }

    private TreeNode<ArchitectureSolution> buildTreeArchitectureSolution(ArchitectureSolution arch) {
        TreeNode<ArchitectureSolution> archNode = new TreeNode<>(arch);

        for (QualityRequirement qr : arch.getQrs()) {
            TreeNode<QualityRequirement> qrNode = new TreeNode<>(qr);
            archNode.addChild(qrNode);

            if (qr.getTechnology() !=null) {
                TreeNode<Technology> techNode = new TreeNode<>(qr.getTechnology());
                qrNode.addChild(techNode);
            }
        }

        return archNode;
    }

    public TreeNode<Object> buildTreeArchitectureSolution(List<ArchitectureSolution> archs) {
        TreeNode<Object> root = new TreeNode<>(null); // Create a root node

        for (ArchitectureSolution arch : archs) {
            root.addChild(buildTreeArchitectureSolution(arch));
        }

        return root;
    }
}
