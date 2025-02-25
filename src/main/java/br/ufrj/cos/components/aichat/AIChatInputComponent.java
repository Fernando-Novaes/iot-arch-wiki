package br.ufrj.cos.components.aichat;

import br.ufrj.cos.api.AsyncRagQueryService;
import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class AIChatInputComponent extends HorizontalLayout {
    private static final Logger logger = LoggerFactory.getLogger(AIChatComponent.class);

    private final TextField messageInput;
    private final Button sendButton;
    private final ApplicationEventPublisher eventPublisher;
    private final AsyncRagQueryService asyncRagQueryService;
    private final AvatarComponent avatar;
    private final MessageList messageList;

    public AIChatInputComponent(
            ApplicationEventPublisher eventPublisher,
            AsyncRagQueryService asyncRagQueryService, AvatarComponent avatar, MessageList messageList) {
        this.eventPublisher = eventPublisher;
        this.asyncRagQueryService = asyncRagQueryService;
        this.avatar = avatar;

        messageInput = new TextField();
        messageInput.setPlaceholder("...");
        messageInput.setPrefixComponent(this.avatar.getAvatar());
        messageInput.setWidthFull();
        messageInput.setClearButtonVisible(true);


        sendButton = new Button("Send");
        messageInput.setSuffixComponent(sendButton);
        configureSendButton();

        setSizeFull();
        add(messageInput);
        this.messageList = messageList;
    }

    @PostConstruct
    private void init() {
        sendHelloMessage();
    }

    private void sendHelloMessage() {
        var ui = UI.getCurrent();
        ui.access(() -> {
            AIChatMessage aiMessage = AIChatMessage.Builder()
                            .text(String.format("Hello %s! Well-come to IoT Solutions Design Assistant. How can I help you?",
                                    SecurityUtils.getUsername()))
                            .aiMessageType(AIMessageType.ASSISTANT)
                            .userDetails(createAIUser())
                            .time(java.time.Instant.now())
                            .build();


            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, aiMessage));
            ui.push();
        });
    }

    private void configureSendButton() {
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setHeight("54px");
        sendButton.setAutofocus(true);

        sendButton.addClickListener(event -> {
            String text = messageInput.getValue();
            if (!text.isEmpty()) {
                sendMessage(text);
                messageInput.clear();
            }
        });
    }

    private void sendMessage(String text) {
        logger.info("### Message sent....");
        AIChatMessage userMessage = AIChatMessage.Builder()
                .text(text)
                .userDetails(SecurityUtils.getAuthenticatedUser())
                .aiMessageType(AIMessageType.USER)
                .time(java.time.Instant.now())
                .build();

        eventPublisher.publishEvent(new ChatMessageSentEvent(this, userMessage));

        this.blockSendButton(true);

        var ui = UI.getCurrent();
        asyncRagQueryService.queryRag(text)
                .subscribe(
                        answer -> {
                            ui.access(() ->
                                handleResponse(answer));
                            },
                        error -> {
                            ui.access(() ->
                                    handleError(error));
                        }
                );
        logger.info("### Message sent.... end.");
    }

    private void handleResponse(String answer) {
        getUI().ifPresent(ui -> ui.access(() -> {
            logger.info("### Answer received....");
            AIChatMessage aiMessage = AIChatMessage.Builder()
                    .text(answer)
                    .aiMessageType(AIMessageType.ASSISTANT)
                    .userDetails(createAIUser())
                    .time(java.time.Instant.now())
                    .build();


            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, aiMessage));

            this.blockSendButton(false);
            logger.info("### " + answer);
            logger.info("### Answer received.... end.");

            ui.push();
        }));
    }

    private void handleError(Throwable error) {
        getUI().ifPresent(ui -> ui.access(() -> {
            logger.info("### Error received....");
                AIChatMessage errorMessage = AIChatMessage.Builder()
                        .text("Error: " + error.getMessage())
                        .aiMessageType(AIMessageType.ASSISTANT)
                        .userDetails(createAIUser())
                        .time(java.time.Instant.now())
                        .build();


            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, errorMessage));

            this.blockSendButton(false);
            logger.info("### " + error.getMessage());
            logger.info("### Error received.... end.");

            ui.push();
        }));
    }

    private UserDetails createAIUser() {
        return User.builder()
                .password("")
                .username("ASSISTANT")
                .build();
    }

    private void blockSendButton(boolean block) {
        sendButton.setEnabled(!block);
        sendButton.setText((block)? "Thinking..." : "Send");
    }
}
