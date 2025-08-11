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
        //safelyRemoveTreeView();
        getContent().removeAll();

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
        //getContent().add(createFilterDiv(), createTreeRootSelectionDiv(), treeView, sliderPanel);
        getContent().add(createFilterDiv(), treeView, sliderPanel);
        loadDataToComboBoxes(ActionType.NONE);
        //createDetailSliderPanel();


        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }

    private void initTreeView() {
            //safelyRemoveTreeView();
            this.treeView.setTreeViewType(TreeViewType.IoTDomain);
            this.treeView.setGridHeader(this.treeRootSelection);
            this.treeView.load();

            this.filterDataTreeView =
                    new FilterDataTreeView(this.treeView,
                            this.iotDomainCombo, this.architectureCombo, this.qualityCombo, this.technologiesCombo,
                            this.architectureSolutionService.findAllOrderedByName(), this.ioTDomainService.findAllOrderByName());
    }

    private enum Direction {
        LEFT, RIGHT
    }

//    private void changeRoot(Direction direction) {
//        safelyRemoveTreeView();
//
//        TreeViewType currentType = treeRootSelection.getTreeViewType();
//        TreeViewType newType = calculateNewTreeViewType(currentType, direction);
//
//        treeRootSelection.setTreeViewType(newType);
//        treeView.load();
//        getContent().add(treeView);
//    }

//    private TreeViewType calculateNewTreeViewType(TreeViewType currentType, Direction direction) {
//        return switch (currentType) {
//            case IoTDomain, Filtered -> direction == Direction.LEFT ?
//                    TreeViewType.QualityRequirement : TreeViewType.ArchitectureSolution;
//            case ArchitectureSolution -> direction == Direction.LEFT ?
//                    TreeViewType.IoTDomain : TreeViewType.QualityRequirement;
//            case QualityRequirement, Technology -> direction == Direction.LEFT ?
//                    TreeViewType.ArchitectureSolution : TreeViewType.IoTDomain;
//            case IoTDomain_Filtered -> direction == Direction.LEFT ?
//                    TreeViewType.QualityRequirement_Filtered : TreeViewType.ArchitectureSolution_Filtered;
//            case ArchitectureSolution_Filtered -> direction == Direction.LEFT ?
//                    TreeViewType.IoTDomain_Filtered : TreeViewType.QualityRequirement_Filtered;
//            case QualityRequirement_Filtered, Technology_Filtered -> direction == Direction.LEFT ?
//                    TreeViewType.ArchitectureSolution_Filtered : TreeViewType.IoTDomain_Filtered;
//        };
//    }

    private <T> MultiSelectComboBox<T> createComboBox(String label, ActionType actionType) {
        MultiSelectComboBox<T> comboBox = new MultiSelectComboBox<>(label);
        comboBox.setPlaceholder("All " + label);
        comboBox.setWidth("300px");
        comboBox.setClearButtonVisible(false);
        comboBox.setLabel(null);
        comboBox.setSelectedItemsOnTop(true);

        applyComboBoxStyles(comboBox);
        setupComboBoxOverlay(comboBox);

//        comboBox.addValueChangeListener(e -> {
//            currentAction = actionType;
//            loadDataToComboBoxes(ActionType.NONE);
//            //loadDataToComboBoxes(currentAction);
//            cancelButton.setVisible(true);
//        });

        return comboBox;
    }

    private void applyComboBoxStyles(MultiSelectComboBox<?> comboBox) {
        comboBox.getStyle()
//                .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.1)")
//                .set("--lumo-body-text-color", "#ffffff")
//                .set("--lumo-secondary-text-color", "#e0e0e0")
//                .set("--lumo-primary-text-color", "#ffffff")
                .set("margin", "0 0.5em")
                .set("position", "relative")
                .set("z-index", "5");
    }

    private void setupComboBoxOverlay(MultiSelectComboBox<?> comboBox) {
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
        Button button = new Button("Reset", VaadinIcon.CLOSE_CIRCLE_O.create());
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

            this.treeRootSelection.setIsFiltering(true);
            this.filterDataTreeView.filterTreeViewDataSource(this.ioTDomainService.findAllOrderByName());

            // Load and add the new tree view
            this.treeView.load();
            this.treeRootSelection.changeToDefaultView();

            //lockFilterPanel(true);
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
        this.treeRootSelection.setIsFiltering(false);
        this.treeRootSelection.changeToDefaultView();
    }

    private void safelyRemoveTreeView() {
        // First check if the content layout exists and contains the tree view
        if (getContent() != null) {
            getContent().getChildren()
                    .filter(component -> component.equals(treeView))
                    .findFirst()
                    .ifPresent(component -> {

                        getContent().remove(component);

                    });

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
        centerContainer.setWidthFull();
        centerContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "100%");

        VerticalLayout mainLayout = new VerticalLayout();
        //mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        styleMainLayout(mainLayout);

        mainLayout.add(comboBoxLayout);
        centerContainer.add(mainLayout);

        return centerContainer;
    }

