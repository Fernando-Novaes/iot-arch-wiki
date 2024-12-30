package br.ufrj.cos.components.sliderpanel;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Builder;

@SpringComponent
@UIScope
@CssImport("./styles/slider-panel.css")
public class SliderPanel extends Div {
    private final Div content;
    private final Button toggleButton;
    private boolean expanded = true; // Default to expanded
    private String expandedText = "Hide Detail";
    private String collapsedText = "Show Detail";

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
        content.getStyle().set("width", "40%");

        // Configure vertical text
        toggleButton.getElement().getStyle()
                .set("writing-mode", "vertical-lr")  // Changed to vertical-lr for better readability
                .set("text-orientation", "mixed")
                .set("transform", "rotate(180deg)"); // This makes text read from top to bottom

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

        // Add icon last (it will appear at the top due to rotation)
        //toggleButton.getElement().appendChild(icon.getElement());
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

    public void setContent(Component... components) {
        content.removeAll();
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("100%");
        layout.setWidth("100%");
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.removeAll();
        layout.add(components);
        content.add(layout);
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