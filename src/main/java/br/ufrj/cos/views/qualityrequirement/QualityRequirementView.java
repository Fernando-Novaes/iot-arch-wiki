package br.ufrj.cos.views.qualityrequirement;

import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
        //getContent().add(pageContent);

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
        //tabs.setSizeFull();

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
                    selectedContent = this.createContentPerformanceEfficiency();
                    break;
                case 2:
                    selectedContent = this.createContentCompatibility();
                    break;
                case 3:
                    selectedContent = this.createContentInteractionCapability();
                    break;
                case 4:
                    selectedContent = this.createContentReliability();
                    break;
                case 5:
                    selectedContent = this.createContentSecurity();
                    break;
                case 6:
                    selectedContent = this.createContentMaintainability();
                    break;
                case 7:
                    selectedContent = this.createContentFlexibility();
                    break;
                case 8:
                    selectedContent = this.createContentSafety();
                    break;
            }
            contentContainer.add(selectedContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        getContent().add(tabs, contentContainer);

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
        box.setEnabled(false);
        box.getStyle().set("font-size", "medium");
        box.getStyle().set("--vaadin-input-field-label-color", "#d9693d");
        box.getStyle().set("--vaadin-input-field-label-font-size", "--lumo-font-size-g");

        return box;
    }

    private VerticalLayout createContentForFunctionalSuitability() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Functional Completeness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_completeness")),
                this.createBoxItem("Functional Completeness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_correctness")),
                this.createBoxItem("Functional Completeness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Functional_appropriateness"))
        );

        return vl;
    }

    private VerticalLayout createContentPerformanceEfficiency() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Time Behavior", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Time_behavior")),
                this.createBoxItem("Resource Utilization", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Resource_utilization")),
                this.createBoxItem("Capacity", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Capacity"))
        );

        return vl;
    }

    private VerticalLayout createContentCompatibility() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Co_existence", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Co_existence")),
                this.createBoxItem("Interoperability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Interoperability"))
        );

        return vl;
    }

    private VerticalLayout createContentInteractionCapability() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Appropriateness Recognizability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Appropriateness_recognizability")),
                this.createBoxItem("Learnability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Learnability")),
                this.createBoxItem("Operability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Operability")),
                this.createBoxItem("User Error Protection", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "User_error_protection")),
                this.createBoxItem("User Engagement", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "User_engagement")),
                this.createBoxItem("Inclusivity", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Inclusivity")),
                this.createBoxItem("User Assistance", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "User_assistance")),
                this.createBoxItem("Self Descriptiveness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Self_descriptiveness"))
        );

        return vl;
    }

    private VerticalLayout createContentReliability() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Faultlessness", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Faultlessness")),
                this.createBoxItem("Availability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Availability")),
                this.createBoxItem("Fault Tolerance", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Fault_tolerance")),
                this.createBoxItem("Recoverability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Recoverability"))
        );

        return vl;
    }

    private VerticalLayout createContentSecurity() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Confidentiality", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Confidentiality")),
                this.createBoxItem("Integrity", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Integrity")),
                this.createBoxItem("Non-Repudiation", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Non_repudiation")),
                this.createBoxItem("Accountability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Accountability")),
                this.createBoxItem("Authenticity", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Authenticity")),
                this.createBoxItem("Resistance", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Resistance"))
        );

        return vl;
    }

    private VerticalLayout createContentMaintainability() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Modularity", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Modularity")),
                this.createBoxItem("Reusability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Reusability")),
                this.createBoxItem("Analyzability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Analyzability")),
                this.createBoxItem("Modifiability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Modifiability")),
                this.createBoxItem("Testability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Testability"))
        );

        return vl;
    }

    private VerticalLayout createContentFlexibility() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Adaptability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Adaptability")),
                this.createBoxItem("Installability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Installability")),
                this.createBoxItem("Replaceability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Replaceability")),
                this.createBoxItem("Scalability", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Scalability"))
        );

        return vl;
    }

    private VerticalLayout createContentSafety() throws IOException {
        VerticalLayout vl = new VerticalLayout();
        vl.add(
                this.createBoxItem("Operational Constraint", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Operational_constraint")),
                this.createBoxItem("Risk Identification", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Risk_identification")),
                this.createBoxItem("Fail Safe", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Fail_safe")),
                this.createBoxItem("Hazard Warning", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Hazard_warning")),
                this.createBoxItem("Safe Integration", this.getValueFromPropertiesFile(PROPERTY_FILE_QR, "Safe_integration"))
        );

        return vl;
    }
}
