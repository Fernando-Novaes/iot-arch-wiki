package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageListItem;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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