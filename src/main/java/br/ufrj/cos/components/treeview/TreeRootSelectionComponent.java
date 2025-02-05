package br.ufrj.cos.components.treeview;

import br.ufrj.cos.components.treeview.events.TreeRootSelectionChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class TreeRootSelectionComponent extends HorizontalLayout {

    private static final String BUTTON_FONT_WEIGHT = "--vaadin-button-font-weight";
    private static final String BUTTON_BACKGROUND = "--vaadin-button-background";
    private static final String BUTTON_TEXT_COLOR = "--vaadin-button-text-color";

    private static final String COLOR_IOT_DOMAIN = "#ED8312E5";
    private static final String COLOR_WHITE = "white";
    private static final String COLOR_YELLOW = "yellow";
    private static final String COLOR_GREEN = "green";
    private static final String COLOR_TEXT = "#2e3033";

    @Getter
    private TreeViewType treeViewType;

    private final Button rootSelection;
    private final Button leafLevelOne;
    private final Button leafLevelTwo;
    private final Button leafLevelThree;
    private final Button changeRight;
    private final Button changeLeft;
    private final Button separatorOne;
    private final Button separatorTwo;
    private final Button separatorThree;

    private final ApplicationEventPublisher eventPublisher;

    public TreeRootSelectionComponent(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        // Initialize buttons
        this.changeRight = new Button("→");
        this.changeLeft = new Button("←");

        this.rootSelection = createStyledButton("Root");
        this.leafLevelOne = createStyledButton("First Level");
        this.leafLevelTwo = createStyledButton("Second Level");
        this.leafLevelThree = createStyledButton("Third Level");

        // Initialize navigation buttons
        this.changeRight.getStyle().set("cursor", "pointer");
        this.changeLeft.getStyle().set("cursor", "pointer");
        this.changeLeft.addClickListener(this::handleChangeLeft);
        this.changeRight.addClickListener(this::handleChangeRight);

        // Initialize separators
        this.separatorOne = createSeparator();
        this.separatorTwo = createSeparator();
        this.separatorThree = createSeparator();

        setupLayout();
    }

    private void handleChangeLeft(ClickEvent<Button> event) {
        TreeViewType newType = getLeftTreeViewType(treeViewType);
        this.setTreeViewType(newType);
        eventPublisher.publishEvent(
                new TreeRootSelectionChangeEvent(this, newType));
    }

    private void handleChangeRight(ClickEvent<Button> event) {
        TreeViewType newType = getRightTreeViewType(treeViewType);
        this.setTreeViewType(newType);
        eventPublisher.publishEvent(
                new TreeRootSelectionChangeEvent(this, newType));
    }

    private TreeViewType getLeftTreeViewType(TreeViewType current) {
        return switch (current) {
            case IoTDomain, Filtered -> TreeViewType.QualityRequirement;
            case ArchitectureSolution -> TreeViewType.IoTDomain;
            case QualityRequirement, Technology -> TreeViewType.ArchitectureSolution;
            default -> TreeViewType.IoTDomain;
        };
    }

    private TreeViewType getRightTreeViewType(TreeViewType current) {
        return switch (current) {
            case IoTDomain, Filtered -> TreeViewType.ArchitectureSolution;
            case ArchitectureSolution -> TreeViewType.QualityRequirement;
            case QualityRequirement, Technology -> TreeViewType.IoTDomain;
            default -> TreeViewType.IoTDomain;
        };
    }

    private Button createStyledButton(String tooltip) {
        Button button = new Button();
        button.getStyle().set(BUTTON_FONT_WEIGHT, "bold");
        button.getStyle().set(BUTTON_TEXT_COLOR, COLOR_TEXT);
        button.setTooltipText(tooltip);
        return button;
    }

    private Button createSeparator() {
        return new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
    }

    private void setupLayout() {
        // Setup change buttons container
        HorizontalLayout changeContainer = new HorizontalLayout();
        changeContainer.setSpacing(false);
        changeContainer.getStyle().setPaddingLeft("1em");
        changeContainer.add(changeLeft, changeRight);

        // Setup main layout
        this.setSpacing(false);
        this.getStyle().setBorderRadius("8px");

        // Add components to layout
        this.add(
                rootSelection,
                separatorOne,
                leafLevelOne,
                separatorTwo,
                leafLevelTwo,
                separatorThree,
                leafLevelThree,
                changeContainer
        );
    }

    @PostConstruct
    private void init() {
        setTreeViewType(TreeViewType.IoTDomain);
    }

    public void setTreeViewType(TreeViewType type) {
        this.treeViewType = type;
        updateButtonLabels();
        updateButtonStyles();
        updateNavigationTooltips();
    }

    private void updateButtonLabels() {
        rootSelection.setText(treeViewType.toString());

        switch (treeViewType) {
            case IoTDomain, IoTDomain_Filtered -> updateLabels(
                    "Architecture Solution", "Quality Requirement", "Technology"
            );
            case ArchitectureSolution, ArchitectureSolution_Filtered -> updateLabels(
                    "Quality Requirement", "Technology", "IoT Domain"
            );
            case QualityRequirement, QualityRequirement_Filtered -> updateLabels(
                    "Technology", "Architecture Solution", "IoT Domain"
            );
            case Technology, Technology_Filtered -> updateLabels(
                    "IoT Domain", "Architecture Solution", "Quality Requirement"
            );
            default -> updateLabels(
                    "Architecture Solution", "Quality Requirement", "Technology"
            );
        }
    }

    private void updateLabels(String level1, String level2, String level3) {
        leafLevelOne.setText(level1);
        leafLevelTwo.setText(level2);
        leafLevelThree.setText(level3);
    }

    private void updateButtonStyles() {
        switch (treeViewType) {
            case IoTDomain, IoTDomain_Filtered -> applyColors(
                    COLOR_IOT_DOMAIN, COLOR_WHITE, COLOR_YELLOW, COLOR_GREEN
            );
            case ArchitectureSolution, ArchitectureSolution_Filtered -> applyColors(
                    COLOR_WHITE, COLOR_YELLOW, COLOR_GREEN, COLOR_IOT_DOMAIN
            );
            case QualityRequirement, QualityRequirement_Filtered -> applyColors(
                    COLOR_YELLOW, COLOR_GREEN, COLOR_WHITE, COLOR_IOT_DOMAIN
            );
            case Technology, Technology_Filtered -> applyColors(
                    COLOR_GREEN, COLOR_IOT_DOMAIN, COLOR_WHITE, COLOR_YELLOW
            );
            default -> applyColors(
                    COLOR_IOT_DOMAIN, COLOR_WHITE, COLOR_YELLOW, COLOR_GREEN
            );
        }
    }

    private void applyColors(String root, String level1, String level2, String level3) {
        rootSelection.getStyle().set(BUTTON_BACKGROUND, root);
        leafLevelOne.getStyle().set(BUTTON_BACKGROUND, level1);
        leafLevelTwo.getStyle().set(BUTTON_BACKGROUND, level2);
        leafLevelThree.getStyle().set(BUTTON_BACKGROUND, level3);
    }

    private void updateNavigationTooltips() {
        switch (treeViewType) {
            case IoTDomain, IoTDomain_Filtered -> updateTooltips(
                    TreeViewType.Technology, TreeViewType.ArchitectureSolution
            );
            case ArchitectureSolution, ArchitectureSolution_Filtered -> updateTooltips(
                    TreeViewType.IoTDomain, TreeViewType.QualityRequirement
            );
            case QualityRequirement, QualityRequirement_Filtered -> updateTooltips(
                    TreeViewType.ArchitectureSolution, TreeViewType.Technology
            );
            case Technology, Technology_Filtered -> updateTooltips(
                    TreeViewType.QualityRequirement, TreeViewType.IoTDomain
            );
            default -> updateTooltips(
                    TreeViewType.Technology, TreeViewType.ArchitectureSolution
            );
        }
    }

    private void updateTooltips(TreeViewType left, TreeViewType right) {
        changeLeft.setTooltipText(String.format("Change Root to %s", left));
        changeRight.setTooltipText(String.format("Change Root to %s", right));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
    }

    public TreeRootSelectionComponent create() {
        return this;
    }
}