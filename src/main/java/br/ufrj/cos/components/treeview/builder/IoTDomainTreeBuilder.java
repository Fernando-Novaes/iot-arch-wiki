package br.ufrj.cos.components.treeview.builder;

import br.ufrj.cos.components.treeview.TreeNode;

import java.util.List;

public interface IoTDomainTreeBuilder {

    //public TreeNode<Object> buildTreeIoTDomain(List<IoTDomain> domains);
    public TreeNode<Object> setNodeAsRoot(List<?> list);
    //public TreeNode<Object> buildTree(List<IoTDomain> domains);

}
