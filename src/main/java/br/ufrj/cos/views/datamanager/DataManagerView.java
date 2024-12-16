package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.utils.GridCRUDUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("IoT-Arch - Data Manager")
@Route(value = "datamanager-view", layout = MainLayout.class)
public class DataManagerView extends BaseView {

    private final IoTDomainService domainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final PaperReferenceService paperReferenceService;
    private final TechnologyService technologyService;

    private final KnowledegeDataMananger knowledgeDataMananger;

    GridCrud<IoTDomain> gridDomains;
    GridCrud<ArchitectureSolution> gridArchs;
    GridCrud<QualityRequirement> gridQualityRequirements;
    GridCrud<Technology> gridTechs;
    GridCrud<PaperReference> gridPapers;

    Tabs tabs = new Tabs();

    ComboBox<IoTDomain> iotDomainRegisterCombo = new ComboBox<>();
    ComboBox<PaperReference> paperReferenceRegisterCombo = new ComboBox<>();
    ComboBox<QualityRequirement> qualityRequirementRegisterCombo = new ComboBox<>();
    ComboBox<Technology> technologyRegisterCombo = new ComboBox<>();

    ArchitectureSolution architectureSolution = new ArchitectureSolution();

    @Autowired
    public DataManagerView(IoTDomainService domainService, ArchitectureSolutionService architectureSolutionService, QualityRequirementService qualityRequirementService, PaperReferenceService paperReferenceService, TechnologyService technologyService, KnowledegeDataMananger knowledgeDataMananger) {
        this.domainService = domainService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.paperReferenceService = paperReferenceService;
        this.technologyService = technologyService;
        this.knowledgeDataMananger = knowledgeDataMananger;
        this.knowledgeDataMananger.setArchitectureSolution(this.architectureSolution);

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("Knowledge Manager");

        tabs = createTabs();

        gridDomains = createIoTDomainGridCrud();
        gridArchs = createArchitectureSolutionGridCrud();
        gridQualityRequirements = createQualityRequirementGridCrud();
        gridTechs = createTechnolgyGridCrud();
        gridPapers = createPaperReferenceGridCrud();

        this.createTabLayout();
    }

    private Span createBadge(String text) {
        Span span = new Span(text);
        span.getElement().getThemeList().add("badge pill small contrast");
        span.getStyle().set("margin-inline-start", "var(--lumo-space-s)");

        return span;
    }

    private void createTabLayout() {
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
                case 5:
                    selectedContent = this.knowledgeDataMananger.createRegisterCrud();
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
        Tab archs = new Tab(new Span("Architectures"), this.createBadge(String.valueOf(this.architectureSolutionService.findAll().size())));
        Tab qrs = new Tab(new Span("Quality Requirements"), this.createBadge(String.valueOf(this.qualityRequirementService.findAll().size())));
        Tab techs = new Tab(new Span("Technologies"), this.createBadge(String.valueOf(this.technologyService.findAll().size())));
        Tab papers = new Tab(new Span("References"), this.createBadge(String.valueOf(this.paperReferenceService.findAll().size())));
        Tab registers = new Tab(new Span("Knowledge"), this.createBadge(String.valueOf(this.architectureSolutionService.findAll().stream().distinct().toList().size())));

        Tabs tabs = new Tabs(domains, archs, qrs, techs, papers, registers);

        return tabs;
    }

    /***
     * Refreshes all data after CRUD operations
     */
    private void refreshAllData() {
        gridDomains.getGrid().getDataProvider().refreshAll();
        gridArchs.getGrid().getDataProvider().refreshAll();
        gridQualityRequirements.getGrid().getDataProvider().refreshAll();
        gridTechs.getGrid().getDataProvider().refreshAll();
        gridPapers.getGrid().getDataProvider().refreshAll();
    }

