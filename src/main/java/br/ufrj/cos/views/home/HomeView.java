package br.ufrj.cos.views.home;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Home View")
@RouteAlias(value = "", layout = MainLayout.class)
@Route(value = "", layout = MainLayout.class)
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        getContent().setSpacing(true);
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
        getContent().setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        this.createPresentationBox();
    }

    private void createPresentationBox() {
        // Main container for vertical centering
        Div centerContainer = new Div();
        //centerContainer.setSizeFull();
        centerContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-height", "80vh");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        //mainLayout.setWidthFull();
        mainLayout.getStyle()
                .set("background-color", "#373a3f")
                .set("border", "1px solid grey")
                .set("border-radius", "8px")
                .set("padding", "clamp(1em, 5vw, 2em)")
                //.set("margin", "0 auto")
                .set("max-height", "90vh")
                .set("overflow-y", "auto");

        Div contentContainer = new Div();
        contentContainer.getStyle()
                .set("max-width", "800px")
                .set("width", "100%")
                .set("padding", "0 clamp(1em, 3vw, 2em)")
                .set("margin-left", "2em"); // Add left margin for tab effect

        // Centered header section with tab effect
        Div headerContainer = new Div();
        headerContainer.getStyle()
                .set("text-align", "center")
                .set("width", "100%")
                .set("margin-bottom", "2em");

        H1 mainTitle = new H1("Welcome to the IoT Design Decision Assistant");
        mainTitle.getStyle().set("text-shadow", "2px 2px 4px rgba(0, 0, 0, 0.5)");
        //configureHeaderText(mainTitle);

        H2 subtitle = new H2("Empowering your IoT Software System Design Process");
        //configureHeaderText(subtitle);

        H3 subheader = new H3("Unlock the Power of Informed Decision-Making");
        //configureHeaderText(subheader);

        headerContainer.add(mainTitle, new Hr(), subtitle, new Span(), subheader);

        H3 whyChooseTitle = new H3("Why Choose Our Tool?");

        UnorderedList whyChooseList = createResponsiveList(
                "Comprehensive Knowledge Base: Access a base of knowledge of IoT domains, architectural solutions, quality requirements, and technologies, all curated from peer-reviewed research and industry best practices.",
                "Intelligent Decision Support: Leverage our advanced technology to match your project requirements with optimal architectural solutions and technologies.",
                "Quality-Driven Approach: Ensure your designs meet the highest standards by aligning them with established quality attributes and requirements specific to IoT systems.",
                "Stay Current: Benefit from regularly updated content from the literature, reflecting the latest advancements in IoT technology and design methodologies."
        );

        H3 featuresTitle = new H3("Key Features:");
        //configureResponsiveText(featuresTitle);

        UnorderedList featuresList = createResponsiveList(
                "Interactive Design Explorer: Visually navigate through IoT domains, solutions, and technologies.",
                "Requirements Analyzer: Define and prioritize your project's quality requirements with ease.",
                "Solution Recommender: Receive tailored architectural recommendations based on your specific needs.",
                "Technology Evaluator: Compare and assess various IoT technologies to find the perfect fit for your project.",
                "Knowledge Contribution: Submit your own experiences and solutions to enrich the community's collective wisdom."
        );

        Paragraph closing = new Paragraph("Embark on your IoT design journey with confidence. Let our Book of Knowledge be your guide to creating robust, efficient, and innovative IoT software systems.");
        //configureResponsiveText(closing);

        Emphasis callToAction = new Emphasis("Start exploring now and transform the way you design IoT solutions!");
        //configureResponsiveText(callToAction);
        callToAction.getStyle()
                .set("display", "block")
                .set("margin-top", "1em");

        contentContainer.add(
                whyChooseTitle,
                whyChooseList,
                featuresTitle,
                featuresList,
                closing,
                callToAction
        );

        mainLayout.add(headerContainer, contentContainer);
        centerContainer.add(mainLayout);
        getContent().add(centerContainer);
    }

    private void configureResponsiveText(Component component) {
        component.getStyle()
                .set("text-align", "center")
                .set("font-size", "clamp(0.9em, 2vw, 1em)")
                .set("width", "100%");
    }

    private UnorderedList createResponsiveList(String... items) {
        UnorderedList list = new UnorderedList();
        list.getStyle()
                .set("max-width", "100%")
                .set("padding-left", "clamp(1.5em, 4vw, 2.5em)")
                .set("font-size", "clamp(1em, 2.2vw, 1.3em)");

        for (String item : items) {
            ListItem listItem = new ListItem(item);
            listItem.getStyle()
                    .set("margin-bottom", "0.8em")
                    .set("line-height", "1.5");
            list.add(listItem);
        }

        return list;
    }

    private void configureHeaderText(Component component) {
        if (component instanceof H1) {
            component.getStyle()
                    .set("text-align", "center")
                    .set("font-size", "clamp(2em, 4vw, 3em)")
                    .set("margin", "0.5em 0");
        } else if (component instanceof H2) {
            component.getStyle()
                    .set("text-align", "center")
                    .set("font-size", "clamp(1.5em, 3vw, 2em)")
                    .set("margin", "0.5em 0");
        } else if (component instanceof H3) {
            component.getStyle()
                    .set("text-align", "center")
                    .set("font-size", "clamp(1.2em, 2.5vw, 1.8em)")
                    .set("margin", "0.5em 0");
        }
    }
}
