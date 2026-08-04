package br.ufrj.cos.views.home;

import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class HomeView extends Composite<VerticalLayout> implements HasTour {

    public HomeView() {
        VerticalLayout rootContent = getContent();
        rootContent.setSpacing(false);
        rootContent.setPadding(true);
        rootContent.setWidthFull();
        rootContent.addClassName("home-view-root");
        rootContent.setAlignItems(FlexComponent.Alignment.CENTER);
        rootContent.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        createPresentationBox(rootContent);
    }

    private void createPresentationBox(VerticalLayout rootLayout) {
        VerticalLayout presentationCard = new VerticalLayout();
        presentationCard.setSpacing(false);
        presentationCard.setPadding(false);
        presentationCard.setAlignItems(FlexComponent.Alignment.CENTER);
        presentationCard.addClassName("home-card");

        // --- Hero Banner ---
        Div heroBanner = new Div();
        heroBanner.addClassName("home-hero-banner");

        H2 mainTitle = new H2("Welcome to the IoT Design Decision Assistant");
        mainTitle.addClassName("home-hero-title");

        Paragraph heroSubtitle = new Paragraph(
                "Your intelligent companion for designing robust, efficient, and innovative IoT architectures"
        );
        heroSubtitle.addClassName("home-hero-subtitle");

        heroBanner.add(mainTitle, heroSubtitle);

        // --- 2-Column Grid Container (Side-by-Side) ---
        Div gridContainer = new Div();
        gridContainer.addClassName("home-grid");

        // --- Column 1: Why Choose Our Tool ---
        Div column1 = new Div();
        column1.addClassName("home-subcard");

        H3 whyChooseTitle = new H3("Why Choose Our Tool?");
        UnorderedList whyChooseList = createResponsiveListWithLabel(
                new String[]{"Comprehensive Knowledge Base", "Curated IoT domains, solutions, quality requirements, and technologies from research and industry standards."},
                new String[]{"Intelligent Decision Support", "Leverage advanced AI to match your project requirements with optimal architectural solutions."},
                new String[]{"Quality-Driven Approach", "Ensure your designs meet high standards by aligning with established quality attributes."},
                new String[]{"Stay Current", "Regularly updated content reflecting the latest advancements in IoT technology and design."}
        );
        column1.add(whyChooseTitle, whyChooseList);

        // --- Column 2: Key Features ---
        Div column2 = new Div();
        column2.addClassName("home-subcard");

        H3 featuresTitle = new H3("Key Features:");
        UnorderedList featuresList = createResponsiveListWithLabel(
                new String[]{"Interactive Design Explorer", "Visually navigate IoT domains, solutions, and technologies."},
                new String[]{"Requirements Analyzer", "Define and prioritize project quality requirements with ease."},
                new String[]{"Solution Recommender", "Receive tailored architectural recommendations for your needs."},
                new String[]{"Technology Evaluator", "Compare and assess various IoT technologies for your project."},
                new String[]{"AI Assistant", "Engage with an intelligent assistant for instant guidance and concepts."},
                new String[]{"Knowledge Contribution", "Submit your experiences to enrich collective wisdom (Admin feature)."}
        );
        column2.add(featuresTitle, featuresList);

        gridContainer.add(column1, column2);

        // --- Footer CTA ---
        Div footerContainer = new Div();
        footerContainer.addClassName("home-footer");

        Paragraph closing = new Paragraph("Embark on your IoT design journey with confidence. Let our Assistant be your guide to creating robust, efficient, and innovative IoT systems.");
        closing.addClassName("home-closing");

        Emphasis callToAction = new Emphasis("Start exploring now and transform the way you design IoT solutions!");
        callToAction.addClassName("home-cta");

        footerContainer.add(closing, callToAction);

        presentationCard.add(heroBanner, gridContainer, footerContainer);
        rootLayout.add(presentationCard);
    }

    @SafeVarargs
    private UnorderedList createResponsiveListWithLabel(String[]... items) {
        UnorderedList list = new UnorderedList();
        for (String[] item : items) {
            ListItem listItem = new ListItem();
            if (item.length == 2) {
                Span label = new Span(item[0] + ": ");
                label.addClassName("home-list-label");
                Span body = new Span(item[1]);
                listItem.add(label, body);
            } else {
                listItem.setText(item[0]);
            }
            list.add(listItem);
        }
        return list;
    }

    @Override
    public Onboarding createTour() {
        return null;
    }

    @Override
    public Boolean startDemoTour() {
        return true;
    }
}