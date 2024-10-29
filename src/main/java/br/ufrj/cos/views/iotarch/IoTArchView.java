package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.components.treeview.TreeRootSelectionComponent;
import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.ArchitectureSolutionService;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.QualityRequirementService;
import br.ufrj.cos.service.TechnologyService;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("IoT-Arch Knowledge Base")
@Route(value = "iot-arch-view", layout = MainLayout.class)
public class IoTArchView extends BaseView {

    private final TreeViewComponent treeView;
    private final TreeRootSelectionComponent treeRootSelection;
    private final SliderPanel sliderPanel;

    //JPAs
    private final IoTDomainService ioTDomainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final TechnologyService technologyService;

    private enum ActionType {
        NONE, IOTDOMAIN, ARCHITECTURESOLUTION, QUALITYREQUIREMENT, TECHNOLOGY
    }
    private ActionType currentAction  = ActionType.NONE;

    ComboBox<IoTDomainRecord> iotDomainCombo;
    ComboBox<ArchitectureSolutionRecord> architectureCombo;
    ComboBox<QualityRequirementRecord> qualityCombo;
    ComboBox<TechnologyRecord> technologiesCombo;
    List<? extends DomainBase> treeViewDataSource;
    HorizontalLayout comboBoxLayout = new HorizontalLayout();
    Button searchButton = new Button("Search");
    Button cancelButton = new Button("Cancel");

    List<IoTDomainRecord> domains;
    List<ArchitectureSolutionRecord> archs;
    List<QualityRequirementRecord> qrs;
    List<TechnologyRecord> techs;

    @Autowired
    public IoTArchView(TreeViewComponent treeView, TreeRootSelectionComponent treeRootSelection, SliderPanel sliderPanel,
                       IoTDomainService ioTDomainService,
                       ArchitectureSolutionService architectureSolutionService,
                       QualityRequirementService qualityRequirementService,
                       TechnologyService technologyService) {
        this.treeView = treeView;
        this.treeRootSelection = treeRootSelection;
        this.sliderPanel = sliderPanel;
        this.ioTDomainService = ioTDomainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;

        this.treeRootSelection.addChangeLeftButtonClickListener(l -> { this.changeRootLeft(this.treeRootSelection.getTreeViewType()); });
        this.treeRootSelection.addChangeRightButtonClickListener(r -> { this.changeRootRight(this.treeRootSelection.getTreeViewType()); });

        this.treeView.addTreeRootSelection(this.treeRootSelection);
        this.treeView.load();
        this.treeViewDataSource = this.treeView.getTreeViewData();

        this.createHeader("IoT-Arch Knowledge Base");
        getContent().add(this.createSearchDiv(), treeView);
        this.loadDataToComboBoxes(ActionType.NONE);

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
    }

