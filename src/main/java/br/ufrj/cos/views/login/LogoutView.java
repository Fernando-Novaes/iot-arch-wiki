package br.ufrj.cos.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

@Route("logout")
@PageTitle("Logout")
@AnonymousAllowed
public class LogoutView extends Div {
    public LogoutView() {
        // Clean up the security context
        SecurityContextHolder.clearContext();

        // Get current UI and create a new QueryParameters
        UI ui = UI.getCurrent();
//        QueryParameters queryParameters = QueryParameters.simple(
//                Collections.singletonMap("action", "logout")
//        );

        // Get current location
        String location = "login/logout";

        // Perform session cleanup
        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close();

        // Navigate to login page with parameters
        if (ui != null) {
            ui.navigate(location);
        }
    }
}