package br.ufrj.cos.components.aichat;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
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
        //setSizeFull();

        // Message List Box
        VerticalLayout messageListBox = new VerticalLayout();
        messageListBox.add(messageDisplay);
        //messageListBox.addClassName("message-list-box");
        messageListBox.setWidth("100%");
        messageListBox.setHeight("70%");

        // Input Box (Fixed at Bottom)
        VerticalLayout inputBox = new VerticalLayout();
        inputBox.add(inputComponent);
        inputBox.addClassName("input-box-fixed");

        add(messageListBox);

        VerticalLayout block = new VerticalLayout();
        block.setHeight("15%");

        add(block);

        add(inputBox);

        Scroller scroller = new Scroller(messageListBox);
        scroller.scrollIntoView();
        add(scroller);
    }

    public void clearMessages() {
        messageService.clearMessages();
    }

}