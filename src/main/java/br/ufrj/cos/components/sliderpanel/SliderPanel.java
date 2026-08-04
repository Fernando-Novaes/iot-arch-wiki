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
    @Getter
    private final Button toggleButton;
    private boolean expanded = false; // Default to collapsed

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
        Icon icon = isExpanded ? VaadinIcon.ANGLE_RIGHT.create() : VaadinIcon.ANGLE_LEFT.create();
        icon.getStyle().set("margin-bottom", "6px");
        toggleButton.setIcon(icon);
        toggleButton.setText(isExpanded ? expandedText : collapsedText);
    }

    public void toggle() {
        if (!expanded) {
            addClassName("expanded");
            updateButtonContent(true);
            expanded = true;
        } else {
            removeClassName("expanded");
            updateButtonContent(false);
            expanded = false;
        }
    }

    private Component createFormattedTextComponent(String rawText, String fontSize, String textColor) {
        if (rawText == null || rawText.isBlank()) return new Div();

        String html = rawText.trim();

        // 1. Standardize newlines
        html = html.replace("\r\n", "\n");

        // 2. Bold section headers like "Data Collection Layer:", "Fog Layer:", "Purpose:", etc.
        html = html.replaceAll("(?<=[.!?\\n\\s]|^)([A-Z][A-Za-z0-9\\s\\-/]{2,35}:)", "<br/><strong style=\"color: var(--lumo-primary-text-color); font-weight: 600;\">$1</strong>");

        // 3. Convert markdown bold **text** to <strong>
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");

        // 4. Convert markdown italic *text* to <em>
        html = html.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "<em>$1</em>");

        // 5. Convert bullet points
        html = html.replaceAll("(?m)^[•\\-*]\\s+(.*)$", "<div style=\"display: flex; gap: 0.4rem; margin: 0.15rem 0;\"><span>•</span><div>$1</div></div>");

        // 6. Convert double newlines to single breaks
        html = html.replace("\n\n", "<br/>").replace("\n", "<br/>");

        // 7. Clean leading breaks
        while (html.startsWith("<br/>")) {
            html = html.substring(5);
        }

        // 8. Wrap in root HTML element
        try {
            return new Html("<div style=\"font-size: " + fontSize + "; color: " + textColor + "; line-height: 1.45; text-align: justify;\">" + html + "</div>");
        } catch (Exception e) {
            Div fallback = new Div(new Text(rawText));
            fallback.getStyle().set("font-size", fontSize).set("color", textColor).set("line-height", "1.45");
            return fallback;
        }
    }

    private Component createDetailCard(String icon, String categoryTitle, String itemTitle, String description, String secondaryNotes, String headerGradient) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setSpacing(false);
        card.setPadding(true);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.06)")
                .set("margin-bottom", "0.5rem");

        Span badge = new Span(icon + " " + categoryTitle.toUpperCase());
        badge.getStyle()
                .set("background", headerGradient)
                .set("color", "#ffffff")
                .set("padding", "3px 12px")
                .set("border-radius", "16px")
                .set("font-weight", "700")
                .set("font-size", "0.75rem")
                .set("letter-spacing", "0.5px")
                .set("margin-bottom", "0.4rem");

        H4 title = new H4(itemTitle);
        title.getStyle()
                .set("margin", "0.2rem 0 0.45rem 0")
                .set("font-size", "1.05rem")
                .set("font-weight", "600")
                .set("color", "var(--lumo-header-text-color)");

        card.add(badge, title);

        if (description != null && !description.isBlank() && !"No description".equalsIgnoreCase(description.trim())) {
            Component formattedDesc = createFormattedTextComponent(description, "0.875rem", "var(--lumo-body-text-color)");
            card.add(formattedDesc);
        }

        if (secondaryNotes != null && !secondaryNotes.isBlank()) {
            Component formattedNotes = createFormattedTextComponent(secondaryNotes, "0.8125rem", "var(--lumo-secondary-text-color)");
            Div notesBox = new Div(formattedNotes);
            notesBox.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "0.5rem 0.75rem")
                    .set("border-radius", "8px")
                    .set("border-left", "3px solid var(--lumo-primary-color)")
                    .set("margin-top", "0.5rem")
                    .set("width", "100%");
            card.add(notesBox);
        }

        return card;
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
        iotDomainHL.add(createDetailCard("🌐", "IoT Domain", domain.getName(), domain.getDescription(), null, "linear-gradient(135deg, #e67e22, #f39c12)"));
    }

    private void setArchitectureContent(ArchitectureSolution solution) {
        archHL.add(createDetailCard("🏛️", "Architecture Solution", solution.getArchitecture().getName(), solution.getDescription(), null, "linear-gradient(135deg, #2980b9, #3498db)"));
    }

    private void setQualityRequirementContent(QualityRequirement qr) {
        qrHL.add(createDetailCard("⚡", "Quality Requirement", qr.getName(), qr.getDescription(), null, "linear-gradient(135deg, #8e44ad, #9b59b6)"));
    }

    private void setTechnologyContent(DataDetails details) {
        String notes = details.getTechnology().getNotes();
        String addNotes = null;
        if (details.getArchitectureSolution() != null && details.getArchitectureSolution().getQualityRequirementTechnologies() != null) {
            Optional<QualityRequirementTechnology> qrAddressedNotes = details.getArchitectureSolution().getQualityRequirementTechnologies().stream()
                    .filter(tech -> tech.getTechnology().equals(details.getTechnology()))
                    .filter(qr -> qr.getQualityRequirement().equals(details.getQualityRequirement()))
                    .findAny();
            if (qrAddressedNotes.isPresent()) {
                addNotes = String.format("%s: %s", qrAddressedNotes.get().getQualityRequirement(), qrAddressedNotes.get().getNotes());
            }
        }
        techHL.add(createDetailCard("🛠️", "Technology", details.getTechnology().getDescription(), notes, addNotes, "linear-gradient(135deg, #27ae60, #2ecc71)"));
    }

    private void setReferenceDetails(PaperReference reference) {
        referenceHL.add(createDetailCard("📜", "Scientific Reference", reference.getTitle(), "Published Year: " + reference.getPublishYear(), reference.getLink(), "linear-gradient(135deg, #475569, #64748b)"));
    }

    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background", "linear-gradient(135deg, #1e293b, #334155)")
                .set("color", "#ffffff")
                .set("width", "100%")
                .set("padding", "0.85rem 1rem")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.15)");

        H3 headerText = new H3("📊 Knowledge Base Details");
        headerText.getStyle()
                .set("color", "#ffffff")
                .set("text-align", "center")
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "600");

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
            expanded = true;
        } else {
            removeClassName("expanded");
            updateButtonContent(false);
            this.clearContents();
            expanded = false;
        }
    }
}