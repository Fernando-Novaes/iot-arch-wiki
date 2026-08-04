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
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class TreeRootSelectionComponent extends HorizontalLayout {

    private static final String GRADIENT_IOT_DOMAIN = "linear-gradient(135deg, #e67e22, #f39c12)";
    private static final String GRADIENT_ARCH_SOL = "linear-gradient(135deg, #2980b9, #3498db)";
    private static final String GRADIENT_QUALITY_REQ = "linear-gradient(135deg, #8e44ad, #9b59b6)";
    private static final String GRADIENT_TECH = "linear-gradient(135deg, #27ae60, #2ecc71)";

    @Getter
    private TreeViewType treeViewType;

    @Getter @Setter
    private Boolean isFiltering = false;

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

        // Initialize navigation buttons
        this.changeRight = new Button(VaadinIcon.ARROW_CIRCLE_RIGHT.create());
        this.changeRight.getStyle()
                .set("border", "none")
                .set("background", "transparent")
                .set("color", "var(--lumo-primary-color)")
                .set("cursor", "pointer");

        this.changeLeft = new Button(VaadinIcon.ARROW_CIRCLE_LEFT.create());
        this.changeLeft.getStyle()
                .set("border", "none")
                .set("background", "transparent")
                .set("color", "var(--lumo-primary-color)")
                .set("cursor", "pointer");

        this.rootSelection = createStyledButton("Root");
        this.leafLevelOne = createStyledButton("First Level");
        this.leafLevelTwo = createStyledButton("Second Level");
        this.leafLevelThree = createStyledButton("Third Level");

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
        if (this.isFiltering) {
            return switch (current) {
                case IoTDomain_Filtered -> TreeViewType.QualityRequirement_Filtered;
                case ArchitectureSolution_Filtered -> TreeViewType.IoTDomain_Filtered;
                case QualityRequirement_Filtered, Technology_Filtered -> TreeViewType.ArchitectureSolution_Filtered;
                default -> TreeViewType.IoTDomain_Filtered;
            };
        } else {
            return switch (current) {
                case IoTDomain, Filtered -> TreeViewType.QualityRequirement;
                case ArchitectureSolution_Filtered -> TreeViewType.IoTDomain;
                case QualityRequirement, Technology -> TreeViewType.ArchitectureSolution;
                default -> TreeViewType.IoTDomain;
            };
        }
    }

    private TreeViewType getRightTreeViewType(TreeViewType current) {
        if (this.isFiltering) {
            return switch (current) {
                case IoTDomain_Filtered -> TreeViewType.ArchitectureSolution_Filtered;
                case ArchitectureSolution_Filtered -> TreeViewType.QualityRequirement_Filtered;
                case QualityRequirement_Filtered, Technology_Filtered -> TreeViewType.IoTDomain_Filtered;
                default -> TreeViewType.IoTDomain_Filtered;
            };
        } else {
            return switch (current) {
                case IoTDomain, Filtered -> TreeViewType.ArchitectureSolution;
                case ArchitectureSolution -> TreeViewType.QualityRequirement;
                case QualityRequirement, Technology -> TreeViewType.IoTDomain;
                default -> TreeViewType.IoTDomain;
            };
        }
    }

    private Button createStyledButton(String tooltip) {
        Button button = new Button();
        button.setTooltipText(tooltip);
        button.getStyle()
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("font-size", "0.8125rem")
                .set("color", "#ffffff")
                .set("border", "none")
                .set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.15)")
                .set("padding", "4px 14px")
                .set("cursor", "pointer")
                .set("transition", "transform 0.15s ease, box-shadow 0.15s ease");
        return button;
    }

    private Button createSeparator() {
        Button separator = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
        separator.getStyle()
                .set("background", "transparent")
                .set("color", "var(--lumo-contrast-50pct)")
                .set("border", "none")
                .set("padding", "0 2px");
        return separator;
    }

    private void setupLayout() {
        HorizontalLayout changeContainer = new HorizontalLayout();
        changeContainer.setSpacing(false);
        changeContainer.setAlignItems(Alignment.CENTER);
        changeContainer.getStyle().setPaddingLeft("0.5em");
        changeContainer.add(changeLeft, changeRight);

        this.setSpacing(true);
        this.setAlignItems(Alignment.CENTER);
        this.getStyle()
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "12px");

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

    public void changeToDefaultView() {
        this.setTreeViewType(TreeViewType.IoTDomain);
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
                    GRADIENT_IOT_DOMAIN, GRADIENT_ARCH_SOL, GRADIENT_QUALITY_REQ, GRADIENT_TECH
            );
            case ArchitectureSolution, ArchitectureSolution_Filtered -> applyColors(
                    GRADIENT_ARCH_SOL, GRADIENT_QUALITY_REQ, GRADIENT_TECH, GRADIENT_IOT_DOMAIN
            );
            case QualityRequirement, QualityRequirement_Filtered -> applyColors(
                    GRADIENT_QUALITY_REQ, GRADIENT_TECH, GRADIENT_ARCH_SOL, GRADIENT_IOT_DOMAIN
            );
            case Technology, Technology_Filtered -> applyColors(
                    GRADIENT_TECH, GRADIENT_IOT_DOMAIN, GRADIENT_ARCH_SOL, GRADIENT_QUALITY_REQ
            );
            default -> applyColors(
                    GRADIENT_IOT_DOMAIN, GRADIENT_ARCH_SOL, GRADIENT_QUALITY_REQ, GRADIENT_TECH
            );
        }
    }

    private void applyColors(String root, String level1, String level2, String level3) {
        rootSelection.getStyle().set("background", root);
        leafLevelOne.getStyle().set("background", level1);
        leafLevelTwo.getStyle().set("background", level2);
        leafLevelThree.getStyle().set("background", level3);
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