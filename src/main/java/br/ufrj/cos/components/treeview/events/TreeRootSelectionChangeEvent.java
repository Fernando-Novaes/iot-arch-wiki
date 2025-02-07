package br.ufrj.cos.components.treeview.events;

import br.ufrj.cos.components.treeview.TreeViewType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TreeRootSelectionChangeEvent extends ApplicationEvent {
    private final TreeViewType newType;

    public TreeRootSelectionChangeEvent(Object source, TreeViewType newType) {
        super(source);
        this.newType = newType;
    }
}
