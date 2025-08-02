package br.ufrj.cos.components.treeview;


import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.components.diagram.EdgeDiagram;
import br.ufrj.cos.components.diagram.NodeDiagram;
import br.ufrj.cos.components.qrcode.QRCodeComponent;
import br.ufrj.cos.components.treeview.dialog.ReferenceDetailsDialog;
import br.ufrj.cos.components.treeview.events.TreeRootSelectionChangeEvent;
import br.ufrj.cos.components.treeview.factory.TreeNodeDetailsFactory;
import br.ufrj.cos.components.treeview.record.DataDetails;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.TreeViewService;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UIScope
@Component
@CssImport(value = "./styles/app-styles.css", themeFor = "vaadin-grid")
public class TreeViewComponent extends VerticalLayout {

    @Getter
    private TreeGrid<TreeNode<?>> treeGrid;
    @Getter
    private TreeNode<Object> rootNode;
    private final QRCodeComponent qrCodeComponent;
    private final DiagramComponent diagramComponent;
    private final TreeViewService treeViewService;
    @Getter private Boolean loaded = Boolean.FALSE;
    @Getter @Setter
    private Boolean isFiltering = Boolean.FALSE;
    StringBuilder pathString;
    @Getter
    private List<? extends DomainBase> treeViewData;
    private final TreeNodeDetailsFactory treeNodeDetailsFactory;
    @Getter @Setter
    private TreeViewType treeViewType;
    @Getter @Setter
    private DataDetails dataDetails;
    @Getter @Setter
    private com.vaadin.flow.component.Component gridHeader;

    private final ApplicationEventPublisher eventPublisher;

    public TreeViewComponent(
            QRCodeComponent qrCodeComponent,
                             DiagramComponent diagramComponent,
                             TreeViewService treeViewService, TreeNodeDetailsFactory treeNodeDetailsFactory, ApplicationEventPublisher eventPublisher) {

        this.qrCodeComponent = qrCodeComponent;
        this.diagramComponent = diagramComponent;
        this.treeViewService = treeViewService;
        this.treeNodeDetailsFactory = treeNodeDetailsFactory;
        this.eventPublisher = eventPublisher;
    }

    public void setTreeViewData(List<? extends DomainBase> treeViewData) {
        this.treeViewService.setTreeViewData(treeViewData);
        this.treeViewData = treeViewData;
    }

    /***
     * Load the TreeView with all data from the knowledge base
     */
    public void load() {
        treeGrid = new TreeGrid<>();

        TreeNode<Object> tree = treeViewService.getTree(this.treeViewType);
        this.setTreeViewData(treeViewService.getTreeViewData());

        // Define columns (e.g., displaying IoT Domain names)
        treeGrid.addComponentHierarchyColumn(node -> {
            Object data = node.getData();

            if (data instanceof IoTDomain) {
                return this.treeNodeDetailsFactory.createButtonForNode(node, "iot-domain");
            } else if (data instanceof ArchitectureSolution) {
                return this.treeNodeDetailsFactory.createButtonForNode(node, "architecture-solution");
            } else if (data instanceof QualityRequirement) {
                return this.treeNodeDetailsFactory.createButtonForNode(node, "quality-requirement");
            } else if (data instanceof Technology) {
                return this.createNodeWithIcon(treeGrid, node);
            }
            return (treeViewData.isEmpty())?
                    new Html("<div style='font-weight: bold; align-content: center; width: max-content; color: red'>No results were found for the current combination of filters. " +
                            "Try adjusting or removing a filter to see more results.</div>") :
                    new Text("Move the cursor over the tree node to see more details on the side panel.");
        }).setHeader(
                (this.getGridHeader() != null)?
                        this.getGridHeader() :
                        new Html("<center><h3>Knowledge Tree</h3></center>"));
        //add set header above

        treeGrid.getStyle().setBorderRadius("8px");

        TreeNode<Object> root =
                new TreeNode<>(new Text("Move the cursor over the tree node to see more details on the side panel."));


//        if (this.isFiltering) {
//            root = treeViewService.getTree(this.treeViewType);
//            this.setTreeViewData(treeViewService.getTreeViewData());
//            if (root != null) treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());
//        } else {

        root = tree;
        this.rootNode = root;
        if (root != null) treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());
        //}

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
        treeGrid.getStyle().setBackgroundColor("#var(--lumo-contrast-10pct)");

