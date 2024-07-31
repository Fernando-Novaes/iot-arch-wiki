package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import java.util.Collections;
import java.util.List;

@PageTitle("IoT-Arch - Data Manager")
@Route(value = "datamanager-view", layout = MainLayout.class)
public class DataManagerView extends BaseView {

    private final IoTDomainService domainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final PaperReferenceService paperReferenceService;
    private final TechnologyService technologyService;

    @Autowired
    public DataManagerView(IoTDomainService domainService, ArchitectureSolutionService architectureSolutionService, QualityRequirementService qualityRequirementService, PaperReferenceService paperReferenceService, TechnologyService technologyService) {
        this.domainService = domainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.paperReferenceService = paperReferenceService;
        this.technologyService = technologyService;

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("Data Manager");
        this.createTabLayout();
    }

    private Span createBadge(String text) {
        Span span = new Span(text);
        span.getElement().getThemeList().add("badge pill small contrast");
        span.getStyle().set("margin-inline-start", "var(--lumo-space-s)");

        return span;
    }

    private void createTabLayout() {
        Tabs tabs = createTabs();

        GridCrud<IoTDomain> gridDomains = createIoTDomainGridCrud();
        GridCrud<ArchitectureSolution> gridArchs = createArchitectureSolutionGridCrud();
        GridCrud<QualityRequirement> gridQualityRequirements = createQualityRequirementGridCrud();
        GridCrud<Technology> gridTechs = createTechnolgyGridCrud();
        GridCrud<PaperReference> gridPapers = createPaperReferenceGridCrud();

        Div contentContainer = new Div();
        contentContainer.setSizeFull();
        contentContainer.add(gridDomains);

        // Add a listener to switch the content when the tab changes
        tabs.addSelectedChangeListener(event -> {
            contentContainer.removeAll();
            Component selectedContent = null;
            switch (tabs.getSelectedIndex()) {
                case 0:
                    selectedContent = gridDomains;
                    break;
                case 1:
                    selectedContent = gridArchs;
                    break;
                case 2:
                    selectedContent = gridQualityRequirements;
                    break;
                case 3:
                    selectedContent = gridTechs;
                    break;
                case 4:
                    selectedContent = gridPapers;
                    break;
            }
            contentContainer.add(selectedContent);
        });

        getContent().add(tabs, contentContainer);
    }

    /***
     * Creates all CRUD tabs
     * @return Tabs
     */
    private Tabs createTabs() {
        Tab domains = new Tab(new Span("IoT Domains"), this.createBadge(String.valueOf(this.domainService.findAll().size())));
        Tab archs = new Tab(new Span("Arch. Solutions"), this.createBadge(String.valueOf(this.architectureSolutionService.findAll().size())));
        Tab qrs = new Tab(new Span("Quality Requirements"), this.createBadge(String.valueOf(this.qualityRequirementService.findAll().size())));
        Tab techs = new Tab(new Span("Technologies"), this.createBadge(String.valueOf(this.technologyService.findAll().size())));
        Tab papers = new Tab(new Span("References"), this.createBadge(String.valueOf(this.paperReferenceService.findAll().size())));

        Tabs tabs = new Tabs(domains, archs, qrs, techs, papers);

        return tabs;
    }

    /***
     * Creates the Grid CRUD to the Technology data
     * @return GridCrud<Technology>
     */
    private GridCrud<Technology> createTechnolgyGridCrud() {
        GridCrud<Technology> gridTechs = new GridCrud<>(Technology.class);
        gridTechs.setSizeFull();
        gridTechs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridTechs.getGrid().getColumnByKey("description").setAutoWidth(true);
        gridTechs.getCrudFormFactory().setVisibleProperties("id","description","architectureSolution", "qualityRequirement", "technology");
        gridTechs.getGrid().setDetailsVisibleOnClick(true);
        gridTechs.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridTechs.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridTechs.setAddOperation(this.technologyService::saveAndFlush);
        gridTechs.setUpdateOperation(this.technologyService::saveAndUpdate);

        ComboBox<ArchitectureSolution> comboBoxArch = new ComboBox<ArchitectureSolution>("Archs", this.architectureSolutionService.findAll());
        ComboBox<QualityRequirement> comboBoxQR = new ComboBox<QualityRequirement>("Archs", Collections.emptyList());
        comboBoxQR.setEnabled(false);

        comboBoxArch.addValueChangeListener(event -> {
            ArchitectureSolution selectedArchSolution = event.getValue();
            if (selectedArchSolution != null) {
                List<QualityRequirement> qualityRequirements = this.qualityRequirementService.findAllByArchitectureSolution(selectedArchSolution);
                comboBoxQR.setItems(qualityRequirements);
                comboBoxQR.setEnabled(true);
            } else {
                comboBoxQR.clear();
                comboBoxQR.setItems(Collections.emptyList());
                comboBoxQR.setEnabled(false);
            }
        });

        gridTechs.getCrudFormFactory().setFieldProvider("architectureSolution", i -> comboBoxArch);
        gridTechs.getCrudFormFactory().setFieldProvider("qualityRequirement", i -> comboBoxQR);

        // additional components
        TextField filter = this.createGridTextFilter(gridTechs,"Filter by Architectural Solution Name","400px");
        gridTechs.setFindAllOperation( () -> this.technologyService.findByArchitectureSolutionName(filter.getValue()));

        return gridTechs;
    }

