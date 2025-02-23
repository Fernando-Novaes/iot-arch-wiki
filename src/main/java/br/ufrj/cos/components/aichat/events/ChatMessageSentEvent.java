package br.ufrj.cos.components.aichat.events;

import br.ufrj.cos.components.aichat.AIChatMessage;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatMessageSentEvent extends ApplicationEvent {

    private final AIChatMessage message;

    public ChatMessageSentEvent(Object source, AIChatMessage message) {
        super(source);
        this.message = message;
    }

}
