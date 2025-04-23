package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@UIScope
@PermitAll
public class AIChatMessageDisplay extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AIChatMessageDisplay.class);

    private final HtmlMessageList messageList;
    private final AIChatMessageService messageService;

    public AIChatMessageDisplay(HtmlMessageList messageList, AIChatMessageService messageService) {
        this.messageList = messageList;
        this.messageService = messageService;
        log.info("AIChatMessageDisplay initialized.");
    }

    @PostConstruct
    private void initializeUI() {
        log.info("Initializing UI for AIChatMessageDisplay");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("ai-chat-message-display");

        messageList.setSizeFull();
        messageList.getStyle().set("height", "auto"); // Let height grow
        messageList.getStyle().set("overflow-y", "auto"); // Make this layout scrollable

        add(messageList);
        expand(messageList);

        this.messageService.clearMessages();
        updateMessageList();
        log.info("UI Initialized. Scroller added and expanded.");
    }

    @EventListener
    public void handleMessageSent(ChatMessageSentEvent event) {
        log.debug("Received ChatMessageSentEvent");
        updateAndScroll();
    }

    @EventListener
    public void handleMessageReceived(ChatMessageReceivedEvent event) {
        log.debug("Received ChatMessageReceivedEvent");
        updateAndScroll();
    }

    @EventListener
    public void clearChat(ClearChatEvent event) {
        log.debug("Received ClearChatEvent");
        this.messageService.clearMessages();
        getUI().ifPresent(ui -> ui.access(this::updateMessageList));
    }

    private void updateAndScroll() {
        getUI().ifPresent(ui -> ui.access(() -> {
            log.info("Updating message list...");
            updateMessageList(); // Update the content first
            // Schedule the scroll to happen after the browser has processed the update
            ui.beforeClientResponse(this, context -> {
                log.info("Executing scroll to bottom via beforeClientResponse");
                scrollToBottom();
            });
        }));
    }

    private void updateMessageList() {
        messageList.setMessages(messageService.getMessages().stream().toList());
        log.debug("Message list updated with {} messages.", messageService.getMessages().size());
    }

    /**
     * Scrolls the message scroller to the bottom using JavaScript.
     */
    public void scrollToBottom() {
        getUI().ifPresent(ui -> { // Target messageList directly
            Element messageListElement = messageList.getElement();
            if (messageListElement.getNode().isAttached()) {
                String script = "var list = $0; setTimeout(function() { list.scrollTop = list.scrollHeight; }, 0);";
                ui.getPage().executeJs(script, messageListElement)
                        .then(r -> log.info("Scroll to bottom JS executed on messageList element: {}", messageListElement.getTag()),
                                e -> log.error("Scroll to bottom JS failed for element {}: {}", messageListElement, e));
                ui.push();
            } else {
                log.warn("MessageList element is not attached, cannot execute JS scroll.");
            }

            ui.push();
        });
    }

    // Optional: Scroll to bottom when the component is first attached and potentially populated
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        log.debug("AIChatMessageDisplay attached. Scheduling initial scroll.");
        // Schedule scroll after the initial render
        attachEvent.getUI().beforeClientResponse(this, context -> scrollToBottom());
    }
}