package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.ClipboardUtils;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@UIScope
@Component
@CssImport("./styles/chat-view-styles.css") // Keep your CSS import
public class HtmlMessageList extends VerticalLayout { // It's still a VerticalLayout to hold messages

    private static final Logger log = LoggerFactory.getLogger(HtmlMessageList.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AvatarComponent avatarComponent;
    private Icon copyIcon;

    public HtmlMessageList(AvatarComponent avatarComponent) {
        this.avatarComponent = avatarComponent;
        log.info("HtmlMessageList initialized.");
    }

    @PostConstruct
    private void initializeUI() {
        log.info("Initializing UI for HtmlMessageList");
        // Configure this VerticalLayout (which holds the messages)
        setClassName("html-message-list"); // Keep your class name
        setWidthFull(); // Take full width within the scroller
        setPadding(true); // Add padding for message spacing from scroller edges
        setSpacing(true); // Add spacing between messages
        // Remove setSizeFull() - height should be determined by content
        getStyle().set("height", "auto");
    }

    private void configCopyBtn(String message) {
        copyIcon =  new Icon(VaadinIcon.COPY);
        copyIcon.setSize("12px");
        copyIcon.setTooltipText("Copy text message.");


        copyIcon.addClickListener(click -> {
            log.info("Copy button clicked...");
            ClipboardUtils.copyToClipboard(message);
            NotificationUtils.showSuccessNotification("Message copied.");
        });
        copyIcon.getStyle().setCursor("pointer");
    }

    public void setMessages(List<AIChatMessage> messages) {
        log.debug("Setting {} messages in HtmlMessageList", messages.size());
        // Clear previous messages from this layout
        removeAll();

        if (messages.isEmpty()) {
            Image load = new Image();
            load.setWidth("20%");
            load.setHeight("20%");
            load.getStyle().setColor("white");
            load.setSrc("/images/dots.gif");
            add(new Span(load)); // Optional placeholder
        } else {

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
                    avatarImg.setImage("/icons/robo.png"); // Ensure this path is correct relative to webapp/frontend
                    avatar.add(avatarImg);
                }

                // Add the message header with username and time
                Div messageHeader = new Div();
                messageHeader.addClassName("message-header");

                Div userName = new Div();
                userName.setText(message.getUserName());
                userName.addClassName("message-username");

                Div timestamp = new Div();
                LocalDateTime time = LocalDateTime.ofInstant(message.getTime(), ZoneId.systemDefault()); // Use system default for display
                timestamp.setText(formatChatTime(time.toInstant(ZoneOffset.UTC))); // Pass Instant for formatting logic
                timestamp.addClassName("message-timestamp");

                Div space = new Div();
                space.getStyle().setWidth("5px");

                configCopyBtn(
                        ClipboardUtils.basicHtmlToCleanString(message.getText()));
                messageHeader.add(avatar, space, userName, space, timestamp, space, copyIcon);

                // Add the content with HTML rendering
                Div contentDiv = new Div();
                contentDiv.setId("content-div");
                contentDiv.addClassName("message-content");

                // Use setInnerHtml to render HTML content
                contentDiv.getElement().setProperty("innerHTML", message.getText());

                // Add all components to the message bubble
                messageBubble.add(messageHeader, contentDiv);
                messageContainer.add(messageBubble);

                // Add the message container to this VerticalLayout (HtmlMessageList)
                add(messageContainer);
            }
        }
        log.info("Finished adding message elements to HtmlMessageList.");
    }

    // formatChatTime method remains the same...
    private String formatChatTime(Instant instant) {
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // Simplified for example

        if (time.toLocalDate().equals(now.toLocalDate())) {
            return timeFormatter.format(time);
        } else if (time.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday " + timeFormatter.format(time);
        } else {
            return DateTimeFormatter.ofPattern("dd/MM/yy HH:mm").format(time); // Or another format for older dates
        }
    }


}