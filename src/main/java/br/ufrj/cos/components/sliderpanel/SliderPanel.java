package br.ufrj.cos.components.sliderpanel;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
@CssImport("./styles/slider-panel.css")
public class SliderPanel extends Div {
    private final Div content;
    private final Button toggleButton;
    private boolean expanded = true; // Default to expanded
    private String expandedText = "Hide Detail";
    private String collapsedText = "Show Detail";

    private HorizontalLayout header = new HorizontalLayout();
    private HorizontalLayout iotDomainHL = new HorizontalLayout();
    private HorizontalLayout archHL = new HorizontalLayout();
    private HorizontalLayout qrHL = new HorizontalLayout();
    private HorizontalLayout techHL = new HorizontalLayout();

    public SliderPanel() {
        this.removeAll();
        addClassName("slider-panel");

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
        add(toggleButton, content);
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

    public void setIoTDomainContent(Component component) {
        //iotDomainHL.removeAll();
        iotDomainHL.add(component);
    }

    public void setArchitectureContent(Component component) {
        //archHL.removeAll();
        archHL.add(component);
    }

    public void setQualityRequirementContent(Component component) {
        //qrHL.removeAll();
        qrHL.add(component);
    }

    public void setTechnologyContent(Component component) {
        //techHL.removeAll();
        techHL.add(component);
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

        // Add a main header if needed
        header.add(new Html("<div><h1>Details</h1></div>"));

        content.add(header, iotDomainHL, archHL, qrHL, techHL);
    }

    public void clearContents() {
        header.removeAll();
        iotDomainHL.removeAll();
        archHL.removeAll();
        qrHL.removeAll();
        techHL.removeAll();
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
}