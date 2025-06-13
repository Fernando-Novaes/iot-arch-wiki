package br.ufrj.cos.views;


import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.views.about.AboutView;
import br.ufrj.cos.views.aichat.AiChatView;
import br.ufrj.cos.views.appconfig.AppConfigView;
import br.ufrj.cos.views.board.BoardView;
import br.ufrj.cos.views.datamanager.DataManagerView;
import br.ufrj.cos.views.home.HomeView;
import br.ufrj.cos.views.iotarch.IoTArchView;
import br.ufrj.cos.views.user.UserRegistrationView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@PermitAll
public class MainLayout extends AppLayout {

    public AvatarComponent avatarComponent = new AvatarComponent();
    private final String HELP_DOC_PATH = "docs/user-manual.pdf";

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL,
                    TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }
        public Class<?> getView() {
            return view;
        }
    }

    public MainLayout() {
        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE, Padding.Vertical.SMALL);
        layout.getStyle().setBoxShadow("0 4px 8px rgba(0, 0, 0, 0.2)");

        layout.setAlignItems(FlexComponent.Alignment.START);
        layout.add(new Html("<div style='font-weight: bold; font-size: xx-large'>ArchIoTect</div>"),
                new Span(" "),
                new Html("<div style='width: 100%'><h3>IoT Architecture Solution Knowledge Base</h3><p >IoT Design Decision Assistant</p></div>"),
                this.avatarComponent.createAvatar());

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, Width.FULL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE, Width.FULL); // Ensure the list itself takes full width

        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);
        }

        // 1. Create the help item. It should NOT take up the full width.
        //    It should only be as wide as its content (the icon).
        ListItem helpItem = new ListItem();
        Button helpBtn = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE));
        helpBtn.getStyle().setCursor("pointer");
        helpBtn.setTooltipText("User manual");

        helpBtn.addClickListener(click -> {
            getUI().ifPresent(ui -> ui.getPage().open(HELP_DOC_PATH, "_blank"));
        });

        helpItem.add(helpBtn);

        // 2. Apply the magic style: margin-left: auto
        //    This tells the flex item to consume all available space to its left,
        //    pushing it to the far right of the flex container (the UnorderedList).
        helpItem.addClassNames(Margin.Start.AUTO, AlignItems.CENTER, Display.FLEX, Padding.Horizontal.XLARGE);

        // 3. Add the correctly styled help item to the list.
        list.add(helpItem);

        header.add(layout, nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        List<MenuItemInfo> menu = new ArrayList<>(List.of(
                new MenuItemInfo("Home", LineAwesomeIcon.HOME_SOLID.create(), HomeView.class),
                new MenuItemInfo("BoK", LineAwesomeIcon.WHMCS.create(), BoardView.class),
                new MenuItemInfo("Knowledge Base", LineAwesomeIcon.PENCIL_RULER_SOLID.create(), IoTArchView.class),
                new MenuItemInfo("AI-Assistant", LineAwesomeIcon.TERMINAL_SOLID.create(), AiChatView.class)
                //new MenuItemInfo("IoT Architecture", LineAwesomeIcon.NETWORK_WIRED_SOLID.create(), QualityRequirementView.class),
                //new MenuItemInfo("IoT Domains", LineAwesomeIcon.PROJECT_DIAGRAM_SOLID.create(), QualityRequirementView.class),
                //new MenuItemInfo("Quality Requirement", LineAwesomeIcon.CHECK_SQUARE_SOLID.create(), QualityRequirementView.class)
        ));

        // Dynamically add the "Knowledge Manager" menu item if the user is an ADMIN
//        if (SecurityUtils.hasRole("ADMIN")) {
//            menu.add(new MenuItemInfo("App Config", LineAwesomeIcon.COG_SOLID.create(), AppConfigView.class));
//            menu.add(new MenuItemInfo("Knowledge Manager", LineAwesomeIcon.DATABASE_SOLID.create(), DataManagerView.class));
//            menu.add(new MenuItemInfo("User Manager", LineAwesomeIcon.USER_ALT_SOLID.create(), UserRegistrationView.class));
//        }

        menu.add(new MenuItemInfo("About", LineAwesomeIcon.ADDRESS_CARD_SOLID.create(), AboutView.class));

        // Convert list to an array and return it
        return menu.toArray(new MenuItemInfo[0]);
    }

}
