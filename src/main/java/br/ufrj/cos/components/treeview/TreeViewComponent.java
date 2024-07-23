package br.ufrj.cos.components.treeview;


import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.service.IoTDomainService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@UIScope
@Component
@CssImport(value = "./styles/app-styles.css", themeFor = "vaadin-grid")
public class TreeViewComponent extends VerticalLayout {

    private final IoTDomainService ioTDomainService;
    @Getter private Boolean loaded = Boolean.FALSE;

    @Autowired
    public TreeViewComponent(IoTDomainService ioTDomainService) {
        this.ioTDomainService = ioTDomainService;
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
                return createNodeWithIcon((Technology) data, node, treeGrid);
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

        TreeNode<Object> root = ioTDomainService.getTree();
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

        // Add a click listener to the TreeGrid nodes
//        treeGrid.addItemClickListener(event -> {
//            TreeNode<?> node = event.getItem();
//            if (node.getData() instanceof Technology) {
//
//                List<TreeNode<?>> path = getPathToRoot(node);
//
//                // Construct the full path string
//                StringBuilder pathString = new StringBuilder();
//                for (int i = path.size() - 1; i >= 0; i--) {
//                    Object data = path.get(i).getData();
//                    if (data instanceof IoTDomain) {
//                        pathString.append(((IoTDomain) data).getName());
//                    } else if (data instanceof ArchitectureSolution) {
//                        pathString.append(((ArchitectureSolution) data).getName());
//                    } else if (data instanceof QualityRequirement) {
//                        pathString.append(((QualityRequirement) data).getName());
//                    } else if (data instanceof Technology) {
//                        pathString.append(((Technology) data).getDescription());
//                    }
//                    if (i > 0) {
//                        pathString.append(" >> ");
//                    }
//                }
//
//                // Print or display the full path
//                NotificationDialog.showNotificationDialogAtTheBotton(
//                        ((Technology) node.getData()).getDescription(), pathString.toString()
//                );
//            }
//        });

        // Add styling variant to the TreeGrid for better visibility
        treeGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        treeGrid.getStyle().setBackgroundColor("#373a3f");

        // Add the TreeGrid to the main layout
        add(treeGrid);
        setSizeFull();
        getStyle().set("flex-grow", "1");

        this.loaded = Boolean.TRUE;
    }

    /***
     * Method to create a component for the node with text and icon
     */
    private HorizontalLayout  createNodeWithIcon(Technology tech, TreeNode<?> node, TreeGrid<TreeNode<?>>  treeGrid) {
        // Create an icon
        Icon icon = VaadinIcon.INFO_CIRCLE.create(); // Use any icon you prefer
        icon.getElement().getStyle().set("cursor", "pointer"); // Change cursor style to pointer for clickable effect

        // Create a button to handle the click event
        Button button = new Button(icon);
        button.setTooltipText("More details");
        button.addClickListener(event -> {
            this.selectRow(tech, node, treeGrid);
            // Action when the icon is clicked
            //Notification.show("Icon clicked for: " + tech.getDescription());

            Dialog dialog = new Dialog();
            dialog.setModal(true);
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setHeaderTitle("Details");
            Button close = new Button("Close", (e) -> dialog.close());
            close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getFooter().add(close);
            dialog.open();
        });

        button.getStyle().set("min-width", "15px"); // Set the button size
        button.getStyle().set("height", "25px"); // Set the button size

        // Create a layout to hold the text and the icon
        HorizontalLayout  layout = new HorizontalLayout ();
        layout.add(new Text(tech.getDescription()), button);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true); // Remove spacing between text and icon

        return layout;
    }
    private void selectRow(Technology tech, TreeNode<?> node, TreeGrid<TreeNode<?>> treeGrid) {
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