//    private Div createTreeRootSelectionDiv() {
//        Div centerContainer = new Div();
//        centerContainer.getStyle()
//                .set("display", "flex")
//                .set("align-items", "center")
//                .set("justify-content", "left");
//                //.set("width", "100%");
//
//        VerticalLayout mainLayout = new VerticalLayout();
//        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
//        styleMainLayout(mainLayout);
//
//        //mainLayout.add(treeRootSelection);
//        centerContainer.add(mainLayout);
//
//        return centerContainer;
//    }

    private void styleMainLayout(VerticalLayout layout) {
        layout.getStyle()
                .set("background-color", "var(--lumo-contrast-10pct)")
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
                // Caso de Reset: Carrega todos os dados iniciais com novas consultas.
                this.iotDomainCombo.setItems(ioTDomainService.findAllIoTDomainGroupedByName());
                this.architectureCombo.setItems(architectureSolutionService.findAllArchitectureSolutionGroupedByName());
                this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
                this.qualityCombo.setItems(qualityRequirementService.findAllQualityRequirementGroupedByName());
            }
//            case IOTDOMAIN -> {
//                Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
//
//                if (selectedDomains != null && !selectedDomains.isEmpty()) {
//                    // Converte o Set<IoTDomain> para um Set<String> com os nomes.
//                    Set<String> domainNames = selectedDomains.stream()
//                            .map(IoTDomainRecord::name)
//                            .collect(Collectors.toSet());
//
//                    // Busca arquiteturas para TODOS os domínios selecionados.
//                    List<ArchitectureSolutionRecord> architectures = architectureSolutionService
//                            .findByIoTDomainNames(domainNames) // Usa o novo método de serviço
//                            .stream()
//                            .map(a -> new ArchitectureSolutionRecord(a.getArchitecture().getName()))
//                            .distinct()
//                            .sorted(Comparator.comparing(ArchitectureSolutionRecord::name))
//                            .toList();
//
//                    this.architectureCombo.setItems(architectures);
//                    // Limpa os combos dependentes que ainda não foram preenchidos.
//                    this.qualityCombo.clear();
//                    this.technologiesCombo.clear();
//                } else {
//                    // Se a seleção de domínio for limpa, reseta os combos dependentes para o estado inicial.
//                    this.architectureCombo.setItems(architectureSolutionService.findAllArchitectureSolutionGroupedByName());
//                    this.qualityCombo.setItems(qualityRequirementService.findAllQualityRequirementGroupedByName());
//                    this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
//                }
//            }
//            case ARCHITECTURESOLUTION -> {
//                Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
//                Set<ArchitectureSolutionRecord> selectedArchitectures = architectureCombo.getValue();
//
//                if (isSetValid(selectedDomains) && isSetValid(selectedArchitectures)) {
//                    // Converte os Sets de seleção para Sets de Strings.
//                    Set<String> domainNames = selectedDomains.stream().map(IoTDomainRecord::name).collect(Collectors.toSet());
//                    Set<String> archNames = selectedArchitectures.stream().map(ArchitectureSolutionRecord::name).collect(Collectors.toSet());
//
//                    // Busca requisitos de qualidade para as combinações selecionadas.
//                    List<QualityRequirementRecord> qualityRequirements = qualityRequirementService
//                            .findByIoTDomainsAndArchitectures(domainNames, archNames) // Usa o novo método
//                            .stream()
//                            .map(qr -> new QualityRequirementRecord(qr.getName()))
//                            .distinct()
//                            .sorted(Comparator.comparing(QualityRequirementRecord::name))
//                            .toList();
//
//                    this.qualityCombo.setItems(qualityRequirements);
//                    this.technologiesCombo.clear();
//                } else {
//                    // Se a seleção de arquitetura for limpa (ou domínio), reseta os combos dependentes.
//                    this.qualityCombo.setItems(qualityRequirementService.findAllQualityRequirementGroupedByName());
//                    this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
//                }
//            }
//            case QUALITYREQUIREMENT -> {
//                Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
//                Set<ArchitectureSolutionRecord> selectedArchitectures = architectureCombo.getValue();
//                Set<QualityRequirementRecord> selectedQRs = qualityCombo.getValue();
//
//                if (isSetValid(selectedDomains) && isSetValid(selectedArchitectures) && isSetValid(selectedQRs)) {
//                    // Converte todos os Sets para Sets de Strings.
//                    Set<String> domainNames = selectedDomains.stream().map(IoTDomainRecord::name).collect(Collectors.toSet());
//                    Set<String> archNames = selectedArchitectures.stream().map(ArchitectureSolutionRecord::name).collect(Collectors.toSet());
//                    Set<String> qrNames = selectedQRs.stream().map(QualityRequirementRecord::name).collect(Collectors.toSet());
//
//                    // Busca tecnologias para as combinações selecionadas.
//                    List<TechnologyRecord> technologies = technologyService
//                            .findByIoTDomainsAndArchitecturesAndQualityRequirements(domainNames, archNames, qrNames) // Usa o novo método
//                            .stream()
//                            .map(t -> new TechnologyRecord(t.getDescription()))
//                            .distinct()
//                            .sorted(Comparator.comparing(TechnologyRecord::description))
//                            .toList();
//
//                    this.technologiesCombo.setItems(technologies);
//                } else {
//                    // Se a seleção de QR for limpa, reseta o combo de tecnologia.
//                    this.technologiesCombo.setItems(technologyService.findAllTechnologyGroupedByName());
//                }
//            }
        }
    }

    /**
     * Método utilitário para verificar se um Set não é nulo e não está vazio.
     * @param set O conjunto a ser verificado.
     * @return true se o conjunto for válido, false caso contrário.
     */
    private boolean isSetValid(Set<?> set) {
        return set != null && !set.isEmpty();
    }


