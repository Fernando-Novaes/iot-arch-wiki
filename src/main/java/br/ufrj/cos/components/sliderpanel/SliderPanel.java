package br.ufrj.cos.components.sliderpanel;

import br.ufrj.cos.components.treeview.record.DataDetails;
import br.ufrj.cos.domain.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@org.springframework.stereotype.Component
@CssImport("./styles/slider-panel.css")
public class SliderPanel extends Div {
    private final Div content;
    private final Button toggleButton;
    private boolean expanded = true; // Default to expanded
    private String expandedText = "Hide Detail";
    private String collapsedText = "Show Detail";

    private final HorizontalLayout header = new HorizontalLayout();
    private final HorizontalLayout iotDomainHL = new HorizontalLayout();
    private final HorizontalLayout archHL = new HorizontalLayout();
    private final HorizontalLayout qrHL = new HorizontalLayout();
    private final HorizontalLayout techHL = new HorizontalLayout();
    private final HorizontalLayout referenceHL = new HorizontalLayout();

    @Getter @Setter
    private DataDetails dataDetails;

    public SliderPanel() {
        this.removeAll();
        addClassName("slider-panel");
        this.getStyle().set("box-shadow", "-3px 0 8px rgba(0, 0, 0, 0.4)");

        // Create toggle button
        toggleButton = new Button();
        toggleButton.addClassName("slider-toggle");

        // Initialize button content
        updateButtonContent(false); // Start with collapsed state

        toggleButton.addClickListener(e -> toggle());

        content = new Div();
        content.addClassName("slider-content");

        // Set default width for right-side panel
        content.getStyle().set("width", "100%");

        // Configure vertical text
        toggleButton.getElement().getStyle()
                .set("writing-mode", "vertical-lr")  // Changed to vertical-lr for better readability
                .set("text-orientation", "mixed")
                .set("transform", "rotate(180deg)"); // This makes text read from top to bottom

        this.addDetailsContent();

        add(toggleButton, this.createHeader(), content, referenceHL);
    }

    private void updateButtonContent(boolean isExpanded) {
        // Clear existing content
        toggleButton.getElement().removeAllChildren();

        // Create icon
        Icon icon = isExpanded ?
                VaadinIcon.ANGLE_LEFT.create() :
                VaadinIcon.ANGLE_RIGHT.create();

        // Configure icon
        icon.getElement().getStyle()
                .set("transform", "rotate(180deg)") // Rotate icon to match text orientation
                .set("display", "block")
                .set("margin", "4px auto"); // Center the icon

        // Add text first (it will appear at the bottom due to rotation)
        String text = isExpanded ? expandedText : collapsedText;
        toggleButton.getElement().appendChild(new Text(text).getElement());
    }

    public void toggle() {
        expanded = !expanded;

        if (expanded) {
            addClassName("expanded");
            updateButtonContent(true);
        } else {
            removeClassName("expanded");
            updateButtonContent(false);
        }
    }

    private String formatSlidePanelDetails(String title, String name, String description) {
        return String.format("<div><h3>%s:</h3><b>%s</b></br><div style='font-style: italic; margin-bottom: 5px;'>%s</div></div>",
                title, name, Optional.ofNullable(description).orElse("No description"));
    }

    public void setDataDetailsContent(DataDetails dataDetails) {
        if (dataDetails != null) {
            if (dataDetails.getIotDomain() != null) {
                this.setIoTDomainContent(dataDetails.getIotDomain());
            }

            if (dataDetails.getArchitectureSolution() != null) {
                this.setArchitectureContent(dataDetails.getArchitectureSolution());
            }

            if (dataDetails.getQualityRequirement() != null) {
                this.setQualityRequirementContent(dataDetails.getQualityRequirement());
            }

            if (dataDetails.getTechnology() != null) {
                this.setTechnologyContent(dataDetails.getTechnology());
            }

            if (dataDetails.getReference() != null) {
                this.setReferenceDetails(dataDetails.getReference());
            }
        }
    }

    private void setIoTDomainContent(IoTDomain domain) {
        //iotDomainHL.removeAll();
        iotDomainHL.add(
                new HorizontalLayout(
                        new Html(formatSlidePanelDetails("IoT Domain", domain.getName(), domain.getDescription()))));
    }

    private void setArchitectureContent(ArchitectureSolution solution) {
        //archHL.removeAll();
        archHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Architecture", solution.getArchitecture().getName(), solution.getDescription()))));
    }

    private void setQualityRequirementContent(QualityRequirement qr) {
        qrHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Quality Requirement", qr.getName(),
                        Optional.ofNullable(qr.getDescription()).orElse("No description")))
        ));
    }

    private void setTechnologyContent(Technology tech) {
        techHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Technology", tech.getDescription(),
                        Optional.ofNullable(tech.getNotes()).orElse("No description")))

                ));
    }

    private void setReferenceDetails(PaperReference reference) {
        referenceHL.add(new Html(
                String.format("<div style='font-style: italic;'><center><b>%s, %s</b></center></div>",
                        reference.getTitle(), reference.getPublishYear())));
    }

    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "#373a3f")
                .set("border", "1px solid #4a4d52")
                .set("border-radius", "12px")
                //.set("margin-top", "px")
                .set("width", "100%") // Increased width
                .set("max-width", "1800px"); // Increased max-width
                //.set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)");

        header.add(new Html("<h3><center>Details</center></h3>"));

        return header;
    }

    private void addDetailsContent() {
        // Keep existing width, spacing, and height configurations
        iotDomainHL.setWidth("100%");
        iotDomainHL.setSpacing(true);
        iotDomainHL.setPadding(false);
        iotDomainHL.setHeight("10%");

        archHL.setWidth("100%");
        archHL.setSpacing(true);
        archHL.setPadding(false);
        archHL.setHeight("25%");

        qrHL.setWidth("100%");
        qrHL.setSpacing(true);
        qrHL.setPadding(false);
        qrHL.setHeight("25%");

        techHL.setWidth("100%");
        techHL.setSpacing(true);
        techHL.setPadding(false);
        techHL.setHeight("30%");

        content.add(iotDomainHL, archHL, qrHL, techHL);
    }

    public void clearContents() {
        header.removeAll();
        iotDomainHL.removeAll();
        archHL.removeAll();
        qrHL.removeAll();
        techHL.removeAll();
        referenceHL.removeAll();
    }

    public void setButtonTexts(String expandedText, String collapsedText) {
        this.expandedText = expandedText;
        this.collapsedText = collapsedText;
        updateButtonContent(expanded);
    }

    public void setHeight(String height) {
        content.getStyle().set("height", height);
    }

    public void setWidth(String width) {
        content.getStyle().set("width", width);
        getElement().getStyle().set("--panel-width", width);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            toggle();
        }
    }

    @EventListener
    public void handleDataDetailsUpdate(DataDetailsUpdateEvent event) {
        // Use UI.access() to ensure we're updating the UI from the correct thread
        UI.getCurrent().access(() -> {
            DataDetails newDetails = event.getDataDetails();
            setDataDetails(newDetails);
            clearContents();  // Clear existing content before updating
            this.setDataDetailsContent(newDetails);
        });
    }

}