package br.ufrj.cos.components.avatar;

import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.appconfig.AppConfigView;
import br.ufrj.cos.views.datamanager.DataManagerView;
import br.ufrj.cos.views.login.LogoutView;
import br.ufrj.cos.views.user.UserRegistrationView;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@PermitAll
public class AvatarComponent {

    public final String USER_ICON = "/images/homem.png";
    public final String ADMIN_ICON = "/images/admin.png";

    public AvatarComponent() {}

    public Div createAvatar() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();

        if (userDetails == null) {
            return new Div(); // Return empty div if user is not authenticated
        }

        Div div = new Div();

        // Avatar with Styling
        Avatar avatar = new Avatar(userDetails.getUsername());
        String imagePath = SecurityUtils.hasRole("ADMIN") ? ADMIN_ICON : USER_ICON;
        avatar.setImage(imagePath);
        avatar.setTooltipEnabled(true);

        // Embedded Styling for Avatar
        avatar.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "50%") // Circular avatar
                .set("width", "36px")
                .set("height", "36px")
                .set("margin-right", "8px") //Spacing between avatar and username
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)"); // Add a subtle shadow

        // User Name Span with Styling
        Span userNameSpan = new Span(userDetails.getUsername().toUpperCase()); // Display username
        userNameSpan.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.1em")
                .set("color", "var(--lumo-primary-text-color)") //Use theme primary text color
                .set("margin-right", "16px"); // Spacing before menu

        // Horizontal Layout to hold Avatar and Username
        HorizontalLayout avatarLayout = new HorizontalLayout(userNameSpan, avatar);
        avatarLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Vertically align items

        // MenuBar
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        // Embedded Styling for Menu Bar
        menuBar.getStyle()
                .set("margin-left", "auto"); // Push to the right

        MenuItem avatarMenuItem = menuBar.addItem(avatarLayout);
        SubMenu subMenu = avatarMenuItem.getSubMenu();
        //Profile
        if (SecurityUtils.isUserLoggedIn()) {
            subMenu.addItem(new RouterLink("Profile",
                    ProfileDialogView.class, new RouteParameters("username", SecurityUtils.getUsername())));
        }
        //When user is ADMIN
        if (SecurityUtils.hasRole("ADMIN")) {
            subMenu.addItem(new RouterLink("Knowledge Manager",
                    DataManagerView.class));
            subMenu.addItem(new RouterLink("User Manager",
                    UserRegistrationView.class));
            subMenu.addItem(new RouterLink("App Config",
                    AppConfigView.class));
        }
        //Logout
        subMenu.addItem(new RouterLink("Logout",
                LogoutView.class));

        div.add(menuBar);
        return div;
    }

    public Avatar getAvatar() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();

        // Avatar with Styling
        Avatar avatar = new Avatar(userDetails.getUsername());
        String imagePath = SecurityUtils.hasRole("ADMIN") ? ADMIN_ICON : USER_ICON;
        avatar.setImage(imagePath);
        avatar.setTooltipEnabled(true);

        // Embedded Styling for Avatar
        avatar.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "50%") // Circular avatar
                .set("width", "36px")
                .set("height", "36px")
                .set("margin-right", "8px") //Spacing between avatar and username
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)"); // Add a subtle shadow

        return avatar;
    }
}