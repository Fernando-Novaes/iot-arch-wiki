package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("IoT-Arch")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends Composite<VerticalLayout> {

    public IoTArchView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
