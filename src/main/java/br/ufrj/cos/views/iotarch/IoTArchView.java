package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.sliderpanel.DataDetailsUpdateEvent;
import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.components.treeview.TreeRootSelectionComponent;
import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import br.ufrj.cos.views.record.IoTDomainRecord;
import br.ufrj.cos.views.record.QualityRequirementRecord;
import br.ufrj.cos.views.record.TechnologyRecord;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

@PermitAll
@UIScope
@PageTitle("IoT-Architecture Knowledge Base")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView {
    private final TreeViewComponent treeView;
    private final TreeRootSelectionComponent treeRootSelection;
    private final SliderPanel sliderPanel;

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
    private ComboBox<IoTDomainRecord> iotDomainCombo;
    private ComboBox<ArchitectureSolutionRecord> architectureCombo;
    private ComboBox<QualityRequirementRecord> qualityCombo;
    private ComboBox<TechnologyRecord> technologiesCombo;

    private HorizontalLayout comboBoxLayout;
    private Button filterButton;
    private Button cancelButton;

    @Autowired
    public IoTArchView(TreeViewComponent treeView,
                       TreeRootSelectionComponent treeRootSelection,
                       SliderPanel sliderPanel,
                       IoTDomainService ioTDomainService,
                       ArchitectureSolutionService architectureSolutionService,
                       QualityRequirementService qualityRequirementService,
                       TechnologyService technologyService) {

        this.treeView = treeView; // Create new instance
        this.treeRootSelection = treeRootSelection; // Create new instance
        this.sliderPanel = sliderPanel;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;

        initializeView();
    }

    private void initializeView() {
        safelyRemoveTreeView();

        // Initialize UI components
        this.iotDomainCombo = createComboBox("IoT Domains", ActionType.IOTDOMAIN);
        this.architectureCombo = createComboBox("Architecture Solutions", ActionType.ARCHITECTURESOLUTION);
        this.qualityCombo = createComboBox("Quality Requirements", ActionType.QUALITYREQUIREMENT);
        this.technologiesCombo = createComboBox("Technologies", ActionType.TECHNOLOGY);

        this.filterButton = createFilterButton();
        this.cancelButton = createCancelButton();
        this.comboBoxLayout = createComboBoxLayout();

        initTreeView();
        createHeader("Knowledge Base");
        getContent().add(createFilterDiv(), createTreeRootSelectionDiv(), treeView, sliderPanel);
        loadDataToComboBoxes(ActionType.NONE);
        createDetailSliderPanel();

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }

    private void initTreeView() {
            safelyRemoveTreeView();
            this.treeView.setTreeViewType(TreeViewType.IoTDomain);
            this.treeView.load();
    }

    private enum Direction {
        LEFT, RIGHT
    }

    private void changeRoot(Direction direction) {
        safelyRemoveTreeView();

        TreeViewType currentType = treeRootSelection.getTreeViewType();
        TreeViewType newType = calculateNewTreeViewType(currentType, direction);

        treeRootSelection.setTreeViewType(newType);
        treeView.load();
        getContent().add(treeView);
    }

    private TreeViewType calculateNewTreeViewType(TreeViewType currentType, Direction direction) {
        return switch (currentType) {
            case IoTDomain, Filtered -> direction == Direction.LEFT ?
                    TreeViewType.QualityRequirement : TreeViewType.ArchitectureSolution;
            case ArchitectureSolution -> direction == Direction.LEFT ?
                    TreeViewType.IoTDomain : TreeViewType.QualityRequirement;
            case QualityRequirement, Technology -> direction == Direction.LEFT ?
                    TreeViewType.ArchitectureSolution : TreeViewType.IoTDomain;
            case IoTDomain_Filtered -> direction == Direction.LEFT ?
                    TreeViewType.QualityRequirement_Filtered : TreeViewType.ArchitectureSolution_Filtered;
            case ArchitectureSolution_Filtered -> direction == Direction.LEFT ?
                    TreeViewType.IoTDomain_Filtered : TreeViewType.QualityRequirement_Filtered;
            case QualityRequirement_Filtered, Technology_Filtered -> direction == Direction.LEFT ?
                    TreeViewType.ArchitectureSolution_Filtered : TreeViewType.IoTDomain_Filtered;
        };
    }

    private <T> ComboBox<T> createComboBox(String label, ActionType actionType) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setPlaceholder("All " + label);
        comboBox.setWidth("300px");
        comboBox.setClearButtonVisible(false);
        comboBox.setLabel(null);

        applyComboBoxStyles(comboBox);
        setupComboBoxOverlay(comboBox);

        comboBox.addValueChangeListener(e -> {
            currentAction = actionType;
            loadDataToComboBoxes(currentAction);
            cancelButton.setVisible(true);
        });

        return comboBox;
    }

    private void applyComboBoxStyles(ComboBox<?> comboBox) {
        comboBox.getStyle()
                .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.1)")
                .set("--lumo-body-text-color", "#ffffff")
                .set("--lumo-secondary-text-color", "#e0e0e0")
                .set("--lumo-primary-text-color", "#ffffff")
                .set("margin", "0 0.5em")
                .set("position", "relative")
                .set("z-index", "5");
    }

    private void setupComboBoxOverlay(ComboBox<?> comboBox) {
        comboBox.addAttachListener(event -> {
            event.getSource().getElement().executeJs(
                    "this.style.setProperty('--vaadin-overlay-viewport-bottom', 'auto');" +
                            "this.style.setProperty('--overlay-box-shadow', '0 2px 8px rgba(0, 0, 0, 0.2)');"
            );
        });
        comboBox.getElement().getThemeList().add("custom-overlay");
    }

    private Button createFilterButton() {
        Button button = new Button("Filter", VaadinIcon.SEARCH.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.getStyle().setCursor("pointer");

        button.addClickListener(e -> handleFilterClick());

        return button;
    }

    private Button createCancelButton() {
        Button button = new Button("Cancel", VaadinIcon.CLOSE_CIRCLE_O.create());
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.setVisible(false);
        button.getStyle().setCursor("pointer");

        button.addClickListener(e -> handleCancelClick());

        return button;
    }

    private void handleFilterClick() {
        if (isAnyFilterSelected()) {
            // Remove existing tree view first
            safelyRemoveTreeView();

            // Setup new filtered view
            cancelButton.setVisible(true);
            treeView.setIsFiltering(true);

            //treeRootSelection.setTreeViewType(TreeViewType.IoTDomain_Filtered);
            this.treeView.setTreeViewType(TreeViewType.IoTDomain_Filtered);
            filterTreeViewDataSource();

            // Load and add the new tree view
            this.treeView.load();

            lockFilterPanel(true);
            getContent().add(this.treeView);
        }
    }

    private boolean isAnyFilterSelected() {
        return iotDomainCombo.getValue() != null ||
                architectureCombo.getValue() != null ||
                qualityCombo.getValue() != null ||
                technologiesCombo.getValue() != null;
    }

    private void handleCancelClick() {
        safelyRemoveTreeView();
        currentAction = ActionType.NONE;
        treeView.setIsFiltering(false);
        //treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
        treeView.setTreeViewType(TreeViewType.IoTDomain);
        treeView.load();
        getContent().add(treeView);
        loadDataToComboBoxes(ActionType.NONE);
        cancelButton.setVisible(false);
        lockFilterPanel(false);
    }

    private void safelyRemoveTreeView() {
        // First check if the content layout exists and contains the tree view
        if (getContent() != null) {
            getContent().getChildren()
                    .filter(component -> component.equals(treeView))
                    .findFirst()
                    .ifPresent(component -> getContent().remove(component));

            // Ensure treeRootSelectionComponent is never removed

//            if ((getContent().getChildren().findAny().isPresent()) && (!getContent().getChildren().anyMatch(component -> component.equals(treeRootSelection)))) {
//                getContent().addComponentAtIndex(1, this.createTreeRootSelectionDiv());
//            }
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
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        layout.getStyle()
                .set("flex-wrap", "nowrap")
                .set("gap", "1em")
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
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        styleMainLayout(mainLayout);

        mainLayout.add(comboBoxLayout);
        centerContainer.add(mainLayout);

        return centerContainer;
    }

    private Div createTreeRootSelectionDiv() {
        Div centerContainer = new Div();
        centerContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "left");
                //.set("width", "100%");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        styleMainLayout(mainLayout);

        mainLayout.add(treeRootSelection);
        centerContainer.add(mainLayout);

        return centerContainer;
    }

    private void styleMainLayout(VerticalLayout layout) {
        layout.getStyle()
                .set("background-color", "#373a3f")
                .set("border", "1px solid #4a4d52")
                .set("border-radius", "12px")
                .set("width", "100%")
                .set("max-width", "1800px");
                //.set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)");
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
                // Load all initial data with fresh queries
                this.iotDomainCombo.setItems(ioTDomainService.findAllIoTDomainGroupedByName());
                this.architectureCombo.setItems(architectureSolutionService.findAllArchitectureSolutionGroupedByName());
                this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
                this.qualityCombo.setItems(qualityRequirementService.findAllQualityRequirementGroupedByName());
            }
            case IOTDOMAIN -> {
                if (iotDomainCombo.getValue() != null) {
                    // Get fresh architecture solutions for selected IoT domain
                    List<ArchitectureSolutionRecord> architectures = architectureSolutionService
                            .findByIoTDomainName(iotDomainCombo.getValue().name())
                            .stream()
                            .map(a -> new ArchitectureSolutionRecord(a.getArchitecture().getName()))
                            .distinct()
                            .sorted(Comparator.comparing(ArchitectureSolutionRecord::name))
                            .toList();

                    this.architectureCombo.setItems(architectures);
                    // Clear dependent comboboxes
                    this.qualityCombo.clear();
                    this.technologiesCombo.clear();
                }
            }
            case ARCHITECTURESOLUTION -> {
                if (iotDomainCombo.getValue() != null && architectureCombo.getValue() != null) {
                    // Get fresh quality requirements for selected architecture
                    List<QualityRequirementRecord> qualityRequirements = qualityRequirementService
                            .findByIoTDomainAndArchitecture(
                                    iotDomainCombo.getValue().name(),
                                    architectureCombo.getValue().name()
                            )
                            .stream()
                            .map(qr -> new QualityRequirementRecord(qr.getName()))
                            .distinct()
                            .sorted(Comparator.comparing(QualityRequirementRecord::name))
                            .toList();

                    this.qualityCombo.setItems(qualityRequirements);
                    // Clear dependent combobox
                    this.technologiesCombo.clear();
                }
            }
            case QUALITYREQUIREMENT -> {
                if (iotDomainCombo.getValue() != null &&
                        architectureCombo.getValue() != null &&
                        qualityCombo.getValue() != null) {
                    // Get fresh technologies for selected quality requirement
                    List<TechnologyRecord> technologies = technologyService
                            .findByIoTDomainAndArchitectureAndQualityRequirement(
                                    iotDomainCombo.getValue().name(),
                                    architectureCombo.getValue().name(),
                                    qualityCombo.getValue().name()
                            )
                            .stream()
                            .map(t -> new TechnologyRecord(t.getDescription()))
                            .distinct()
                            .sorted(Comparator.comparing(TechnologyRecord::description))
                            .toList();

                    this.technologiesCombo.setItems(technologies);
                }
            }
        }
    }

    private void filterTreeViewDataSource() {
        // Start with a copy of the full dataset
        List<IoTDomain> filteredDomains = new ArrayList<>((List<IoTDomain>) this.treeView.getTreeViewData());

        // Filter by IoT Domain if selected
        if (iotDomainCombo.getValue() != null) {
            filteredDomains = filteredDomains.stream()
                    .filter(domain -> domain.getName().equals(iotDomainCombo.getValue().name()))
                    .collect(Collectors.toList());
        }

        // For each remaining domain, filter its architecture solutions
        filteredDomains.forEach(domain -> {
            List<ArchitectureSolution> filteredSolutions = new ArrayList<>(domain.getArchitectureSolutions());

            // Filter by Architecture if selected
            if (architectureCombo.getValue() != null) {
                filteredSolutions = filteredSolutions.stream()
                        .filter(solution -> solution.getArchitecture().getName()
                                .equals(architectureCombo.getValue().name()))
                        .collect(Collectors.toList());
            }

            // Filter by Quality Requirement if selected
            if (qualityCombo.getValue() != null) {
                filteredSolutions = filteredSolutions.stream()
                        .filter(solution -> solution.getQualityRequirements().stream()
                                .anyMatch(qr -> qr.getName().equals(qualityCombo.getValue().name())))
                        .map(solution -> {
                            // Create a copy of the solution with only matching quality requirements
                            ArchitectureSolution filteredSolution = new ArchitectureSolution();
                            BeanUtils.copyProperties(solution, filteredSolution);

                            // Filter QualityRequirementTechnology entries
                            List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies()
                                    .stream()
                                    .filter(qrt -> qrt.getQualityRequirement().getName()
                                            .equals(qualityCombo.getValue().name()))
                                    .collect(Collectors.toList());

                            filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
                            return filteredSolution;
                        })
                        .collect(Collectors.toList());
            }

            // Filter by Technology if needed
            if (technologiesCombo != null && technologiesCombo.getValue() != null) {
                filteredSolutions = filteredSolutions.stream()
                        .filter(solution -> solution.getTechnologies().stream()
                                .anyMatch(tech -> tech.getDescription()
                                        .equals(technologiesCombo.getValue().description())))
                        .map(solution -> {
                            // Create a copy of the solution with only matching technologies
                            ArchitectureSolution filteredSolution = new ArchitectureSolution();
                            BeanUtils.copyProperties(solution, filteredSolution);

                            // Filter QualityRequirementTechnology entries
                            List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies()
                                    .stream()
                                    .filter(qrt -> qrt.getTechnology().getDescription()
                                            .equals(technologiesCombo.getValue().description()))
                                    .collect(Collectors.toList());

                            filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
                            return filteredSolution;
                        })
                        .collect(Collectors.toList());
            }

            // Update the domain with filtered solutions
            domain.setArchitectureSolutions(filteredSolutions);
        });

        // Remove domains that have no matching solutions after filtering
        filteredDomains = filteredDomains.stream()
                .filter(domain -> !domain.getArchitectureSolutions().isEmpty())
                .collect(Collectors.toList());

        // Update the tree view with filtered results
        this.treeView.setTreeViewData(filteredDomains);
    }

    /***
     * Creates the search panel at the bottom of the page
     */
    private void createDetailSliderPanel() {
        sliderPanel.setButtonTexts("Hide Details", "Show Details");
        sliderPanel.setExpanded(false);
    }

    @EventListener
    public void handleDataDetailsUpdate(DataDetailsUpdateEvent event) {
        sliderPanel.setDataDetailsContent(event.getDataDetails());
    }
}
