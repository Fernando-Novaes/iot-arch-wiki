package br.ufrj.cos.components.treeview;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class TreeRootSelectionComponent extends HorizontalLayout {

    private final Button rootSelection = new Button("Root");
    private final Button leafLevelOne = new Button("LeafLevelOne");
    private final Button leafLevelTwo = new Button("LeafLevelTwo");
    private final Button leafLevelThree = new Button("LeafLevelThree");

    private final Button changeRight = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
    private final Button changeLeft = new Button(new Icon(VaadinIcon.ARROW_LEFT));

    @Getter
    private TreeViewType treeViewType;

    public TreeRootSelectionComponent() {
        this.add(rootSelection, new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT)),
                leafLevelOne, new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT)), leafLevelTwo, new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT)), leafLevelThree,
                changeLeft, changeRight);
        this.treeViewType = TreeViewType.IoTDomain;
        this.styleButtons();
    }

    private void styleButtons() {
        switch (this.treeViewType) {
            case IoTDomain -> {
                rootSelection.getStyle().set("--vaadin-button-background", "#ED8312E5");
                leafLevelOne.getStyle().set("--vaadin-button-background", "white");
                leafLevelTwo.getStyle().set("--vaadin-button-background", "yellow");
                leafLevelThree.getStyle().set("--vaadin-button-background", "green");

                changeLeft.setTooltipText(String.format("Change Root to %s", TreeViewType.Technology.toString()));
                changeRight.setTooltipText(String.format("Change Root to %s", TreeViewType.ArchitectureSolution.toString()));
            }
            case ArchitectureSolution -> {
                rootSelection.getStyle().set("--vaadin-button-background", "white");
                leafLevelOne.getStyle().set("--vaadin-button-background", "yellow");
                leafLevelTwo.getStyle().set("--vaadin-button-background", "green");
                leafLevelThree.getStyle().set("--vaadin-button-background", "#ED8312E5");

                changeLeft.setTooltipText(String.format("Change Root to %s", TreeViewType.IoTDomain.toString()));
                changeRight.setTooltipText(String.format("Change Root to %s", TreeViewType.QualityRequirement.toString()));
            }
            case QualityRequirement -> {
                rootSelection.getStyle().set("--vaadin-button-background", "yellow");
                leafLevelOne.getStyle().set("--vaadin-button-background", "green");
                leafLevelTwo.getStyle().set("--vaadin-button-background", "#ED8312E5");
                leafLevelThree.getStyle().set("--vaadin-button-background", "white");

                changeLeft.setTooltipText(String.format("Change Root to %s", TreeViewType.ArchitectureSolution.toString()));
                changeRight.setTooltipText(String.format("Change Root to %s", TreeViewType.Technology.toString()));
            }
            case Technology -> {
                rootSelection.getStyle().set("--vaadin-button-background", "green");
                leafLevelOne.getStyle().set("--vaadin-button-background", "#ED8312E5");
                leafLevelTwo.getStyle().set("--vaadin-button-background", "white");
                leafLevelThree.getStyle().set("--vaadin-button-background", "yellow");

                changeLeft.setTooltipText(String.format("Change Root to %s", TreeViewType.QualityRequirement.toString()));
                changeRight.setTooltipText(String.format("Change Root to %s", TreeViewType.IoTDomain.toString()));
            }
            default -> {
                rootSelection.getStyle().set("--vaadin-button-background", "#ED8312E5");
                leafLevelOne.getStyle().set("--vaadin-button-background", "white");
                leafLevelTwo.getStyle().set("--vaadin-button-background", "yellow");
                leafLevelThree.getStyle().set("--vaadin-button-background", "green");

                changeLeft.setTooltipText(String.format("Change Root to %s", TreeViewType.Technology.toString()));
                changeRight.setTooltipText(String.format("Change Root to %s", TreeViewType.ArchitectureSolution.toString()));
            }
        }

        rootSelection.getStyle().set("--vaadin-button-text-color", "#2e3033");
        leafLevelOne.getStyle().set("--vaadin-button-text-color", "#2e3033");
        leafLevelTwo.getStyle().set("--vaadin-button-text-color", "#2e3033");
        leafLevelThree.getStyle().set("--vaadin-button-text-color", "#2e3033");

        rootSelection.setTooltipText("Root");
        leafLevelOne.setTooltipText("First Level");
        leafLevelTwo.setTooltipText("Second Level");
        leafLevelThree.setTooltipText("Third Level");

        changeRight.getStyle().set("cursor", "pointer");
        changeLeft.getStyle().set("cursor", "pointer");

    }

    public TreeRootSelectionComponent create() {
        return this;
    }

    public void setTreeViewType(TreeViewType t) {
        this.treeViewType = t;
        rootSelection.setText(treeViewType.toString());

        switch (this.treeViewType) {
            case IoTDomain -> {
                leafLevelOne.setText(TreeViewType.ArchitectureSolution.toString());
                leafLevelTwo.setText(TreeViewType.QualityRequirement.toString());
                leafLevelThree.setText(TreeViewType.Technology.toString());
            }
            case ArchitectureSolution -> {
                leafLevelOne.setText(TreeViewType.QualityRequirement.toString());
                leafLevelTwo.setText(TreeViewType.Technology.toString());
                leafLevelThree.setText(TreeViewType.IoTDomain.toString());
            }
            case QualityRequirement -> {
                leafLevelOne.setText(TreeViewType.Technology.toString());
                leafLevelTwo.setText(TreeViewType.IoTDomain.toString());
                leafLevelThree.setText(TreeViewType.ArchitectureSolution.toString());
            }
            case Technology -> {
                leafLevelOne.setText(TreeViewType.IoTDomain.toString());
                leafLevelTwo.setText(TreeViewType.ArchitectureSolution.toString());
                leafLevelThree.setText(TreeViewType.QualityRequirement.toString());
            }
            default -> {
                treeViewType = TreeViewType.IoTDomain;
                rootSelection.setText(TreeViewType.IoTDomain.toString());
                leafLevelOne.setText(TreeViewType.ArchitectureSolution.toString());
                leafLevelTwo.setText(TreeViewType.QualityRequirement.toString());
                leafLevelThree.setText(TreeViewType.Technology.toString());
            }
        }

        this.styleButtons();
    }

    /***
     * Adding Click event to the Change left button
     *
     * @param clickEvent
     */
    public void addChangeLeftButtonClickListener(ComponentEventListener<ClickEvent<Button>> clickEvent) {
        this.changeLeft.addClickListener(clickEvent);
    }

    /***
     * Adding Click event to the Change right button
     *
     * @param clickEvent
     */
    public void addChangeRightButtonClickListener(ComponentEventListener<ClickEvent<Button>> clickEvent) {
        this.changeRight.addClickListener(clickEvent);
    }
}
