package br.ufrj.cos.views.about;

import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("About - ArchIoTect")
@Route(value = "about-view", layout = MainLayout.class)
@PermitAll
public class AboutView extends BaseView {

    public AboutView() {
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setWidthFull();
        mainContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);

        Div contentCard = new Div();
        contentCard.getStyle()
                .set("width", "clamp(350px, 850px, 92vw)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "1.25rem");

        // 1. App Hero Card
        contentCard.add(createHeroCard());

        // 2. Institution Cards
        contentCard.add(createInstitutionCard(
                "images/pesc-logo.png",
                "PESC - Programa de Engenharia de Sistemas e Computação",
                "O PESC é um programa de pós-graduação de excelência da COPPE/UFRJ, focado na pesquisa avançada, inovação tecnológica e formação de mestres e doutores em Ciência e Engenharia da Computação."
        ));

        contentCard.add(createInstitutionCard(
                "images/coppe-logo.png",
                "UFRJ / COPPE",
                "A COPPE (Instituto Alberto Luiz Coimbra de Pós-Graduação e Pesquisa de Engenharia) é o maior centro de ensino e pesquisa em engenharia da América Latina, integrado à Universidade Federal do Rio de Janeiro."
        ));

        // 3. Authors Card
        contentCard.add(createAuthorsSection());

        // 4. Footer info
        contentCard.add(createFooterSection());

        mainContainer.add(contentCard);
        getContent().add(mainContainer);
    }

    private Component createHeroCard() {
        Div heroCard = new Div();
        heroCard.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "16px")
                .set("padding", "1.75rem 2rem")
                .set("color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.2)");

        H2 title = new H2("ArchIoTect");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.8rem")
                .set("font-weight", "700")
                .set("color", "#ffffff");

        Span versionBadge = new Span("v2.0");
        versionBadge.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("background", "rgba(255, 255, 255, 0.2)")
                .set("color", "#ffffff")
                .set("backdrop-filter", "blur(4px)")
                .set("margin-left", "10px");

        HorizontalLayout titleBox = new HorizontalLayout(title, versionBadge);
        titleBox.setAlignItems(FlexComponent.Alignment.CENTER);
        titleBox.getStyle().set("margin-bottom", "0.4rem");

        Paragraph subtitle = new Paragraph("Knowledge Base Explorer & Architecture Decision Assistant for Internet of Things (IoT)");
        subtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.95rem")
                .set("opacity", "0.9")
                .set("line-height", "1.5");

        heroCard.add(titleBox, subtitle);
        return heroCard;
    }

    private Component createInstitutionCard(String logoPath, String titleText, String descriptionText) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "14px")
                .set("padding", "1.25rem 1.5rem")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)");

        Image logo = new Image(logoPath, titleText);
        logo.getStyle()
                .set("width", "72px")
                .set("height", "72px")
                .set("object-fit", "contain")
                .set("padding", "6px")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("flex-shrink", "0");

        VerticalLayout textBox = new VerticalLayout();
        textBox.setPadding(false);
        textBox.setSpacing(false);

        H3 title = new H3(titleText);
        title.getStyle()
                .set("margin", "0 0 0.3rem 0")
                .set("font-size", "1.1rem")
                .set("font-weight", "600")
                .set("color", "var(--lumo-header-text-color)");

        Paragraph desc = new Paragraph(descriptionText);
        desc.getStyle()
                .set("margin", "0")
                .set("font-size", "0.875rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("line-height", "1.5");

        textBox.add(title, desc);
        card.add(logo, textBox);
        card.setFlexGrow(1, textBox);

        return card;
    }

    private Component createAuthorsSection() {
        Div section = new Div();
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "14px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)");

        H3 title = new H3("Authors");
        title.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.15rem")
                .set("font-weight", "600")
                .set("color", "var(--lumo-header-text-color)");

        Div authorsGrid = new Div();
        authorsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))")
                .set("gap", "1rem");

        authorsGrid.add(
                createAuthorCard("Guilherme Horta Travassos", "ght@cos.ufrj.br", VaadinIcon.SPECIALIST),
                createAuthorCard("Bruno Pedraça de Souza", "bpsouza@cos.ufrj.br", VaadinIcon.USER),
                createAuthorCard("Fernando Novaes Ribeiro da Silva", "fernandonrs@cos.ufrj.br", VaadinIcon.CODE)
        );

        section.add(title, authorsGrid);
        return section;
    }

    private Component createAuthorCard(String name, String email, VaadinIcon icon) {
        HorizontalLayout card = new HorizontalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "12px")
                .set("padding", "0.75rem 1rem")
                .set("border", "1px solid var(--lumo-contrast-10pct)");

        Icon authorIcon = icon.create();
        authorIcon.getStyle()
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-primary-color)")
                .set("padding", "8px")
                .set("border-radius", "50%")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.06)");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "0.9rem")
                .set("color", "var(--lumo-header-text-color)")
                .set("margin-bottom", "0.15rem");

        Anchor emailLink = new Anchor("mailto:" + email, email);
        emailLink.getStyle()
                .set("font-size", "0.78rem")
                .set("color", "var(--lumo-primary-color)")
                .set("text-decoration", "none")
                .set("font-weight", "500");

        info.add(nameSpan, emailLink);
        card.add(authorIcon, info);
        card.setFlexGrow(1, info);

        return card;
    }

    private Component createFooterSection() {
        Div footer = new Div();
        footer.getStyle()
                .set("text-align", "center")
                .set("font-size", "0.8rem")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("padding", "0.5rem 0");

        footer.setText("ArchIoTect v2.0 • PESC / COPPE / UFRJ © 2026. All rights reserved.");
        return footer;
    }
}
