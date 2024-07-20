package br.ufrj.cos.components.treeview;

import java.util.ArrayList;
import java.util.List;

/***
 *     "iot_domain": "Healthcare",
 *     "id": "ARC25 - SS25",
 *     "architecture_solution": "Distributed Edge-Cloud ",
 *     "quality_requirement": "Security",
 *     "technologies": ""
 */
public class TreeNode<T> {
    private T data;
    private TreeNode<?> parent;
    private List<TreeNode<?>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new ArrayList<>();
    }

    public T getData() {
        return data;
    }

    public TreeNode<?> getParent() {
        return parent;
    }

    public List<TreeNode<?>> getChildren() {
        return children;
    }

    public void addChild(TreeNode<?> child) {
        child.setParent(this);
        children.add(child);
    }

    private void setParent(TreeNode<?> parent) {
        this.parent = parent;
    }
}
