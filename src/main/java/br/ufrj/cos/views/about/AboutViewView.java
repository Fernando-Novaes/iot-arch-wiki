package br.ufrj.cos.views.about;

import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("About View")
@Route(value = "about-view", layout = MainLayout.class)
public class AboutViewView extends Composite<VerticalLayout> {

    public AboutViewView() {
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        this.createAppNameBox("IoT-Arch Wiki");
    }

    private void createAppNameBox(String text) {
        // Create a Div to act as the box
        Div box = new Div();
        box.add(new Text(text));
        box.addClassName("centered-aboutbox");

        Div box2 = new Div();
        box2.add(new Text("UFRJ - Universidade Federal do Rio de Janeiro"));
        box2.addClassName("centered-aboutbox");

        // Create a HorizontalLayout to center the box horizontally
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSizeFull(); // Make it take the full width of the screen
        hLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Center the content horizontally

        // Create a VerticalLayout to center the box vertically
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSizeFull(); // Make it take the full height of the screen
        vLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Center the content vertically

        // Add the box to the vertical layout
        vLayout.add(box,box2);

        // Add the vertical layout to the horizontal layout
        hLayout.add(vLayout);

        // Add the horizontal layout to the main layout
        getContent().add(hLayout);
    }
}
