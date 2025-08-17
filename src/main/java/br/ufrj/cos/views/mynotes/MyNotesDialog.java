package br.ufrj.cos.views.mynotes;

import br.ufrj.cos.domain.Annotation;
import br.ufrj.cos.service.AnnotationService;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;

import java.util.Date;

public class MyNotesDialog extends Dialog {

    // --- UI Components ---
    private final TextField title = new TextField("Title");
    private final TextField topic = new TextField("Topic");
    // Using a rich text editor for a better user experience
    private EnhancedRichTextEditor text;

    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    // --- Services and State ---
    private final AnnotationService annotationService;
    private final UserApplicationService userApplicationService;
    private final Binder<Annotation> binder = new BeanValidationBinder<>(Annotation.class);
    private Annotation currentAnnotation;

    public MyNotesDialog(AnnotationService annotationService, UserApplicationService userApplicationService) {
        this.annotationService = annotationService;
        this.userApplicationService = userApplicationService;

        setHeaderTitle("New Note");
        setDraggable(true);
        setResizable(true);
        setWidth("800px");

        text = this.createRichText();

        // --- Layout and Binding ---
        FormLayout formLayout = createFormLayout();
        binder.bindInstanceFields(this); // Binds fields by name (title, topic, text)

        // Add validation
        binder.forField(title).asRequired("Title cannot be empty.").bind(Annotation::getTitle, Annotation::setTitle);
        binder.forField(text).asRequired("Text content cannot be empty.").bind(Annotation::getText, Annotation::setText);

        add(formLayout);
        getFooter().add(createButtonsLayout());

        // Close the dialog when pressing Escape
        addDialogCloseActionListener(e -> close());
    }

    private EnhancedRichTextEditor createRichText() {
        EnhancedRichTextEditor richTextArea = new EnhancedRichTextEditor();
        richTextArea.setSizeFull();
        richTextArea.setValueChangeMode(ValueChangeMode.EAGER);

        return richTextArea;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        text.setHeight("400px");
        formLayout.add(title, topic, text);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(text, 2);
        return formLayout;
    }

    private Component createButtonsLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Add keyboard shortcuts
        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        // Add click listeners
        saveButton.addClickListener(event -> validateAndSave());
        cancelButton.addClickListener(event -> close());

        return new HorizontalLayout(cancelButton, saveButton);
    }

    /**
     * Opens the dialog to edit a given Annotation. If the annotation is new,
     * it prepares the form for creation.
     * @param annotation The annotation to edit, or a new Annotation object.
     */
    public void open(Annotation annotation) {
        this.currentAnnotation = annotation;
        binder.setBean(this.currentAnnotation);

        if (annotation.getId() == null) {
            setHeaderTitle("New Note");
        } else {
            setHeaderTitle("Edit Note: " + annotation.getTitle());
        }

        super.open();
    }

    private void validateAndSave() {
        if (binder.validate().isOk()) {
            // Set fields that are not on the form
            currentAnnotation.setLastUpdate(new Date());
            // Set the owner of the note to the currently logged-in user
            if (currentAnnotation.getUserApplication() == null) {
                currentAnnotation.setUserApplication(userApplicationService.findByUserName(SecurityUtils.getUsername()));
            }

            currentAnnotation.setText(text.getHtmlValue());

            // Persist the data
            Annotation savedAnnotation = annotationService.saveAnnotation(currentAnnotation);

            // Fire an event to notify the parent view
            fireEvent(new SaveEvent(this, savedAnnotation));
            close();
        }
    }

    // --- Custom Event System ---
    public static abstract class MyNotesDialogEvent extends ComponentEvent<MyNotesDialog> {
        private final Annotation annotation;
        protected MyNotesDialogEvent(MyNotesDialog source, Annotation annotation) {
            super(source, false);
            this.annotation = annotation;
        }
        public Annotation getAnnotation() {
            return annotation;
        }
    }

    public static class SaveEvent extends MyNotesDialogEvent {
        SaveEvent(MyNotesDialog source, Annotation annotation) {
            super(source, annotation);
        }
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

}
