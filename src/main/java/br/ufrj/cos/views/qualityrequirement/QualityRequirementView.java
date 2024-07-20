package br.ufrj.cos.views.qualityrequirement;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Quality Requirement")
@Route(value = "qualityreq-view", layout = MainLayout.class)
public class QualityRequirementView extends Composite<VerticalLayout> {

    public QualityRequirementView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
