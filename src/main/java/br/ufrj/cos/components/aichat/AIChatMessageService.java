package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import com.vaadin.flow.component.messages.MessageListItem;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AIChatMessageService {
    private final List<AIChatMessage> messages = new CopyOnWriteArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    public AIChatMessageService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Collection<MessageListItem> getMessages() {
        return new ArrayList<>(messages);
    }

    @EventListener
    public void handleMessageSent(ChatMessageSentEvent event) {
        //messages.add(event.getMessage());
    }

    @EventListener
    public void handleMessageReceived(ChatMessageReceivedEvent event) {
        AIChatMessage message = event.getMessage();
        message.setText(
                this.sanitizeHtml(message.getText()));
        messages.add(event.getMessage());
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