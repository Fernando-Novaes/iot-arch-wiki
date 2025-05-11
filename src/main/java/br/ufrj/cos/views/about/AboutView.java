package br.ufrj.cos.views.about;

import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("About")
@Route(value = "about-view", layout = MainLayout.class)
@PermitAll
public class AboutView extends BaseView {

    public AboutView() {
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
        getContent().setPadding(false);

        createAboutLayout();
    }

    private void createAboutLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Center the boxes horizontally

        // Box 1: PESC - Programa de Engenharia de Sistemas da Computação
        HorizontalLayout pescBox = createInfoBox(
                "images/pesc-logo.png", // Replace with your PESC logo
                "Programa de Engenharia de Sistemas e Computação (PESC)",
                "O PESC é um programa de pós-graduação da COPPE/UFRJ que oferece cursos de Mestrado e Doutorado em Engenharia de Sistemas e Computação.");
        mainLayout.add(pescBox);

        // Box 2: UFRJ/COPPE
        HorizontalLayout ufrjCoppeBox = createInfoBox(
                "images/coppe-logo.png", // Replace with your UFRJ/COPPE logo
                "UFRJ / COPPE",
                "Universidade Federal do Rio de Janeiro (UFRJ). A COPPE é o Instituto de Pós-Graduação e Pesquisa em Engenharia da UFRJ.");
        mainLayout.add(ufrjCoppeBox);

        // Box 3: Authors
        VerticalLayout authorsBox = createAuthorsBox();
        mainLayout.add(authorsBox);

        getContent().add(mainLayout);
    }

    private HorizontalLayout createInfoBox(String imagePath, String titleText, String descriptionText) {
        HorizontalLayout box = new HorizontalLayout();
        box.addClassName("info-box");
        box.setWidth("80%"); // Adjust as needed
        box.setAlignItems(FlexComponent.Alignment.CENTER);

        Image image = new Image(imagePath, titleText);
        image.setWidth("100px"); // Adjust image size as needed
        image.setHeight("100px");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        H2 title = new H2(titleText);
        title.getStyle().setColor("#ee973a");
        Paragraph description = new Paragraph(descriptionText);

        textLayout.add(title, description);

        box.add(image, textLayout);
        box.setFlexGrow(1, textLayout); //make text take all the space

        return box;
    }

    private VerticalLayout createAuthorsBox() {
        VerticalLayout box = new VerticalLayout();
        box.addClassName("info-box");
        box.setWidth("80%"); // Adjust as needed
        box.setAlignItems(FlexComponent.Alignment.START);
        box.setPadding(true);

        H2 title = new H2("Authors");
        title.getStyle().setColor("#ee973a");
        box.add(title);

        // Author 1
        box.add(createAuthorInfo("Guilherme Horta Travassos", "ght@cos.ufrj.br"));

        // Author 2
        box.add(createAuthorInfo("Bruno Pedraça de Souza", "bpsouza@cos.ufrj.br"));

        // Author 3
        box.add(createAuthorInfo("Fernando Novaes Ribeiro da Silva", "fernandonrs@cos.ufrj.br"));

        return box;
    }

    private HorizontalLayout createAuthorInfo(String name, String email) {
        HorizontalLayout authorLayout = new HorizontalLayout();
        authorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        authorLayout.setSpacing(true);

        Text nameLabel = new Text(name);
        Text emailLabel = new Text(" - " + email);

        authorLayout.add(nameLabel, emailLabel);

        return authorLayout;
    }
}
