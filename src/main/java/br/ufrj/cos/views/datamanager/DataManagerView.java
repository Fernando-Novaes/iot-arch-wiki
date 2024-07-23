package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.PaperReference;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.repository.ArchitectureSolutionRepository;
import br.ufrj.cos.repository.IoTDomainRepository;
import br.ufrj.cos.repository.QualityRequirementRepository;
import br.ufrj.cos.service.ArchitectureSolutionService;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.PaperReferenceService;
import br.ufrj.cos.service.QualityRequirementService;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.CrudFormFactory;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

@PageTitle("IoTArch - Data Manager View")
@Route(value = "datamanager-view", layout = MainLayout.class)
public class DataManagerView extends BaseView {

    private final IoTDomainService domainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final PaperReferenceService paperReferenceService;

    @Autowired
    public DataManagerView(IoTDomainService domainService, ArchitectureSolutionService architectureSolutionService, QualityRequirementService qualityRequirementService, PaperReferenceService paperReferenceService) {
        this.domainService = domainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.paperReferenceService = paperReferenceService;

        getContent().setWidth("100%");
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
        Tab papers = new Tab(new Span("References"), this.createBadge(String.valueOf(this.paperReferenceService.findAll().size())));

        Tabs tabs = new Tabs(domains, archs, qrs, papers);
        tabs.setSizeFull();

        GridCrud<IoTDomain> gridDomains = new GridCrud<>(IoTDomain.class);
        gridDomains.setSizeFull();
        gridDomains.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridDomains.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");

        gridDomains.getCrudFormFactory().setFieldProvider("archs",
                new CheckBoxGroupProvider<>("Arch. Solutions", this.architectureSolutionService.findAll(),
                        ArchitectureSolution::getName));
        gridDomains.setAddOperation(this.domainService::saveAndFlush);
        gridDomains.setFindAllOperation(this.domainService::findAll);

        GridCrud<ArchitectureSolution> gridArchs = new GridCrud<>(ArchitectureSolution.class);
        gridArchs.setSizeFull();
        gridArchs.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridArchs.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridArchs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridArchs.getGrid().getColumnByKey("name").setAutoWidth(true);
        gridArchs.setAddOperation(this.architectureSolutionService::saveAndFlush);
        gridArchs.setFindAllOperation(this.architectureSolutionService::findAll);

        GridCrud<QualityRequirement> gridQualityRequirements = new GridCrud<>(QualityRequirement.class);
        gridQualityRequirements.setSizeFull();
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridQualityRequirements.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridQualityRequirements.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridQualityRequirements.setAddOperation(this.qualityRequirementService::saveAndFlush);
        gridQualityRequirements.setFindAllOperation(this.qualityRequirementService::findAll);

        GridCrud<PaperReference> gridPapers = new GridCrud<>(PaperReference.class);
        gridPapers.setSizeFull();
        gridPapers.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridPapers.getGrid().getColumnByKey("paperTitle").setAutoWidth(true);
        gridPapers.getCrudFormFactory().setDisabledProperties(CrudOperation.ADD, "id");
        gridPapers.getCrudFormFactory().setDisabledProperties(CrudOperation.UPDATE, "id");
        gridPapers.setAddOperation(this.paperReferenceService::saveAndFlush);
        gridPapers.setFindAllOperation(this.paperReferenceService::findAll);

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
                    selectedContent = gridPapers;
                    break;
            }
            contentContainer.add(selectedContent);
        });

        getContent().add(tabs, contentContainer);
    }
}
