package br.ufrj.cos.components.sliderpanel;

import br.ufrj.cos.components.sliderpanel.events.SliderPanelResizeEvent;
import br.ufrj.cos.components.treeview.record.DataDetails;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.views.board.ChartDialog;
import com.github.appreciated.apexcharts.ApexCharts;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@UIScope
@org.springframework.stereotype.Component
@CssImport("./styles/slider-panel.css")  // Keep your main CSS
public class SliderPanel extends Div {
    private final Div content;
    private final Button toggleButton;
    private boolean expanded = true; // Default to expanded
    private String expandedText = "Hide Details";
    private String collapsedText = "Show Details";

    private final HorizontalLayout header = new HorizontalLayout();
    private final HorizontalLayout iotDomainHL = new HorizontalLayout();
    private final HorizontalLayout archHL = new HorizontalLayout();
    private final HorizontalLayout qrHL = new HorizontalLayout();
    private final HorizontalLayout techHL = new HorizontalLayout();
    private final HorizontalLayout referenceHL = new HorizontalLayout();

    @Getter
    @Setter
    private DataDetails dataDetails;

    @Getter
    @Setter
    private Component chartComponent; // To store the chart component

    @Getter
    private double currentWidth;

    @Autowired
    private ApplicationEventPublisher eventPublisher;  // Inject Spring's event publisher

    public SliderPanel() {
        this.removeAll();
        addClassName("slider-panel");
        this.getStyle().set("box-shadow", "-3px 0 8px rgba(0, 0, 0, 0.4)");

        // Create toggle button
        toggleButton = new Button();
        toggleButton.addClassName("slider-toggle");

        // Initialize button content
        updateButtonContent(false); // Start with collapsed state

        toggleButton.addClickListener(e -> toggle());

        content = new Div();
        content.addClassName("slider-content");

        // Set default width for right-side panel
        //content.getStyle().set("width", "100%"); // REMOVE THIS, controlled by CSS and JS

        // Configure vertical text
        toggleButton.getElement().getStyle()
                .set("writing-mode", "vertical-lr")  // Changed to vertical-lr for better readability
                .set("text-orientation", "mixed")
                .set("transform", "rotate(180deg)"); // This makes text read from top to bottom

        toggleButton.setIcon(new Icon(VaadinIcon.ANGLE_LEFT));

        this.addDetailsContent();

        add(toggleButton, this.createHeader(), content, referenceHL);

        // Add resizer element
        Div resizer = new Div();
        resizer.addClassName("slider-resizer");
        add(resizer);
        currentWidth = 800.0;
        // Embed JavaScript to handle resizing
        String script = "" +
                "const slider = this;" +
                "const resizer = slider.querySelector('.slider-resizer');" +
                "let originalWidth = 0;" +
                "let originalMouseX = 0;" +

                "resizer.addEventListener('mousedown', function(e) {" +
                "  originalWidth = parseFloat(getComputedStyle(slider, null).getPropertyValue('width').replace('px', ''));" +
                "  originalMouseX = e.pageX;" +
                "  document.addEventListener('mousemove', resize);" +
                "  document.addEventListener('mouseup', stopResize);" +
                "});" +

                "function resize(e) {" +
                "  const width = originalWidth - (e.pageX - originalMouseX);" +
                "  slider.style.width = width + 'px';" +
                "};" +

                "function stopResize() {" +
                "  document.removeEventListener('mousemove', resize);" +
                "  document.removeEventListener('mouseup', stopResize);" +
                "  slider.currentWidth = parseFloat(getComputedStyle(slider, null).getPropertyValue('width').replace('px', ''));" +
                "  slider.dispatchEvent(new CustomEvent('width-changed', { detail: String(e.detail) }));" +

                "};" +

                "slider.addEventListener('slider-resized', function(e) {" +
                "   slider.currentWidth = e.detail;" + // Access the width from the event detail
                "   slider.dispatchEvent(new CustomEvent('width-changed', { detail: e.detail }));" +
                "});";

        getElement().executeJs(script);

        // Add listener for width changes (Vaadin-side)
        getElement().addEventListener("width-changed", e -> {
            String newWidthStr = e.getEventData().getString("event.detail");
            double newWidth = Double.parseDouble(newWidthStr);
            setWidth(newWidth); //Update java object.
            // Publish a Spring event when the width changes
            eventPublisher.publishEvent(new SliderPanelResizeEvent(this, newWidth));
        });

    }

