package br.ufrj.cos.components.notification;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.stereotype.Component;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@Component
public class NotificationDialog {

    private static Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(),
                clickEvent -> notification.close());
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    public static void showNotificationDialogOnBotton(String title, String message) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.BOTTOM_STRETCH);

        Icon icon = VaadinIcon.CHECK_CIRCLE.create();
        icon.setColor("var(--lumo-success-color)");

        Div uploadSuccessful = new Div(new Text(title));
        uploadSuccessful.getStyle()
                .set("font-weight", "600")
                .setColor("var(--lumo-success-text-color)");

        Div info = new Div(uploadSuccessful,
                new Div(new Text(message)));

        info.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .setColor("var(--lumo-primary-text-color)");

        var layout = new HorizontalLayout(icon, info,
                createCloseBtn(notification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);

        notification.open();
    }

}
