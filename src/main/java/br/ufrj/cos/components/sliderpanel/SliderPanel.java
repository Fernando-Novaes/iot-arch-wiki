package br.ufrj.cos.components.sliderpanel;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
@CssImport("./styles/slider-panel.css")
public class SliderPanel extends Div {
    private final Div content;
    private final Button toggleButton;
    private boolean expanded = true; // Default to expanded
    private String expandedText = "Hide Panel";
    private String collapsedText = "Show Panel";

    public SliderPanel() {
        this.removeAll();
        addClassName("slider-panel");
        addClassName("bottom");

        // Create toggle button
        toggleButton = new Button(expandedText);
        toggleButton.addClassName("slider-toggle");

        // Add initial icon
        Icon icon = VaadinIcon.ANGLE_DOWN.create();
        toggleButton.getElement().insertChild(0, icon.getElement());

        toggleButton.addClickListener(e -> toggle());

        content = new Div();
        content.addClassName("slider-content");

        add(toggleButton, content);

        // Set initial expanded state
        addClassName("expanded");
    }

    public void setContent(Component... components) {
        content.removeAll();
        VerticalLayout layout = new VerticalLayout();
        layout.removeAll();
        layout.add(components);
        content.add(layout);
    }

    public void toggle() {
        expanded = !expanded;
        if (expanded) {
            addClassName("expanded");
            toggleButton.setText(expandedText);
            // Update icon
            toggleButton.getElement().removeChild(0);
            Icon icon = VaadinIcon.ANGLE_DOWN.create();
            toggleButton.getElement().insertChild(0, icon.getElement());
        } else {
            removeClassName("expanded");
            toggleButton.setText(collapsedText);
            // Update icon
            toggleButton.getElement().removeChild(0);
            Icon icon = VaadinIcon.ANGLE_UP.create();
            toggleButton.getElement().insertChild(0, icon.getElement());
        }
    }

    public void setButtonTexts(String expandedText, String collapsedText) {
        this.expandedText = expandedText;
        this.collapsedText = collapsedText;
        toggleButton.setText(expanded ? expandedText : collapsedText);
    }

    public void setHeight(String height) {
        content.getStyle().set("height", height);
    }

    public void setWidth(String width) {
        content.getStyle().set("width", width);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            toggle();
        }
    }
}