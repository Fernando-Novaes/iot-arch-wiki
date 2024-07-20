package br.ufrj.cos.components.treeview;


import br.ufrj.cos.components.notification.NotificationDialog;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.service.IoTDomainService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@CssImport(value = "./styles/app-styles.css", themeFor = "vaadin-grid")
public class TreeViewComponent extends VerticalLayout {

    private final IoTDomainService ioTDomainService;

    @Autowired
    public TreeViewComponent(IoTDomainService ioTDomainService) {
        this.ioTDomainService = ioTDomainService;
    }

    public void load() {
        // Define columns (e.g., displaying IoT Domain names)
        TreeGrid<TreeNode<?>> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(node -> {
            Object data = node.getData();
            if (data instanceof IoTDomain) {
                return ((IoTDomain) data).getName();
            } else if (data instanceof ArchitectureSolution) {
                return ((ArchitectureSolution) data).getName();
            } else if (data instanceof QualityRequirement) {
                return ((QualityRequirement) data).getName();
            } else if (data instanceof Technology) {
                return ((Technology) data).getDescription();
            }
            return "";
        }).setHeader("IoT Domain -> Architectural Solution -> Quality Requirement -> Technology/Feature");

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
            return "";
        });

        // Add a click listener to the TreeGrid nodes
        treeGrid.addItemClickListener(event -> {
            TreeNode<?> node = event.getItem();
            if (node.getData() instanceof Technology) {
                List<TreeNode<?>> path = getPathToRoot(node);

                // Construct the full path string
                StringBuilder pathString = new StringBuilder();
                for (int i = path.size() - 1; i >= 0; i--) {
                    Object data = path.get(i).getData();
                    if (data instanceof IoTDomain) {
                        pathString.append(((IoTDomain) data).getName());
                    } else if (data instanceof ArchitectureSolution) {
                        pathString.append(((ArchitectureSolution) data).getName());
                    } else if (data instanceof QualityRequirement) {
                        pathString.append(((QualityRequirement) data).getName());
                    } else if (data instanceof Technology) {
                        pathString.append(((Technology) data).getDescription());
                    }
                    if (i > 0) {
                        pathString.append(" >> ");
                    }
                }

                // Print or display the full path
                NotificationDialog.showNotificationDialogOnBotton(
                        ((Technology) node.getData()).getDescription(), pathString.toString()
                );
            }
        });

        // Add styling variant to the TreeGrid for better visibility
        treeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Add the TreeGrid to the main layout
        add(treeGrid);
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

}
