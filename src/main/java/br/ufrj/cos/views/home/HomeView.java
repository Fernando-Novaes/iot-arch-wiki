package br.ufrj.cos.views.home;

import br.ufrj.cos.components.sliderpanel.OpenCloseEvent;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
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
// No @CssImport needed if all styles are inline via Java
public class HomeView extends Composite<VerticalLayout> implements HasTour {

    public HomeView() {
        VerticalLayout rootContent = getContent();
        rootContent.setSpacing(false);
        rootContent.setPadding(false);
        rootContent.setSizeFull();
        rootContent.getStyle().set("flex-grow", "1");
        // Center the main presentation card within the root content area
        rootContent.setAlignItems(FlexComponent.Alignment.CENTER);
        rootContent.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // Align card to top usually

        createPresentationBox(rootContent);
    }

    private void createPresentationBox(VerticalLayout rootLayout) {
        VerticalLayout presentationCard = new VerticalLayout();
        presentationCard.setSpacing(true); // Spacing between elements inside the card
        presentationCard.setPadding(false); // Padding will be controlled by style.set("padding", ...)
        presentationCard.setAlignItems(FlexComponent.Alignment.CENTER); // Center direct children of the card

        // Style the presentation card
        presentationCard.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "clamp(1em, 4vw, 2.5em)") // Responsive padding
                .set("margin", "var(--lumo-space-s)") // Margin around the card
                .set("box-sizing", "border-box"); // Important for width calculations with padding/border

        // Control width for different screen sizes
        // On small screens, it will be 90% of the parent (rootContent).
        // On large screens, it will be capped at 1000px.
        // The browser will pick the smaller of the two.
        presentationCard.setWidth("100%");
        presentationCard.setMaxWidth("1000px");


        // --- Header ---
        Div headerContainer = new Div();
        headerContainer.getStyle()
                .set("text-align", "center")
                .set("width", "100%") // Take full width of presentationCard
                .set("margin-bottom", "var(--lumo-space-s)");

        H2 mainTitle = new H2("Welcome to the IoT Design Decision Assistant");
        mainTitle.getStyle().setFontSize("clamp(1.8em, 3vw, 2.5em)");
        mainTitle.getStyle().set("margin-top", "0"); // Remove default H2 top margin

        headerContainer.add(mainTitle, new Hr());

        // --- Content Container (for lists and paragraphs) ---
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setWidth("100%");
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);
        contentContainer.setAlignItems(FlexComponent.Alignment.STRETCH); // Make children like lists take full width

        H3 whyChooseTitle = new H3("Why Choose Our Tool?");
        whyChooseTitle.getStyle().setFontSize("clamp(1.2em, 2.2vw, 1.6em)");
        whyChooseTitle.getStyle().set("margin-top", "var(--lumo-space-s)"); // Add some top margin
        whyChooseTitle.getStyle().set("margin-bottom", "var(--lumo-space-xs)");


        UnorderedList whyChooseList = createResponsiveList(
                "Comprehensive Knowledge Base: Access a curated collection of IoT domains, architectural solutions, quality requirements, and technologies from peer-reviewed research and industry best practices.",
                "Intelligent Decision Support: Leverage advanced AI to match your project requirements with optimal architectural solutions and technologies.",
                "Quality-Driven Approach: Ensure your designs meet high standards by aligning with established quality attributes specific to IoT systems.",
                "Stay Current: Benefit from regularly updated content reflecting the latest advancements in IoT technology and design methodologies."
        );

        H3 featuresTitle = new H3("Key Features:");
        featuresTitle.getStyle().setFontSize("clamp(1.2em, 2.2vw, 1.6em)");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-s)"); // Add some top margin
        featuresTitle.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        UnorderedList featuresList = createResponsiveList(
                "Interactive Design Explorer: Visually navigate IoT domains, solutions, and technologies.",
                "Requirements Analyzer: Define and prioritize your project's quality requirements with ease.",
                "Solution Recommender: Receive tailored architectural recommendations based on your specific needs.",
                "Technology Evaluator: Compare and assess various IoT technologies for your project.",
                "AI Assistant: Engage with an intelligent assistant for instant guidance and exploration of IoT concepts.",
                "Knowledge Contribution: Submit your experiences to enrich our collective wisdom (Admin/Curator feature)."
        );

        Paragraph closing = new Paragraph("Embark on your IoT design journey with confidence. Let our Assistant be your guide to creating robust, efficient, and innovative IoT software systems.");
        closing.getStyle().setFontSize("clamp(0.9em, 1.8vw, 1.1em)");
        closing.getStyle().set("text-align", "center");
        closing.getStyle().set("margin-top", "var(--lumo-space-s)");


        Emphasis callToAction = new Emphasis("Start exploring now and transform the way you design IoT solutions!");
        callToAction.getStyle()
                .set("display", "block") // Make it a block to center it
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-s)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-body-text-color)");
        callToAction.getStyle().setFontSize("clamp(1.0em, 2.0vw, 1.2em)");

        contentContainer.add(
                whyChooseTitle,
                whyChooseList,
                featuresTitle,
                featuresList,
                closing,
                callToAction
        );

        presentationCard.add(headerContainer, contentContainer);
        rootLayout.add(presentationCard);
    }

    private UnorderedList createResponsiveList(String... items) {
        UnorderedList list = new UnorderedList();
        list.getStyle()
                .set("padding-left", "clamp(1.2em, 3vw, 2em)")
                .setFontSize("clamp(0.9em, 1.8vw, 1.1em)");
        // list.getStyle().set("max-width", "800px"); // Optional: constrain list width for readability
        // list.getStyle().set("margin", "0 auto"); // Center the list if max-width is set

        for (String itemText : items) {
            ListItem listItem = new ListItem(itemText);
            listItem.getStyle()
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("line-height", "1.2");
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