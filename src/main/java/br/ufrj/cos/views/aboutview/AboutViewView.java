package br.ufrj.cos.views.aboutview;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("About View")
@Route(value = "about-view", layout = MainLayout.class)
public class AboutViewView extends Composite<VerticalLayout> {

    public AboutViewView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
