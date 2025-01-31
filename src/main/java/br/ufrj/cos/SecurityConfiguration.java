package br.ufrj.cos;

import br.ufrj.cos.views.login.AccessDeniedView;
import br.ufrj.cos.views.login.LoginView;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure Vaadin's security
        super.configure(http);

        // Set up the login view
        setLoginView(http, LoginView.class);

        // Configure access denied handling
        http.exceptionHandling(handling ->
                handling.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.sendRedirect(
                                new RouterLink(AccessDeniedView.class.getName(), AccessDeniedView.class).getHref());
                })
        );
    }

    @Bean
    UserDetailsManager userDetailsManager() {
        var user = User.withUsername("user")
                .password("{noop}user")
                .roles("USER")
                .build();

        var admin = User.withUsername("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    // Add ConfigureAccess annotation to ensure proper security handling
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }
}
