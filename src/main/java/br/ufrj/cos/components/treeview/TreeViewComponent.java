package br.ufrj.cos.components.treeview;


import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.components.diagram.EdgeDiagram;
import br.ufrj.cos.components.diagram.NodeDiagram;
import br.ufrj.cos.components.qrcode.QRCodeComponent;
import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.TreeViewService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@UIScope
@Component
@CssImport(value = "./styles/app-styles.css", themeFor = "vaadin-grid")
public class TreeViewComponent extends VerticalLayout {

    private TreeGrid<TreeNode<?>> treeGrid;
    private final QRCodeComponent qrCodeComponent;
    private final DiagramComponent diagramComponent;
    private final TreeViewService treeViewService;
    @Getter private Boolean loaded = Boolean.FALSE;
    @Getter @Setter
    private Boolean isFiltering = Boolean.FALSE;
    private TreeRootSelectionComponent treeRootSelectionComponent;
    StringBuilder pathString;
    @Getter
    private List<? extends DomainBase> treeViewData;
    @Getter @Setter
    private SliderPanel detailsSliderPanel;

    @Autowired
    public TreeViewComponent(QRCodeComponent qrCodeComponent,
                             IoTDomainService ioTDomainService,
                             DiagramComponent diagramComponent,
                             TreeViewService treeViewService,
                             TreeRootSelectionComponent treeRootSelectionComponent) {

        this.qrCodeComponent = qrCodeComponent;
        this.diagramComponent = diagramComponent;
        this.treeViewService = treeViewService;
        this.treeRootSelectionComponent = treeRootSelectionComponent;
    }

    public void setTreeViewData(List<? extends DomainBase> treeViewData) {
        this.treeViewService.setTreeViewData(treeViewData);
        this.treeViewData = treeViewData;
    }

