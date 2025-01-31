package br.ufrj.cos.views.login;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {
    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        var login = new LoginForm();
        login.setAction("login");

        this.add(
                new Html("<div><center><h1>IoT Architecture Solution Knowledge Base</h1><p>IoT Design Decision Assistant</p></br></br></center></div>"), login);
    }
}
