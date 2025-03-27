package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.avatar.AvatarComponent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@UIScope
@Component
@CssImport("./styles/chat-view-styles.css")
public class HtmlMessageList extends VerticalLayout {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final AvatarComponent avatarComponent;

    public HtmlMessageList(AvatarComponent avatarComponent) {
        this.avatarComponent = avatarComponent;
        setClassName("html-message-list");
        setSizeFull();
        // Add padding for better appearance
        setPadding(true);
        setSpacing(true);
    }

    public void setMessages(List<AIChatMessage> messages) {
        removeAll();

        for (AIChatMessage message : messages) {
            // Create the message container
            Div messageContainer = new Div();
            messageContainer.addClassName("message-container");

            // Add appropriate class based on message type
            if (message.getAiMessageType().equals(AIMessageType.USER)) {
                messageContainer.addClassName("user-message-container");
            } else if (message.getAiMessageType().equals(AIMessageType.ASSISTANT)) {
                messageContainer.addClassName("assistant-message-container");
            }

            // Create the message bubble
            Div messageBubble = new Div();
            messageBubble.addClassName("message-bubble");

            // Create avatar div
            Div avatar = new Div();
            avatar.addClassName("avatar");

            // Add specific styling class based on message type
            if (message.getAiMessageType().equals(AIMessageType.USER)) {
                messageBubble.addClassName("user-message");
                avatar.add(avatarComponent.getAvatar());
            } else {
                messageBubble.addClassName("assistant-message");
                Avatar avatarImg = new Avatar();
                avatarImg.setImage("/icons/robo.png");
                avatar.add(avatarImg);
            }

            // Add the message header with username and time
            Div messageHeader = new Div();
            messageHeader.addClassName("message-header");

            Div userName = new Div();
            userName.setText(message.getUserName());
            userName.addClassName("message-username");

            Div timestamp = new Div();
            LocalDateTime time = LocalDateTime.ofInstant(message.getTime(), ZoneId.of("UTC"));
            timestamp.setText(formatChatTime(time.toInstant(ZoneOffset.UTC)));
            timestamp.addClassName("message-timestamp");

            Div space = new Div();
            space.getStyle().setWidth("5px");

            messageHeader.add(avatar, space, userName, space,  timestamp);

            // Add the content with HTML rendering
            Div contentDiv = new Div();
            contentDiv.addClassName("message-content");

            // Use setInnerHtml to render HTML content
            contentDiv.getElement().setProperty("innerHTML", message.getText());

            // Add all components to the message bubble
            messageBubble.add(messageHeader, contentDiv);
            messageContainer.add(messageBubble);

            // Add the message container to the layout
            add(messageContainer);
        }
    }

    /**
     * Format Instant time in a chat-friendly format:
     * - If today: "HH:mm" (e.g., "14:23")
     * - If yesterday: "Yesterday at HH:mm"
     * - If this year: "MMM d at HH:mm" (e.g., "Jan 15 at 14:23")
     * - If earlier: "yyyy-MM-dd HH:mm" (e.g., "2023-01-15 14:23")
     */
    private String formatChatTime(Instant instant) {
        // Convert Instant to LocalDateTime using system default zone
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        // Format for time only
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

        // If it's today
        if (time.toLocalDate().equals(now.toLocalDate())) {
            return timeFormatter.format(time);
        }

        // If it's yesterday
        if (time.toLocalDate().equals(now.toLocalDate().minus(1, ChronoUnit.DAYS))) {
            return "Yesterday at " + timeFormatter.format(time);
        }

        // If it's this year
        if (time.getYear() == now.getYear()) {
            DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MMM d");
            return monthDayFormatter.format(time) + " at " + timeFormatter.format(time);
        }

        // If it's older
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return fullFormatter.format(time);
    }
}