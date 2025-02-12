package br.ufrj.cos.components.annotation;

import br.ufrj.cos.components.annotation.events.AnnotationDialogRequestedEvent;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@UIScope
@org.springframework.stereotype.Component
@PermitAll
public class AnnotationDialog extends Dialog {

    private Div contentBox;
    private final AnnotationService annotationService;

    @Getter @Setter
    private DomainBase domainBase;

    private Date lastUpdate;
    private final HorizontalLayout lastUpdateMessage;

    private EnhancedRichTextEditor textEditor;
    private AnnotationData annotationData;

    @Autowired
    public AnnotationDialog(AnnotationService annotationService, UserApplicationService userApplicationService) {
        this.annotationService = annotationService;
        this.lastUpdateMessage = new HorizontalLayout();

        this.setWidth("50%");
        this.setHeight("42%");
        this.setModal(false);
        this.setDraggable(true);
        this.setResizable(true);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);
    }

    @PostConstruct
    private void init() {
        // Content Box (to insert Vaadin components)
        contentBox = new Div();
        contentBox.setSizeFull(); // Ensure it takes up the dialog width
        contentBox.getStyle().set("padding", "10px");
        contentBox.getStyle().setBorder("1px solid lightgray");

        // Main Layout
        //VerticalLayout dialogLayout = new VerticalLayout(createTitleBox(title), contentBox);
        VerticalLayout dialogLayout = new VerticalLayout(contentBox); // Create a main layout
        dialogLayout.setPadding(false); // No padding for the main layout
        dialogLayout.setSpacing(true);   // Add spacing for a cleaner look
        dialogLayout.setSizeFull();     // Ensure it takes up the entire dialog width
        dialogLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(dialogLayout);

        // Close Button
        Button closeButton = new Button("Close", event -> close());
        Button saveButton = new Button("Save", buttonClickEvent -> {
            Annotation notes = this.annotationData.getAnnotation();
            if (this.textEditor.getValue().isEmpty()) {
                notes.setText("");
            } else {
                notes.setText(this.textEditor.getValue());
            }
            notes.setLastUpdate(new Date());
            this.lastUpdate = notes.getLastUpdate();
            this.annotationService.updateAnnotation(notes);
            this.updateLastUpdateMessage();
            NotificationUtils.showSuccessNotification("Annotation updated.");
        });
        saveButton.setThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName());

        this.updateLastUpdateMessage();

        HorizontalLayout hl2 = new HorizontalLayout(saveButton, closeButton);
        getFooter().add(this.lastUpdateMessage, hl2);

        // Add a CSS class to the dialog itself for styling
        addClassName("custom-dialog");

        this.textEditor = createRichText();

        setContent(this.textEditor); // Now passing Component
    }

    private void updateLastUpdateMessage() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        this.lastUpdateMessage.removeAll();
        this.lastUpdateMessage.add(
                new Html(String.format("<b><i>Last update: %s.</i></b>",
                        (this.lastUpdate != null)? formatter.format(this.lastUpdate) : "-")));
        this.lastUpdateMessage.setAlignItems(FlexComponent.Alignment.START);
        this.lastUpdateMessage.setAlignSelf(FlexComponent.Alignment.START);
        this.lastUpdateMessage.setWidth("72%");
    }

    // Method to set content
    private void setContent(Component... components) { // Now accepts Vaadin Component
        contentBox.removeAll(); // Clear the existing content
        contentBox.add(components);
    }

    private Div createTitleBox(String title) {
        Div titleBox = new Div();
        titleBox.setWidthFull();
        titleBox.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "var(--lumo-primary-contrast-color)")
                .set("padding", "10px")
                .set("text-align", "center");

        H3 titleText = new H3(title);
        titleText.getStyle().set("margin", "0");
        titleBox.add(titleText);

        return titleBox;
    }

    private EnhancedRichTextEditor createRichText() {
        EnhancedRichTextEditor richTextArea = new EnhancedRichTextEditor();
        richTextArea.setSizeFull();
        richTextArea.setValueChangeMode(ValueChangeMode.EAGER);

        return richTextArea;
    }

    private AnnotationData fillAnnotationData(UserApplication userApplication,
                                              DomainBase domainBase,
                                              Date lastUpdate,
                                              String text) {

        return AnnotationData.builder().domainBase(domainBase).lastUpdate(lastUpdate).text(text).build();
    }

    @EventListener
    public void handleAnnotationDialogRequestedEvent(AnnotationDialogRequestedEvent event) {
        AnnotationAction action = event.getAction();
        DomainBase domainBase = event.getDomainBase();
        UserApplication user = event.getUser();

        AnnotationData notes = new AnnotationData(
                "",
                event.getUser(),
                new Date(),
                event.getDomainBase()
        );

        if (domainBase instanceof IoTDomain domain) {
            setHeaderTitle(String.format("Annotation viewer [%s - %s]", domain.getName(), (action.equals(AnnotationAction.ADD))? "Adding" : "Editing"));
            Optional<Annotation> annotation = this.annotationService.getAnnotationsByUserApplicationAndIoTDomain(user, domain);
            if (annotation.isPresent()) {
                notes.setText(annotation.get().getText());
                notes.setLastUpdate(annotation.get().getLastUpdate());
            }
        } else if (domainBase instanceof Architecture architecture) {
            setHeaderTitle(String.format("Annotation viewer [%s - %s]", architecture.getName(), (action.equals(AnnotationAction.ADD))? "Adding" : "Editing"));
            Optional<Annotation> annotation = this.annotationService.getAnnotationsByUserApplicationAndArchitecture(user, architecture);
            if (annotation.isPresent()) {
                notes.setText(annotation.get().getText());
                notes.setLastUpdate(annotation.get().getLastUpdate());
            }
        } else if (domainBase instanceof QualityRequirement qr) {
            setHeaderTitle(String.format("Annotation viewer [%s - %s]", qr.getName(), (action.equals(AnnotationAction.ADD))? "Adding" : "Editing"));
            Optional<Annotation> annotation = this.annotationService.getAnnotationsByUserApplicationAndQualityRequirement(user, qr);
            if (annotation.isPresent()) {
                notes.setText(annotation.get().getText());
                notes.setLastUpdate(annotation.get().getLastUpdate());
            }
        } else if (domainBase instanceof Technology tech) {
            setHeaderTitle(String.format("Annotation viewer [%s - %s]", tech.getDescription(), (action.equals(AnnotationAction.ADD))? "Adding" : "Editing"));
            Optional<Annotation> annotation = this.annotationService.getAnnotationsByUserApplicationAndTechnology(user, tech);
            if (annotation.isPresent()) {
                notes.setText(annotation.get().getText());
                notes.setLastUpdate(annotation.get().getLastUpdate());
            }
        }

        this.textEditor.setValue(notes.getText());
        this.lastUpdate = notes.getLastUpdate();
        this.updateLastUpdateMessage();
        this.annotationData = notes;

        open();
    }
}