    private void updateButtonContent(boolean isExpanded) {
        // Clear existing content
        //toggleButton.getElement().removeAllChildren();

        // Create icon
        Icon icon = isExpanded ? VaadinIcon.LEVEL_RIGHT_BOLD.create() :
                VaadinIcon.LEVEL_LEFT_BOLD.create();
                ;

        // Configure icon
        icon.getElement().getStyle()
                .set("transform", "rotate(180deg)") // Rotate icon to match text orientation
                .set("display", "block")
                .set("margin", "4px auto"); // Center the icon

        // Add text first (it will appear at the bottom due to rotation)
        String text = isExpanded ? expandedText : collapsedText;
        toggleButton.setText(text);
    }

    public void toggle() {
        if (expanded) {
            toggleButton.setIcon(new Icon(VaadinIcon.ANGLE_LEFT));
            addClassName("expanded");
            updateButtonContent(true);
            expanded = !expanded;
        } else {
            toggleButton.setIcon(new Icon(VaadinIcon.ANGLE_RIGHT));
            removeClassName("expanded");
            updateButtonContent(false);
            expanded = !expanded;
        }
    }

    private String formatSlidePanelDetails(String title, String name, String description, String addressedNotes) {
        return String.format("<div><h3>%s:</h3><b>%s</b></br><div style='font-style: italic; margin-bottom: 5px;'>%s</div></br><div style='font-style: italic; margin-bottom: 5px;'>%s</div></div>",
                title, name, Optional.ofNullable(description).orElse("No description"), Optional.ofNullable(addressedNotes).orElse("No description"));
    }

    public void setDataDetailsContent(DataDetails dataDetails) {
        if (dataDetails != null) {
            if (dataDetails.getIotDomain() != null) {
                this.setIoTDomainContent(dataDetails.getIotDomain());
            }

            if (dataDetails.getArchitectureSolution() != null) {
                this.setArchitectureContent(dataDetails.getArchitectureSolution());
            }

            if (dataDetails.getQualityRequirement() != null) {
                this.setQualityRequirementContent(dataDetails.getQualityRequirement());
            }

            if (dataDetails.getTechnology() != null) {
                this.setTechnologyContent(dataDetails);
            }

            if (dataDetails.getReference() != null) {
                this.setReferenceDetails(dataDetails.getReference());
            }
        }
    }

    private void setIoTDomainContent(IoTDomain domain) {
        iotDomainHL.add(
                new HorizontalLayout(
                        new Html(formatSlidePanelDetails("IoT Domain", domain.getName(), domain.getDescription(),  null))

                ));
    }

    private void setArchitectureContent(ArchitectureSolution solution) {
        archHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Architecture", solution.getArchitecture().getName(), solution.getDescription(), null))));
    }

