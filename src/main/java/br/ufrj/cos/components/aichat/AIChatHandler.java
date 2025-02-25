package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AIChatHandler {

    private final ApplicationEventPublisher eventPublisher;

    public AIChatHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Async  // Run this method asynchronously
    @EventListener
    public void handleChatMessageSent(ChatMessageSentEvent event) {
        AIChatMessage userMessage = event.getMessage();

        eventPublisher.publishEvent(
                new ChatMessageReceivedEvent(this, userMessage));
    }
}