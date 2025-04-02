package br.ufrj.cos.views.home;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home View")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        VerticalLayout content = getContent();
        content.setSpacing(true);
        content.setSizeFull();
        content.getStyle().set("flex-grow", "1");
        content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        createPresentationBox(content);
    }

    private void createPresentationBox(VerticalLayout rootLayout) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        mainLayout.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("padding", "clamp(1em, 5vw, 2em)")
                .set("max-height", "92vh")
                .set("overflow-y", "none");
        mainLayout.setWidth("95%"); // Make the box wider
        mainLayout.setMaxWidth("1200px"); // Define a maximum width for larger screens

        Div contentContainer = new Div();
        contentContainer.getStyle()
                .set("width", "100%")
                .set("padding", "0 clamp(1em, 3vw, 2em)")
                .set("margin-left", "0"); // Remove the left margin

        Div headerContainer = new Div();
        headerContainer.getStyle()
                .set("text-align", "center")
                .set("width", "100%")
                .set("margin-bottom", "2em");

        H3 mainTitle = new H3("Welcome to the IoT Design Decision Assistant");
        mainTitle.getStyle().setFontSize("clamp(2.0em, 3.5vw, 3.0em)");  // Responsive Font Size
//        H3 subtitle = new H3("Empowering your IoT Software System Design Process");
//        subtitle.getStyle().setFontSize("clamp(1.5em, 3.0vw, 2.0em)"); // Responsive Font Size
//        H4 subheader = new H4("Unlock the Power of Informed Decision-Making");
//        subheader.getStyle().setFontSize("clamp(1.2em, 2.5vw, 1.8em)"); // Responsive Font Size

        headerContainer.add(mainTitle, new Hr());//, subtitle, new Span(), subheader);

        H4 whyChooseTitle = new H4("Why Choose Our Tool?");
        whyChooseTitle.getStyle().setFontSize("clamp(1.2em, 2.5vw, 1.8em)"); // Responsive Font Size

        UnorderedList whyChooseList = createResponsiveList(
                "Comprehensive Knowledge Base: Access a base of knowledge of IoT domains, architectural solutions, quality requirements, and technologies, all curated from peer-reviewed research and industry best practices.",
                "Intelligent Decision Support: Leverage our advanced technology to match your project requirements with optimal architectural solutions and technologies.",
                "Quality-Driven Approach: Ensure your designs meet the highest standards by aligning them with established quality attributes and requirements specific to IoT systems.",
                "Stay Current: Benefit from regularly updated content from the literature, reflecting the latest advancements in IoT technology and design methodologies."
        );

        H4 featuresTitle = new H4("Key Features:");
        featuresTitle.getStyle().setFontSize("clamp(1.2em, 2.5vw, 1.8em)"); // Responsive Font Size

        UnorderedList featuresList = createResponsiveList(
                "Interactive Design Explorer: Visually navigate through IoT domains, solutions, and technologies.",
                "Requirements Analyzer: Define and prioritize your project's quality requirements with ease.",
                "Solution Recommender: Receive tailored architectural recommendations based on your specific needs.",
                "Technology Evaluator: Compare and assess various IoT technologies to find the perfect fit for your project.",
                "AI Assistant: Engage in dynamic conversations with an intelligent assistant to receive instant guidance, explore concepts, and troubleshoot challenges within the IoT domain.",
                "Knowledge Contribution: Submit your own experiences and solutions to enrich the community's collective wisdom."
        );

        Paragraph closing = new Paragraph("Embark on your IoT design journey with confidence. Let our Book of Knowledge be your guide to creating robust, efficient, and innovative IoT software systems.");
        closing.getStyle().setFontSize("clamp(1.0em, 2.0vw, 1.2em)"); // Responsive Font Size

        Emphasis callToAction = new Emphasis("Start exploring now and transform the way you design IoT solutions!");
        callToAction.getStyle()
                .set("display", "block")
                .set("margin-top", "1em");
        callToAction.getStyle().setFontSize("clamp(1.0em, 2.0vw, 1.2em)"); // Responsive Font Size

        contentContainer.add(
                whyChooseTitle,
                whyChooseList,
                featuresTitle,
                featuresList,
                closing,
                callToAction
        );

        mainLayout.add(headerContainer, contentContainer);
        rootLayout.add(mainLayout);
    }

    private UnorderedList createResponsiveList(String... items) {
        UnorderedList list = new UnorderedList();
        list.getStyle()
                .set("max-width", "100%")
                .set("padding-left", "clamp(1.5em, 4vw, 2.5em)")
                .setFontSize("clamp(1.0em, 2.0vw, 1.2em)"); //Responsive Font Size

        for (String item : items) {
            ListItem listItem = new ListItem(item);
            listItem.getStyle()
                    .set("margin-bottom", "0.8em")
                    .set("line-height", "1.5");
            list.add(listItem);
        }

        return list;
    }
}