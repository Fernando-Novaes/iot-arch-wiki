package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@UIScope
public class AIChatMessageDisplay extends VerticalLayout {
    private MessageList messageList;
    private final AIChatMessageService messageService;

    public AIChatMessageDisplay(AIChatMessageService messageService) {
        removeAll();
        this.messageService = messageService;
        this.messageService.clearMessages();
        add(this.configMessageList());
        setSizeFull();
        this.updateMessageList();
    }

    private MessageList configMessageList() {
        this.messageList = new MessageList();
        this.messageList.setSizeFull();

        return this.messageList;
    }

    @PostConstruct
    private void init() {
        UI.getCurrent().getUI().ifPresent(ui ->
                ui.access(() ->
                        // Set up the renderer using raw JavaScript
                        messageList.getElement().executeJs(
                                "this.querySelector('vaadin-message-list').renderer = function(root, list, model) {" +
                                        "  const textSpan = document.createElement('span');" +
                                        "  textSpan.innerHTML = model.item.text;" +
                                        "  root.appendChild(textSpan);" +
                                        "}"
                        )));
    }

    @EventListener
    public void handleMessageSent(ChatMessageSentEvent event) {
        getUI().ifPresent(ui -> ui.access(this::updateMessageList));
    }

    @EventListener
    public void handleMessageReceived(ChatMessageReceivedEvent event) {
        getUI().ifPresent(ui -> ui.access(this::updateMessageList));
    }

    @EventListener
    public void clearChat(ClearChatEvent event) {
        this.messageService.clearMessages();
        this.updateMessageList();
    }

    private void updateMessageList() {
        messageList.setItems(messageService.getMessages());
    }
}