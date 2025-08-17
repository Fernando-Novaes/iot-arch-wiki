package br.ufrj.cos.views.mynotes;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.domain.Annotation;
import br.ufrj.cos.service.AnnotationService;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final Button searchButton = new Button("Search", VaadinIcon.SEARCH.create());
    private final Button clearButton = new Button("Clear", VaadinIcon.ERASER.create());

    //New note
    Button floatingButton = new Button();

    private FlexLayout cardContainer; // Use FlexLayout for responsive card grid

    public MyNotesView(UserApplicationService userApplicationService, AnnotationService annotationService) {
        this.userApplicationService = userApplicationService;
        this.annotationService = annotationService;

        createHeader("My Notes");
        createSearchContainer("Search Notes", "Search by content...", "Filter by title...");
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
                .collect(Collectors.toList());

        logger.info(String.format("Displaying %d filtered annotations", filteredAnnotations.size()));

        if (filteredAnnotations.isEmpty()) {
            cardContainer.add(new Span("No notes match your criteria."));
        } else {
            filteredAnnotations.forEach(annotation -> {
                FlipLayout card = createCard(
                        annotation.getTitle(),
                        annotation.getTopic(),
                        annotation.getLastUpdate() != null ? annotation.getLastUpdate().toString() : "N/A",
                        annotation.getText()
                );
                cardContainer.add(card);
            });
        }
    }

    private FlipLayout createCard(String title, String topic, String lastUpdate, String fullText) {
        // --- Front of the Card ---
        VerticalLayout frontForm = new VerticalLayout();
        frontForm.setPadding(true);
        frontForm.setSpacing(false);
        frontForm.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        frontForm.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        frontForm.getStyle().setBackgroundColor("var(--lumo-contrast-10pct)");
        frontForm.setWidth("45em");
        frontForm.setHeight("25em"); // Give cards a consistent height

        H3 cardTitle = new H3(title != null ? title : "No Title");
        cardTitle.getStyle().set("margin-top", "0");
        cardTitle.getStyle().set("margin-bottom", "0.5em");

        Span cardTopic = new Span(topic != null ? topic : "");
        cardTopic.getStyle().set("color", "var(--lumo-secondary-text-color)");
        cardTopic.getStyle().set("font-size", "var(--lumo-font-size-s)");
        cardTopic.getStyle().set("margin-bottom", "auto"); // Pushes the date and button to the bottom

        Span cardLastUpdate = new Span("Last Update: " + lastUpdate);
        cardLastUpdate.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        cardLastUpdate.getStyle().set("font-size", "var(--lumo-font-size-xs)");

        Button flipButton = new Button("View Details", VaadinIcon.ARROW_FORWARD.create());
        flipButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        frontForm.add(cardTitle, cardTopic, cardLastUpdate, flipButton);
        frontForm.setAlignItems(FlexComponent.Alignment.START);

        // --- Back of the Card ---
        VerticalLayout backForm = new VerticalLayout();
        backForm.setPadding(true);
        backForm.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        backForm.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        backForm.setWidth(frontForm.getWidth());
        backForm.setHeight(frontForm.getHeight());

        Html cardFullText = new Html("<div style='overflow: auto; height: 100%;'>" + (fullText != null ? fullText : "") + "</div>");

        Button unflipButton = new Button("Back to Summary", VaadinIcon.ARROW_BACKWARD.create());
        unflipButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        backForm.add(cardFullText, unflipButton);
        backForm.expand(cardFullText); // Make the text area take up available space

        // --- FlipLayout Configuration ---
        FlipLayout flipLayout = new FlipLayout(frontForm, backForm);
        flipButton.addClickListener(event -> flipLayout.flip());
        unflipButton.addClickListener(event -> flipLayout.flip());

        return flipLayout;
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
                .addStep(filterLayout, "Filter options", new Html("<div>The filtering mechanism allows for precise refinement of the available notes.</div>"), PopupPosition.BOTTOM)
                .addStep(searchField, "Filter by content", new Html("<div>Here you can search a Note by its content.</div>"), PopupPosition.BOTTOM)
                .addStep(categoryComboBox, "Filter by topic", new Html("<div>Select the available topic to search a note.</div>"), PopupPosition.BOTTOM)
                .addStep(searchButton, "Search button", new Html("<div>Perform the filter by clicking on the Search button.</div>"), PopupPosition.BOTTOM)
                .addStep(clearButton, "Clear button", new Html("<div>Clear all filter options and all notes will be visible.</div>"), PopupPosition.BOTTOM)
                .addStep(floatingButton, "New note button", new Html("<div>Add a Note hitting this button.</div>"), PopupPosition.BOTTOM)
                .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}