    /***
     * Creates the Grid CRUD to the Technology data
     * @return GridCrud<Technology>
     */
    private GridCrud<Technology> createTechnolgyGridCrud() {
        GridCrud<Technology> gridTechs = new GridCrud<>(Technology.class);
        gridTechs.setSizeFull();
//        gridTechs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridTechs.getGrid().getColumnByKey("description").setAutoWidth(true);
        gridTechs.getCrudFormFactory().setVisibleProperties("architectureSolution", "qualityRequirement", "description");
        gridTechs.getGrid().setDetailsVisibleOnClick(true);
        gridTechs.getCrudFormFactory().setVisibleProperties("description", "remark");
        gridTechs.setAddOperation(tech -> {
            this.technologyService.saveAndFlush(tech);
            this.refreshAllData();

            return tech;
        });
        gridTechs.setUpdateOperation(this.technologyService::saveAndUpdate);

        GridCRUDUtils.setColumnsOrder(gridTechs,"id", "description", "remark", "qualityRequirement", "architectureSolution", "ioTDomain");

        ComboBox<ArchitectureSolution> comboBoxArch = new ComboBox<ArchitectureSolution>("Archs", this.architectureSolutionService.findAllOrderedByName());
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
        TextField filter = GridCRUDUtils.createGridTextFilter(gridTechs,"Filter by Technology","400px");
        gridTechs.setFindAllOperation( () -> this.technologyService.findByDescription(filter.getValue()));

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
        gridPapers.getCrudFormFactory().setVisibleProperties("paperTitle", "paperDoi", "paperLink");
        gridPapers.setAddOperation(paper -> {
            this.paperReferenceService.saveAndFlush(paper);
            this.refreshAllData();

            return paper;
        });

        gridPapers.setUpdateOperation(paper -> {
            this.paperReferenceService.saveAndFlush(paper);
            this.refreshAllData();

            return paper;
        });

        gridPapers.setDeleteOperation(
                paper -> {
                    try {
                        this.paperReferenceService.delete(paper);
                        this.refreshAllData();
                    } catch (Exception e) {
                        Notification.show("Error: " + e.getMessage());
                    }
                }
        );

        gridPapers.setFindAllOperation(this.paperReferenceService::findAll);

        // additional components
        TextField filter = GridCRUDUtils.createGridTextFilter(gridPapers,"Filter by Paper Title","400px");
        gridPapers.setFindAllOperation( () -> this.paperReferenceService.findByPaperReferenceTitle(filter.getValue()));

        return gridPapers;
    }

