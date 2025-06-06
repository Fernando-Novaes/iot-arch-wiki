package br.ufrj.cos.components.aichat;

import br.ufrj.cos.api.AsyncRagQueryService;
import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
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
    private Button actionsBtn;
    private final ApplicationEventPublisher eventPublisher;
    private final AsyncRagQueryService asyncRagQueryService;
    private final AvatarComponent avatar;
    private final MessageList messageList;

    public final String CHAT_ACTIONS_ICON = "/icons/square_dots.png";
    public final String HELLO_MESSAGE = """
            <div>
                    <p>Hello %s! I am your <b>IoT Architectural Design Assistant</b>.</p>
            <p>I can help you explore architectural solutions, technologies, and quality requirements for your Internet of Things projects based on a curated knowledge base of scientific literature and industry best practices.</p>
            <p>To get started, you can ask me to design an IoT system by describing your needs. The more details you provide, the better I can assist you. Here's an example of how you can phrase your request:</p>
                    <div>
                        "Design an IoT system for <b>smart agriculture</b> that needs to monitor <b>soil moisture, temperature, and sunlight</b> across <b>large farms (e.g., 1000+ acres)</b>. Key challenges include <b>scalability and energy efficiency</b> for battery-powered sensors. The budget per sensor node is around <b>$50</b>, and there's <b>no existing network infrastructure</b>."
                    </div>
                    <p>Alternativsely, you can ask me about specific IoT domains, architectural patterns, quality requirements, or technologies you're interested in. For example:</p>
                    <ul>
                        <li>"Tell me about common architectures for Industrial IoT."</li>
                        <li>"What are the key security considerations for healthcare IoT systems?"</li>
                        <li>"Compare LoRaWAN and NB-IoT for wide-area connectivity."</li>
                    </ul>
            <p>How can I help you design your IoT system today?</p>
        </div>""";

    public AIChatInputComponent(
            ApplicationEventPublisher eventPublisher,
            AsyncRagQueryService asyncRagQueryService, AvatarComponent avatar,
            MessageList messageList) {
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
        configActionsButton();

        setSizeFull();
        add(messageInput, actionsBtn);
        this.messageList = messageList;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Set focus to the text field when the view is attached.
        messageInput.focus();
    }

    @PostConstruct
    private void init() {
        sendHelloMessage();
    }

    private void sendHelloMessage() {
        var ui = UI.getCurrent();
        ui.access(() -> {
            AIChatMessage aiMessage = AIChatMessage.Builder()
                            .text(String.format(HELLO_MESSAGE,
                                    SecurityUtils.getUsername()))
                            .aiMessageType(AIMessageType.ASSISTANT)
                            .userDetails(createAIUser())
                            .time(java.time.Instant.now())
                            .build();


            eventPublisher.publishEvent(new ChatMessageSentEvent(this, aiMessage));
            ui.push();
        });
    }

//    private void sendHelloMessage() {
//        logger.info("### Hello message sent....");
//        AIChatMessage userMessage = AIChatMessage.Builder()
//                .text(HELLO_MESSAGE)
//                .userDetails(SecurityUtils.getAuthenticatedUser())
//                .aiMessageType(AIMessageType.USER)
//                .time(java.time.Instant.now())
//                .build();
//
//        //eventPublisher.publishEvent(new ChatMessageSentEvent(this, userMessage));
//
//        this.blockSendButton(true);
//
//        var ui = UI.getCurrent();
//        asyncRagQueryService.queryRag(HELLO_MESSAGE)
//                .subscribe(
//                        answer -> {
//                            ui.access(() ->
//                                    handleResponse(answer));
//                        },
//                        error -> {
//                            ui.access(() ->
//                                    handleError(error));
//
//                        }
//                );
//        logger.info("### Hello message sent.... end.");
//    }

    private void configureSendButton() {
        Shortcuts.addShortcutListener(messageInput, sendButton::click, Key.ENTER, KeyModifier.CONTROL);

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

    private void configActionsButton() {
        Image icon = new Image();
        icon.setSrc(CHAT_ACTIONS_ICON);
        icon.setHeight("20px");
        icon.setWidth("14px");

        this.actionsBtn = new Button(icon);
        this.actionsBtn.setHeight("62px");

        Popover popover = new Popover();
        popover.setTarget(actionsBtn);
        popover.setWidth("90px");
        popover.addThemeVariants(PopoverVariant.ARROW,
                PopoverVariant.LUMO_NO_PADDING);
        popover.setPosition(PopoverPosition.TOP);
        popover.setOpenOnClick(true);
        popover.add(new Button("Clear chat", event -> {
            eventPublisher.publishEvent(new ClearChatEvent(this));

            var ui = UI.getCurrent();
            ui.access(() -> {
                AIChatMessage aiMessage = AIChatMessage.Builder()
                        .text(String.format("Nice, %s! Much better now.",
                                SecurityUtils.getUsername()))
                        .aiMessageType(AIMessageType.ASSISTANT)
                        .userDetails(createAIUser())
                        .time(java.time.Instant.now())
                        .build();


                eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, aiMessage));
                ui.push();
            });
        }));
        add(popover);
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
                        //.text("Error: " + error.getMessage())
                        .text("Something went wrong. Please, try again.")
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
