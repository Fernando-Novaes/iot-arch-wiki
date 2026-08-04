package br.ufrj.cos.components.avatar;

import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.appconfig.AppConfigView;
import br.ufrj.cos.views.datamanager.DataManagerView;
import br.ufrj.cos.views.login.LogoutView;
import br.ufrj.cos.views.user.UserRegistrationView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

@org.springframework.stereotype.Component
@PermitAll
public class AvatarComponent {

    public final String USER_ICON = "/images/homem.png";
    public final String ADMIN_ICON = "/images/admin.png";

    public AvatarComponent() {}

    public Div createAvatar() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();

        if (userDetails == null) {
            return new Div();
        }

        Div div = new Div();
        boolean isAdmin = SecurityUtils.hasRole("ADMIN");
        String imagePath = isAdmin ? ADMIN_ICON : USER_ICON;

        Avatar avatar = new Avatar(userDetails.getUsername());
        avatar.setImage(imagePath);
        avatar.setTooltipEnabled(true);
        avatar.getStyle()
                .set("cursor", "pointer")
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)");

        Span userNameSpan = new Span(userDetails.getUsername());
        userNameSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "0.88rem")
                .set("color", "var(--lumo-header-text-color)");

        Span roleBadge = new Span(isAdmin ? "ADMIN" : "USER");
        roleBadge.getStyle()
                .set("font-size", "0.62rem")
                .set("font-weight", "800")
                .set("padding", "2px 7px")
                .set("border-radius", "10px")
                .set("background", isAdmin ? "linear-gradient(135deg, #e67e22, #f39c12)" : "linear-gradient(135deg, #2563eb, #3b82f6)")
                .set("color", "#ffffff")
                .set("margin-left", "6px");

        Icon chevron = VaadinIcon.CHEVRON_DOWN.create();
        chevron.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("margin-left", "4px");

        HorizontalLayout userDetailsBox = new HorizontalLayout(userNameSpan, roleBadge);
        userDetailsBox.setAlignItems(FlexComponent.Alignment.CENTER);
        userDetailsBox.setSpacing(false);

        HorizontalLayout avatarLayout = new HorizontalLayout(userDetailsBox, avatar, chevron);
        avatarLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        avatarLayout.setSpacing(true);
        avatarLayout.getStyle()
                .set("padding", "4px 10px 4px 14px")
                .set("border-radius", "24px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("cursor", "pointer")
                .set("transition", "background 0.2s ease, box-shadow 0.2s ease");

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menuBar.getStyle().set("margin-left", "auto");

        MenuItem avatarMenuItem = menuBar.addItem(avatarLayout);
        SubMenu subMenu = avatarMenuItem.getSubMenu();

        // 1. Profile Header Card
        subMenu.add(createProfileHeader(userDetails.getUsername(), isAdmin, imagePath));

        // 2. User & Preferences Group
        if (SecurityUtils.isUserLoggedIn()) {
            RouterLink profileLink = new RouterLink(ProfileDialogView.class, new RouteParameters("username", SecurityUtils.getUsername()));
            profileLink.add(createMenuItemLayout("Profile", VaadinIcon.USER.create(), false));
            subMenu.addItem(profileLink);

            RouterLink configLink = new RouterLink(AppConfigView.class);
            configLink.add(createMenuItemLayout("App Config", VaadinIcon.COG.create(), false));
            subMenu.addItem(configLink);
        }

        // 3. Admin Governance Group
        if (isAdmin) {
            subMenu.add(createMenuDivider());

            RouterLink dataLink = new RouterLink(DataManagerView.class);
            dataLink.add(createMenuItemLayout("Knowledge Manager", VaadinIcon.DATABASE.create(), false));
            subMenu.addItem(dataLink);

            RouterLink userLink = new RouterLink(UserRegistrationView.class);
            userLink.add(createMenuItemLayout("User Manager", VaadinIcon.USERS.create(), false));
            subMenu.addItem(userLink);
        }

        // 4. Help & Resources Group
        subMenu.add(createMenuDivider());
        MenuItem helpMenuItem = subMenu.addItem(createMenuItemLayout("User Manual", VaadinIcon.QUESTION_CIRCLE.create(), false));
        helpMenuItem.addClickListener(e -> {
            com.vaadin.flow.component.UI.getCurrent().getPage().open("/doc/manual_do_usuario.pdf", "_blank");
        });

        // 5. Session Group
        subMenu.add(createMenuDivider());
        RouterLink logoutLink = new RouterLink(LogoutView.class);
        logoutLink.add(createMenuItemLayout("Logout", VaadinIcon.SIGN_OUT.create(), true));
        subMenu.addItem(logoutLink);

        div.add(menuBar);
        return div;
    }

    private Component createProfileHeader(String username, boolean isAdmin, String imagePath) {
        Avatar avatarHeader = new Avatar(username);
        avatarHeader.setImage(imagePath);
        avatarHeader.getStyle().set("width", "36px").set("height", "36px").set("border-radius", "50%");

        Span name = new Span(username);
        name.getStyle().set("font-weight", "700").set("font-size", "0.92rem").set("color", "var(--lumo-header-text-color)");

        Span roleBadge = new Span(isAdmin ? "ADMINISTRATOR" : "USER");
        roleBadge.getStyle()
                .set("font-size", "0.6rem")
                .set("font-weight", "800")
                .set("padding", "2px 7px")
                .set("border-radius", "10px")
                .set("background", isAdmin ? "linear-gradient(135deg, #e67e22, #f39c12)" : "linear-gradient(135deg, #2563eb, #3b82f6)")
                .set("color", "#ffffff")
                .set("letter-spacing", "0.5px");

        VerticalLayout textInfo = new VerticalLayout(name, roleBadge);
        textInfo.setPadding(false);
        textInfo.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(avatarHeader, textInfo);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle()
                .set("padding", "10px 14px 10px 14px")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("margin-bottom", "4px");

        return header;
    }

    private Component createMenuDivider() {
        Hr hr = new Hr();
        hr.getStyle()
                .set("margin", "4px 0")
                .set("border", "none")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)");
        return hr;
    }

    private Component createMenuItemLayout(String text, Icon icon, boolean isDestructive) {
        Div iconBox = new Div(icon);
        iconBox.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "28px")
                .set("height", "28px")
                .set("border-radius", "8px")
                .set("background", isDestructive ? "rgba(239, 68, 68, 0.1)" : "var(--lumo-contrast-5pct)")
                .set("color", isDestructive ? "#ef4444" : "var(--lumo-primary-color)")
                .set("margin-right", "0.7rem")
                .set("flex-shrink", "0");

        icon.getStyle().set("font-size", "0.95rem");

        Span spanText = new Span(text);
        spanText.getStyle()
                .set("font-weight", "500")
                .set("font-size", "0.88rem")
                .set("color", isDestructive ? "#ef4444" : "var(--lumo-body-text-color)");

        HorizontalLayout layout = new HorizontalLayout(iconBox, spanText);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);
        layout.getStyle()
                .set("padding", "0.2rem 0.4rem")
                .set("border-radius", "8px");

        return layout;
    }

    public Avatar getAvatar() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();

        Avatar avatar = new Avatar(userDetails != null ? userDetails.getUsername() : "User");
        String imagePath = SecurityUtils.hasRole("ADMIN") ? ADMIN_ICON : USER_ICON;
        avatar.setImage(imagePath);
        avatar.setTooltipEnabled(true);

        avatar.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "50%")
                .set("width", "36px")
                .set("height", "36px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)");

        return avatar;
    }
}