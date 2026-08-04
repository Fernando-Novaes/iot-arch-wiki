package br.ufrj.cos.views;

import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.about.AboutView;
import br.ufrj.cos.views.aichat.AiChatView;
import br.ufrj.cos.views.board.BoardView;
import br.ufrj.cos.views.builder.ArchitectureBuilderView;
import br.ufrj.cos.views.home.HomeView;
import br.ufrj.cos.views.iotarch.IoTArchView;
import br.ufrj.cos.views.mynotes.MyNotesView;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Main layout featuring a top header and a vertically-centered icon sidebar menu.
 */
@PermitAll
public class MainLayout extends AppLayout {

    private final AvatarComponent avatarComponent = new AvatarComponent();
    private static final String HELP_DOC_PATH = "docs/user-manual.pdf";
    private Button tourBtn;
    private Button helpBtn;
    private Registration clickRegistration;

    public static class MenuItemInfo extends ListItem {
        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            link.setRoute(view);
            link.addClassName("drawer-link");
            link.getElement().setAttribute("title", menuTitle);

            if (icon != null) {
                icon.getElement().getClassList().add("drawer-link-icon");
                link.add(icon);
            }

            add(link);
        }

        public Class<?> getView() {
            return view;
        }
    }

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);
        setDrawerOpened(true);
        addToNavbar(createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.addClassName("header-navbar");
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);

        // App title / Logo
        Div logoDiv = new Div();
        logoDiv.addClassName("header-logo-container");

        Span logoName = new Span("ArchIoTect");
        logoName.addClassName("header-logo-title");

        Span subBadgeDot = new Span();
        subBadgeDot.getStyle()
                .set("width", "6px")
                .set("height", "6px")
                .set("border-radius", "50%")
                .set("background", "var(--lumo-primary-color)")
                .set("display", "inline-block")
                .set("box-shadow", "0 0 8px var(--lumo-primary-color)");

        Span logoSubText = new Span("IoT Architecture Assistant");

        Span logoSub = new Span(subBadgeDot, logoSubText);
        logoSub.addClassName("header-logo-sub");

        logoDiv.add(logoName, logoSub);

        // User action buttons on the right
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.addClassName("header-actions");
        actionsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsLayout.setSpacing(true);

        HorizontalLayout toolsGroup = new HorizontalLayout();
        toolsGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        toolsGroup.setSpacing(false);
        toolsGroup.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "20px")
                .set("padding", "2px 6px")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("margin-right", "8px");

        helpBtn = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE));
        helpBtn.setTooltipText("User Manual");
        helpBtn.getStyle()
                .set("min-width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("border", "none")
                .set("background", "transparent")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("cursor", "pointer");
        helpBtn.addClickListener(click ->
                getUI().ifPresent(ui -> ui.getPage().open(HELP_DOC_PATH, "_blank"))
        );

        tourBtn = new Button(VaadinIcon.INFO_CIRCLE.create());
        tourBtn.setTooltipText("Start guided tour of this page");
        tourBtn.getStyle()
                .set("min-width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("border", "none")
                .set("background", "transparent")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("cursor", "pointer");

        toolsGroup.add(helpBtn, tourBtn);

        actionsLayout.add(toolsGroup, avatarComponent.createAvatar());

        navbar.add(logoDiv, actionsLayout);
        return navbar;
    }

    private Component createDrawerContent() {
        Nav nav = new Nav();
        nav.addClassName("drawer-nav");

        UnorderedList list = new UnorderedList();
        list.addClassName("drawer-menu-list");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);
        }

        return nav;
    }

    private MenuItemInfo[] createMenuItems() {
        List<MenuItemInfo> menu = new ArrayList<>(List.of(
                new MenuItemInfo("Home", LineAwesomeIcon.HOME_SOLID.create(), HomeView.class),
                new MenuItemInfo("BoK", LineAwesomeIcon.TACHOMETER_ALT_SOLID.create(), BoardView.class),
                new MenuItemInfo("Knowledge Base", LineAwesomeIcon.PENCIL_RULER_SOLID.create(), IoTArchView.class),
                new MenuItemInfo("Arch Builder", LineAwesomeIcon.CUBES_SOLID.create(), ArchitectureBuilderView.class),
                new MenuItemInfo("AI-Assistant", LineAwesomeIcon.TERMINAL_SOLID.create(), AiChatView.class),
                new MenuItemInfo("My Notes", LineAwesomeIcon.NOTES_MEDICAL_SOLID.create(), MyNotesView.class),
                new MenuItemInfo("About", LineAwesomeIcon.ADDRESS_CARD_SOLID.create(), AboutView.class)
        ));

        return menu.toArray(new MenuItemInfo[0]);
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        tourBtn.addClickListener(ComponentEvent::unregisterListener);

        if (clickRegistration != null) {
            clickRegistration.remove();
        }

        getElement().getStyle().set("height", "100%");
        getContent().getElement().getStyle().set("flex-grow", "1");

        Component currentView = getContent();
        if (currentView instanceof HasTour viewWithTour) {
            Onboarding tour = (Onboarding) viewWithTour.createTour();

            if (tour != null && !tour.getSteps().isEmpty()) {
                tourBtn.setVisible(true);
                clickRegistration = tourBtn.addClickListener(e -> tour.start());
            }

            if (tour == null && viewWithTour instanceof HomeView) {
                createPageTour();
            }

            if (viewWithTour.startDemoTour()) {
                tourBtn.setVisible(true);
                startDemoTour();
            }
        } else {
            tourBtn.setVisible(false);
        }
    }

    private void startDemoTour() {
        tourBtn.addClickListener(ComponentEvent::unregisterListener);

        this.addAttachListener(l -> new TourUtils().build()
                .addStep(tourBtn,
                        "Page Features and Tips Tour",
                        new Html("<div>When this button is visible a guided tour is available for the page.</div>"),
                        com.vaadin.componentfactory.PopupPosition.BOTTOM).startTour());
    }

    private void createPageTour() {
        tourBtn.addClickListener(ComponentEvent::unregisterListener);
        if (clickRegistration != null) { clickRegistration.remove(); }

        clickRegistration = tourBtn.addClickListener(e -> new TourUtils().build()
                .addStep(tourBtn,
                        "Page Features and Manual",
                        new Html("<div>From here, you can open the User Manual or take a guided tour of the page.</div>"),
                        com.vaadin.componentfactory.PopupPosition.BOTTOM).startTour());
    }
}
