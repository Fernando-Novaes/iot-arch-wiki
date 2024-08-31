package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.IoTDomain;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IoTDomainTreeBuilder {

    public TreeNode<Object> buildTreeIoTDomain(List<IoTDomain> domains);
    //public TreeNode<Object> buildTree(List<IoTDomain> domains);

}
