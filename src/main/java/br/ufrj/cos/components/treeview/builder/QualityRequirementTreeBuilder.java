package br.ufrj.cos.components.treeview.builder;

import br.ufrj.cos.components.treeview.TreeNode;

import java.util.List;

public interface QualityRequirementTreeBuilder {
    public TreeNode<Object> setNodeAsRoot(List<?> list);
}
