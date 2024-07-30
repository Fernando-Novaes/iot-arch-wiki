package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.ui.LoadMode;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("IoT-Arch Wiki - Tree")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView {

    private TreeViewComponent treeView;

    @Autowired
    public IoTArchView(TreeViewComponent treeView) {
        this.treeView = treeView;

        this.createHeader("IoT-Arch Wiki");
        getContent().add(treeView);
        this.treeView.load();
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }
}
