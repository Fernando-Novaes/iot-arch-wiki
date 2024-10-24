package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.components.treeview.TreeRootSelectionComponent;
import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.components.treeview.TreeViewType;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.service.ArchitectureSolutionService;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.QualityRequirementService;
import br.ufrj.cos.service.TechnologyService;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
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

@PageTitle("IoT-Arch Wiki - Tree")
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

        this.createHeader("IoT-Arch Knowledge Base");
        getContent().add(treeView);
        this.treeView.addTreeRootSelection(this.treeRootSelection);
        //this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
        this.treeView.load();

        this.createSearchSliderPanel();

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
                return;
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
                this.treeView.load();
                getContent().add(treeView);
                return;
            }
            case QualityRequirement, Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.ArchitectureSolution);
                this.treeView.load();
                getContent().add(treeView);
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
                return;
            }
            case ArchitectureSolution: {
                this.treeRootSelection.setTreeViewType(TreeViewType.QualityRequirement);
                this.treeView.load();
                getContent().add(treeView);
                return;
            }
            case QualityRequirement, Technology: {
                this.treeRootSelection.setTreeViewType(TreeViewType.IoTDomain);
                this.treeView.load();
                getContent().add(treeView);
                return;
            }
        }
    }

    private Div createSearchBox() {
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
                .set("width", "95%") // Increased width
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
        ComboBox<IoTDomain> iotDomainCombo = new ComboBox<>("IoT Domain");
        this.setStyleForComboBox(iotDomainCombo);
        iotDomainCombo.setItems(this.ioTDomainService.findAllOrderByName());

        ComboBox<ArchitectureSolution> architectureCombo = new ComboBox<>("Architecture Solution");
        this.setStyleForComboBox(architectureCombo);
        architectureCombo.setItems(this.architectureSolutionService.findAllOrderedByName());

        ComboBox<QualityRequirement> qualityCombo = new ComboBox<>("Quality Requirement");
        this.setStyleForComboBox(qualityCombo);
        qualityCombo.setItems(this.qualityRequirementService.findAllOrderedByName());

        ComboBox<Technology> technologiesCombo = new ComboBox<>("Technologies");
        this.setStyleForComboBox(technologiesCombo);
        technologiesCombo.setItems(this.technologyService.findAllOrderedByDescription());

        // Create search button
        Button searchButton = new Button("Search");
        searchButton.setIcon(VaadinIcon.SEARCH.create());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Single horizontal layout for all combo boxes
        HorizontalLayout comboBoxLayout = new HorizontalLayout(
                iotDomainCombo,
                architectureCombo,
                qualityCombo,
                technologiesCombo,
                searchButton
        );

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
                searchTitle,
                comboBoxLayout
        );

        centerContainer.add(mainLayout);
        return centerContainer;
    }

    /***
     * Sets the styles and the config for exhibition list
     * @param comboBox
     */
    private void setStyleForComboBox(ComboBox comboBox) {
        comboBox.setPlaceholder("Select " + comboBox.getLabel());
        comboBox.setWidth("300px");
        comboBox.setClearButtonVisible(true);
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
    }

    /***
     * Creates the search panel at the bottom of the page
     */
    private void createSearchSliderPanel() {
        this.sliderPanel.setButtonTexts("Hide Search Panel", "Show Search Panel");
        this.sliderPanel.setHeight("40%");
        this.sliderPanel.setContent(this.createSearchBox());

        getContent().add(this.sliderPanel);
    }
}
