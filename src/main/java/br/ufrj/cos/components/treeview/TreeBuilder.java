package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;

import java.util.List;

public class TreeBuilder {

    public TreeNode<IoTDomain> buildTree(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        for (ArchitectureSolution solution : domain.getArchs()) {
            TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
            domainNode.addChild(solutionNode);

            for (QualityRequirement qr : solution.getQrs()) {
                TreeNode<QualityRequirement> qrNode = new TreeNode<>(qr);
                solutionNode.addChild(qrNode);

                for (Technology tech : qr.getTechs()) {
                    TreeNode<Technology> techNode = new TreeNode<>(tech);
                    qrNode.addChild(techNode);
                }
            }
        }

        return domainNode;
    }

    public TreeNode<Object> buildTree(List<IoTDomain> domains) {
        TreeNode<Object> root = new TreeNode<>(null); // Create a root node

        for (IoTDomain domain : domains) {
            root.addChild(buildTree(domain));
        }

        return root;
    }
}
