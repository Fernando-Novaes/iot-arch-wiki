package br.ufrj.cos.components.treeview;


import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.components.diagram.EdgeDiagram;
import br.ufrj.cos.components.diagram.NodeDiagram;
import br.ufrj.cos.components.notification.NotificationDialog;
import br.ufrj.cos.components.qrcode.QRCodeComponent;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.TreeViewService;
import br.ufrj.cos.utils.ColorUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@UIScope
@Component
@CssImport(value = "./styles/app-styles.css", themeFor = "vaadin-grid")
public class TreeViewComponent extends VerticalLayout {

    private final QRCodeComponent qrCodeComponent;
    private final DiagramComponent diagramComponent;
    private final TreeViewService treeViewService;
    @Getter private Boolean loaded = Boolean.FALSE;
    StringBuilder pathString;

    @Autowired
    public TreeViewComponent(QRCodeComponent qrCodeComponent, IoTDomainService ioTDomainService, DiagramComponent diagramComponent, TreeViewService treeViewService) {
        this.qrCodeComponent = qrCodeComponent;
        this.diagramComponent = diagramComponent;
        this.treeViewService = treeViewService;
    }

    public void load() {
        // Define columns (e.g., displaying IoT Domain names)
        TreeGrid<TreeNode<?>> treeGrid = new TreeGrid<>();

        treeGrid.addComponentHierarchyColumn(node -> {
            Object data = node.getData();
            if (data instanceof IoTDomain) {
                return new Text(((IoTDomain) data).getName());
            } else if (data instanceof ArchitectureSolution) {
                return new Text(((ArchitectureSolution) data).getName());
            } else if (data instanceof QualityRequirement) {
                return new Text(((QualityRequirement) data).getName());
            } else if (data instanceof Technology) {
                String nodeNames = this.createPathToNode(node);
                return this.createNodeWithIcon(treeGrid, node, nodeNames);
            }
            return new Text("");
        }).setHeader("IoT Domain -> Architectural Solution -> Quality Requirement -> Technology/Feature");

        treeGrid.getElement().executeJs(
                "this.shadowRoot.querySelectorAll('thead th').forEach(th => {" +
                        "    th.style.fontSize = '16px';" +
                        "    th.style.fontWeight = 'bold';" +
                        "    th.style.color = '#373a3f';" +
                        "    th.style.border = '2px solid gray';" +
                        "});"
        );

        TreeNode<Object> root = treeViewService.getTree(TreeViewType.IoTDomain);
        treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());

        // Add ExpandListener
        treeGrid.addExpandListener(event -> {
            // Get the expanded node
            TreeNode<?> expandedNode = event.getItems().stream().findFirst().get();

            //if (expandedNode.getChildren().getFirst().getData() instanceof Technology) {
                //getting the root node of the Technology node
//                while (expandedNode.getParent().getData() != null) {
//                    expandedNode = expandedNode.getParent();
//                }

                // Get all the root nodes
                List<TreeNode<?>> rootNodes = List.of(root);

                // Collapse all other nodes
                for (TreeNode<?> node : rootNodes) {
                    collapseAll(treeGrid, node, expandedNode.getParent());
                }
            //}
        });

        treeGrid.expand(root);

