package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.utils.ClassTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TreeBuilder implements IoTDomainTreeBuilder, ArchitectureSolutionTreeBuilder, QualityRequirementTreeBuilder {

    private TreeNode<Object> root;

    private TreeNode<Object> getRootTreeNode() {
        root = new TreeNode<>(null); // Create a root node
        

        return root;
    }

    public TreeNode<Object> setNodeAsRoot(List<?> list) {
        root = this.getRootTreeNode();

       if (list.getFirst() instanceof IoTDomain) {
            for (IoTDomain domain : (List<IoTDomain>) list) {
                root.addChild(buildTreeIoTDomain(domain));
            }
        } else if (list.getFirst() instanceof ArchitectureSolution) {
            for (ArchitectureSolution arch : (List<ArchitectureSolution>) list) {
                if (root.getChildren().stream().anyMatch(r -> ((ArchitectureSolution) r.getData()).getName().equals(arch.getName()))) {
                    TreeNode<ArchitectureSolution> rootAux = (TreeNode<ArchitectureSolution>) root.getChildren().stream().filter(treeNode -> ((ArchitectureSolution) treeNode.getData()).getName().equals(arch.getName())).findFirst().get();
                    buildTreeQualityRequirement(arch, rootAux);

                    rootAux.getChildren().forEach(qr -> {
                        qr.getChildren().forEach(tech -> {
                            if (tech.getChildren().isEmpty()) {
                                tech.addChild(new TreeNode<>(((Technology) tech.getData()).getArchitectureSolution().getIoTDomain()));
                            }
                        });
                    });
                } else {
                    root.addChild(buildTreeArchitectureSolution(arch));
                }
            }
        } else if (list.getFirst() instanceof QualityRequirement) {
           for (QualityRequirement qr : (List<QualityRequirement>) list) {
               if (root.getChildren().stream().anyMatch(qrAux -> ((QualityRequirement) qrAux.getData()).getName().equals(qr.getName()))) {
                   TreeNode<QualityRequirement> rootAux =
                           (TreeNode<QualityRequirement>) root.getChildren().stream().filter(treeNode -> ((QualityRequirement) treeNode.getData()).getName().equals(qr.getName())).findFirst().get();

                   TreeNode<Technology> treeNodeTech = new TreeNode<>(qr.getTechnology());
                   TreeNode<ArchitectureSolution> treeNodeArch = new TreeNode<>(qr.getArchitectureSolution());
                   TreeNode<IoTDomain> treeNodeDomain = new TreeNode<>(qr.getArchitectureSolution().getIoTDomain());
                   treeNodeArch.addChild(treeNodeDomain);
                   treeNodeTech.addChild(treeNodeArch);
                   rootAux.addChild(treeNodeTech);
               } else {
                   TreeNode<QualityRequirement> treeNodeQr = new TreeNode<>(qr);
                   TreeNode<Technology> treeNodeTech = new TreeNode<>(qr.getTechnology());
                   TreeNode<ArchitectureSolution> treeNodeArch = new TreeNode<>(qr.getArchitectureSolution());
                   TreeNode<IoTDomain> treeNodeDomain = new TreeNode<>(qr.getArchitectureSolution().getIoTDomain());
                   treeNodeArch.addChild(treeNodeDomain);
                   treeNodeTech.addChild(treeNodeArch);
                   treeNodeQr.addChild(treeNodeTech);

                   root.addChild(treeNodeQr);

               }
           }
       }

        return root;
    }

    private TreeNode<IoTDomain> buildTreeIoTDomain(IoTDomain domain) {
        TreeNode<IoTDomain> domainNode = new TreeNode<>(domain);

        for (ArchitectureSolution solution : domain.getArchs()) {
            TreeNode<ArchitectureSolution> solutionNode = new TreeNode<>(solution);
            domainNode.addChild(solutionNode);

            buildTreeQualityRequirement(solution, solutionNode);
        }

        return domainNode;
    }

//    public TreeNode<Object> buildTreeIoTDomain(List<IoTDomain> domains) {
//        TreeNode<Object> root = getRootTreeNode();
//
//        for (IoTDomain domain : domains) {
//            root.addChild(buildTreeIoTDomain(domain));
//        }
//
//        return root;
//    }

    private TreeNode<ArchitectureSolution> buildTreeArchitectureSolution(ArchitectureSolution arch) {
        TreeNode<ArchitectureSolution> archNode = new TreeNode<>(arch);

        buildTreeQualityRequirement(arch, archNode);

        archNode.getChildren().forEach(qr -> {
            qr.getChildren().forEach(tech -> {
                tech.addChild(new TreeNode<>(((Technology)tech.getData()).getArchitectureSolution().getIoTDomain()));
            });
        });

        return archNode;
    }

    private void buildTreeQualityRequirement(ArchitectureSolution arch, TreeNode<ArchitectureSolution> archNode) {
        for (QualityRequirement qr : arch.getQrs()) {
            if (archNode.getChildren().stream().anyMatch(q -> ((QualityRequirement) q.getData()).getName().equals(qr.getName()))) {
                TreeNode<QualityRequirement> qrNodeAux = (TreeNode<QualityRequirement>) archNode.getChildren().stream().filter(treeNode -> ((QualityRequirement) treeNode.getData()).getName().equals(qr.getName())).findFirst().get();
                buildTreeTechnology(qr.getTechnology(), qrNodeAux);
            } else {
                TreeNode<QualityRequirement> qrNode = new TreeNode<>(qr);
                buildTreeTechnology(qr.getTechnology(), qrNode);
                archNode.addChild(qrNode);
            }
        }
    }

    private void buildTreeTechnology(Technology tech, TreeNode<QualityRequirement> qrNode) {
        if (tech !=null) {
            TreeNode<Technology> techNode = new TreeNode<>(tech);
            qrNode.addChild(techNode);
        }
    }

//    public TreeNode<Object> buildTreeArchitectureSolution(List<ArchitectureSolution> archs) {
//        TreeNode<Object> root = getRootTreeNode();
//
//        for (ArchitectureSolution arch : archs) {
//            if (root.getChildren().stream().anyMatch(r -> ((ArchitectureSolution) r.getData()).getName().equals(arch.getName()))) {
//                TreeNode<ArchitectureSolution> rootAux = (TreeNode<ArchitectureSolution>) root.getChildren().stream().filter(treeNode -> ((ArchitectureSolution) treeNode.getData()).getName().equals(arch.getName())).findFirst().get();
//                buildTreeQualityRequirement(arch, rootAux);
//            } else {
//                root.addChild(buildTreeArchitectureSolution(arch));
//            }
//        }
//
//        return root;
//    }
}
