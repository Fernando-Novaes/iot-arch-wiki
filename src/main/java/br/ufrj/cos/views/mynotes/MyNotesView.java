package br.ufrj.cos.views.mynotes;

import br.ufrj.cos.domain.Annotation;
import br.ufrj.cos.service.AnnotationService;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.matthew.fliplayout.FlipLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("My Notes")
@Route(value = "mynotes-view", layout = MainLayout.class)
@PermitAll
public class MyNotesView extends BaseView implements HasTour {

    private static final Logger logger = LoggerFactory.getLogger(MyNotesView.class);

    // --- Injected Services ---
    private final UserApplicationService userApplicationService;
    private final AnnotationService annotationService;

    // --- UI Components ---

    //Search
    private final HorizontalLayout filterLayout = new HorizontalLayout();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryComboBox = new ComboBox<>();
    private final Button searchButton = new Button("Filter", VaadinIcon.SEARCH.create());
    private final Button clearButton = new Button("Clear", VaadinIcon.ERASER.create());

    //New note
    Button floatingButton = new Button();

    private FlexLayout cardContainer; // Use FlexLayout for responsive card grid

    public MyNotesView(UserApplicationService userApplicationService, AnnotationService annotationService) {
        this.userApplicationService = userApplicationService;
        this.annotationService = annotationService;

        createHeader("My Notes");
        createSearchContainer("Filter Notes", "Filter by content...", "Filter by title...");
        initializeCardContainer();
        createNewFloatButton();
    }

    @PostConstruct
    private void init() {
        // Initial load of all cards
        filterAndDisplayCards();
    }

    private void createNewFloatButton() {
        Icon plusIcon = VaadinIcon.PLUS.create();
        floatingButton.setIcon(plusIcon);
        Style buttonStyle = floatingButton.getStyle();

        buttonStyle.set("position", "fixed");
        buttonStyle.set("bottom", "25px");
        buttonStyle.set("right", "25px");
        buttonStyle.set("z-index", "1000");
        buttonStyle.set("width", "56px");
        buttonStyle.set("height", "56px");
        buttonStyle.set("border-radius", "50%");
        buttonStyle.set("background-color", "var(--lumo-primary-color)");
        buttonStyle.set("color", "var(--lumo-primary-contrast-color)");
        buttonStyle.set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.25)");
        buttonStyle.set("border", "none");
        buttonStyle.set("cursor", "pointer");
        buttonStyle.set("display", "flex");
        buttonStyle.set("align-items", "center");
        buttonStyle.set("justify-content", "center");

        plusIcon.getStyle().set("width", "24px");
        plusIcon.getStyle().set("height", "24px");

        Tooltip.forComponent(floatingButton).withText("Create a new note");
        floatingButton.addClickListener(event -> {
            this.createNewNote();
        });

