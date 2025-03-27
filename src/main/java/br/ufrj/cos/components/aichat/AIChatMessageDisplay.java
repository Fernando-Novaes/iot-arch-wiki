package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@UIScope
@PermitAll
public class AIChatMessageDisplay extends VerticalLayout {
    //private MessageList messageList;
    private final HtmlMessageList messageList;
    private final AIChatMessageService messageService;

    public AIChatMessageDisplay(HtmlMessageList messageList, AIChatMessageService messageService) {
        removeAll();
        this.messageList = messageList;
        this.messageService = messageService;
        this.messageService.clearMessages();

        add(this.configMessageList());
        setSizeFull();
        this.updateMessageList();
    }

    private HtmlMessageList configMessageList() {
        //this.messageList = new MessageList();
        this.messageList.setSizeFull();
        return this.messageList;
    }

    @EventListener
    public void handleMessageSent(ChatMessageSentEvent event) {
        getUI().ifPresent(ui -> ui.access(this::updateMessageList));
    }

    @EventListener
    public void handleMessageReceived(ChatMessageReceivedEvent event) {
        getUI().ifPresent(ui -> {
            ui.access(this::updateMessageList);
        });
    }

    @EventListener
    public void clearChat(ClearChatEvent event) {
        this.messageService.clearMessages();
        getUI().ifPresent(ui -> ui.access(this::updateMessageList));
    }

    private void updateMessageList() {
        messageList.setMessages(messageService.getMessages().stream().toList());
    }
}