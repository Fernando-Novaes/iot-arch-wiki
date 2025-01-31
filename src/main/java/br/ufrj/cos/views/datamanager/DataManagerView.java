package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.utils.GridCRUDUtils;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("IoT-Arch - Data Manager")
@Route(value = "datamanager-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class DataManagerView extends BaseView {

    private final IoTDomainService domainService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final ArchitectureService architectureService;
    private final QualityRequirementService qualityRequirementService;
    private final PaperReferenceService paperReferenceService;
    private final TechnologyService technologyService;

    private final ArchitectureSolutionDataManager architectureSolutionDataManager;

    GridCrud<IoTDomain> gridDomains;
    GridCrud<Architecture> gridArchs;
    GridCrud<QualityRequirement> gridQualityRequirements;
    GridCrud<Technology> gridTechs;
    GridCrud<PaperReference> gridPapers;

    Tabs tabs = new Tabs();

    private Span domainBadge;
    private Span archBadge;
    private Span qualityBadge;
    private Span techBadge;
    private Span paperBadge;
    private Span archSolutionBadge;

    ComboBox<IoTDomain> iotDomainRegisterCombo = new ComboBox<>();
    ComboBox<PaperReference> paperReferenceRegisterCombo = new ComboBox<>();
    ComboBox<QualityRequirement> qualityRequirementRegisterCombo = new ComboBox<>();
    ComboBox<Technology> technologyRegisterCombo = new ComboBox<>();

    ArchitectureSolution architectureSolution = new ArchitectureSolution();

    @Autowired
    public DataManagerView(IoTDomainService domainService, ArchitectureSolutionService architectureSolutionService, ArchitectureService architectureService, QualityRequirementService qualityRequirementService, PaperReferenceService paperReferenceService, TechnologyService technologyService, ArchitectureSolutionDataManager architectureSolutionDataManager) {
        this.domainService = domainService;
        this.architectureSolutionService = architectureSolutionService;
        this.architectureService = architectureService;
        this.qualityRequirementService = qualityRequirementService;
        this.paperReferenceService = paperReferenceService;
        this.technologyService = technologyService;
        this.architectureSolutionDataManager = architectureSolutionDataManager;
        this.architectureSolutionDataManager.setArchitectureSolution(this.architectureSolution);

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("Knowledge Manager");

        tabs = createTabs();

        gridDomains = createIoTDomainGridCrud();
        gridArchs = createArchitectureGridCrud();
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

    /***
     * Refreshes badge count
     */
    private void refreshBadgeCount() {
        this.paperBadge = this.createBadge(String.valueOf(this.paperReferenceService.findAll().size()));
        this.archBadge = this.createBadge(String.valueOf(this.architectureService.findAll().size()));
        this.qualityBadge = this.createBadge(String.valueOf(this.qualityRequirementService.findAll().size()));
        this.techBadge = this.createBadge(String.valueOf(this.technologyService.findAll().size()));
        this.archSolutionBadge = this.createBadge(String.valueOf(this.architectureSolutionService.findAll().size()));
        this.domainBadge = this.createBadge(String.valueOf(this.domainService.findAll().size()));
    }

    private void createTabLayout() {
        Div contentContainer = new Div();
        contentContainer.setSizeFull();
        contentContainer.add(gridPapers);
        refreshBadgeCount();

        // Add a listener to switch the content when the tab changes
        tabs.addSelectedChangeListener(event -> {
            contentContainer.removeAll();
            Component selectedContent = null;
            switch (tabs.getSelectedIndex()) {
                case 0:
                    selectedContent = gridPapers;
                    break;
                case 1:
                    selectedContent = gridDomains;
                    break;
                case 2:
                    selectedContent = gridArchs;
                    break;
                case 3:
                    selectedContent = gridQualityRequirements;
                    break;
                case 4:
                    selectedContent = gridTechs;
                    break;
                case 5:
                    selectedContent = this.architectureSolutionDataManager.createKnowledgeCrud();
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
        this.refreshBadgeCount();

        Tab papers = new Tab(new Span("References"), this.paperBadge);
        Tab domains = new Tab(new Span("IoT Domains"), this.domainBadge);
        Tab archs = new Tab(new Span("Architectures"), this.archBadge);
        Tab qrs = new Tab(new Span("Quality Requirements"), this.qualityBadge);
        Tab techs = new Tab(new Span("Technologies"), this.techBadge);
        Tab knowledge = new Tab(new Span("Architecture Solution"), this.archSolutionBadge);

        return new Tabs(papers, domains, archs, qrs, techs, knowledge);
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

        this.refreshBadgeCount();
    }

    /***
     * Creates the Grid CRUD to the Technology data
     * @return GridCrud<Technology>
     */
    private GridCrud<Technology> createTechnolgyGridCrud() {
        GridCrud<Technology> gridTechs = new GridCrud<>(Technology.class);
        gridTechs.setShowNotifications(false);
        gridTechs.setSizeFull();
//        gridTechs.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridTechs.getGrid().getColumnByKey("description").setAutoWidth(true);
        gridTechs.getCrudFormFactory().setVisibleProperties("architectureSolution", "qualityRequirement", "description");
        gridTechs.getGrid().setDetailsVisibleOnClick(true);
        gridTechs.getCrudFormFactory().setVisibleProperties("description", "notes");
        gridTechs.setAddOperation(tech -> {
            this.technologyService.saveAndFlush(tech);
            this.refreshAllData();
            this.refreshBadgeCount();
            NotificationUtils.showSuccessNotification("Technology saved.");
            return tech;
        });
        gridTechs.setUpdateOperation(this.technologyService::saveAndUpdate);

        gridTechs.setDeleteOperation(tech -> {
            this.technologyService.delete(tech);
            this.refreshAllData();
            this.refreshBadgeCount();
            NotificationUtils.showSuccessNotification("Technology deleted.");
        });

        GridCRUDUtils.setColumnsOrder(gridTechs,"id", "description", "notes", "associations", "architectureSolutions", "qualityRequirements");

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
        gridPapers.setShowNotifications(false);
        gridPapers.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridPapers.getGrid().getColumnByKey("title").setAutoWidth(true);
        gridPapers.getCrudFormFactory().setVisibleProperties("title", "doi", "link", "publishYear");
        gridPapers.setAddOperation(paper -> {
            this.paperReferenceService.saveAndFlush(paper);
            this.refreshAllData();

            return paper;
        });

        gridPapers.setUpdateOperation(paper -> {
            this.paperReferenceService.saveAndFlush(paper);
            this.refreshAllData();
            NotificationUtils.showSuccessNotification("Paper Reference saved.");
            return paper;
        });

        gridPapers.setDeleteOperation(
                paper -> {
                    try {
                        if (paper.getArchitectureSolution() != null) {
                            // Confirm the deletion
                            ConfirmDialog confirmDialog = new ConfirmDialog();
                            confirmDialog.setText(
                                    new Html(String.format("<p>There is an Architecture Solution associated with this Paper Reference.</br>This Solution will also be deleted. Are you sure you want to delete this Paper Reference: </br></br><b>%s</b></p>?", paper.getTitle())));
                            confirmDialog.setHeader("Confirm Deletion");
                            confirmDialog.setCancelable(true);
                            confirmDialog.setConfirmText("Delete");
                            confirmDialog.addConfirmListener(confirmEvent -> {
                                // Perform deletion logic
                                ArchitectureSolution as = paper.getArchitectureSolution();
                                as.setPaperReference(null);
                                as.setArchitecture(null);
                                as.setDescription(null);
                                as.setIoTDomain(null);
                                this.architectureSolutionService.saveAndUpdate(as);
                                this.architectureSolutionService.delete(as);
                                this.paperReferenceService.delete(paper);
                                this.refreshAllData();
                                NotificationUtils.showSuccessNotification("Paper Reference deleted.");
                            });

                            confirmDialog.open();
                        }  else {
                            this.paperReferenceService.delete(paper);
                            this.refreshAllData();
                            NotificationUtils.showSuccessNotification("Paper Reference deleted.");
                        }
                    } catch (Exception e) {
                        NotificationUtils.showErrorNotification("Error: " + e.getMessage());
                    }
                }
        );

        gridPapers.setFindAllOperation(this.paperReferenceService::finAllOrderByPaperReferenceTitle);

        // additional components
        TextField filter = GridCRUDUtils.createGridTextFilter(gridPapers,"Filter by Paper Title","400px");
        gridPapers.setFindAllOperation( () -> this.paperReferenceService.findByPaperReferenceTitle(filter.getValue()));

        return gridPapers;
    }

    private GridCrud<QualityRequirement> createQualityRequirementGridCrud() {
        GridCrud<QualityRequirement> gridQualityRequirements = new GridCrud<>(QualityRequirement.class);
        gridQualityRequirements.setSizeFull();
        gridQualityRequirements.setShowNotifications(false);
        gridQualityRequirements.getGrid().setDetailsVisibleOnClick(true);
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties("name", "architectureSolution");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name");
        gridQualityRequirements.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name");
        gridQualityRequirements.getGrid().getColumnByKey("id").setWidth("100px").setFlexGrow(0);
        gridQualityRequirements.getGrid().getColumnByKey("name").setAutoWidth(true);
        //gridQualityRequirements.getGrid().getColumnByKey("technology").setAutoWidth(true);

        GridCRUDUtils.setColumnsOrder(gridQualityRequirements, "id", "name", "associations", "architectureSolutions", "technologies");
        gridQualityRequirements.setFindAllOperation(this.qualityRequirementService::findAllOrderedByName);

        gridQualityRequirements.setAddOperation(
                qr -> {
                    this.qualityRequirementService.saveAndFlush(qr);
                    this.refreshAllData();
                    NotificationUtils.showSuccessNotification("Quality Requirement saved.");
                    return qr;
                }
        );

        gridQualityRequirements.setUpdateOperation(qr -> {
            this.qualityRequirementService.saveAndFlush(qr);
            this.refreshAllData();
            NotificationUtils.showSuccessNotification("Quality Requirement saved.");
            return qr;
        });

        gridQualityRequirements.setDeleteOperation(qr -> {
                    this.qualityRequirementService.delete(qr);
                    this.refreshAllData();
                    NotificationUtils.showSuccessNotification("Quality Requirement deleted.");
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
                NotificationUtils.showErrorNotification("Error: " + e.getMessage());
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
                        .filter(q -> q.getArchitectureSolutions().stream().anyMatch(arch -> arch.getArchitecture().getName().toLowerCase().contains(filterByArch.getValue().toLowerCase()))).toList();
            }
            if (filterByQR.getValue() != null) {
                qrs = qrs.stream().filter(q -> q.getArchitectureSolutions().stream().anyMatch(arch -> arch.getArchitecture().getName().toLowerCase().contains(filterByArch.getValue().toLowerCase()))).toList();
            }
            return qrs;
        });

        return gridQualityRequirements;
    }

    private GridCrud<Architecture> createArchitectureGridCrud() {
        GridCrud<Architecture> gridArchs = new GridCrud<>(Architecture.class);
        gridArchs.setSizeFull();
        gridArchs.setShowNotifications(false);
        gridArchs.getGrid().getColumnByKey("id").setWidth("60px").setFlexGrow(0);
        gridArchs.getGrid().getColumnByKey("name").setAutoWidth(true);
        gridArchs.setDeleteOperation(
                arch -> {
                    try {
                        this.architectureService.delete(arch);
                        this.refreshAllData();
                        NotificationUtils.showSuccessNotification("Architecture Solution deleted.");
                    } catch (Exception e) {
                        NotificationUtils.showErrorNotification("Error: " + e.getMessage());
                    }
                }
        );
        gridArchs.setAddOperation(
                arch -> {
                    try {
                        this.architectureService.saveAndFlush(arch);
                        this.refreshAllData();
                        NotificationUtils.showSuccessNotification("Architecture Solution saved.");
                        return arch;
                    } catch (Exception e) {
                        NotificationUtils.showErrorNotification("A Paper reference is mandatory.");
                        return null;
                    }
                });

        gridArchs.setFindAllOperation(this.architectureService::findAll);
        gridArchs.setUpdateOperation(arch -> {
                    try {
                        this.architectureService.saveAndFlush(arch);
                        this.refreshAllData();
                        NotificationUtils.showSuccessNotification("Architecture Solution saved.");
                        return arch;
                    } catch (Exception e) {
                        NotificationUtils.showErrorNotification("A Paper reference is mandatory.");
                        return null;
                    }
                });
        gridArchs.getCrudFormFactory().setVisibleProperties("id", "name");
        //gridArchs.getGrid().removeColumnByKey("qrs");
        //gridArchs.getGrid().removeColumnByKey("technologies");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name");
        gridArchs.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name");
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
            List<Architecture> archs = this.architectureService.findAll();
            if (!filterByArch.getValue().isEmpty()) {
                archs = archs.stream()
                        .filter(a -> a.getName().toLowerCase().contains(filterByArch.getValue().toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (filterByDomain.getValue() != null) {
                archs = archs.stream().filter(a -> a.getArchitectureSolutions().stream()
                        .allMatch(d -> d.getIoTDomain().equals(filterByDomain.getValue()))).toList();
            }
            return archs;
        });

        return gridArchs;
    }

    private GridCrud<IoTDomain> createIoTDomainGridCrud() {
        GridCrud<IoTDomain> gridDomains = new GridCrud<>(IoTDomain.class);
        gridDomains.setSizeFull();
        gridDomains.setShowNotifications(false);
        gridDomains.getCrudFormFactory().setVisibleProperties("id", "name");
        gridDomains.getGrid().removeColumnByKey("architectureSolutions");
        gridDomains.setShowNotifications(true);
        gridDomains.setSavedMessage("IoT Domain Saved");
        gridDomains.setDeletedMessage("IoT Domain Deleted");
        gridDomains.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name");
        gridDomains.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "name");
        gridDomains.setAddOperation(
                domain -> {
                    this.domainService.saveAndFlush(domain);
                    this.refreshAllData();
                    NotificationUtils.showErrorNotification("IoT Domain saved.");
                    return domain;
                });
        gridDomains.setFindAllOperation(this.domainService::findAllOrderByName);
        gridDomains.setUpdateOperation(
                domain -> {
                    this.domainService.saveOrUpdate(domain);
                    this.refreshAllData();
                    NotificationUtils.showErrorNotification("IoT Domain saved.");
                    return domain;
                });
        gridDomains.setDeleteOperation(
                d -> {
                    try {
                        this.domainService.delete(d);
                        this.refreshAllData();
                        NotificationUtils.showErrorNotification("IoT Domain deleted.");
                    } catch (Exception e) {
                        NotificationUtils.showErrorNotification("Error: " + e.getMessage());
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
