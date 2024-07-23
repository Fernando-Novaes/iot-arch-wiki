package br.ufrj.cos.views.qualityrequirement;

import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;

@PageTitle("Quality Requirement")
@Route(value = "qualityreq-view", layout = MainLayout.class)
public class QualityRequirementView extends BaseView {

    private final String PROPERTY_FILE_QR = "quality-requirement.properties";

    public QualityRequirementView() throws IOException {
        this.createHeader("Quality Requirement");

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        getContent().add(this.createContainer(
                "Functional Completeness",
                this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_completeness")
        ));
    }

    /***
     *
     * @param mainText
     * @param description
     * @return
     */
    private HorizontalLayout createContainer(String mainText, String description) {
        HorizontalLayout container = new HorizontalLayout();
        container.setWidth("100%");
        container.setSpacing(true);

        container.getStyle().set("border", "1px solid gray");
        container.getStyle().set("radius", "5");

        H1 title = new H1(mainText);
        title.setWidth("100%");

        VerticalLayout boxMainText = new VerticalLayout();
        boxMainText.setWidth("100%");
        boxMainText.add(title);

        Text textDescription = new Text(description);

        VerticalLayout boxDescription = new VerticalLayout();
        boxMainText.setWidth("100%");
        boxMainText.add(textDescription);

        return container;
    }
}
