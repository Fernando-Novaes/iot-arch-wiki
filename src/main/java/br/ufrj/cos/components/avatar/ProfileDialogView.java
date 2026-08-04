package br.ufrj.cos.components.avatar;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
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

        // --- Hero Banner (replaces old H1 header) ---
        Div heroBanner = new Div();
        heroBanner.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "16px")
                .set("padding", "1.75rem 2rem")
                .set("color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.2)")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("margin", "0 auto");

        com.vaadin.flow.component.html.H2 bannerTitle = new com.vaadin.flow.component.html.H2("User Profile");
        bannerTitle.getStyle()
                .set("margin", "0 0 0.4rem 0")
                .set("font-size", "1.8rem")
                .set("font-weight", "700")
                .set("color", "#ffffff");

        com.vaadin.flow.component.html.Paragraph bannerSubtitle = new com.vaadin.flow.component.html.Paragraph(
                "View and manage your account details and security settings"
        );
        bannerSubtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.95rem")
                .set("opacity", "0.9")
                .set("line-height", "1.5");

        heroBanner.add(bannerTitle, bannerSubtitle);

        HorizontalLayout bannerWrapper = new HorizontalLayout(heroBanner);
        bannerWrapper.setWidthFull();
        bannerWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        bannerWrapper.setPadding(true);
        bannerWrapper.setSpacing(false);
        bannerWrapper.getStyle().set("margin-bottom", "0");

        getContent().add(bannerWrapper);

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSizeFull();
        hLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        hLayout.setPadding(true);

        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("box-shadow", "0 10px 30px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04)")
                .set("padding", "2rem 2.5rem")
                .set("width", "clamp(420px, 600px, 92vw)")
                .set("margin-top", "0");

        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(false);
        content.setWidthFull();

        try {
            this.user = this.userService.findByUserName(SecurityUtils.getUsername());
            if (this.user != null) {
                // Hero header with avatar image and role badge
                Component heroHeader = createProfileHero();

                // User info form with responsive layout
                FormLayout formLayout = createResponsiveFormLayout();
                createProfileSection(formLayout);

                // Action buttons
                HorizontalLayout buttonLayout = createButtonLayout();

                content.add(heroHeader, formLayout, buttonLayout);
                card.add(content);
                hLayout.add(card);
            } else {
                logger.warn("User not found for username: {}", SecurityUtils.getUsername());
                content.add(new Div("User profile not found."));
                card.add(content);
                hLayout.add(card);
            }
        } catch (Exception e) {
            logger.error("Error retrieving user profile.", e);
            content.add(new Div("Error retrieving user profile. Please contact support."));
            card.add(content);
            hLayout.add(card);
        }

        getContent().add(hLayout);
    }

    private Component createProfileHero() {
        VerticalLayout hero = new VerticalLayout();
        hero.setAlignItems(FlexComponent.Alignment.CENTER);
        hero.setSpacing(false);
        hero.getStyle().set("margin-bottom", "1.5rem");

        boolean isAdmin = user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().name());
        String imagePath = isAdmin ? "/images/admin.png" : "/images/homem.png";

        Image avatarImg = new Image(imagePath, "User Avatar");
        avatarImg.getStyle()
                .set("width", "84px")
                .set("height", "84px")
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("box-shadow", "0 6px 16px rgba(0, 0, 0, 0.15)")
                .set("border", "3px solid var(--lumo-base-color)")
                .set("margin-bottom", "0.75rem");

        H3 nameTitle = new H3(user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getUserName());
        nameTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.35rem")
                .set("font-weight", "700")
                .set("color", "var(--lumo-header-text-color)");

        Span roleBadge = new Span(isAdmin ? "ADMINISTRATOR" : "USER");
        roleBadge.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("background", isAdmin ? "linear-gradient(135deg, #e67e22, #f39c12)" : "linear-gradient(135deg, #2563eb, #3b82f6)")
                .set("color", "#ffffff")
                .set("margin-top", "0.4rem")
                .set("letter-spacing", "0.5px");

        hero.add(avatarImg, nameTitle, roleBadge);
        return hero;
    }

    private FormLayout createResponsiveFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        return formLayout;
    }

    private void createProfileSection(FormLayout formLayout) {
        TextField nameField = createStyledTextField("Full Name", user.getName(), VaadinIcon.USER);
        TextField emailField = createStyledTextField("Email Address", user.getEmail(), VaadinIcon.ENVELOPE);
        TextField userNameField = createStyledTextField("Username", user.getUserName(), VaadinIcon.USER_CHECK);
        TextField roleField = createStyledTextField("User Role", user.getRole() != null ? user.getRole().name() : "", VaadinIcon.SHIELD);

        formLayout.add(nameField, emailField, userNameField, roleField);
    }

    private TextField createStyledTextField(String label, String value, VaadinIcon icon) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        field.setWidthFull();
        field.setPrefixComponent(icon.create());

        field.getStyle()
                .set("margin-bottom", "10px");

        return field;
    }

    private HorizontalLayout createButtonLayout() {
        Button changePasswordBtn = new Button("Change Password", VaadinIcon.KEY.create());
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordBtn.addClickListener(e -> showChangePasswordDialog());
        changePasswordBtn.getStyle()
                .set("margin-top", "1.5rem")
        		.set("padding", "0.6rem 1.5rem")
                .set("border-radius", "8px")
                .set("font-weight", "600")
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("box-shadow", "0 4px 12px rgba(37, 99, 235, 0.25)")
                .set("cursor", "pointer");

        HorizontalLayout buttonLayout = new HorizontalLayout(changePasswordBtn);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);

        return buttonLayout;
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(ChangePassword.ChangePasswordMode.CHANGE_KNOWN);

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