        treeGrid.setClassNameGenerator(node -> {
            Object data = node.getData();

            if (data instanceof IoTDomain) {
                return "iot-domain";
            } else if (data instanceof ArchitectureSolution) {
                return "architecture-solution";
            } else if (data instanceof QualityRequirement) {
                return "quality-requirement";
            } else if (data instanceof Technology) {
                return "technology";
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

    // Method to collapse all nodes except the expanded one
    private void collapseAll(TreeGrid<TreeNode<?>> grid, TreeNode<?> node, TreeNode<?> expandedNode) {
        if ((!node.equals(expandedNode) && (node.getData() != null))) {
            grid.collapse(node);
        }

        for (TreeNode<?> child : node.getChildren()) {
            collapseAll(grid, child, expandedNode);
        }
    }


    /***
     * Create the Path of the referenced Node
     * @param node Node
     * @return The name to be splited and used in the Diagram (Description!Type = HealthCare!IoT Domain)
     */
    private String createPathToNode(TreeNode<?> node) {
        List<TreeNode<?>> path = getPathToRoot(node);
        StringBuilder diagramNames = new StringBuilder();

        // Construct the full path string
        this.pathString = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            Object data = path.get(i).getData();
            if (data instanceof IoTDomain) {
                diagramNames.append(((IoTDomain) data).getName()).append("!IoT Domain").append("#");
                pathString.append(((IoTDomain) data).getName().toUpperCase()).append(" >> ");
            } else if (data instanceof ArchitectureSolution) {
                diagramNames.append(((ArchitectureSolution) data).getName()).append("!Architecture Solution").append("#");
                pathString.append(((ArchitectureSolution) data).getName().toUpperCase()).append(" >> ");
            } else if (data instanceof QualityRequirement) {
                diagramNames.append(((QualityRequirement) data).getName()).append("!Quality Requirement").append("#");
                pathString.append(((QualityRequirement) data).getName().toUpperCase()).append(" >> ");
            } else if (data instanceof Technology) {
                diagramNames.append(((Technology) data).getDescription()).append("!Technology").append("#");
                pathString.append(((Technology) data).getDescription().toUpperCase());
            }
        }

        return diagramNames.toString();
    }

    /***
     * Method to create a component for the node with text and icon
     */
    private HorizontalLayout createNodeWithIcon(TreeGrid<TreeNode<?>>  treeGrid, TreeNode<?> node, String diagramNames) {
        // Create an icon
        Icon icon = VaadinIcon.INFO_CIRCLE.create(); // Use any icon you prefer
        icon.getElement().getStyle().set("cursor", "pointer"); // Change cursor style to pointer for clickable effect

        // Create a button to handle the click event
        Button button = new Button(icon);
        button.setTooltipText("More details");
        button.addClickListener(event -> {
            this.selectRow(node, treeGrid);
            // Action when the icon is clicked
            //Notification.show("Icon clicked for: " + tech.getDescription());

            addDetailsDialog(
                    ((Technology) node.getData()).getArchitectureSolution().getPaperReference().getPaperTitle(),
                    ((Technology) node.getData()).getArchitectureSolution().getPaperReference().getPaperLink());
        });

        this.createDiagram(diagramNames);

        button.getStyle().set("min-width", "15px"); // Set the button size
        button.getStyle().set("height", "25px"); // Set the button size

        // Create a layout to hold the text and the icon
        HorizontalLayout  layout = new HorizontalLayout ();
        layout.add(new Text(((Technology) node.getData()).getDescription()), button);
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(true); // Remove spacing between text and icon

        return layout;
    }

    /***
     * Creates the Diagram of the selected solution on the Tree
     * @param diagramNames Names of the items of the selected Node
     * @return DiagramComponent
     */
    private DiagramComponent createDiagram(String diagramNames) {
        List<NodeDiagram> nodes = this.getNodesToDiagram(diagramNames);
        List<EdgeDiagram> edges = getEdgeDiagrams(nodes.size());

        this.diagramComponent.setNodes(nodes);
        this.diagramComponent.setEdges(edges);
        this.diagramComponent.execute();

        return this.diagramComponent;
    }

    private static List<EdgeDiagram> getEdgeDiagrams(int edgesCount) {
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
            NodeDiagram dom = NodeDiagram.builder().id(String.valueOf(names.indexOf(n))).label(namesAndTypes[0]).color(ColorUtils.generateRandomColorCode()).tooltip(namesAndTypes[1]).build();
            nodes.add(dom);
        });

        return nodes;
    }

    private void addDetailsDialog(String paperTitle, String paperLink) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Details");
        dialog.addAttachListener(attachEvent -> this.diagramComponent.execute());

        HorizontalLayout hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.CENTER);
        //hl.setSpacing(true);

        VerticalLayout vlLeft = new VerticalLayout();
        VerticalLayout vlRight = new VerticalLayout();
        vlLeft.setAlignItems(Alignment.START);
        vlRight.setAlignItems(Alignment.END);

        H4 paperTitleH1 = new H4(paperTitle);
        Anchor link = new Anchor(paperLink, paperLink);
        link.setTarget("_blank"); // Opens the link in a new tab

        Div divDiagram = new Div();
        divDiagram.setId("diagram");

        vlLeft.add(paperTitleH1, link, divDiagram, new Text(this.pathString.toString()));
        vlRight.add(this.qrCodeComponent.generateQRCode(paperLink, 100, 100));

        dialog.add(vlLeft, vlRight, this.diagramComponent);

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
            current = current.getParent();
        }
        return path;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.removeAll();
    }
}
