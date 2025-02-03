package br.ufrj.cos.components.avatar;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;

import static org.reflections.Reflections.log;

@PageTitle("User Profile")
@Route(value = "profile-dialog/:username", layout = MainLayout.class)
@PermitAll
public class ProfileDialogView extends BaseView implements BeforeEnterObserver {

    private final UserApplicationService userService;
    private final PasswordEncoder passwordEncoder;
    private UserApplication user = null;
    private String userName;

    public ProfileDialogView(UserApplicationService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        //Header
        this.createHeader("User Profile");

        Div box = new Div();
        box.addClassName("centered-aboutbox");

        // Create a HorizontalLayout to center the box horizontally
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSizeFull(); // Make it take the full width of the screen
        hLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Center the content horizontally

        // Main container with padding and spacing
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(true);
        content.setSizeFull();

        // User info form with responsive layout
        FormLayout formLayout = createResponsiveFormLayout();

        this.user = this.userService.findByUserName(SecurityUtils.getUsername());
        // Profile section
        if (this.user != null) {
            createProfileSection(formLayout);
            // Action buttons with responsive layout
            HorizontalLayout buttonLayout = createButtonLayout();
            // Add components to main layout
            content.add(formLayout, buttonLayout);
            // Footer
            //createFooter();

            box.add(content);
            hLayout.add(box);
        }

        getContent().add(hLayout);
    }

    private FormLayout createResponsiveFormLayout() {
        FormLayout formLayout = new FormLayout();

        // Make form layout responsive
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), // One column on small screens
                new FormLayout.ResponsiveStep("500px", 2) // Two columns on larger screens
        );

        return formLayout;
    }

    private void createProfileSection(FormLayout formLayout) {
        // User info fields with proper styling
        TextField nameField = createStyledTextField("Name", user.getName());
        TextField emailField = createStyledTextField("Email", user.getEmail());
        TextField userNameField = createStyledTextField("Username", user.getUserName());
        TextField roleField = createStyledTextField("Role", user.getRole().name());

        formLayout.add(nameField, emailField, userNameField, roleField);
    }

    private TextField createStyledTextField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        field.setWidthFull();

        // Add some styling
        field.getStyle()
                //.set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius)");

        return field;
    }

    private HorizontalLayout createButtonLayout() {
        // Change Password Button
        Button changePasswordBtn = new Button(
                "Change Password",
                VaadinIcon.KEY.create()
        );
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordBtn.addClickListener(e -> showChangePasswordDialog());

        // Layout for buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(changePasswordBtn);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);

        return buttonLayout;
    }

//    private void createFooter() {
//        // Close button in footer
//        Button closeButton = new Button("Close", e -> close());
//        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//
//        HorizontalLayout footer = new HorizontalLayout(closeButton);
//        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
//        footer.setPadding(true);
//
//        getFooter().add(footer);
//    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(ChangePassword.ChangePasswordMode.CHANGE_KNOWN);

        // Add password rules
        changePasswordDialog.addPasswordRules(
                ChangePasswordRule.length(8, 20),
                ChangePasswordRule.hasSpecials(1),
                ChangePasswordRule.hasUppercaseLetters(1)
        );

        changePasswordDialog.addCancelListener(cancel -> {
            changePasswordDialog.reset();
            changePasswordDialog.close();
        });

        changePasswordDialog.addOkListener(ok -> {
            String currentPassword = changePasswordDialog.getCurrentPassword();
            String newPassword = changePasswordDialog.getDesiredPassword();

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                NotificationUtils.showErrorNotification("Wrong current password!");
                changePasswordDialog.reset();
            } else if (currentPassword.equals(newPassword)) {
                NotificationUtils.showErrorNotification("New password must be different from the old password.");
                changePasswordDialog.reset();
            } else {
                updatePassword(passwordEncoder.encode(newPassword));
                changePasswordDialog.close();
            }
        });

        changePasswordDialog.open();
    }

//    private boolean validateOldPassword(String oldPassword) {
//        try {
//            return passwordEncoder.matches(oldPassword, user.getPassword());
//        } catch (Exception e) {
//            log.error("Error validating password", e);
//            return false;
//        }
//    }

    private boolean updatePassword(String newPassword) {
        try {
            user.setPassword(newPassword);
            userService.saveAndUpdate(user);
            NotificationUtils.showSuccessNotification("Password updated successfully");
            return true;
        } catch (Exception e) {
            log.error("Error updating password", e);
            NotificationUtils.showErrorNotification("Failed to update password");
            return false;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        userName = beforeEnterEvent.getRouteParameters().get("username").
                orElse(null);
    }
}