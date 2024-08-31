package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.ArchitectureSolution;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ArchitectureSolutionTreeBuilder {

    public TreeNode<Object> buildTreeArchitectureSolution(List<ArchitectureSolution> archs);
    //public TreeNode<Object> buildTree(List<ArchitectureSolution> archs);

}
