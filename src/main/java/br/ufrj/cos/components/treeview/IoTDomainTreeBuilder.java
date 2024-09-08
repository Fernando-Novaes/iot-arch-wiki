package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.IoTDomain;

import java.util.List;

public interface IoTDomainTreeBuilder {

    //public TreeNode<Object> buildTreeIoTDomain(List<IoTDomain> domains);
    public TreeNode<Object> setNodeAsRoot(List<?> list);
    //public TreeNode<Object> buildTree(List<IoTDomain> domains);

}
