package br.ufrj.cos.components.treeview.dialog;

import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.components.qrcode.QRCodeComponent;
import br.ufrj.cos.components.sliderpanel.OpenCloseEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.ApplicationEventPublisher;

@PermitAll
public class ReferenceDetailsDialog {
    private final Dialog dialog;
    private final DiagramComponent diagramComponent;
    private final QRCodeComponent qrCodeComponent;
    private final StringBuilder pathString;
    private final ApplicationEventPublisher eventPublisher;

    public ReferenceDetailsDialog(DiagramComponent diagramComponent,
                                  QRCodeComponent qrCodeComponent,
                                  StringBuilder pathString,
                                  ApplicationEventPublisher eventPublisher) {
        this.diagramComponent = diagramComponent;
        this.qrCodeComponent = qrCodeComponent;
        this.pathString = pathString;
        this.eventPublisher = eventPublisher;
        this.dialog = createDialog();
    }

    public void open(String paperTitle, String paperLink) {
        configureAndOpenDialog(paperTitle, paperLink);
    }

    private Dialog createDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        dialog.setWidth("95%");
        dialog.setMaxWidth("1200px");
        dialog.setHeight("95%");
        dialog.setMaxHeight("800px");

        return dialog;
    }

    private void configureAndOpenDialog(String paperTitle, String paperLink) {
        dialog.setHeaderTitle("Reference Details");

        // Create main layout
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(true);
        mainLayout.setSizeFull();
        mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Create compact header section
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setSpacing(true);
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Create combined title and link component
        Anchor titleLink = new Anchor(paperLink, paperTitle);
        titleLink.setTarget("_blank");
        titleLink.getElement().setAttribute("aria-label", "Open paper in new tab");
        titleLink.addClassName("paper-title-link");

        // Create QR code with smaller size
        Component qrCode = qrCodeComponent.generateQRCode(paperLink, 60, 60);
        Div qrCodeContainer = new Div(qrCode);
        qrCodeContainer.addClassName("qr-code-container");

        // Add components to header layout
        headerLayout.add(titleLink, qrCodeContainer);
        headerLayout.setFlexGrow(1, titleLink);

        // Create diagram section
        Div diagramContainer = new Div();
        diagramContainer.setId("diagram");
        diagramContainer.setWidthFull();
        diagramContainer.setHeight("600px");
        diagramContainer.addClassName("diagram-container");

        // Add path information
        Span pathInfo = new Span(pathString.toString());
        pathInfo.addClassName("path-info");

        // Add CSS styles
        UI.getCurrent().getElement().executeJs(
                "document.head.innerHTML += '<style>" +
                        ".paper-title-link { " +
                        "   text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5); " +
                        "   margin: 0; " +
                        "   word-wrap: break-word; " +
                        "   font-size: 1.2em; " +
                        "   font-weight: 500; " +
                        "   text-decoration: none; " +
                        "   color: var(--lumo-primary-text-color); " +
                        "} " +
                        ".paper-title-link:hover { " +
                        "   text-decoration: underline; " +
                        "   color: var(--lumo-primary-color); " +
                        "} " +
                        ".qr-code-container { display: flex; justify-content: center; margin: 0; min-width: 60px; }" +
                        ".diagram-container { min-height: 200px; margin: 1rem 0; flex-grow: 1; }" +
                        ".path-info { word-wrap: break-word; max-width: 100%; font-size: 0.9em; color: var(--lumo-secondary-text-color); }" +
                        "</style>';"
        );

        // Combine all components
        mainLayout.add(
                headerLayout,
                diagramContainer,
                pathInfo
        );

        // Add main content
        dialog.add(mainLayout);

        // Configure header with close button
        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());
        closeButton.getElement().setAttribute("aria-label", "Close dialog");
        dialog.getHeader().add(closeButton);

        // Configure footer
        Button footerCloseButton = new Button("Close", e -> dialog.close());
        footerCloseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footerLayout = new HorizontalLayout(footerCloseButton);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footerLayout.setWidthFull();

        dialog.getFooter().add(footerLayout);

        // Add diagram initialization
        dialog.addAttachListener(event -> {
            this.diagramComponent.execute();
        });

        dialog.open();
        eventPublisher.publishEvent(new OpenCloseEvent(OpenCloseEvent.Action.CLOSE));
    }
}