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
import com.vaadin.flow.component.icon.VaadinIcon;
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

        dialog.setWidth("75vw");
        dialog.setMaxWidth("1100px");
        dialog.setHeight("85vh");
        dialog.setMaxHeight("850px");

        return dialog;
    }

    private void configureAndOpenDialog(String paperTitle, String paperLink) {
        dialog.setHeaderTitle("Reference Details & Architecture Hierarchy");

        // Create main layout
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setSizeFull();

        // Create compact header section
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setSpacing(true);
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Title and paper link
        Icon linkIcon = VaadinIcon.EXTERNAL_LINK.create();
        linkIcon.getStyle().set("margin-right", "0.4em").set("font-size", "1.1em");

        Anchor titleLink = new Anchor(paperLink, paperTitle);
        titleLink.setTarget("_blank");
        titleLink.getElement().setAttribute("aria-label", "Open paper in new tab");
        titleLink.addClassName("paper-title-link");

        HorizontalLayout titleBox = new HorizontalLayout(linkIcon, titleLink);
        titleBox.setAlignItems(FlexComponent.Alignment.CENTER);

        // QR Code
        Component qrCode = qrCodeComponent.generateQRCode(paperLink, 120, 120);
        Div qrCodeContainer = new Div(qrCode);
        qrCodeContainer.addClassName("qr-code-container");

        headerLayout.add(titleBox, qrCodeContainer);
        headerLayout.setFlexGrow(1, titleBox);

        // Diagram section
        Div diagramContainer = new Div();
        diagramContainer.setId("diagram");
        diagramContainer.setWidthFull();
        diagramContainer.setHeight("480px");
        diagramContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "12px")
                .set("overflow", "hidden");

        // Add CSS styles
        UI.getCurrent().getElement().executeJs(
                "document.head.innerHTML += '<style>" +
                        ".paper-title-link { " +
                        "   margin: 0; " +
                        "   word-wrap: break-word; " +
                        "   font-size: 1.15em; " +
                        "   font-weight: 600; " +
                        "   text-decoration: none; " +
                        "   color: var(--lumo-primary-text-color); " +
                        "} " +
                        ".paper-title-link:hover { " +
                        "   text-decoration: underline; " +
                        "   color: var(--lumo-primary-color); " +
                        "} " +
                        ".qr-code-container { display: flex; justify-content: center; align-items: center; margin: 0; min-width: 120px; padding: 6px; border: 1px solid var(--lumo-contrast-15pct); border-radius: 10px; background: #ffffff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }" +
                        "</style>';"
        );

        mainLayout.add(
                headerLayout,
                diagramContainer
        );

        dialog.add(mainLayout);

        // Header close button
        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());
        closeButton.getElement().setAttribute("aria-label", "Close dialog");
        dialog.getHeader().add(closeButton);

        // Footer
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