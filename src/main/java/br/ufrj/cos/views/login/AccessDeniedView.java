package br.ufrj.cos.views.login;

import br.ufrj.cos.views.home.HomeView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("IoT-Arch - Access Denied")
@Route(value = "access-denied")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        // Create a wrapper div to center the content
        Div container = new Div();
        container.add(new H1("Access Denied"));
        container.add(new Paragraph("You do not have permission to view this page."));
        container.add(new RouterLink("Go to Home page!", HomeView.class));

        // Apply styling to center the content
        container.getStyle()
                .set("text-align", "center")
                .set("padding", "2rem")
                .set("border-radius", "10px")
                .set("border-color", "grey")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)")
                .set("background-color", "var(--lumo-base-color)")
                .set("max-width", "400px")
                .set("width", "100%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("height", "20vh")
                .set("flex-direction", "column");

        // Center the container inside the layout
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(container);
    }

}
