package br.ufrj.cos.components.aichat;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class AIChatComponent extends VerticalLayout {
    private final AIChatMessageService messageService;
    private final AIChatMessageDisplay messageDisplay;
    private final AIChatInputComponent inputComponent;

    public AIChatComponent(
            AIChatMessageService messageService,
            AIChatMessageDisplay messageDisplay,
            AIChatInputComponent inputComponent) {
        this.messageService = messageService;
        this.messageDisplay = messageDisplay;
        this.inputComponent = inputComponent;

        configureLayout();
    }

    private void configureLayout() {
        setSizeFull();
        add(messageDisplay);
        add(inputComponent);
    }

    public void clearMessages() {
        messageService.clearMessages();
    }

}