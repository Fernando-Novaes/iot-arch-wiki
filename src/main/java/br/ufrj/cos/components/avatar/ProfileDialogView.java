package br.ufrj.cos.components.avatar;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.reflections.Reflections.log;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;

@PageTitle("User Profile")
@Route(value = "profile-dialog/:username", layout = MainLayout.class)
@PermitAll
public class ProfileDialogView extends BaseView {

    private static final Logger logger = LoggerFactory.getLogger(ProfileDialogView.class);

    private final UserApplicationService userService;
    private final PasswordEncoder passwordEncoder;
    private UserApplication user = null;

    public ProfileDialogView(UserApplicationService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        //Header
        this.createHeader("User Profile");

        Div box = new Div();
        box.addClassName("centered-aboutbox");
        box.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("padding", "20px");

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
        formLayout.getStyle().set("width", "100%"); // Make the form take up the full width of its container

        try {
            this.user = this.userService.findByUserName(SecurityUtils.getUsername());
            if (this.user != null) {
                // Profile Section Header
                H3 profileHeader = new H3("Profile Information");
                profileHeader.getStyle().set("margin-bottom", "10px"); // Space below the header

                createProfileSection(formLayout);
                // Action buttons with responsive layout
                HorizontalLayout buttonLayout = createButtonLayout();
                // Add components to main layout
                content.add(profileHeader, formLayout, buttonLayout);
                // Footer
                //createFooter();

                box.add(content);
                hLayout.add(box);
            } else {
                logger.warn("User not found for username: {}", SecurityUtils.getUsername());
                // Handle the case where the user is not found (e.g., display an error message)
                content.add(new Div("User profile not found.")); // Simple error message
            }
        } catch (Exception e) {
            logger.error("Error retrieving user profile.", e);
            content.add(new Div("Error retrieving user profile.  Please contact support.")); // Generic error message
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
                .set("border-radius", "var(--lumo-border-radius)")
                .set("margin-bottom", "10px"); // Add some spacing between fields

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
        changePasswordBtn.getStyle().set("margin-top", "15px"); // Move button slightly down

        // Layout for buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(changePasswordBtn);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);

        return buttonLayout;
    }

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

    private boolean updatePassword(String newPassword) {
        try {
            user.setPassword(newPassword);
            userService.saveAndUpdate(user);
            NotificationUtils.showSuccessNotification("Password updated successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error updating password", e);
            NotificationUtils.showErrorNotification("Failed to update password");
            return false;
        }
    }
}