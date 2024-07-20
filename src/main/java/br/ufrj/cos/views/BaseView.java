package br.ufrj.cos.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@CssImport(value = "./styles/app-styles.css")
public abstract class BaseView extends Composite<VerticalLayout> {

    /***
     * Creates the Header of the Page
     * @param headerText
     */
    public void createHeader(String headerText) {
        HorizontalLayout header = new HorizontalLayout();
        H1 h1 = new H1();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        header.setWidthFull();
        getContent().setFlexGrow(1.0, header);
        header.setWidth("100%");
        header.getStyle().set("flex-grow", "1");
        header.setMinHeight("5%");
        header.setMaxHeight("5%");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        h1.setText(headerText);
        h1.setWidth("max-content");
        header.add(h1);

        getContent().add(header);
    }

    /****
     * Creates the content box
     */
    public HorizontalLayout createContentLayout() {
        HorizontalLayout content = new HorizontalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        content.setWidthFull();
        getContent().setFlexGrow(1.0, content);
        content.addClassName(LumoUtility.Gap.MEDIUM);

        content.setMinHeight("100%");
        content.setMaxHeight("100%");

        content.getStyle().set("background-color", "lightgray");

        return content;
    }

    /***
     * Creates the layout to the Tree View
     * @param title
     * @param treeView
     * @return Details
     */
//    public Details createTreeViewLayout(String title, TreeViewComponent treeView) {
//        Details treeViewDetails = new Details(title, treeView);
//        treeViewDetails.setOpened(true);
//        treeViewDetails.setWidth("100%");
//        treeViewDetails.setHeight("100%");
//        treeViewDetails.getStyle().set("background-color", "#dce8fa");
//        treeViewDetails.addThemeVariants(DetailsVariant.FILLED);
//
//        return treeViewDetails;
//    }

    /***
     * Creates container details
     * @param title
     * @param textContent
     * @return
     */
    public Details createDetailsPanel(String title, String textContent) {
        HorizontalLayout content = new HorizontalLayout(
                new Span(textContent)
        );

        Details details = new Details(title, content);
        details.setOpened(true);
        details.getStyle().set("background-color", "lightgray");
        details.addThemeVariants(DetailsVariant.FILLED);

        return details;
    }

    /***
     * Gets the value from a property name in a properties file
     * @param fileName
     * @param propertyName
     * @return String
     */
    public String getValueFromPropertiesFile(String fileName, String propertyName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IOException("Sorry, unable to find " + fileName);
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
        return properties.getProperty(propertyName);
    }
}
