package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.sliderpanel.DataDetailsUpdateEvent;
import br.ufrj.cos.components.sliderpanel.OpenCloseEvent;
import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.components.treeview.TreeRootSelectionComponent;
import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.service.*;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import br.ufrj.cos.views.record.IoTDomainRecord;
import br.ufrj.cos.views.record.QualityRequirementRecord;
import br.ufrj.cos.views.record.TechnologyRecord;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("IoT-Architecture Knowledge Base")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView implements HasTour {
    private final TreeViewComponent treeViewComponent;
    private TreeViewComponent treeView;
    private final TreeRootSelectionComponent treeRootSelection;
    private final SliderPanel sliderPanel;
    private final ApplicationEventPublisher eventPublisher;

    // Services
    private final IoTDomainService ioTDomainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final TechnologyService technologyService;

    private enum ActionType {
        NONE, IOTDOMAIN, ARCHITECTURESOLUTION, QUALITYREQUIREMENT, TECHNOLOGY
    }
    private ActionType currentAction = ActionType.NONE;

    // ComboBoxes
    private MultiSelectComboBox<IoTDomainRecord> iotDomainCombo;
    private MultiSelectComboBox<ArchitectureSolutionRecord> architectureCombo;
    private MultiSelectComboBox<QualityRequirementRecord> qualityCombo;
    private MultiSelectComboBox<TechnologyRecord> technologiesCombo;

    private HorizontalLayout comboBoxLayout;
    private Button filterButton;
    private Button cancelButton;

    FilterDataTreeView filterDataTreeView;

    @Autowired
    public IoTArchView(TreeViewComponent treeView,
                       TreeRootSelectionComponent treeRootSelection,
                       SliderPanel sliderPanel, ApplicationEventPublisher eventPublisher,
                       IoTDomainService ioTDomainService,
                       ArchitectureSolutionService architectureSolutionService,
                       QualityRequirementService qualityRequirementService,
                       TechnologyService technologyService, TreeViewComponent treeViewComponent) {

        this.treeView = treeView; // Create new instance
        this.treeRootSelection = treeRootSelection; // Create new instance
        this.sliderPanel = sliderPanel;
        this.eventPublisher = eventPublisher;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;

        initializeView();
        this.treeViewComponent = treeViewComponent;
    }

    @Override
    public Onboarding createTour() {
            return new TourUtils().build()
                    .addStep(filterButton, "Filter options", new Html("<div>The filtering mechanism allows for precise refinement of the knowledge base\n" +
                            "view. To apply filters, proceed as follows:</br></br>" +
                            " • Select one or more of the following: Architecture (Pattern/Name), IoT Domain,\n" +
                            "Quality Requirement, Technology or combine more than one option. Then, click the\n" +
                            "Filter button to perform the action.</br>" +
                            " • Combining Filters: When you select multiple criteria (e.g., Domain: 'Healthcare' AND\n" +
                            "QR: 'Security'), the system will only show results that match all selected criteria.</br>" +
                            " • To reset the view, click the Cancel button when visible.</div>"), PopupPosition.BOTTOM)
                    .addStep(iotDomainCombo, "Multiselection Comboboxes", new Html("<p>You can select multiple values from any combobox.</p>"), PopupPosition.END, Optional.of(l -> {
                        iotDomainCombo.updateSelection(Set.of(new IoTDomainRecord("Healthcare"), new IoTDomainRecord("Generic")), Set.of());
                        iotDomainCombo.setOpened(true);
                    }))
                    .addStep(treeView, "Hierarchical Knowledge Base", new Html("<div>Browse all data from Knowledge Base here. All data is organized in hierarchical way, considering: IoT Domain, Architectural Solution, " +
                            "Quality Requirement, and Technology.</br>" +
                            "When you open an item in the Tree and move the mouser point over it, the Details View" +
                            "Panel will display all associated attributes, such as:" +
                            "Description, Source Paper / Reference, Target IoT Domain(s), Addressed Quality" +
                            "Requirements (QRs), and Associated Technologies.</div>"), PopupPosition.BOTTOM,
                            Optional.of(l -> {
                                iotDomainCombo.updateSelection(Set.of(), Set.of(new IoTDomainRecord("Healthcare"), new IoTDomainRecord("Generic")));
                            }))
                    .addStep(treeRootSelection, "Hierarchical Representation", new Html("<div><p>Here is shown how data is presented in the Tree (root and leaf).</p></div>"), PopupPosition.BOTTOM)
                    .addStep(sliderPanel.getToggleButton(), "Show/Hide Details", new Html("<p>You can make the Details View Panel visible or hidden, by clicking on the Show/Hide Details buttons" +
                            "The Show/Hide panel is resizable. To adjust its width, hover the cursor over the left" +
                            "edge until it changes into a resize icon, then click and drag to the desired size.</p>"), PopupPosition.END,
                            Optional.of(l -> eventPublisher.publishEvent(new OpenCloseEvent(OpenCloseEvent.Action.OPEN))))
                    .addStep(treeViewComponent.getTreeGrid(), "Move the cursor over the nodes", new Html("<p>To see more details about a node in the details panel, move the cursor over it.</p>"),
                            PopupPosition.BOTTOM, Optional.of(l -> eventPublisher.publishEvent(new OpenCloseEvent(OpenCloseEvent.Action.CLOSE))))
                    .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }

    private void initializeView() {
        getContent().removeAll();

        // Initialize UI components with modern icon placeholders
        this.iotDomainCombo = createComboBox("IoT Domains", "🌐 All IoT Domains", ActionType.IOTDOMAIN);
        this.architectureCombo = createComboBox("Architecture Solutions", "🏛️ All Architecture Solutions", ActionType.ARCHITECTURESOLUTION);
        this.qualityCombo = createComboBox("Quality Requirements", "⚡ All Quality Requirements", ActionType.QUALITYREQUIREMENT);
        this.technologiesCombo = createComboBox("Technologies", "🛠️ All Technologies", ActionType.TECHNOLOGY);

        this.filterButton = createFilterButton();
        this.cancelButton = createCancelButton();
        this.comboBoxLayout = createComboBoxLayout();

        initTreeView();
        getContent().setSpacing(false);
        getContent().getStyle().set("gap", "0.5rem");
        getContent().add(createFilterDiv(), treeView, sliderPanel);
        loadDataToComboBoxes(ActionType.NONE);

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }

    private void initTreeView() {
        this.treeView.setTreeViewType(TreeViewType.IoTDomain);
        this.treeView.setGridHeader(this.treeRootSelection);
        this.treeView.setSizeFull();
        this.treeView.setMinHeight("500px");
        this.treeView.load();

            this.filterDataTreeView =
                    new FilterDataTreeView(this.treeView,
                            this.iotDomainCombo, this.architectureCombo, this.qualityCombo, this.technologiesCombo,
                            this.architectureSolutionService.findAllOrderedByName(), this.ioTDomainService.findAllOrderByName());
    }

    private <T> MultiSelectComboBox<T> createComboBox(String label, String placeholder, ActionType actionType) {
        MultiSelectComboBox<T> comboBox = new MultiSelectComboBox<>();
        comboBox.setPlaceholder(placeholder);
        comboBox.setWidth("260px");
        comboBox.setClearButtonVisible(true);
        comboBox.setLabel(null);
        comboBox.setSelectedItemsOnTop(true);

        applyComboBoxStyles(comboBox);
        setupComboBoxOverlay(comboBox);

        return comboBox;
    }

    private void applyComboBoxStyles(MultiSelectComboBox<?> comboBox) {
        comboBox.getStyle()
                .set("margin", "0 0.4em")
                .set("position", "relative")
                .set("z-index", "5");
    }

    private void setupComboBoxOverlay(MultiSelectComboBox<?> comboBox) {
        comboBox.addAttachListener(event -> {
            event.getSource().getElement().executeJs(
                    "this.style.setProperty('--vaadin-overlay-viewport-bottom', 'auto');" +
                            "this.style.setProperty('--overlay-box-shadow', '0 8px 24px rgba(0, 0, 0, 0.15)');"
            );
        });
        comboBox.getElement().getThemeList().add("custom-overlay");
    }

    private Button createFilterButton() {
        Button button = new Button("Apply Filter", VaadinIcon.SEARCH.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "8px")
                .set("font-weight", "600")
                .set("padding", "0.5em 1.2em");

        button.addClickListener(e -> handleFilterClick());
        return button;
    }

    private Button createCancelButton() {
        Button button = new Button("Reset", VaadinIcon.REFRESH.create());
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.setVisible(false);
        button.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "8px")
                .set("font-weight", "600");

        button.addClickListener(e -> handleCancelClick());
        return button;
    }

    private void handleFilterClick() {
        if (isAnyFilterSelected()) {
            safelyRemoveTreeView();
            cancelButton.setVisible(true);
            treeView.setIsFiltering(true);
            this.treeView.setTreeViewType(TreeViewType.IoTDomain_Filtered);
            this.treeRootSelection.setIsFiltering(true);
            this.filterDataTreeView.filterTreeViewDataSource(this.ioTDomainService.findAllOrderByName());
            this.treeView.load();
            this.treeRootSelection.changeToDefaultView();
            getContent().add(this.treeView);
        }
    }

    private boolean isAnyFilterSelected() {
        return (iotDomainCombo.getValue() != null && !iotDomainCombo.getValue().isEmpty()) ||
                (architectureCombo.getValue() != null && !architectureCombo.getValue().isEmpty()) ||
                (qualityCombo.getValue() != null && !qualityCombo.getValue().isEmpty()) ||
                (technologiesCombo.getValue() != null && !technologiesCombo.getValue().isEmpty());
    }

    private void handleCancelClick() {
        safelyRemoveTreeView();
        currentAction = ActionType.NONE;
        treeView.setIsFiltering(false);
        treeView.setTreeViewType(TreeViewType.IoTDomain);
        treeView.load();
        getContent().add(treeView);
        loadDataToComboBoxes(ActionType.NONE);
        cancelButton.setVisible(false);
        lockFilterPanel(false);
        this.treeRootSelection.setIsFiltering(false);
        this.treeRootSelection.changeToDefaultView();
    }

    private void safelyRemoveTreeView() {
        if (getContent() != null) {
            getContent().getChildren()
                    .filter(component -> component.equals(treeView))
                    .findFirst()
                    .ifPresent(component -> getContent().remove(component));
        }
    }

    private HorizontalLayout createComboBoxLayout() {
        HorizontalLayout layout = new HorizontalLayout(
                iotDomainCombo,
                architectureCombo,
                qualityCombo,
                technologiesCombo,
                filterButton,
                cancelButton
        );

        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        layout.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "0.8em")
                .set("width", "100%")
                .set("position", "relative")
                .set("z-index", "1")
                .set("overflow", "visible");

        return layout;
    }

    private Div createFilterDiv() {
        Div centerContainer = new Div();
        centerContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "100%");

        VerticalLayout mainLayout = new VerticalLayout();
        styleMainLayout(mainLayout);

        HorizontalLayout headerBox = new HorizontalLayout();
        headerBox.setWidthFull();
        headerBox.setAlignItems(FlexComponent.Alignment.CENTER);

        H3 title = new H3("🔍 Knowledge Base Explorer & Filter");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.05rem")
                .set("font-weight", "600");

        Span subtitle = new Span("Filter by IoT domain, architecture solution, quality requirement, or technology stack");
        subtitle.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout textContainer = new VerticalLayout(title, subtitle);
        textContainer.setPadding(false);
        textContainer.setSpacing(false);
        textContainer.getStyle().set("margin-bottom", "0.3rem");

        mainLayout.add(textContainer, comboBoxLayout);
        centerContainer.add(mainLayout);

        return centerContainer;
    }

    private void styleMainLayout(VerticalLayout layout) {
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle()
                .set("padding", "0.6rem 1rem")
                .set("gap", "0.4rem")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.05)")
                .set("width", "100%")
                .set("max-width", "1800px");
    }

    private void lockFilterPanel(boolean lock) {
        iotDomainCombo.setEnabled(!lock);
        architectureCombo.setEnabled(!lock);
        qualityCombo.setEnabled(!lock);
        technologiesCombo.setEnabled(!lock);
        filterButton.setEnabled(!lock);
    }

    private void loadDataToComboBoxes(ActionType actionType) {
        switch (actionType) {
            case NONE -> {
                // Caso de Reset: Carrega todos os dados iniciais com novas consultas.
                this.iotDomainCombo.setItems(ioTDomainService.findAllIoTDomainGroupedByName());
                this.architectureCombo.setItems(architectureSolutionService.findAllArchitectureSolutionGroupedByName());
                this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
                this.qualityCombo.setItems(qualityRequirementService.findAllQualityRequirementGroupedByName());
            }
        }
    }

    @EventListener
    public void handleDataDetailsUpdate(DataDetailsUpdateEvent event) {
        sliderPanel.setDataDetailsContent(event.getDataDetails());
    }
}
