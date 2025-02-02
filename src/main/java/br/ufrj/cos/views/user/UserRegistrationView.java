package br.ufrj.cos.views.user;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.GridCRUDUtils;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;

import java.util.Date;

@PageTitle("UserApplication Registration")
@Route(value = "userregistration-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class UserRegistrationView extends BaseView {

    private final UserApplicationService userApplicationService;

    GridCrud<UserApplication> gridUsers;

    public UserRegistrationView(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createHeader("User Registration");

        gridUsers = createUserGridCrud();
        getContent().add(gridUsers);
    }

    private GridCrud<UserApplication> createUserGridCrud() {
        GridCrud<UserApplication> gridUsers = new GridCrud<>(UserApplication.class);
        gridUsers.setShowNotifications(false);
        gridUsers.setSizeFull();

        gridUsers.getGrid().getColumnByKey("password").setVisible(false);
        gridUsers.getGrid().getColumnByKey("id").setWidth("60px").setFlexGrow(0);

        gridUsers.getCrudFormFactory().setVisibleProperties("name", "email", "userName", "role", "password", "notes");
        gridUsers.getGrid().setDetailsVisibleOnClick(true);

        // Create and configure the Add operation
        gridUsers.setAddOperation(userApplication -> {
            userApplication.setDateOfCreation(new Date());
            userApplicationService.save(userApplication);
            refreshAllData();
            NotificationUtils.showSuccessNotification("User registered.");
            return userApplication;
        });

        gridUsers.getCrudFormFactory().setFieldProvider("password", i -> new PasswordField());

        // Create and configure the Update operation
        gridUsers.setUpdateOperation(userApplicationService::saveAndUpdate);
        gridUsers.setFindAllOperation(userApplicationService::findAll);

        // Create and configure the Delete operation
        gridUsers.setDeleteOperation(userApplication -> {
            userApplicationService.delete(userApplication);
            refreshAllData();
            NotificationUtils.showSuccessNotification("UserApplication deleted.");
        });

        // Add filtering functionality
        TextField nameFilter = GridCRUDUtils.createGridTextFilter(gridUsers, "Filter by name", "300px");
        gridUsers.setFindAllOperation(() -> this.userApplicationService.findByName(nameFilter.getValue()));
//        nameFilter.addValueChangeListener(event -> {
//            gridUsers.setFindAllOperation(() -> this.userApplicationService.findByName(event.getValue()));
//            gridUsers.getGrid().getDataProvider().refreshAll();
//        });

        // Set the initial operation for findAll
        //gridUsers.setFindAllOperation(userApplicationService::findAll);

        // Add filter to the layout
        nameFilter.setWidthFull();
        nameFilter.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        // Setup additional configurations for the grid if needed
        GridCRUDUtils.setColumnsOrder(gridUsers, "id", "name", "userName", "email", "role", "dateOfCreation", "notes", "password");

        return gridUsers;
    }

    private void refreshAllData() {
        gridUsers.getGrid().getDataProvider().refreshAll();
    }
}

