package br.ufrj.cos.views.login;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordEncoder passEncoder;
    private final AuthenticationManager authenticationManager;  // To authenticate user
    private final UserApplicationService userService;  // Service to handle user logic

    public LoginView(PasswordEncoder passEncoder, AuthenticationManager authenticationManager, UserApplicationService userService) {
        this.passEncoder = passEncoder;
        this.authenticationManager = authenticationManager;
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        var login = new LoginForm();
        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");

        // Add a login form with event listener
        login.addLoginListener(event -> authenticate(event.getUsername(), event.getPassword()));

        this.add(
                new Html("<div><center><h1>IoT Architecture Solution Knowledge Base</h1><p>IoT Design Decision Assistant</p></br></br></center></div>"),
                login);

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation(
                "Contact admin@company.com if you're experiencing issues logging into your account");
        login.setI18n(i18n);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (SecurityUtils.isUserLoggedIn()) {
            event.forwardTo("");  // Redirect to main page if already logged in
        }
    }

    private void authenticate(String username, String password) {
        try {
            // Check if user exists in the database
            Optional<UserApplication> userOpt = Optional.ofNullable(userService.findByUserName(username)); // Assume userService can find user by email
            if (userOpt.isPresent()) {
                UserApplication user = userOpt.get();

                // Perform authentication using Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user.getEmail(), passEncoder.encode(password));  // Assuming email and password as credentials
                Authentication authentication = authenticationManager.authenticate(authToken);

                // If authentication is successful, log the user in
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Redirect to main page
                UI.getCurrent().navigate("");  // Main page after successful login
            } else {
                // Handle invalid credentials or user not found
                NotificationUtils.showErrorNotification("Invalid username or password!");
            }
        } catch (Exception e) {
            System.out.println("####" + e.getMessage());
            //NotificationUtils.showErrorNotification("Authentication failed");
        }
    }
}

