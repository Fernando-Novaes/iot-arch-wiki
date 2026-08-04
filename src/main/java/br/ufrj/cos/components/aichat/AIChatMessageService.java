package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import com.vaadin.flow.component.messages.MessageListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AIChatMessageService {
    private static final Logger logger = LoggerFactory.getLogger(AIChatMessageService.class);

    private final List<AIChatMessage> messages = new CopyOnWriteArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    public AIChatMessageService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Collection<AIChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    @EventListener
    public void handleMessageSent(ChatMessageSentEvent event) {
        if (event.getMessage() != null && !messages.contains(event.getMessage())) {
            messages.add(event.getMessage());
        }
    }

    @EventListener
    public void handleMessageReceived(ChatMessageReceivedEvent event) {
        AIChatMessage message = event.getMessage();
        if (message != null) {
            message.setText(this.sanitizeHtml(message.getText()));
            // Only add if not already in messages list (to prevent duplicates when replacing)
            if (!messages.contains(message)) {
                messages.add(message);
            }
        }
    }

    public void replaceMessage(AIChatMessage oldMessage, AIChatMessage newMessage) {
        if (newMessage == null) return;
        newMessage.setText(this.sanitizeHtml(newMessage.getText()));
        int index = messages.indexOf(oldMessage);
        if (index != -1) {
            messages.set(index, newMessage);
        } else {
            messages.add(newMessage);
        }
    }

    public void clearMessages() {
        messages.clear();
    }

    private String sanitizeHtml(String html) {
        // Basic example - you might want to use a proper HTML sanitizer library
        return html.replace("<script>", "&lt;script&gt;")
                .replace("</script>", "&lt;/script&gt;")
                .replace("```html", "")
                .replace("```", "" )
                .trim();
    }
}