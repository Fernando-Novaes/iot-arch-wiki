package br.ufrj.cos.views.login;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

@Route(value = "login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements HasUrlParameter<String> {

    private final PasswordEncoder passEncoder;
    private final AuthenticationManager authenticationManager;  // To authenticate user
    private final UserApplicationService userService;
    private String message = "";

    LoginForm login = new LoginForm();

    private final String ROUTER_PARAMETER_ACTION = "action";
    private final String ROUTER_PARAMETER_USER_NAME = "username";

    //Router parameter
    private String actionParameter = "";

    //Router parameters enum
    private enum ActionsParameter {
        LOGOUT, ERROR;

        public static ActionsParameter fromString(String value) {
            for (ActionsParameter actions : ActionsParameter.values()) {
                if (actions.name().toUpperCase().equalsIgnoreCase(value)) {
                    return actions;
                }
            }

            return null;
        }

        public boolean equalsString(String value) {
            return this.name().equalsIgnoreCase(value);
        }
    }

    public LoginView(PasswordEncoder passEncoder, AuthenticationManager authenticationManager, UserApplicationService userService) {
        this.passEncoder = passEncoder;
        this.authenticationManager = authenticationManager;
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);


        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");

        // Add a login form with event listener
        login.addLoginListener(event ->
        {
            //event.getSource().setEnabled(false);
            try {
                authenticate(event.getUsername(), event.getPassword());
            } finally {
                //event.getSource().setEnabled(true);
            }
        });

        this.add(
                new Html("<div><center><h1>IoT Architecture Solution Knowledge Base</h1><p>IoT Design Decision Assistant</p></br></br></center></div>"),
                login,
                new Html(String.format("<div></br><center>- %s -</center></div>", this.message)));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation(
                "Contact admin@company.com if you're experiencing issues logging into your account");
        login.setI18n(i18n);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        if (SecurityUtils.isUserLoggedIn()) {
            beforeEvent.forwardTo("");
        }

        if(beforeEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }

        if (s != null) {
            this.actionParameter = s;
            this.actionPerform(ActionsParameter.fromString(this.actionParameter));
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
                        user.getUserName(), password);
                Authentication authentication = authenticationManager.authenticate(authToken);

                // If authentication is successful, log the user in
                SecurityContextHolder.getContext().setAuthentication(authentication);
                VaadinSession.getCurrent().setAttribute(Authentication.class, authentication);

                if (SecurityContextHolder.getContext().getAuthentication() != null
                        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                    // Redirect to main page
                    UI.getCurrent().navigate("");
                }
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        } catch (BadCredentialsException e) {
            // Only show notification, don't set login form error
            //NotificationUtils.showErrorNotification("Invalid username or password!");
            System.out.println("####" + e.getMessage());
        } catch (Exception e) {
            // Only show notification, don't set login form error
            //NotificationUtils.showErrorNotification("Login failed: " + e.getMessage());
            System.out.println("####" + e.getMessage());
        }
    }

    private void actionPerform(ActionsParameter action) {
        switch (action) {
            case LOGOUT -> this.message = "You are logged out!";
            case ERROR -> login.setError(true);
            case null, default -> this.message = "- -";
        }
    }
}

