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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
    private final HorizontalLayout filterLayout = new HorizontalLayout();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryComboBox = new ComboBox<>();
    private final Button searchButton = new Button("Filter", VaadinIcon.SEARCH.create());
    private final Button clearButton = new Button("Clear", VaadinIcon.ERASER.create());

    private final Button floatingButton = new Button();
    private FlexLayout cardContainer;

    public MyNotesView(UserApplicationService userApplicationService, AnnotationService annotationService) {
        this.userApplicationService = userApplicationService;
        this.annotationService = annotationService;

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setWidthFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);

        // Hero Banner Header
        mainContainer.add(createHeroCard());

        // Search & Filter Card
        createSearchContainer("Filter Notes", "Filter by content...", "Filter by title...", mainContainer);

        // Cards Container
        initializeCardContainer(mainContainer);

        // Floating Action Button
        createNewFloatButton();

        getContent().add(mainContainer);
    }

    @PostConstruct
    private void init() {
        filterAndDisplayCards();
    }

    private Component createHeroCard() {
        Div heroCard = new Div();
        heroCard.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "16px")
                .set("padding", "1.75rem 2rem")
                .set("color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.2)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        H2 title = new H2("My Notes & Annotations");
        title.getStyle()
                .set("margin", "0 0 0.4rem 0")
                .set("font-size", "1.8rem")
                .set("font-weight", "700")
                .set("color", "#ffffff");

        Paragraph subtitle = new Paragraph("Organize, search, and manage your personal IoT architecture research notes and insights");
        subtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.95rem")
                .set("opacity", "0.9")
                .set("line-height", "1.5");

        heroCard.add(title, subtitle);
        return heroCard;
    }

    private void createNewFloatButton() {
        Icon plusIcon = VaadinIcon.PLUS.create();
        floatingButton.setIcon(plusIcon);
        Style buttonStyle = floatingButton.getStyle();

        buttonStyle.set("position", "fixed");
        buttonStyle.set("bottom", "28px");
        buttonStyle.set("right", "28px");
        buttonStyle.set("z-index", "1000");
        buttonStyle.set("width", "56px");
        buttonStyle.set("height", "56px");
        buttonStyle.set("border-radius", "50%");
        buttonStyle.set("background", "linear-gradient(135deg, #2563eb, #1d4ed8)");
        buttonStyle.set("color", "#ffffff");
        buttonStyle.set("box-shadow", "0 6px 20px rgba(37, 99, 235, 0.4)");
        buttonStyle.set("border", "none");
        buttonStyle.set("cursor", "pointer");
        buttonStyle.set("display", "flex");
        buttonStyle.set("align-items", "center");
        buttonStyle.set("justify-content", "center");

        plusIcon.getStyle().set("width", "24px").set("height", "24px");

        Tooltip.forComponent(floatingButton).withText("Create a new note");
        floatingButton.addClickListener(event -> this.createNewNote());

        getContent().add(floatingButton);
    }

    private void createSearchContainer(String title, String searchPlaceholder, String titlePlaceholder, VerticalLayout parentContainer) {
        Div searchCard = new Div();
        searchCard.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "14px")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)")
                .set("padding", "1rem 1.25rem")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        searchField.setPlaceholder(searchPlaceholder);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        categoryComboBox.setPlaceholder(titlePlaceholder);
        categoryComboBox.setClearButtonVisible(true);

        List<String> titles = this.userApplicationService.findByUserName(SecurityUtils.getUsername())
                .getAnnotations().stream()
                .map(Annotation::getTitle)
                .distinct()
                .collect(Collectors.toList());
        categoryComboBox.setItems(titles);

        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "8px")
                .set("font-weight", "600");
        searchButton.addClickListener(click -> filterAndDisplayCards());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> {
            searchField.clear();
            categoryComboBox.clear();
            filterAndDisplayCards();
        });

        filterLayout.removeAll();
        filterLayout.add(searchField, categoryComboBox, searchButton, clearButton);
        filterLayout.setWidthFull();
        filterLayout.setFlexGrow(1, searchField);
        filterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        filterLayout.setSpacing(true);

        searchCard.add(filterLayout);
        parentContainer.add(searchCard);
    }

    private void initializeCardContainer(VerticalLayout parentContainer) {
        cardContainer = new FlexLayout();
        cardContainer.setWidthFull();
        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        cardContainer.getStyle().set("gap", "1.25rem");
        cardContainer.getStyle().set("padding-top", "0.5rem");
        parentContainer.add(cardContainer);
    }

    private void filterAndDisplayCards() {
        cardContainer.removeAll();

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
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("padding", "2rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "0.95rem");
            emptyState.setText("No notes match your criteria.");
            cardContainer.add(emptyState);
        } else {
            filteredAnnotations.forEach(annotation -> {
                FlipLayout card = createCard(annotation);
                cardContainer.add(card);
            });
        }
    }

    private FlipLayout createCard(Annotation annotation) {
        FlipLayout flipLayout = new FlipLayout();

        // --- Front of the Card ---
        VerticalLayout frontForm = new VerticalLayout();
        frontForm.setPadding(true);
        frontForm.setSpacing(true);
        frontForm.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 6px 18px rgba(0, 0, 0, 0.06)")
                .set("width", "360px")
                .set("height", "240px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("justify-content", "space-between");

        H3 cardTitle = new H3(annotation.getTitle() != null ? annotation.getTitle() : "No Title");
        cardTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "600")
                .set("color", "var(--lumo-header-text-color)");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Delete this note");
        deleteButton.getStyle().set("margin-left", "auto");

        deleteButton.addClickListener(e -> confirmAndDelete(annotation, flipLayout));

        HorizontalLayout headerLayout = new HorizontalLayout(cardTitle, deleteButton);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span cardTopic = new Span(annotation.getTopic() != null ? annotation.getTopic() : "General Note");
        cardTopic.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("color", "var(--lumo-primary-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("width", "fit-content");

        Span cardLastUpdate = new Span("Updated: " + (annotation.getLastUpdate() != null ? annotation.getLastUpdate().toString() : "N/A"));
        cardLastUpdate.getStyle()
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("font-size", "0.8rem");

        Button flipButton = new Button("View Details", VaadinIcon.ARROW_FORWARD.create());
        flipButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        flipButton.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "8px")
                .set("font-weight", "600")
                .set("width", "100%");

        frontForm.add(headerLayout, cardTopic, cardLastUpdate, flipButton);

        // --- Back of the Card ---
        VerticalLayout backForm = new VerticalLayout();
        backForm.setPadding(true);
        backForm.setSpacing(true);
        backForm.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 6px 18px rgba(0, 0, 0, 0.06)")
                .set("width", "360px")
                .set("height", "240px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column");

        Html cardFullText = new Html("<div style='overflow: auto; height: 100%; font-size: 0.875rem; line-height: 1.5; color: var(--lumo-body-text-color);'>" + (annotation.getText() != null ? annotation.getText() : "") + "</div>");

        Button editButton = new Button("Edit Note", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editButton.getStyle().set("margin-right", "auto");
        editButton.addClickListener(e -> editNote(annotation));

        Button unflipButton = new Button("Back", VaadinIcon.ARROW_BACKWARD.create());
        unflipButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        unflipButton.getStyle().set("margin-left", "auto");

        HorizontalLayout backActions = new HorizontalLayout(editButton, unflipButton);
        backActions.setWidthFull();

        backForm.add(cardFullText, backActions);
        backForm.expand(cardFullText);

        flipLayout.setFrontComponent(frontForm);
        flipLayout.setBackComponent(backForm);
        flipButton.addClickListener(event -> flipLayout.flip());
        unflipButton.addClickListener(event -> flipLayout.flip());

        return flipLayout;
    }

    private void confirmAndDelete(Annotation annotation, FlipLayout card) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Note: '" + annotation.getTitle() + "'");
        dialog.setText("Are you sure you want to permanently delete this note?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        Style cardStyle = card.getFrontComponent().getStyle();
        cardStyle.set("border", "2px solid var(--lumo-error-color)");
        cardStyle.set("box-shadow", "0 0 10px var(--lumo-error-color-50pct)");

        dialog.addDetachListener(e -> {
            cardStyle.remove("border");
            cardStyle.set("box-shadow", "0 6px 18px rgba(0, 0, 0, 0.06)");
        });

        dialog.addConfirmListener(event -> deleteAnnotation(annotation));
        dialog.open();
    }

    private void deleteAnnotation(Annotation annotation) {
        try {
            if (annotation.getUserApplication() != null) {
                annotation.getUserApplication().getAnnotations().remove(annotation);
            }
            annotationService.delete(annotation);
            NotificationUtils.showSuccessNotification("Note deleted successfully.");
            filterAndDisplayCards();
        } catch (Exception e) {
            logger.error("Error deleting annotation with ID: " + annotation.getId(), e);
            NotificationUtils.showErrorNotification("Error deleting note. Please try again.");
        }
    }

    private void createNewNote() {
        MyNotesDialog dialog = new MyNotesDialog(annotationService, userApplicationService);

        dialog.addSaveListener(event -> {
            Notification.show("Note saved: " + event.getAnnotation().getTitle());
            filterAndDisplayCards();
        });

        dialog.open(new Annotation());
    }

    private void editNote(Annotation annotation) {
        MyNotesDialog dialog = new MyNotesDialog(annotationService, userApplicationService);

        dialog.addSaveListener(event -> {
            Notification.show("Note updated: " + event.getAnnotation().getTitle());
            filterAndDisplayCards();
        });

        dialog.open(annotation);
    }

    @Override
    public Onboarding createTour() {
        return new TourUtils().build()
                .addStep(filterLayout, "Filter Options", new Html("<div>This section allows you to refine the list of notes.</div>"), PopupPosition.BOTTOM)
                .addStep(searchField, "Search by Content", new Html("<div>Enter a keyword to search within your notes.</div>"), PopupPosition.BOTTOM)
                .addStep(categoryComboBox, "Filter by Topic", new Html("<div>Narrow the list by selecting a topic.</div>"), PopupPosition.BOTTOM)
                .addStep(searchButton, "Apply Filters", new Html("<div>Click here to apply your search term and topic filter to the list of notes.</div>"), PopupPosition.BOTTOM)
                .addStep(clearButton, "Clear Filters", new Html("<div>Click this button to remove all filters and view all of your notes again.</div>"), PopupPosition.BOTTOM)
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
