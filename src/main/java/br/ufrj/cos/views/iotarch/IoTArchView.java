package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@PageTitle("IoT-Arch - Wiki")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView {

    private TreeViewComponent treeView;
    //private final HorizontalLayout pageContent;

    @PostConstruct
    public void init() {
        getContent().add(this.createTreeViewLayout("", this.treeView));
        this.treeView.load();
    }

    @Autowired
    public IoTArchView(TreeViewComponent treeView) {
        this.treeView = treeView;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("IoT-Arch Wiki");
//        pageContent = this.createContentLayout();
//        getContent().add(pageContent);
    }
}
