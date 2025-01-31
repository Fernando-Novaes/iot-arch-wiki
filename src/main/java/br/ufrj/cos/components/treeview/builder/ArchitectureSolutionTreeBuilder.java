package br.ufrj.cos.components.treeview.builder;

import br.ufrj.cos.components.treeview.TreeNode;

import java.util.List;

public interface ArchitectureSolutionTreeBuilder {

    //public TreeNode<Object> buildTreeArchitectureSolution(List<ArchitectureSolution> archs);
    public TreeNode<Object> setNodeAsRoot(List<?> list);

}
