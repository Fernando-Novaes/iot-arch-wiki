package br.ufrj.cos.components.avatar;

import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@PermitAll
public class AvatarComponent {

    private final String USER_ICON = "images/homem.png";
    private final String ADMIN_ICON = "images/admin.png";

    public AvatarComponent() {}

    public Div createAvatar() {
        UserDetails userDetails = SecurityUtils.getAuthenticatedUser();

        if (userDetails == null) {
            return new Div(); // Return empty div if user is not authenticated
        }

        Div div = new Div();
        Avatar avatar = new Avatar(userDetails.getUsername());

        // Set avatar image based on user role
        String imagePath = SecurityUtils.hasRole("ADMIN") ? ADMIN_ICON : USER_ICON;
        avatar.setImage(imagePath);
        avatar.setTooltipEnabled(true);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        MenuItem menuItem = menuBar.addItem(avatar);
        SubMenu subMenu = menuItem.getSubMenu();
        if (SecurityUtils.isUserLoggedIn()) {
            subMenu.addItem(new RouterLink("Edit user details",
                    ProfileDialogView.class, new RouteParameters("userName", SecurityUtils.getUsername())));
        }
        subMenu.addItem(new RouterLink("Logout", LoginView.class));

        div.add(menuBar);
        return div;
    }
}
