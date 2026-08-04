package br.ufrj.cos.views.user;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.GridCRUDUtils;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.layout.impl.WindowBasedCrudLayout;

import java.util.Date;

@PageTitle("User Registration")
@Route(value = "userregistration-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class UserRegistrationView extends BaseView {

    private final UserApplicationService userApplicationService;
    private final PasswordEncoder passwordEncoder;

    GridCrud<UserApplication> gridUsers;

    public UserRegistrationView(UserApplicationService userApplicationService, PasswordEncoder passwordEncoder) {
        this.userApplicationService = userApplicationService;
        this.passwordEncoder = passwordEncoder;

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
        getContent().getStyle().set("padding", "1.25rem");
        getContent().getStyle().set("box-sizing", "border-box");
        getContent().getStyle().setOverflow(Style.Overflow.HIDDEN);

        configureHeroBanner();

        gridUsers = createUserGridCrud();
        getContent().add(gridUsers);
    }

    private void configureHeroBanner() {
        Div heroCard = new Div();
        heroCard.addClassName("appconfig-hero-card");
        heroCard.setWidthFull();
        heroCard.getStyle()
                .set("margin", "0 0 1.25rem 0")
                .set("box-sizing", "border-box");

        Span badge = new Span("System Access Control");
        badge.addClassName("appconfig-badge");

        H3 heroTitle = new H3("👥 User Registration & Management");
        heroTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "1.5rem")
                .set("font-weight", "800")
                .set("color", "white");

        Paragraph heroSubtitle = new Paragraph("Register and manage application user accounts, role-based security credentials, and access permissions.");
        heroSubtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9rem")
                .set("color", "rgba(255, 255, 255, 0.85)");

        VerticalLayout heroContent = new VerticalLayout(badge, heroTitle, heroSubtitle);
        heroContent.setPadding(false);
        heroContent.setSpacing(false);
        heroCard.add(heroContent);

        getContent().add(heroCard);
    }

    private GridCrud<UserApplication> createUserGridCrud() {
        GridCrud<UserApplication> gridUsers = new GridCrud<>(UserApplication.class, new WindowBasedCrudLayout());
        gridUsers.setShowNotifications(false);
        gridUsers.setSizeFull();

        gridUsers.getGrid().getColumnByKey("password").setVisible(false);
        gridUsers.getGrid().getColumnByKey("id").setWidth("70px").setFlexGrow(0);

        gridUsers.getCrudFormFactory().setVisibleProperties("name", "email", "userName", "role", "password", "notes");
        gridUsers.getGrid().setDetailsVisibleOnClick(true);

        // Create and configure the Add operation
        gridUsers.setAddOperation(userApplication -> {
            if (this.userApplicationService.findByUserName(userApplication.getUserName()) != null) {
                NotificationUtils.showErrorNotification("The username has been already registered!");
                return null;
            } else {
                userApplication.setDateOfCreation(new Date());
                userApplication.setPassword(
                    passwordEncoder.encode(userApplication.getPassword()));
                userApplicationService.save(userApplication);
                refreshAllData();
                NotificationUtils.showSuccessNotification("User registered successfully.");
                return userApplication;
            }
        });

        gridUsers.getCrudFormFactory().setFieldProvider("password", i -> new PasswordField());

        // Create and configure the Update operation
        gridUsers.setUpdateOperation(userApplicationService::saveAndUpdate);
        gridUsers.setFindAllOperation(userApplicationService::findAll);

        // Create and configure the Delete operation
        gridUsers.setDeleteOperation(userApplication -> {
            userApplicationService.delete(userApplication);
            refreshAllData();
            NotificationUtils.showSuccessNotification("User deleted.");
        });

        // Add filtering functionality
        TextField nameFilter = GridCRUDUtils.createGridTextFilter(gridUsers, "Filter by name", "300px");
        gridUsers.setFindAllOperation(() -> this.userApplicationService.findByName(nameFilter.getValue()));

        GridCRUDUtils.setColumnsOrder(gridUsers, "id", "name", "userName", "email", "role", "dateOfCreation", "notes");

        ((WindowBasedCrudLayout) gridUsers.getCrudLayout()).setFormWindowWidth("50%");

        return gridUsers;
    }

    private void refreshAllData() {
        gridUsers.getGrid().getDataProvider().refreshAll();
    }
}