//    private void filterTreeViewDataSource() {
//        // Start with a copy of the full dataset
//        List<IoTDomain> filteredDomains = new ArrayList<>((List<IoTDomain>) this.treeView.getTreeViewData());
//
//        // Filter by IoT Domain if selected
//        if (iotDomainCombo.getValue() != null) {
//            filteredDomains = filteredDomains.stream()
//                    .filter(domain -> domain.getName().equals(iotDomainCombo.getValue().name()))
//                    .collect(Collectors.toList());
//        }
//
//        // For each remaining domain, filter its architecture solutions
//        filteredDomains.forEach(domain -> {
//            List<ArchitectureSolution> filteredSolutions = new ArrayList<>(domain.getArchitectureSolutions());
//
//            // Filter by Architecture if selected
//            if (architectureCombo.getValue() != null) {
//                filteredSolutions = filteredSolutions.stream()
//                        .filter(solution -> solution.getArchitecture().getName()
//                                .equals(architectureCombo.getValue().name()))
//                        .collect(Collectors.toList());
//            }
//
//            // Filter by Quality Requirement if selected
//            if (qualityCombo.getValue() != null) {
//                filteredSolutions = filteredSolutions.stream()
//                        .filter(solution -> solution.getQualityRequirements().stream()
//                                .anyMatch(qr -> qr.getName().equals(qualityCombo.getValue().name())))
//                        .map(solution -> {
//                            // Create a copy of the solution with only matching quality requirements
//                            ArchitectureSolution filteredSolution = new ArchitectureSolution();
//                            BeanUtils.copyProperties(solution, filteredSolution);
//
//                            // Filter QualityRequirementTechnology entries
//                            List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies()
//                                    .stream()
//                                    .filter(qrt -> qrt.getQualityRequirement().getName()
//                                            .equals(qualityCombo.getValue().name()))
//                                    .collect(Collectors.toList());
//
//                            filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
//                            return filteredSolution;
//                        })
//                        .collect(Collectors.toList());
//            }
//
//            // Filter by Technology if needed
//            if (technologiesCombo != null && technologiesCombo.getValue() != null) {
//                filteredSolutions = filteredSolutions.stream()
//                        .filter(solution -> solution.getTechnologies().stream()
//                                .anyMatch(tech -> tech.getDescription()
//                                        .equals(technologiesCombo.getValue().description())))
//                        .map(solution -> {
//                            // Create a copy of the solution with only matching technologies
//                            ArchitectureSolution filteredSolution = new ArchitectureSolution();
//                            BeanUtils.copyProperties(solution, filteredSolution);
//
//                            // Filter QualityRequirementTechnology entries
//                            List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies()
//                                    .stream()
//                                    .filter(qrt -> qrt.getTechnology().getDescription()
//                                            .equals(technologiesCombo.getValue().description()))
//                                    .collect(Collectors.toList());
//
//                            filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
//                            return filteredSolution;
//                        })
//                        .collect(Collectors.toList());
//            }
//
//            // Update the domain with filtered solutions
//            domain.setArchitectureSolutions(filteredSolutions);
//        });
//
//        // Remove domains that have no matching solutions after filtering
//        filteredDomains = filteredDomains.stream()
//                .filter(domain -> !domain.getArchitectureSolutions().isEmpty())
//                .collect(Collectors.toList());
//
//        // Update the tree view with filtered results
//        this.treeView.setTreeViewData(filteredDomains);
//    }

    /***
     * Creates the search panel at the bottom of the page
     */
//    private void createDetailSliderPanel() {
//        sliderPanel.setButtonTexts("Hide Details", "Show Details");
//        sliderPanel.setExpanded(false);
//    }

    @EventListener
    public void handleDataDetailsUpdate(DataDetailsUpdateEvent event) {
        sliderPanel.setDataDetailsContent(event.getDataDetails());
    }
}