    /***
     * Creates the filter for the GridCRUD
     * @param gridCRUD the Grid to insert the filter
     * @param filterText Text to be shown in the filter
     * @param textFieldSize the ize of the filter field
     * @return TextField
     */
    private TextField createGridTextFilter(GridCrud<?> gridCRUD, String filterText, String textFieldSize) {
        TextField filter = new TextField();
        filter.setPlaceholder(filterText);
        filter.setClearButtonVisible(true);
        filter.setMinWidth(textFieldSize);
        gridCRUD.getCrudLayout().addFilterComponent(filter);
        filter.addValueChangeListener(e -> gridCRUD.refreshGrid());

        return filter;
    }

    /***
     * Creates the Grid CRUD to the PaperReference data
     * @return GridCrud<PaperReference>
     */
    private GridCrud<PaperReference> createPaperReferenceGridCrud() {
        GridCrud<PaperReference> gridPapers = new GridCrud<>(PaperReference.class);
        gridPapers.setSizeFull();
        gridPapers.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridPapers.getGrid().getColumnByKey("paperTitle").setAutoWidth(true);
        gridPapers.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridPapers.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridPapers.setAddOperation(this.paperReferenceService::saveAndFlush);
        gridPapers.setFindAllOperation(this.paperReferenceService::findAll);
        gridPapers.setUpdateOperation(this.paperReferenceService::saveAndUpdate);

        // additional components
        TextField filter = this.createGridTextFilter(gridPapers,"Filter by Paper Title","400px");
        gridPapers.setFindAllOperation( () -> this.paperReferenceService.findByPaperReferenceTitle(filter.getValue()));

        return gridPapers;
    }

    private GridCrud<QualityRequirement> createQualityRequirementGridCrud() {
        GridCrud<QualityRequirement> gridQualityRequirements = new GridCrud<>(QualityRequirement.class);
        gridQualityRequirements.setSizeFull();
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties("id","name","technology", "architectureSolution");
        gridQualityRequirements.getGrid().setDetailsVisibleOnClick(true);
        gridQualityRequirements.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridQualityRequirements.setAddOperation(this.qualityRequirementService::saveAndFlush);
        gridQualityRequirements.setFindAllOperation(this.qualityRequirementService::findAll);
        gridQualityRequirements.setUpdateOperation(this.qualityRequirementService::saveAndUpdate);
        gridQualityRequirements.setDeleteOperation(this.qualityRequirementService::delete);

        ComboBox<QualityRequirement> comboBoxQR = new ComboBox<QualityRequirement>("Name", this.qualityRequirementService.listAllByNameDistinct());
        comboBoxQR.setAllowCustomValue(true);
        gridQualityRequirements.getCrudFormFactory().setFieldProvider("Name", qr -> comboBoxQR);

        gridQualityRequirements.getCrudFormFactory().setFieldProvider("architectureSolution",
                new ComboBoxProvider<ArchitectureSolution>("Arch", this.architectureSolutionService.findAll()));

        // additional components
        TextField filter = this.createGridTextFilter(gridQualityRequirements,"Filter by Quality Requirement","250px");
        gridQualityRequirements.setFindAllOperation( () -> this.qualityRequirementService.findAllByName(filter.getValue()));

        return gridQualityRequirements;
    }

    private GridCrud<ArchitectureSolution> createArchitectureSolutionGridCrud() {
        GridCrud<ArchitectureSolution> gridArchs = new GridCrud<>(ArchitectureSolution.class);
        gridArchs.setSizeFull();
        gridArchs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridArchs.getGrid().getColumnByKey("name").setAutoWidth(true);
        gridArchs.setAddOperation(this.architectureSolutionService::saveAndFlush);
        gridArchs.setFindAllOperation(this.architectureSolutionService::findAll);
        gridArchs.setUpdateOperation(this.architectureSolutionService::saveAndUpdate);
        gridArchs.getCrudFormFactory().setVisibleProperties("id", "name", "paperReference", "qrs", "technologies");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name", "paperReference");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name", "paperReference");
        gridArchs.getCrudFormFactory().setFieldProvider("paperReference",
                new ComboBoxProvider<PaperReference>("Reference", this.paperReferenceService.findAll()));

        // additional components
        TextField filter = this.createGridTextFilter(gridArchs,"Filter by Architectural Solution Name","400px");
        gridArchs.setFindAllOperation( () -> this.architectureSolutionService.findByNameContainingIgnoreCase(filter.getValue()));
        return gridArchs;
    }

    private GridCrud<IoTDomain> createIoTDomainGridCrud() {
        GridCrud<IoTDomain> gridDomains = new GridCrud<>(IoTDomain.class);
        gridDomains.setSizeFull();
        gridDomains.getCrudFormFactory().setVisibleProperties("name","archs");
        gridDomains.getCrudFormFactory().setFieldProvider("archs",
                new CheckBoxGroupProvider<ArchitectureSolution>("Arch. Solutions", this.architectureSolutionService.findAll(),
                        ArchitectureSolution::getName));
        gridDomains.setAddOperation(this.domainService::saveAndFlush);
        gridDomains.setFindAllOperation(this.domainService::findAll);
        gridDomains.setUpdateOperation(this.domainService::saveAndUpdate);
        return gridDomains;
    }
}