    private GridCrud<QualityRequirement> createQualityRequirementGridCrud() {
        GridCrud<QualityRequirement> gridQualityRequirements = new GridCrud<>(QualityRequirement.class);
        gridQualityRequirements.setSizeFull();
        gridQualityRequirements.getGrid().setDetailsVisibleOnClick(true);
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties("name", "architectureSolution");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name");
        gridQualityRequirements.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridQualityRequirements.getGrid().getColumnByKey("name").setAutoWidth(true);
        gridQualityRequirements.getGrid().getColumnByKey("technology").setAutoWidth(true);

        GridCRUDUtils.setColumnsOrder(gridQualityRequirements, "id", "name", "technology", "architectureSolution");
        gridQualityRequirements.setFindAllOperation(this.qualityRequirementService::findAllOrderedByName);

        gridQualityRequirements.setAddOperation(
                qr -> {
                    this.qualityRequirementService.saveAndFlush(qr);
                    this.refreshAllData();

                    return qr;
                }
        );

        gridQualityRequirements.setUpdateOperation(qr -> {
            this.qualityRequirementService.saveAndFlush(qr);
            this.refreshAllData();

            return qr;
        });

        gridQualityRequirements.setDeleteOperation(qr -> {
                    this.qualityRequirementService.delete(qr);
                    this.refreshAllData();
                }
        );

        ComboBox<String> comboBoxQR = new ComboBox<>("Name", this.qualityRequirementService.listAllByNameDistinct().stream().map(QualityRequirement::getName).collect(Collectors.toList()));
        comboBoxQR.setAllowCustomValue(true);
        comboBoxQR.addCustomValueSetListener(event -> {
            try {
                String customValue = event.getDetail();
                // Process custom value
                comboBoxQR.setValue(customValue);
            } catch (Exception e) {
                Notification.show("Error: " + e.getMessage());
            }
        });
        gridQualityRequirements.getCrudFormFactory().setFieldProvider("name", qr -> comboBoxQR);

        gridQualityRequirements.getCrudFormFactory().setFieldProvider("architectureSolution",
                new ComboBoxProvider<>("Arch", this.architectureSolutionService.findAllOrderedByName()));

        // Create the filter components
        ComboBox<QualityRequirement> filterByQR = (ComboBox<QualityRequirement>) GridCRUDUtils.createGridComboFilter(gridQualityRequirements, "Filter by Quality Requirement", "400px",
                this.qualityRequirementService.listAllByNameDistinct().stream().sorted(Comparator.comparing(QualityRequirement::getName)).collect(Collectors.toList()));

        TextField filterByArch = GridCRUDUtils.createGridTextFilter(gridQualityRequirements, "Filter by Architecture Solution", "400px");

        // Set the filter operations
        gridQualityRequirements.setFindAllOperation(() -> {
            List<QualityRequirement> qrs = this.qualityRequirementService.findAllOrderedByName();
            if (!filterByArch.getValue().isEmpty()) {
                qrs = qrs.stream()
                        .filter(q -> q.getTechnology().getArchitectureSolution().getName().toLowerCase().contains(filterByArch.getValue().toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (filterByQR.getValue() != null) {
                qrs = qrs.stream()
                        .filter(q -> q.getName().toLowerCase().contains(filterByQR.getValue().getName().toLowerCase()))
                        .collect(Collectors.toList());
            }
            return qrs;
        });


        return gridQualityRequirements;
    }

    private GridCrud<ArchitectureSolution> createArchitectureSolutionGridCrud() {
        GridCrud<ArchitectureSolution> gridArchs = new GridCrud<>(ArchitectureSolution.class);
        gridArchs.setSizeFull();
        gridArchs.getGrid().getColumnByKey("id").setWidth("60px").setFlexGrow(0);
        gridArchs.getGrid().getColumnByKey("name").setAutoWidth(true);
        gridArchs.getGrid().getColumnByKey("ioTDomain").setAutoWidth(true);
        gridArchs.getGrid().getColumnByKey("paperReference").setAutoWidth(true);
        gridArchs.setDeleteOperation(
                arch -> {
                    try {
                        this.architectureSolutionService.delete(arch);
                        this.refreshAllData();
                    } catch (Exception e) {
                        Notification.show("Error: " + e.getMessage());
                    }
                }
        );
        gridArchs.setAddOperation(
                arch -> {
                    try {
                        this.architectureSolutionService.saveAndFlush(arch);
                        this.refreshAllData();
                        return arch;
                    } catch (Exception e) {
                        Notification.show("A Paper reference is mandatory.");
                        return null;
                    }
                });

        gridArchs.setFindAllOperation(this.architectureSolutionService::findAllOrderedByName);
        gridArchs.setUpdateOperation(arch -> {
                    try {
                        this.architectureSolutionService.saveAndFlush(arch);
                        this.refreshAllData();
                        return arch;
                    } catch (Exception e) {
                        Notification.show("A Paper reference is mandatory.");
                        return null;
                    }
                });
        gridArchs.getCrudFormFactory().setVisibleProperties("id", "name", "paperReference");
        gridArchs.getGrid().removeColumnByKey("qrs");
        gridArchs.getGrid().removeColumnByKey("technologies");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name", "description");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name", "description");

//        ComboBox<String> comboBoxArch = new ComboBox<>("Name", this.architectureSolutionService.findAllOrderedByName().stream().map(ArchitectureSolution::getName).collect(Collectors.toList()));
//        comboBoxArch.setAllowCustomValue(true);
//        comboBoxArch.addCustomValueSetListener(event -> {
//            try {
//                String customValue = event.getDetail();
//                // Process custom value
//                comboBoxArch.setValue(customValue);
//            } catch (Exception e) {
//                Notification.show("Error: " + e.getMessage());
//            }
//        });
//        gridArchs.getCrudFormFactory().setFieldProvider("name", qr -> comboBoxArch);

        gridArchs.getCrudFormFactory().setFieldProvider("paperReference",
                new ComboBoxProvider<PaperReference>("Reference", this.paperReferenceService.findAll()));
        gridArchs.getCrudFormFactory().setFieldProvider("ioTDomain",
                new ComboBoxProvider<IoTDomain>("IoT Domain", this.domainService.findAllOrderByName().stream().toList()));

        // Create the filter components
        ComboBox<IoTDomain> filterByDomain = (ComboBox<IoTDomain>) GridCRUDUtils.createGridComboFilter(gridArchs, "Filter by IoT Domain", "400px",
                this.domainService.findAllOrderByName().stream().toList());
        TextField filterByArch = GridCRUDUtils.createGridTextFilter(gridArchs, "Filter by Architectural Solution", "400px");

        // Set the filter operations
        gridArchs.setFindAllOperation(() -> {
            List<ArchitectureSolution> solutions = this.architectureSolutionService.findAllOrderedByName();
            if (!filterByArch.getValue().isEmpty()) {
                solutions = solutions.stream()
                        .filter(s -> s.getName().toLowerCase().contains(filterByArch.getValue().toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (filterByDomain.getValue() != null) {
                solutions = solutions.stream()
                        .filter(s -> s.getIoTDomain().equals(filterByDomain.getValue()))
                        .collect(Collectors.toList());
            }
            return solutions;
        });

        return gridArchs;
    }

    private GridCrud<IoTDomain> createIoTDomainGridCrud() {
        GridCrud<IoTDomain> gridDomains = new GridCrud<>(IoTDomain.class);
        gridDomains.setSizeFull();
        gridDomains.getCrudFormFactory().setVisibleProperties("id", "name");
        gridDomains.getGrid().removeColumnByKey("archs");
        gridDomains.setShowNotifications(true);
        gridDomains.setSavedMessage("IoT Domain Saved");
        gridDomains.setDeletedMessage("IoT Domain Deleted");
        gridDomains.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name");
        gridDomains.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name");
        gridDomains.setAddOperation(
                domain -> {
                    this.domainService.saveAndFlush(domain);
                    this.refreshAllData();

                    return domain;
                });
        gridDomains.setFindAllOperation(this.domainService::findAllOrderByName);
        gridDomains.setUpdateOperation(
                domain -> {
                    this.domainService.saveAndUpdate(domain);
                    this.refreshAllData();

                    return domain;
                });
        gridDomains.setDeleteOperation(
                d -> {
                    try {
                        this.domainService.delete(d);
                        this.refreshAllData();
                    } catch (Exception e) {
                        Notification.show("Error: " + e.getMessage());
                    }
                }
        );
        ComboBox<IoTDomain> filterByDomain = (ComboBox<IoTDomain>) GridCRUDUtils.createGridComboFilter(gridDomains, "Filter by IoT Domain", "400px",
                this.domainService.findAllOrderByName());

        // Set the filter operations
        gridDomains.setFindAllOperation(() -> {
            List<IoTDomain> domains = this.domainService.findAllOrderByName();

            if (filterByDomain.getValue() != null) {
                domains = domains.stream()
                        .filter(d -> {
                            return Objects.equals(d.getName(), filterByDomain.getValue().getName());
                        })
                        .collect(Collectors.toList());
            }
            return domains;
        });

        return gridDomains;
    }
}
