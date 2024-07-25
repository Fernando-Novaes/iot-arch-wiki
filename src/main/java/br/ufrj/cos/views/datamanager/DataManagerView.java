package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

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
        Tab domains = new Tab(new Span("IoT Domains"), this.createBadge(String.valueOf(this.domainService.findAll().size())));
        Tab archs = new Tab(new Span("Arch. Solutions"), this.createBadge(String.valueOf(this.architectureSolutionService.findAll().size())));
        Tab qrs = new Tab(new Span("Quality Requirements"), this.createBadge(String.valueOf(this.qualityRequirementService.findAll().size())));
        Tab techs = new Tab(new Span("Technologies"), this.createBadge(String.valueOf(this.technologyService.findAll().size())));
        Tab papers = new Tab(new Span("References"), this.createBadge(String.valueOf(this.paperReferenceService.findAll().size())));

        Tabs tabs = new Tabs(domains, archs, qrs, techs, papers);

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
        gridTechs.setFindAllOperation(this.technologyService::findAll);
        gridTechs.setUpdateOperation(this.technologyService::saveAndUpdate);
        gridTechs.getCrudFormFactory().setFieldProvider("architectureSolution",
                new ComboBoxProvider<ArchitectureSolution>("Archs", this.architectureSolutionService.findAll()));
        gridTechs.getCrudFormFactory().setFieldProvider("qualityRequirement",
                new ComboBoxProvider<QualityRequirement>("QRs", this.qualityRequirementService.findAll()));
        return gridTechs;
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
        return gridPapers;
    }

    private GridCrud<QualityRequirement> createQualityRequirementGridCrud() {
        GridCrud<QualityRequirement> gridQualityRequirements = new GridCrud<>(QualityRequirement.class);
        gridQualityRequirements.setSizeFull();
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties("id","name");
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties("architectureSolutions","technologies");
        gridQualityRequirements.getGrid().setDetailsVisibleOnClick(true);
        gridQualityRequirements.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridQualityRequirements.setAddOperation(this.qualityRequirementService::saveAndFlush);
        gridQualityRequirements.setFindAllOperation(this.qualityRequirementService::findAll);
        gridQualityRequirements.setUpdateOperation(this.qualityRequirementService::saveAndUpdate);
//        gridQualityRequirements.getCrudFormFactory().setFieldProvider("architectureSolutions",
//                new ComboBoxProvider<ArchitectureSolution>("Archs", this.architectureSolutionService.findAll()));
        return gridQualityRequirements;
    }

    private GridCrud<ArchitectureSolution> createArchitectureSolutionGridCrud() {
        GridCrud<ArchitectureSolution> gridArchs = new GridCrud<>(ArchitectureSolution.class);
        gridArchs.setSizeFull();
        gridArchs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridArchs.getGrid().getColumnByKey("name").setAutoWidth(true);
        //gridArchs.getGrid().getColumnByKey("qrs").setAutoWidth(true);
        gridArchs.setAddOperation(this.architectureSolutionService::saveAndFlush);
        gridArchs.setFindAllOperation(this.architectureSolutionService::findAll);
        gridArchs.setUpdateOperation(this.architectureSolutionService::saveAndUpdate);
        gridArchs.getCrudFormFactory().setFieldProvider("paperReference",
                new ComboBoxProvider<PaperReference>("Reference", this.paperReferenceService.findAll()));
//        gridArchs.getCrudFormFactory().setFieldProvider("qrs",
//                new CheckBoxGroupProvider<QualityRequirement>("QRs", this.qualityRequirementService.findAll(),
//                        QualityRequirement::getName));
        gridArchs.getCrudFormFactory().setVisibleProperties("name","paperReference");
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
