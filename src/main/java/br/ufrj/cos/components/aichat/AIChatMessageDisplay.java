package br.ufrj.cos.components.aichat;

import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller; // CHANGE 1: Import Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@UIScope
@PermitAll
public class AIChatMessageDisplay extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AIChatMessageDisplay.class);

    private final HtmlMessageList messageList;
    private final AIChatMessageService messageService;
    private Scroller scroller; // CHANGE 2: Add a field for the Scroller

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

        // CHANGE 3: Use a Scroller to wrap the messageList
        scroller = new Scroller();
        scroller.setSizeFull();
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setContent(messageList); // Put the message list inside the scroller

        messageList.getStyle().set("height", "auto"); // Allow message list to grow vertically

        add(scroller); // Add the scroller to the layout
        expand(scroller); // Expand the scroller, not the message list
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
        Collection<AIChatMessage> messages = messageService.getMessages();
        messageList.setMessages(messages.stream().toList());

        // Showing load image before chat response
        if (!messages.isEmpty() && messages.stream().toList().getLast().getAiMessageType().equals(AIMessageType.USER)) {
            Image load = new Image("/images/dots.gif", "Thinking...");
            load.setWidth("64px"); // Use fixed size for better layout
            load.setHeight("32px");
            load.getStyle().set("margin-left", "var(--lumo-space-l)"); // Align with other messages
            messageList.add(new Span(load));
        }

        // CHANGE 4: Remove the scroll call from here. It belongs in updateAndScroll.
        // this.scrollToBottom();

        log.info("Message list updated with {} messages.", messageService.getMessages().size());
    }

    /**
     * Scrolls the message scroller to the bottom using JavaScript.
     */
    public void scrollToBottom() {
        getUI().ifPresent(ui -> {
            // CHANGE 5: Target the Scroller's element, which is the true scrollable container.
            Element scrollerElement = scroller.getElement();

            if (scrollerElement.getNode().isAttached()) {
                String script = "var scroller = $0; setTimeout(function() { scroller.scrollTop = scroller.scrollHeight; }, 100);";
                ui.getPage().executeJs(script, scrollerElement);
                log.info("Scroll command sent to scroller element.");
            } else {
                log.warn("Scroller element is not attached, cannot execute JS scroll.");
            }
        });
    }
}