package br.ufrj.cos.views.qualityrequirement;

import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.IOException;

@PageTitle("Quality Requirement")
@Route(value = "qualityreq-view", layout = MainLayout.class)
public class QualityRequirementView extends BaseView {

    private final String PROPERTY_FILE_QR = "quality-requirement.properties";
    private final HorizontalLayout pageContent;

    public QualityRequirementView() throws IOException {
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("Quality Requirement");

        this.pageContent = this.createContentLayout();
        this.createTabs();
        getContent().add(pageContent);

//        getContent().add(this.createContainer(
//                "Functional Completeness",
//                this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_completeness")
//        ));
    }

    private void createTabs() throws IOException {
        Tab functional_suitability = new Tab(VaadinIcon.COG.create(), new Span("Functional Suitability"));
        Tab performance_efficiency = new Tab(VaadinIcon.COG.create(), new Span("Performance Efficiency"));
        Tab compatibility = new Tab(VaadinIcon.COG.create(), new Span("Compatibility"));
        Tab interaction_capability = new Tab(VaadinIcon.COG.create(), new Span("Interaction Capability"));
        Tab reliability = new Tab(VaadinIcon.COG.create(), new Span("Reliability"));
        Tab security = new Tab(VaadinIcon.COG.create(), new Span("Security"));
        Tab maintainability = new Tab(VaadinIcon.COG.create(), new Span("Maintainability"));
        Tab flexibility = new Tab(VaadinIcon.COG.create(), new Span("Flexibility"));
        Tab safety = new Tab(VaadinIcon.COG.create(), new Span("Safety"));

        Tabs tabs = new Tabs(functional_suitability, performance_efficiency, compatibility, interaction_capability, reliability, security, maintainability, flexibility, safety);
        tabs.addThemeVariants(TabsVariant.LUMO_CENTERED);
        tabs.setSizeFull();

        Div contentContainer = new Div();
        contentContainer.setSizeFull();
        contentContainer.add(this.createContentForFunctionalSuitability());

        // Add a listener to switch the content when the tab changes
        tabs.addSelectedChangeListener(event -> {
            try {
            contentContainer.removeAll();
            Component selectedContent = null;
            switch (tabs.getSelectedIndex()) {
                case 0:
                    selectedContent = this.createContentForFunctionalSuitability();
                    break;
                case 1:
                    selectedContent = this.createContentForFunctionalSuitability();;
                    break;
                case 2:
                    selectedContent = this.createContentForFunctionalSuitability();;
                    break;
                case 3:
                    selectedContent = this.createContentForFunctionalSuitability();;
                    break;
                case 4:
                    selectedContent = this.createContentForFunctionalSuitability();;
                    break;
            }
            contentContainer.add(selectedContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        this.pageContent.add(tabs, contentContainer);

    }

    /***
     * Creates the box for QR
     * @param title QR name
     * @param text QR description
     * @return TextArea
     */
    private TextArea createBoxItem(String title, String text) {
        TextArea box = new TextArea();
        box.setWidthFull();
        box.setLabel(title);
        box.setValue(text);

        return box;
    }

    private HorizontalLayout createContentForFunctionalSuitability() throws IOException {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(
            this.createBoxItem("Functional Completeness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_completeness")));

        return horizontalLayout;
    }
}