    private void setQualityRequirementContent(QualityRequirement qr) {
        qrHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Quality Requirement", qr.getName(),
                        Optional.ofNullable(qr.getDescription()).orElse("No description"), null))
        ));
    }

    private void setTechnologyContent(DataDetails details) {
        Optional<QualityRequirementTechnology> qrAddressedNotes = details.getArchitectureSolution().getQualityRequirementTechnologies().stream()
                .filter(tech -> tech.getTechnology().equals(details.getTechnology()))
                .filter(qr ->  qr.getQualityRequirement().equals(details.getQualityRequirement()))
                .findAny();

        techHL.add(new HorizontalLayout(
                new Html(formatSlidePanelDetails("Technology", details.getTechnology().getDescription(),
                        Optional.ofNullable(details.getTechnology().getNotes()).orElse("No description"),
                        String.format("%s: %s", qrAddressedNotes.get().getQualityRequirement(), qrAddressedNotes.get().getNotes())))

        ));
    }

    private void setReferenceDetails(PaperReference reference) {
        referenceHL.add(new Html(
                String.format("<div style='font-style: italic;'><center><b>%s, %s</b></center></div>",
                        reference.getTitle(), reference.getPublishYear())));
    }

    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "var(--lumo-primary-color)") // Use primary color for header
                .set("color", "var(--lumo-primary-contrast-color)") // Use contrast color for text
                .set("border-radius", "0") // Remove border radius for a cleaner look
                .set("width", "100%")
                .set("padding", "1px"); // Add padding for better spacing
        //.set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.2)"); // Add a subtle shadow (optional)

        H3 headerText = new H3("Details");
        headerText.getStyle()
                .set("color", "var(--lumo-primary-contrast-color)") // Ensure text color matches header
                .set("text-align", "center")
                .set("margin", "0"); // Remove default H3 margins

        header.add(headerText);

        return header;
    }


    private void addDetailsContent() {
        // Keep existing width, spacing, and height configurations
        iotDomainHL.setWidth("100%");
        iotDomainHL.setSpacing(true);
        iotDomainHL.setPadding(false);

        archHL.setWidth("100%");
        archHL.setSpacing(true);
        archHL.setPadding(false);

        qrHL.setWidth("100%");
        qrHL.setSpacing(true);
        qrHL.setPadding(false);

        techHL.setWidth("100%");
        techHL.setSpacing(true);
        techHL.setPadding(false);

        // Container for chart and expand icon
        VerticalLayout chartContainer = new VerticalLayout();
        chartContainer.setWidthFull();
        chartContainer.setPadding(false);
        chartContainer.setSpacing(false);
        chartContainer.getStyle().setPosition(Style.Position.RELATIVE); // For absolute positioning of icon

        if (chartComponent != null) {

            // Add the expand Icon
            Icon expandIcon = new Icon(VaadinIcon.EXPAND);
            expandIcon.getStyle().setCursor("pointer");
            expandIcon.getStyle().setPosition(Style.Position.ABSOLUTE); // Absolute positioning
            expandIcon.getStyle().setBottom("0"); // Bottom-right corner
            expandIcon.getStyle().setRight("0");
            expandIcon.addClickListener(event -> openChartInDialog());

            chartContainer.add(chartComponent, expandIcon);
        }


        // Add other details content
        content.add(iotDomainHL, archHL, qrHL, techHL, chartContainer);

    }

    public void clearContents() {
        header.removeAll();
        iotDomainHL.removeAll();
        archHL.removeAll();
        qrHL.removeAll();
        techHL.removeAll();
        referenceHL.removeAll();
    }


    private void openChartInDialog() {
        if (chartComponent == null) {
            return; // Or show an error message
        }

        ChartDialog chartDialog = new ChartDialog((ApexCharts) chartComponent, "Chart Details");
        chartDialog.open();
    }

    // Call this method from your board view to set the chart
    public void setChart(Component chart) {
        this.chartComponent = chart;
        // Re-render the details content
        addDetailsContent();
    }


    public void setHeight(String height) {
        content.getStyle().set("height", height);
    }


    //  IMPORTANT:  setWidth is called from the JavaScript listener!
    public void setWidth(double width) {
        currentWidth = width;
        getElement().getStyle().set("width", width + "px"); // Set the width

    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            toggle();
        }
    }

    @EventListener
    public void handleDataDetailsUpdate(DataDetailsUpdateEvent event) {
        // Use UI.access() to ensure we're updating the UI from the correct thread
        UI.getCurrent().access(() -> {
            DataDetails newDetails = event.getDataDetails();
            setDataDetails(newDetails);
            clearContents();  // Clear existing content before updating
            this.setDataDetailsContent(newDetails);
        });
    }

    @EventListener
    public void openClose(OpenCloseEvent event) {
        if (event.getAction() == OpenCloseEvent.Action.OPEN) {
            addClassName("expanded");
            updateButtonContent(true);
            expanded = !expanded;
        } else {
            removeClassName("expanded");
            updateButtonContent(false);
            this.clearContents();
        }
    }
}