        // Add the button to the main content of the view
        getContent().add(floatingButton);
    }

    private void createSearchContainer(String title, String searchPlaceholder, String titlePlaceholder) {
        H3 header = new H3(title);

        searchField.setPlaceholder(searchPlaceholder);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        categoryComboBox.setPlaceholder(titlePlaceholder);
        categoryComboBox.setClearButtonVisible(true);
        // Populate the category combo box with actual titles from the user's notes
        List<String> titles = this.userApplicationService.findByUserName(SecurityUtils.getUsername())
                .getAnnotations().stream()
                .map(Annotation::getTitle)
                .distinct()
                .collect(Collectors.toList());
        categoryComboBox.setItems(titles);

        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(click -> filterAndDisplayCards());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> {
            searchField.clear();
            categoryComboBox.clear();
            filterAndDisplayCards(); // Re-run to show all cards
        });

        filterLayout.add(searchField, categoryComboBox, searchButton, clearButton);
        filterLayout.setWidthFull();
        filterLayout.setFlexGrow(1, searchField);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        filterLayout.getStyle().setBorder("1px solid grey");
        filterLayout.setSpacing(true);
        filterLayout.setPadding(true);

        VerticalLayout searchContainer = new VerticalLayout(header, filterLayout);
        searchContainer.setWidthFull();
        searchContainer.setPadding(false);
        searchContainer.setSpacing(false);

        getContent().add(searchContainer);
    }

    private void initializeCardContainer() {
        cardContainer = new FlexLayout();
        cardContainer.setWidthFull();
        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Automatically wraps cards to new lines
        cardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        cardContainer.getStyle().set("gap", "1em"); // Consistent spacing between cards
        cardContainer.getStyle().set("padding-top", "1em");
        getContent().add(cardContainer);
    }

    private void filterAndDisplayCards() {
        cardContainer.removeAll(); // Clear any existing cards before displaying new results

        String searchTerm = searchField.getValue().trim().toLowerCase();
        String selectedTitle = categoryComboBox.getValue();

        List<Annotation> allAnnotations = this.userApplicationService.findByUserName(SecurityUtils.getUsername()).getAnnotations();

        List<Annotation> filteredAnnotations = allAnnotations.stream()
                .filter(annotation -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            (annotation.getTitle() != null && annotation.getTitle().toLowerCase().contains(searchTerm)) ||
                            (annotation.getTopic() != null && annotation.getTopic().toLowerCase().contains(searchTerm)) ||
                            (annotation.getText() != null && annotation.getText().toLowerCase().contains(searchTerm));

                    boolean matchesTitle = selectedTitle == null ||
                            (annotation.getTitle() != null && annotation.getTitle().equals(selectedTitle));

                    return matchesSearch && matchesTitle;
                })
                .toList();

        logger.info(String.format("Displaying %d filtered annotations", filteredAnnotations.size()));

        if (filteredAnnotations.isEmpty()) {
            cardContainer.add(new Span("No notes match your criteria."));
        } else {
            filteredAnnotations.forEach(annotation -> {
                FlipLayout card = createCard(annotation);
                cardContainer.add(card);
            });
        }
    }

    private FlipLayout createCard(Annotation annotation) {
        // --- FlipLayout Configuration (created first) ---
        FlipLayout flipLayout = new FlipLayout();

        // --- Front of the Card ---
        VerticalLayout frontForm = new VerticalLayout();
        // ... (rest of the frontForm setup is the same)
        frontForm.setPadding(true);
        frontForm.setSpacing(false);
        frontForm.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        frontForm.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        frontForm.getStyle().setBackgroundColor("var(--lumo-contrast-10pct)");
        frontForm.setWidth("45em");
        frontForm.setHeight("25em");

        // --- Card Header with Title and Delete Button ---
        H1 cardTitle = new H1(annotation.getTitle() != null ? annotation.getTitle() : "No Title");
        cardTitle.getStyle().set("margin-top", "0");
        cardTitle.getStyle().set("margin-bottom", "0.5em");
        cardTitle.getStyle().set("font-size", "var(--lumo-font-size-xl)");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Delete this note");
        deleteButton.getStyle().set("margin-left", "auto");

        // *** KEY CHANGE: Pass the flipLayout to the confirmAndDelete method ***
        deleteButton.addClickListener(e -> confirmAndDelete(annotation, flipLayout));

        HorizontalLayout headerLayout = new HorizontalLayout(cardTitle, deleteButton);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // ... (rest of the frontForm body setup is the same) ...
        Span cardTopic = new Span(annotation.getTopic() != null ? annotation.getTopic() : "");
        cardTopic.getStyle().set("color", "var(--lumo-secondary-text-color)");
        cardTopic.getStyle().set("font-size", "var(--lumo-font-size-m)");
        cardTopic.getStyle().set("margin-bottom", "auto");

        Span cardLastUpdate = new Span("Last Update: " + (annotation.getLastUpdate() != null ? annotation.getLastUpdate().toString() : "N/A"));
        cardLastUpdate.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        cardLastUpdate.getStyle().set("font-size", "var(--lumo-font-size-m)");

        Button flipButton = new Button("View Details", VaadinIcon.ARROW_FORWARD.create());
        flipButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        frontForm.add(headerLayout, cardTopic, cardLastUpdate, flipButton);
        frontForm.setAlignItems(FlexComponent.Alignment.STRETCH);

        // --- Back of the Card ---
        VerticalLayout backForm = new VerticalLayout();
        // ... (rest of the backForm setup is the same) ...
        backForm.setPadding(true);
        backForm.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        backForm.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        backForm.setWidth(frontForm.getWidth());
        backForm.setHeight(frontForm.getHeight());

        Html cardFullText = new Html("<div style='overflow: auto; height: 100%;'>" + (annotation.getText() != null ? annotation.getText() : "") + "</div>");
        Button unflipButton = new Button("Back to Summary", VaadinIcon.ARROW_BACKWARD.create());
        unflipButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        backForm.add(cardFullText, unflipButton);
        backForm.expand(cardFullText);

        // --- Final FlipLayout Configuration ---
        flipLayout.setFrontComponent(frontForm);
        flipLayout.setBackComponent(backForm);
        flipButton.addClickListener(event -> flipLayout.flip());
        unflipButton.addClickListener(event -> flipLayout.flip());

        return flipLayout;
    }

    // *** NEW METHOD for delete confirmation and logic ***
    private void confirmAndDelete(Annotation annotation, FlipLayout card) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Note: '" + annotation.getTitle() + "'");
        dialog.setText("Are you sure you want to permanently delete this note?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        // --- Get the style object for the card's front component ---
        // We style the front component as it's the one that's visible.
        Style cardStyle = card.getFrontComponent().getStyle();

        // --- Apply the style when the dialog opens ---
        cardStyle.set("border", "2px solid var(--lumo-error-color)");
        cardStyle.set("box-shadow", "0 0 10px var(--lumo-error-color-50pct)");

        // --- Remove the style when the dialog is closed for any reason ---
        dialog.addDetachListener(e -> {
            cardStyle.remove("border");
            // Reset the box-shadow to its original state
            cardStyle.set("box-shadow", "var(--lumo-box-shadow-s)");
        });

        // --- Define the action on confirmation ---
        dialog.addConfirmListener(event -> deleteAnnotation(annotation));

        dialog.open();
    }

    private void deleteAnnotation(Annotation annotation) {
        try {
            if (annotation.getUserApplication() != null) {
                // Just by removing it from the list, JPA will automatically delete the "orphaned" annotation.
                annotation.getUserApplication().getAnnotations().remove(annotation);
            }
            annotationService.delete(annotation);
            NotificationUtils.showSuccessNotification("Note deleted successfully.");
            filterAndDisplayCards(); // Refresh the view
        } catch (Exception e) {
            logger.error("Error deleting annotation with ID: " + annotation.getId(), e);
            NotificationUtils.showErrorNotification("Error deleting note. Please try again.");
        }
    }

    // Method to open the dialog for a new note
    private void createNewNote() {
        MyNotesDialog dialog = new MyNotesDialog(annotationService, userApplicationService);

        // Listen for the save event
        dialog.addSaveListener(event -> {
            // This code runs AFTER a note is successfully saved
            Notification.show("Note saved: " + event.getAnnotation().getTitle());
            // Refresh your list of note cards
            filterAndDisplayCards();
        });

        // Open the dialog with a new, empty Annotation object
        dialog.open(new Annotation());
    }

    // To edit an existing note (e.g., from a button on a card)
    private void editNote(Annotation annotation) {
        MyNotesDialog dialog = new MyNotesDialog(annotationService, userApplicationService);

        dialog.addSaveListener(event -> {
            Notification.show("Note updated: " + event.getAnnotation().getTitle());
            filterAndDisplayCards();
        });

        // Open the dialog with the existing annotation object
        dialog.open(annotation);
    }

    @Override
    public Onboarding createTour() {
        return new TourUtils().build()
                .addStep(filterLayout, "Filter Options", new Html("<div>This section allows you to refine the list of notes.</div>"), PopupPosition.BOTTOM)
                .addStep(searchField, "Search by Content", new Html("<div>Enter a keyword to search within your notes.</div>"), PopupPosition.BOTTOM)
                .addStep(categoryComboBox, "Filter by Topic", new Html("<div>Narrow the list by selecting a topic.</div>"), PopupPosition.BOTTOM)
                .addStep(searchButton, "Apply Filters", new Html("<div>Click here to apply your search term and topic filter to the list of notes.</div>"), PopupPosition.BOTTOM)
                .addStep(clearButton, "Clear Filters", new Html("<div>ick this button to remove all filters and view all of your notes again.</div>"), PopupPosition.BOTTOM)
                .addStep(floatingButton, "Create a New Note", new Html("<div>Click here to start a new note.</div>"), PopupPosition.BOTTOM)
                .addStep(cardContainer, "Notes",
                        new Html("<div>Your notes are organized as cards. The front of each card shows key details like the title and topic. Click the 'View Details' button to flip the card and read the full text.</div>"), PopupPosition.BOTTOM)
                .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}
