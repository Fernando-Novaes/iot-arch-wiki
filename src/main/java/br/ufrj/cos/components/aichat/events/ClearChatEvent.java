package br.ufrj.cos.components.aichat.events;

import br.ufrj.cos.components.aichat.AIChatMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ClearChatEvent extends ApplicationEvent {

    public ClearChatEvent(Object source) {
        super(source);
    }

}
