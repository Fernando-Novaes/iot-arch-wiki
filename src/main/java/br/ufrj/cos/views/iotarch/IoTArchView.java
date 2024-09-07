package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.treeview.TreeRootSelectionComponent;
import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("IoT-Arch Wiki - Tree")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView {

    private TreeViewComponent treeView;
    private TreeRootSelectionComponent treeRootSelection;

    @Autowired
    public IoTArchView(TreeViewComponent treeView, TreeRootSelectionComponent treeRootSelection) {
        this.treeView = treeView;
        this.treeRootSelection = treeRootSelection;

        this.createHeader("IoT-Arch Wiki");
        getContent().add(treeView);
        this.treeView.addTreeRootSelection(this.treeRootSelection);
        this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
        this.treeRootSelection.addChangeLeftButtonClickListener(l -> { this.changeRootLeft(); });
        this.treeRootSelection.addChangeRightButtonClickListener(r -> { this.changeRootRight(); });
        this.treeView.load();

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }

    private void changeRootLeft() {
        getContent().remove(this.treeView);

        switch (this.treeRootSelection.getTreeViewType()) {
            case IoTDomain: {
                this.treeRootSelection.setTreeViewType(TreeViewType.Technology);
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
            }
            case QualityRequirement: {
                this.treeRootSelection.setTreeViewType(TreeViewType.ArchitectureSolution);
            }
            case Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.QualityRequirement);
            }
        }

        this.treeView.load();
        getContent().add(treeView);
    }

    private void changeRootRight() {
        switch (this.treeRootSelection.getTreeViewType()) {
            case IoTDomain: {
                this.treeRootSelection.setTreeViewType(TreeViewType.ArchitectureSolution);
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.QualityRequirement);
            }
            case QualityRequirement: {
                this.treeRootSelection.setTreeViewType(TreeViewType.Technology);
            }
            case Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
            }
        }

        getContent().remove(this.treeView);
        this.treeView.load();
        getContent().add(treeView);
    }

}
