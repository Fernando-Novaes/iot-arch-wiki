package br.ufrj.cos.views.login;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.home.HomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Route(value = "login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements HasUrlParameter<String> {

    private final AuthenticationManager authenticationManager;
    private final UserApplicationService userService;
    private final LoginForm login = new LoginForm();

    public LoginView(AuthenticationManager authenticationManager, UserApplicationService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("login-view-root");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form formI18n = i18n.getForm();
        formI18n.setTitle("Welcome Back");
        formI18n.setSubmit("Sign In");
        i18n.setForm(formI18n);
        i18n.setAdditionalInformation(
                "Contact archiotect@cos.ufrj.br if you're experiencing issues logging into your account"
        );
        login.setI18n(i18n);

        login.addLoginListener(event -> {
            try {
                authenticate(event.getUsername(), event.getPassword());
            } catch (Exception e) {
                login.setError(true);
            }
        });

        VerticalLayout loginCard = new VerticalLayout();
        loginCard.addClassName("login-card");
        loginCard.setSpacing(false);
        loginCard.setPadding(false);
        loginCard.setAlignItems(Alignment.CENTER);

        // --- Card Header ---
        Div header = new Div();
        header.addClassName("login-header");

        H1 appTitle = new H1("ArchIoTect");
        appTitle.addClassName("login-app-title");

        Paragraph appSub = new Paragraph("IoT Architecture Knowledge Base & Assistant");
        appSub.addClassName("login-app-sub");

        header.add(appTitle, appSub);

        // --- Footer branding ---
        Span footerText = new Span("— ArchIoTect —");
        footerText.addClassName("login-footer-text");

        loginCard.add(header, login, footerText);
        add(loginCard);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        if (SecurityUtils.isUserLoggedIn()) {
            beforeEvent.forwardTo("");
        }

        if (beforeEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }

    private void authenticate(String username, String password) {
        try {
            Optional<UserApplication> userOpt = Optional.ofNullable(userService.findByUserName(username));

            if (userOpt.isPresent()) {
                UserApplication user = userOpt.get();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUserName(), password);
                Authentication authentication = authenticationManager.authenticate(authToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                VaadinSession.getCurrent().setAttribute(Authentication.class, authentication);

                if (SecurityContextHolder.getContext().getAuthentication() != null
                        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                    UI.getCurrent().navigate(HomeView.class);
                }
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        } catch (BadCredentialsException e) {
            login.setError(true);
        } catch (Exception e) {
            login.setError(true);
        }
    }
}
