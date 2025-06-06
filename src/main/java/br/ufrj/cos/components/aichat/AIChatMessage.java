package br.ufrj.cos.components.aichat;

import com.vaadin.flow.component.messages.MessageListItem;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(builderMethodName = "Builder")
@EqualsAndHashCode(callSuper = true)  // Changed to true to properly handle inheritance
public class AIChatMessage extends MessageListItem {

    private AIMessageType aiMessageType;
    private UserDetails userDetails;
    private static final List<String> DEFAULT_CLASS_NAMES = new ArrayList<>();
    private String plainMessage;

    static {
        DEFAULT_CLASS_NAMES.add("message-list-item");
    }

    public AIChatMessage() {
        super();
        setTime(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC));
        // Use addClassName instead of addClassNames to avoid collection modification
        DEFAULT_CLASS_NAMES.forEach(this::addClassNames);
    }

    public static class AIChatMessageBuilder {
        private String text;
        private UserDetails userDetails;  // Renamed from user to match field name
        private String userImage;
        private Instant time;
        private AIMessageType aiMessageType;  // Added missing field

        AIChatMessageBuilder() {
        }

        public AIChatMessageBuilder text(final String text) {
            this.text = text;
            return this;
        }

        public AIChatMessageBuilder userDetails(final UserDetails userDetails) {
            this.userDetails = userDetails;
            return this;
        }

        public AIChatMessageBuilder userImage(final String userImage) {
            this.userImage = userImage;
            return this;
        }

        public AIChatMessageBuilder time(final Instant time) {
            this.time = time;
            return this;
        }

        public AIChatMessageBuilder aiMessageType(final AIMessageType aiMessageType) {
            this.aiMessageType = aiMessageType;
            return this;
        }

        public AIChatMessage build() {
            AIChatMessage message = new AIChatMessage();
            message.setText(this.text);
            message.setUserDetails(this.userDetails);

            if (this.userDetails != null) {
                message.setUserName(this.userDetails.getUsername().toUpperCase());
            }

            message.setUserImage(this.userImage);
            message.setAiMessageType(this.aiMessageType);

            // Set time with null check
            Instant messageTime = this.time != null ? this.time :
                    LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC);
            message.setTime(messageTime);

            if (this.aiMessageType.equals(AIMessageType.USER)) {
                message.addClassNames("user-message");
            } else {
                message.addClassNames("assistant-message");
                message.setUserImage("/icons/robo.png");
            }

            return message;
        }
    }
}