        // Add the TreeGrid to the main layout
        add(treeGrid);
        setSizeFull();
        getStyle().set("flex-grow", "1");

        this.loaded = Boolean.TRUE;
    }

    private String createPathToNode(IoTDomain iotDomain, ArchitectureSolution architectureSolution, QualityRequirement qualityRequirement, Technology technology) {
        StringBuilder diagramlabels = new StringBuilder();

        // Construct the full path string
        this.pathString = new StringBuilder();

        // Append to diagram names
        diagramlabels.append(iotDomain.getName()).append("!").append("IoT Domain").append("#");
        diagramlabels.append(architectureSolution.getArchitecture().getName()).append("!").append("Architecture").append("#");
        diagramlabels.append(qualityRequirement.getName()).append("!").append("Quality Requirement").append("#");
        diagramlabels.append(technology.getDescription()).append("!").append("Technology").append("#");

        pathString.append(iotDomain.getName()).append(" >> ");
        pathString.append(architectureSolution.getArchitecture().getName()).append(" >> ");
        pathString.append(qualityRequirement.getName()).append(" >> ");
        pathString.append(technology.getDescription());

        return diagramlabels.toString();
    }

    /***
     * Method to create a component for the node with text and icon
     */
    private HorizontalLayout createNodeWithIcon(TreeGrid<TreeNode<?>>  treeGrid, TreeNode<?> node) {
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

            Technology tech = (Technology) node.getData();
            QualityRequirement qr = (QualityRequirement) node.getParent().getData();
            ArchitectureSolution archSol = (ArchitectureSolution) node.getParent().getParent().getData();
            IoTDomain domain = (IoTDomain) node.getParent().getParent().getParent().getData();

            PaperReference ref = archSol.getPaperReference();

            String diagramNames = this.createPathToNode(domain, archSol, qr, tech);
            this.createDiagram(diagramNames);

            createReferenceDetailsDialog(
                    ref.getTitle(),
                    ref.getLink());
        });

        button.getStyle().set("min-width", "20px"); // Set the button size
        button.getStyle().set("height", "22px"); // Set the button size

        // Create a layout to hold the text and the icon
        HorizontalLayout  layout = new HorizontalLayout ();
        layout.add(this.treeNodeDetailsFactory.createButtonForNode(node, "technology"), button);
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
        ReferenceDetailsDialog referenceDetailsDialog = new ReferenceDetailsDialog(
                this.diagramComponent,
                this.qrCodeComponent,
                this.pathString,
                eventPublisher
        );

        referenceDetailsDialog.open(paperTitle, paperLink);
    }

    private void selectRow(TreeNode<?> node, TreeGrid<TreeNode<?>> treeGrid) {
        treeGrid.collapse(treeGrid.getSelectedItems());
        treeGrid.getSelectionModel().select(node);
    }

//    private List<TreeNode<?>> getPathToRoot(TreeNode<?> node) {
//        List<TreeNode<?>> path = new ArrayList<>();
//        TreeNode<?> current = node;
//        while (current != null) {
//            path.add(current);
//
//            if (current.getParent().getData() != null) {
//                current = current.getParent();
//            } else {
//                current = null;
//            }
//        }
//
//        return path;
//    }

    private void loadTree(TreeViewType type) {
        this.removeAll();  // Remove existing tree
        this.setTreeViewType(type);
        this.load();
    }

    @EventListener
    public void handleTreeRootChange(TreeRootSelectionChangeEvent event) {
        getUI().ifPresent(ui -> ui.access(() -> {
            loadTree(event.getNewType());
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.removeAll();
    }
}