    private Button addBoxToTreeViewNode(TreeNode<?> node, String className) {
        this.detailsSliderPanel.clearContents();
        String SLIDEPANEL_DETAILS_FORMAT_STRING =
                "<div>" +
                "<h3>%s: </h3>" +
                "<b>%s</b>" +
                "</br><div style='font-style: italic; margin-bottom: 5px;'>%s</div>" +
                "</div>";

        Button btn = new Button();
        btn.addClassName("button-base");
        btn.addClassName(className);

        Object data = node.getData();
        ArchitectureSolution archSolution = null;
        if (data instanceof ArchitectureSolution solution) {
            archSolution = solution;
            btn.setText(solution.getArchitecture().getName());
            btn.getElement().addEventListener("mouseover", event -> {
                IoTDomain domain = solution.getIoTDomain();
                this.detailsSliderPanel.setIoTDomainContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "IoT Domain",
                                domain.getName(),
                                Optional.ofNullable(domain.getDescription()).orElse("No description")))));
                this.detailsSliderPanel.setArchitectureContent(
                        new HorizontalLayout(
                                new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Architecture",
                                        solution.getArchitecture().getName(),
                                        solution.getDescription().isEmpty() ? "No description" : solution.getDescription()))));
                //Adding reference details
                String REFERENCE_DETAILS_STRING_FORMAT = "<div style='font-style: italic;'><center><b>%s, %s</b></center></div>";
                this.detailsSliderPanel.setReferenceDetails(
                        new Html(String.format(REFERENCE_DETAILS_STRING_FORMAT,
                                solution.getPaperReference().getTitle(), solution.getPaperReference().getPublishYear())));
            });
        } else if (data instanceof IoTDomain domain) {
            btn.setText(domain.getName());
            btn.getElement().addEventListener("mouseover", event -> {
                this.detailsSliderPanel.setIoTDomainContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "IoT Domain",
                                domain.getName(),
                                Optional.ofNullable(domain.getDescription()).orElse("No description")))));
            });
        } else if (data instanceof QualityRequirement qr) {
            btn.setText(qr.getName());
            //Getting the Architecture Solution
            ArchitectureSolution finalArchSolution = (ArchitectureSolution) node.getParent().getData();
            btn.getElement().addEventListener("mouseover", event -> {
                IoTDomain domain = finalArchSolution.getIoTDomain();

                this.detailsSliderPanel.setIoTDomainContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "IoT Domain",
                                domain.getName(),
                                Optional.ofNullable(domain.getDescription()).orElse("No description")))));

                this.detailsSliderPanel.setArchitectureContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Architecture",
                                finalArchSolution.getArchitecture().getName(),
                                finalArchSolution.getDescription().isEmpty() ? "No description" : finalArchSolution.getDescription()))));

                QualityRequirementTechnology assoc = finalArchSolution.getQualityRequirementTechnologies().stream().filter(assocs ->
                        assocs.getQualityRequirement().getId().equals(qr.getId())).findAny().get();

                this.detailsSliderPanel.setQualityRequirementContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Quality Requirement",
                                qr.getName(),
                                (assoc.getNotes() == null)? "No description" : assoc.getNotes()))));

                //Adding reference details
                String REFERENCE_DETAILS_STRING_FORMAT = "<div style='font-style: italic;'><center><b>%s, %s</b></center></div>";
                this.detailsSliderPanel.setReferenceDetails(
                        new Html(String.format(REFERENCE_DETAILS_STRING_FORMAT,
                                finalArchSolution.getPaperReference().getTitle(), finalArchSolution.getPaperReference().getPublishYear())));
            });
        } else if (data instanceof Technology tech) {
            btn.setText(tech.getDescription());
            //Getting the ArchitectureSolution
            ArchitectureSolution finalArchSolution = (ArchitectureSolution) node.getParent().getParent().getData();
            btn.getElement().addEventListener("mouseover", event -> {
                IoTDomain domain = finalArchSolution.getIoTDomain();

                this.detailsSliderPanel.setIoTDomainContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "IoT Domain",
                                domain.getName(),
                                Optional.ofNullable(domain.getDescription()).orElse("No description")))));

                this.detailsSliderPanel.setArchitectureContent(new HorizontalLayout(
                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Architecture",
                                finalArchSolution.getArchitecture().getName(),
                                finalArchSolution.getDescription() == null ? "No description" : finalArchSolution.getDescription()))));

                QualityRequirementTechnology assoc = finalArchSolution.getQualityRequirementTechnologies().stream().filter(assocs ->
                        assocs.getTechnology().getId().equals(tech.getId())).findAny().get();

                this.detailsSliderPanel.setQualityRequirementContent(new HorizontalLayout(

                        new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Quality Requirement",
                                assoc.getQualityRequirement().getName(),
                                (assoc.getNotes() == null)? "No description" : assoc.getNotes()))));

                this.detailsSliderPanel.setTechnologyContent(
                        new HorizontalLayout(new Html(String.format(SLIDEPANEL_DETAILS_FORMAT_STRING, "Technology",
                                tech.getDescription(),
                                (assoc.getNotes() == null)? "No description" : assoc.getNotes()))));

                //Adding reference details
                String REFERENCE_DETAILS_STRING_FORMAT = "<div style='font-style: italic;'><center><b>%s, %s</b></center></div>";
                this.detailsSliderPanel.setReferenceDetails(
                        new Html(String.format(REFERENCE_DETAILS_STRING_FORMAT,
                                finalArchSolution.getPaperReference().getTitle(), finalArchSolution.getPaperReference().getPublishYear())));
            });
        }

        btn.addClickListener(click -> {
            this.selectRow(node, this.treeGrid);
        });

      btn.getElement().addEventListener("mouseout", event -> {
          this.detailsSliderPanel.clearContents();
      });

        return btn;
    }

    public void load() {
        treeGrid = new TreeGrid<>();
        // Define columns (e.g., displaying IoT Domain names)
        treeGrid.addComponentHierarchyColumn(node -> {
            Object data = node.getData();

            if (data instanceof IoTDomain) {
                return addBoxToTreeViewNode(node, "iot-domain");
            } else if (data instanceof ArchitectureSolution) {
                return addBoxToTreeViewNode(node, "architecture-solution");
            } else if (data instanceof QualityRequirement) {
                return addBoxToTreeViewNode(node, "quality-requirement");
            } else if (data instanceof Technology) {
                String nodeNames = null;
                try {
                    nodeNames = this.createPathToNode(node);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return this.createNodeWithIcon(treeGrid, node, nodeNames);
            }
            return new Text("");
        }).setHeader(this.treeRootSelectionComponent.create());
        //add set header above

        treeGrid.getStyle().setBorderRadius("8px");

        TreeNode<Object> root;
        if (this.isFiltering) {
            root = treeViewService.getTree(this.treeRootSelectionComponent.getTreeViewType());
            this.setTreeViewData(treeViewService.getTreeViewData());
            if (root != null) treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());
        } else {
            root = treeViewService.getTree(this.treeRootSelectionComponent.getTreeViewType());
            if (root != null) treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());
            this.setTreeViewData(treeViewService.getTreeViewData());
        }

        treeGrid.expand(root);

        treeGrid.setClassNameGenerator(node -> {
            Object data = node.getData();

            // Get the text content
            String text = ""; // You'll need to get this from your node data
            if (data instanceof IoTDomain) {
                IoTDomain domain = (IoTDomain) data;
                text = domain.getName(); // or whatever field contains the text
                return "treeView-cell";
            } else if (data instanceof ArchitectureSolution) {
                ArchitectureSolution solution = (ArchitectureSolution) data;
                text = solution.getArchitecture().getName();
                return "treeView-cell";
            } else if (data instanceof QualityRequirement) {
                QualityRequirement req = (QualityRequirement) data;
                text = req.getName();
                return "treeView-cell";
            } else if (data instanceof Technology) {
                Technology tech = (Technology) data;
                text = tech.getDescription();
                return "treeView-cell";
            }
            return "root";
        });


        // Add styling variant to the TreeGrid for better visibility
        treeGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        treeGrid.getStyle().setBackgroundColor("#373a3f");

        // Add the TreeGrid to the main layout
        add(treeGrid);
        setSizeFull();
        getStyle().set("flex-grow", "1");

        this.loaded = Boolean.TRUE;
    }

    private String createPathToNode(TreeNode<?> node) throws Exception {
        List<TreeNode<?>> path = getPathToRoot(node);
        StringBuilder diagramlabels = new StringBuilder();

        // Construct the full path string
        this.pathString = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            Object data = path.get(i).getData();

            if (data instanceof Technology) {
                Technology technology = (Technology) data;

                // Find the specific association that links this technology
                QualityRequirementTechnology association =
                        technology.getAssociations().stream()
                                .findFirst()
                                .orElseThrow(() -> new Exception("No associated Architecture Solution found!"));

                ArchitectureSolution architectureSolution = association.getArchitectureSolution();
                QualityRequirement qualityRequirement = association.getQualityRequirement();
                IoTDomain iotDomain = architectureSolution.getIoTDomain();

                // Append to diagram names
                diagramlabels.append(iotDomain.getName()).append("!").append("IoT Domain").append("#");
                diagramlabels.append(architectureSolution.getArchitecture().getName()).append("!").append("Architecture Solution").append("#");
                diagramlabels.append(qualityRequirement.getName()).append("!").append("Quality Requirement").append("#");
                diagramlabels.append(technology.getDescription()).append("!").append("Technology").append("#");

                pathString.append(iotDomain.getName()).append(" >> ");
                pathString.append(architectureSolution.getArchitecture().getName()).append(" >> ");
                pathString.append(qualityRequirement.getName()).append(" >> ");
                pathString.append(technology.getDescription());
            }
        }

        return diagramlabels.toString();
    }

    /***
     * Method to create a component for the node with text and icon
     */
    private HorizontalLayout createNodeWithIcon(TreeGrid<TreeNode<?>>  treeGrid, TreeNode<?> node, String diagramNames) {
        // Create an icon
        Icon icon = VaadinIcon.INFO_CIRCLE.create(); // Use any icon you prefer
        icon.getElement().getStyle().set("cursor", "pointer"); // Change cursor style to pointer for clickable effect
        icon.setSize("20px");

        // Create a button to handle the click event
        Button button = new Button(icon);
        button.setTooltipText("Reference details");
        button.addClickListener(event -> {
            this.selectRow(node, treeGrid);
            // Action when the icon is clicked
            //Notification.show("Icon clicked for: " + tech.getDescription());

            // Find the specific association that links this technology
            QualityRequirementTechnology association =
                    ((Technology) node.getData()).getAssociations().stream()
                            .findFirst().get();

            createReferenceDetailsDialog(
                    association.getArchitectureSolution().getPaperReference().getTitle(),
                    association.getArchitectureSolution().getPaperReference().getLink());
        });

        this.createDiagram(diagramNames);

        button.getStyle().set("min-width", "20px"); // Set the button size
        button.getStyle().set("height", "22px"); // Set the button size

        // Create a layout to hold the text and the icon
        HorizontalLayout  layout = new HorizontalLayout ();
        layout.add(addBoxToTreeViewNode(node, "technology"), button);
        layout.setAlignItems(Alignment.CENTER);
        //layout.setSpacing(true); // Remove spacing between text and icon

        return layout;
    }

    /***
     * Creates the Diagram of the selected solution on the Tree
     * @param diagramNames Names of the items of the selected Node
     * @return DiagramComponent
     */
    private DiagramComponent createDiagram(String diagramNames) {
        List<NodeDiagram> nodes = this.getNodesToDiagram(diagramNames);
        List<EdgeDiagram> edges = getEdgesDiagrams(nodes.size());

        this.diagramComponent.setNodes(nodes);
        this.diagramComponent.setEdges(edges);
        this.diagramComponent.execute();

        return this.diagramComponent;
    }

    /***
     * Set the background color of the node accordingly the pattern
     */
    private String setBoxStyleToNodes(String domain) {
        return switch (domain) {
            case "IoT Domain" -> "#ED8312E5";
            case "Architecture Solution" -> "#ffffff";
            case "Quality Requirement" -> "yellow";
            case "Technology" -> "green";
            default -> "#ED8312E5";
        };
    }

    private List<EdgeDiagram> getEdgesDiagrams(int edgesCount) {
        List<EdgeDiagram> edges = new ArrayList<>();

        for (int i = 0; i < edgesCount; i++) {
            edges.add(EdgeDiagram.builder().from(String.valueOf(i)).to(String.valueOf(i+1)).build());
        }

        return edges;
    }

    /**
     * @param diagramNames List
     * @return List<NodeDiagram>
     */
    private List<NodeDiagram> getNodesToDiagram(String diagramNames) {
        List<NodeDiagram> nodes = new ArrayList<>();
        List<String> names = Arrays.asList(diagramNames.split("#"));

        names.forEach(n -> {
            String[] namesAndTypes = n.split("!");
            NodeDiagram dom = NodeDiagram.builder().id(String.valueOf(names.indexOf(n))).label(namesAndTypes[0]).color(this.setBoxStyleToNodes(namesAndTypes[1])).tooltip(namesAndTypes[1]).build();
            nodes.add(dom);
        });

        return nodes;
    }

    private void createReferenceDetailsDialog(String paperTitle, String paperLink) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Details");
        dialog.addAttachListener(attachEvent -> this.diagramComponent.execute());

        HorizontalLayout hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.CENTER);
        //hl.setSpacing(true);

        VerticalLayout vl = new VerticalLayout();
        vl.setAlignItems(Alignment.CENTER);

        H2 paperTitleH2 = new H2(paperTitle);
        paperTitleH2.getStyle().set("text-shadow", "2px 2px 4px rgba(0, 0, 0, 0.5)");
        Anchor link = new Anchor(paperLink, paperLink);
        link.setTarget("_blank"); // Opens the link in a new tab

        Div divDiagram = new Div();
        divDiagram.setId("diagram");
        divDiagram.setWidthFull();

        vl.add(paperTitleH2, link, this.qrCodeComponent.generateQRCode(paperLink, 100, 100), divDiagram, new Text(this.pathString.toString()));

        dialog.add(vl, this.diagramComponent);

        Button closeXButton = new Button(new Icon("lumo", "cross"),
                (e) -> dialog.close());
        closeXButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeXButton);

        Button close = new Button("Close", (e) -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(close);
        dialog.open();
    }

    private void selectRow(TreeNode<?> node, TreeGrid<TreeNode<?>> treeGrid) {
        treeGrid.collapse(treeGrid.getSelectedItems());
        treeGrid.getSelectionModel().select(node);
    }

    private List<TreeNode<?>> getPathToRoot(TreeNode<?> node) {
        List<TreeNode<?>> path = new ArrayList<>();
        TreeNode<?> current = node;
        while (current != null) {
            path.add(current);

            if (current.getParent().getData() != null) {
                current = current.getParent();
            } else {
                current = null;
            }
        }

        return path;
    }

    public void addTreeRootSelection(TreeRootSelectionComponent rootSelection) {
        this.treeRootSelectionComponent = rootSelection;
    }

    private ComponentEventListener clickListener;
    public void addNodeClickEvent(ComponentEventListener clickEvent) {
        this.clickListener = clickEvent;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.removeAll();
    }
}
