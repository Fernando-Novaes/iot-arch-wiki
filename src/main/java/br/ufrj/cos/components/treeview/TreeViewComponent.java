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

import java.util.ArrayList;
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
                //return new Text(((Technology) data).getDescription());
                List<String> nodeNames = this.createPathToNode(node);
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

        TreeNode<Object> root = treeViewService.getTree(TreeViewType.ArchitectureSolution);
        treeGrid.setItems(List.of(root), node -> ((TreeNode<?>) node).getChildren());

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

    private List<String> createPathToNode(TreeNode<?> node) {
        List<TreeNode<?>> path = getPathToRoot(node);
        List<String> diagramNames = new ArrayList<>();

        // Construct the full path string
        this.pathString = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            Object data = path.get(i).getData();
            if (data instanceof IoTDomain) {
                diagramNames.add(((IoTDomain)data).getName());
                pathString.append(((IoTDomain) data).getName().toUpperCase() + " >> ");
            } else if (data instanceof ArchitectureSolution) {
                diagramNames.add(((ArchitectureSolution)data).getName());
                pathString.append(((ArchitectureSolution) data).getName().toUpperCase() + " >> ");
            } else if (data instanceof QualityRequirement) {
                diagramNames.add(((QualityRequirement)data).getName());
                pathString.append(((QualityRequirement) data).getName().toUpperCase() + " >> ");
            } else if (data instanceof Technology) {
                diagramNames.add(((Technology)data).getDescription());
                pathString.append(((Technology) data).getDescription().toUpperCase());
            }
        }

        return diagramNames;
    }

    /***
     * Method to create a component for the node with text and icon
     */
    private HorizontalLayout createNodeWithIcon(TreeGrid<TreeNode<?>>  treeGrid, TreeNode<?> node, List<String> diagramNames) {
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

    private DiagramComponent createDiagram(List<String> diagramNames) {
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
    private List<NodeDiagram> getNodesToDiagram(List<String> diagramNames) {
        List<NodeDiagram> nodes = new ArrayList<>();

        diagramNames.forEach(n -> {
            int index = 0;
            NodeDiagram dom = NodeDiagram.builder().id(String.valueOf(diagramNames.indexOf(n))).label(n).color(ColorUtils.generateRandomColorCode()).tooltip("Not Implemented Yet!").build();
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
//        vlLeft.setSpacing(true);
//        vlRight.setSpacing(true);
        //vlRight.setWidth("50%");
        //vlRight.setSizeFull();
        //vlLeft.setSizeFull();
        //vlLeft.setHeight("50%");

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