    /***
     *
     * @param type
     */
    private void changeRootLeft(TreeViewType type) {
        try {
            getContent().remove(this.treeView);
        } catch (Exception e) {}

        switch (type) {
            case IoTDomain: {
                this.treeRootSelection.setTreeViewType(TreeViewType.QualityRequirement);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.QualityRequirement);
                return;
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.IoTDomain);
                return;
            }
            case QualityRequirement, Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.ArchitectureSolution);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.ArchitectureSolution);
                return;
            }
        }
    }

    /***
     *
     * @param type
     */
    private void changeRootRight(TreeViewType type) {
        try {
            getContent().remove(this.treeView);
        } catch (Exception e) {}

        switch (type) {
            case IoTDomain: {
                this.treeRootSelection.setTreeViewType(TreeViewType.ArchitectureSolution);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.ArchitectureSolution);
                return;
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.QualityRequirement);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.QualityRequirement);
                return;
            }
            case QualityRequirement, Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
                this.treeView.load();
                getContent().add(treeView);
                //this.prepareComboBoxLayout(TreeViewType.IoTDomain);
                return;
            }
        }
    }

    private Div createSearchDiv() {
        // Main container for vertical centering
        Div centerContainer = new Div();
        centerContainer.getStyle()
                .set("display", "flex")
                //.set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                //.set("min-height", "3vh")
                .set("width", "100%");

        // Main content layout with dark theme - now wider
        VerticalLayout mainLayout = new VerticalLayout();
        //mainLayout.setSpacing(true);
        //mainLayout.setPadding(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        mainLayout.getStyle()
                .set("background-color", "#373a3f")
                .set("border", "1px solid #4a4d52")
                .set("border-radius", "12px")
                //.set("padding", "2em")
                //.set("margin", "0 auto")
                .set("width", "100%") // Increased width
                .set("max-width", "1800px") // Increased max-width
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)");

        H3 searchTitle = new H3("- Search on Knowledge Base -");
        searchTitle.getStyle()
                .set("color", "#e0e0e0")
                .set("font-size", "1.3rem")
                //.set("margin", "0 0 2em 0")
                .set("shadow", "0 4px 8px rgba(0, 0, 0, 0.2)")
                .set("text-align", "center");

        // Create ComboBoxes with consistent styling - narrower width for inline layout
        this.iotDomainCombo = new ComboBox<>("IoT Domain");
        this.prepareComboBox(iotDomainCombo, ActionType.IOTDOMAIN);
        //iotDomainCombo.setItems(this.ioTDomainService.findAllOrderByName());

        this.architectureCombo = new ComboBox<>("Architecture Solution");
        this.prepareComboBox(architectureCombo, ActionType.ARCHITECTURESOLUTION);
        //architectureCombo.setItems(this.architectureSolutionService.findAllOrderedByName());

        this.qualityCombo = new ComboBox<>("Quality Requirement");
        this.prepareComboBox(qualityCombo, ActionType.QUALITYREQUIREMENT);
        //qualityCombo.setItems(this.qualityRequirementService.findAllOrderedByName());

        this.technologiesCombo = new ComboBox<>("Technologies");
        this.prepareComboBox(technologiesCombo, ActionType.TECHNOLOGY);
        //technologiesCombo.setItems(this.technologyService.findAllOrderedByDescription());

        this.prepareComboBoxLayout(TreeViewType.IoTDomain);

        // Configure the horizontal layout
        comboBoxLayout.setSpacing(true);
        comboBoxLayout.setPadding(false);
        comboBoxLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        comboBoxLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        comboBoxLayout.getStyle()
                .set("flex-wrap", "nowrap")
                .set("gap", "1em")
                .set("width", "100%")
                .set("position", "relative") // Added for stacking context
                .set("z-index", "1") // Ensure proper stacking
                .set("overflow", "visible"); // Allow dropdowns to overflow

        // Add components to main layout
        mainLayout.add(
                comboBoxLayout
        );

        centerContainer.add(mainLayout);
        return centerContainer;
    }

    private void prepareComboBoxLayout(TreeViewType treeViewType) {
        // Create search button
        searchButton.setIcon(VaadinIcon.SEARCH.create());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(click -> {
            cancelButton.setVisible(true);
            this.treeView.setIsFiltering(true);
            this.filterTreeViewDataSource();
            this.treeView.load();
        });

        // Create cancel button
        cancelButton.setIcon(VaadinIcon.CLOSE_CIRCLE_O.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setVisible(false);
        cancelButton.addClickListener(click -> {
            this.currentAction = ActionType.NONE;
            cancelButton.setVisible(false);
            this.loadDataToComboBoxes(ActionType.NONE);
            this.treeView.setIsFiltering(false);
        });

        comboBoxLayout.add(
                this.iotDomainCombo,
                this.architectureCombo,
                this.qualityCombo,
                this.technologiesCombo,
                searchButton,
                cancelButton
        );
    }

    /***
     * Sets the styles and the config for exhibition list
     * @param comboBox
     */
    private void prepareComboBox(ComboBox comboBox, ActionType actionType) {
        comboBox.clear();

        comboBox.setPlaceholder("All " + comboBox.getLabel());
        comboBox.setWidth("300px");
        comboBox.setClearButtonVisible(false);
        comboBox.setLabel(null);

        comboBox.getStyle()
                .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.1)")
                .set("--lumo-body-text-color", "#ffffff")
                .set("--lumo-secondary-text-color", "#e0e0e0")
                .set("--lumo-primary-text-color", "#ffffff")
                .set("margin", "0 0.5em")
                .set("position", "relative") // Added for stacking context
                .set("z-index", "5"); // Higher than container

        // Add custom style to ensure overlay appears on top
        comboBox.addAttachListener(event -> {
            event.getSource().getElement().executeJs(
                    "this.style.setProperty('--vaadin-overlay-viewport-bottom', 'auto');" +
                            "this.style.setProperty('--overlay-box-shadow', '0 2px 8px rgba(0, 0, 0, 0.2)');"
            );
        });

        // Set overlay theme to ensure proper rendering
        comboBox.getElement().getThemeList().add("custom-overlay");

        // Setting the action when a value is changed in a combobox
        // This will make the changes on the others comboboxes
        comboBox.addValueChangeListener(listener -> {
            this.currentAction = actionType;
            this.loadDataToComboBoxes(this.currentAction);
            cancelButton.setVisible(true);
        });
    }

    private void loadDataToComboBoxes(ActionType actionType) {

        if (actionType == ActionType.NONE) {
            List<IoTDomainRecord> list = new ArrayList<>();
            ((List<IoTDomain>)this.treeViewDataSource).stream().forEach(
                    d -> {
                        list.add(new IoTDomainRecord(d.getName()));
                    }
            );

            list.sort(Comparator.comparing(IoTDomainRecord::name));
            this.iotDomainCombo.setItems(list);
        } else if (actionType == ActionType.IOTDOMAIN) {
            List<ArchitectureSolutionRecord> list = new ArrayList<>();
            ((List<IoTDomain>)this.treeViewDataSource).stream().filter(d -> d.getName().equals(this.iotDomainCombo.getValue().name())).forEach(
                    d -> {
                        d.getArchs().stream().distinct().forEach(a -> {
                            list.add(new ArchitectureSolutionRecord(a.getName()));
                        });
                    }
            );

            list.sort(Comparator.comparing(ArchitectureSolutionRecord::name));
            this.architectureCombo.setItems(list.stream().distinct().toList());
        } else if (actionType == ActionType.ARCHITECTURESOLUTION) {
            List<QualityRequirementRecord> list = new ArrayList<>();
            ((List<IoTDomain>)this.treeViewDataSource).stream().filter(d -> d.getName().equals(this.iotDomainCombo.getValue().name())).forEach(
                    d -> {
                        d.getArchs().stream().filter(a -> a.getName().equals(architectureCombo.getValue().name())).distinct().forEach(a -> {
                            a.getQrs().stream().distinct().forEach(aq -> {
                                list.add(new QualityRequirementRecord(aq.getName()));
                            });
                        });
                    }
            );

            list.sort(Comparator.comparing(QualityRequirementRecord::name));
            this.qualityCombo.setItems(list.stream().distinct().toList());
        } else if (actionType == ActionType.QUALITYREQUIREMENT) {
            List<TechnologyRecord> list = new ArrayList<>();
            ((List<IoTDomain>)this.treeViewDataSource).stream().filter(d -> d.getName().equals(this.iotDomainCombo.getValue().name())).forEach(
                    d -> {
                        d.getArchs().stream().filter(a -> a.getName().equals(architectureCombo.getValue().name())).distinct().forEach(a -> {
                            a.getQrs().stream().filter(qr -> qr.getName().equals(qualityCombo.getValue().name())).distinct().forEach(aq -> {
                                list.add(new TechnologyRecord(aq.getTechnology().getDescription()));
                            });
                        });
                    }
            );

            list.sort(Comparator.comparing(TechnologyRecord::description));
            this.technologiesCombo.setItems(list.stream().distinct().toList());
        }
    }

    private void filterTreeViewDataSource() {
        IoTDomain domain = new IoTDomain();

        if (iotDomainCombo.getValue() != null) {
            domain = ((List<IoTDomain>)this.treeViewDataSource).stream().filter(d -> d.getName().equals(this.iotDomainCombo.getValue().name())).findFirst().get();
        }

        if (architectureCombo.getValue() != null) {
            domain.setArchs(this.architectureSolutionService.findAllByIoTDomain(domain));
            domain.setArchs(
                    domain.getArchs().stream().filter(arch -> arch.getName().equals(architectureCombo.getValue().name())).collect(Collectors.toList()));
        }

        IoTDomain finalDomain = domain;
        if(qualityCombo.getValue() != null) {

            domain.getArchs().forEach(arc -> {
                arc.getQrs().forEach(qr -> {
                    if (!qr.getName().equals(qualityCombo.getValue().name())) {
                        finalDomain.getArchs().stream().filter(arc2 -> arc2.getName().equals(arc.getName())).forEach(a -> {
                            a.getQrs().remove(qr);
                        });
                    }
                });
            });

        }

        List<IoTDomain> list = new ArrayList<>();
        list.add(finalDomain);
        this.treeView.setTreeViewData(list);
    }

    /***
     * Creates the search panel at the bottom of the page
     */
//    private void createSearchSliderPanel() {
//        this.sliderPanel.setButtonTexts("Hide Search Panel", "Show Search Panel");
//        this.sliderPanel.setHeight("40%");
//        this.sliderPanel.setContent(this.createSearchBox());
//        this.updateComboBoxesData();
//
//        getContent().add(this.sliderPanel);
//    }


}
