package br.ufrj.cos.components.aichat;

import br.ufrj.cos.api.AsyncRagQueryService;
import br.ufrj.cos.service.ai.AiRagService;
import br.ufrj.cos.components.aichat.events.ChatMessageReceivedEvent;
import br.ufrj.cos.components.aichat.events.ChatMessageSentEvent;
import br.ufrj.cos.components.aichat.events.ClearChatEvent;
import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@UIScope
public class AIChatInputComponent extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(AIChatComponent.class);

    @Getter private final TextArea messageInput;
    @Getter private final Button sendButton;
    private final HtmlMessageList htmlMessageList;
    @Getter private Button actionsBtn;
    private final ApplicationEventPublisher eventPublisher;
    private final AsyncRagQueryService asyncRagQueryService;
    @Getter private final AvatarComponent avatar;
    private final MessageList messageList;

    // This will be used to give ID to history messages and keep context
    private final UUID uuid_conversation_id;

    public final String CHAT_ACTIONS_ICON = "/icons/square_dots.png";
    public final String HELLO_MESSAGE = """
            <div>
                    <p>Hello %s! I am your <b>IoT Architectural Design Assistant</b>.</p>
            <p>I can help you explore architectural solutions, technologies, and quality requirements for your Internet of Things projects based on a curated knowledge base of scientific literature and industry best practices.</p>
            <p>To get started, you can ask me to design an IoT system by describing your needs. Here is an example request:</p>
                    <div>
                        "Design an IoT system for <b>smart agriculture</b> that needs to monitor <b>soil moisture, temperature, and sunlight</b> across <b>large farms (1000+ acres)</b>. Key challenges include <b>scalability and energy efficiency</b>."
                    </div>
            <p>Or click one of the quick suggestions below to explore!</p>
        </div>""";

    private final AiRagService aiRagService;
    private final AIChatMessageService messageService;

    public AIChatInputComponent(
            ApplicationEventPublisher eventPublisher,
            AsyncRagQueryService asyncRagQueryService,
            AiRagService aiRagService,
            AIChatMessageService messageService,
            AvatarComponent avatar,
            MessageList messageList, HtmlMessageList htmlMessageList) {
        this.eventPublisher = eventPublisher;
        this.asyncRagQueryService = asyncRagQueryService;
        this.aiRagService = aiRagService;
        this.messageService = messageService;
        this.avatar = avatar;
        uuid_conversation_id = UUID.randomUUID();

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("chat-input-bar");

        messageInput = new TextArea();
        messageInput.setPlaceholder("Ask about IoT architectures, protocols, security, or design patterns... (Ctrl+Enter to send)");
        messageInput.setPrefixComponent(this.avatar.getAvatar());
        messageInput.setWidthFull();
        messageInput.setClearButtonVisible(true);

        sendButton = new Button("Send");
        sendButton.getStyle().setColor("white");
        messageInput.setSuffixComponent(sendButton);

        configureSendButton();
        configActionsButton();

        // --- Quick Prompts Suggestion Chips ---
        HorizontalLayout quickPrompts = new HorizontalLayout();
        quickPrompts.addClassName("quick-prompts-container");

        String[] prompts = new String[]{
                "🌐 Industrial IoT Architectures",
                "🔒 Healthcare Security Best Practices",
                "📡 Compare LoRaWAN vs NB-IoT",
                "🚜 Smart Farming"
        };

        for (String promptText : prompts) {
            Button chip = new Button(promptText);
            chip.addClassName("quick-prompt-chip");
            chip.addClickListener(e -> {
                String cleanQuery = promptText.replaceAll("^[\\p{So}\\p{Cn}]+\\s*", "");
                sendMessage("Tell me about " + cleanQuery);
            });
            quickPrompts.add(chip);
        }

        HorizontalLayout inputRow = new HorizontalLayout();
        inputRow.setWidthFull();
        inputRow.setAlignItems(Alignment.CENTER);
        inputRow.add(messageInput, actionsBtn);

        add(quickPrompts, inputRow);
        this.messageList = messageList;
        this.htmlMessageList = htmlMessageList;
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
                            .aiMessageType(AIMessageType.HELLO_MESSAGE)
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

    private reactor.core.Disposable activeSubscription;
    private AIChatMessage currentThinkingMessage;
    private boolean isProcessing = false;
    private ScheduledExecutorService typewriterExecutor;
    private ScheduledFuture<?> typewriterTask;

    private void configureSendButton() {
        Shortcuts.addShortcutListener(messageInput, sendButton::click, Key.ENTER, KeyModifier.CONTROL);

        messageInput.addKeyDownListener(Key.ENTER, event -> {
            if (event.getModifiers().contains(KeyModifier.CONTROL) || event.getModifiers().contains(KeyModifier.META)) {
                if (isProcessing) {
                    cancelCurrentRequest();
                } else {
                    String text = messageInput.getValue();
                    if (text != null && !text.trim().isEmpty()) {
                        sendMessage(text.trim());
                        messageInput.clear();
                    }
                }
            }
        });

        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setHeight("54px");
        sendButton.setAutofocus(true);

        sendButton.addClickListener(event -> {
            if (isProcessing) {
                cancelCurrentRequest();
            } else {
                String text = messageInput.getValue();
                if (text != null && !text.trim().isEmpty()) {
                    sendMessage(text.trim());
                    messageInput.clear();
                }
            }
        });
    }

    private void cancelTypewriterTask() {
        if (typewriterTask != null && !typewriterTask.isCancelled()) {
            typewriterTask.cancel(true);
        }
    }

    private void cancelCurrentRequest() {
        logger.info("### User requested to cancel current AI request.");
        cancelTypewriterTask();
        if (activeSubscription != null && !activeSubscription.isDisposed()) {
            activeSubscription.dispose();
        }

        if (currentThinkingMessage != null) {
            currentThinkingMessage.setText("<p style='color: var(--lumo-error-text-color); margin: 0;'>🛑 <em>Requisição cancelada pelo usuário.</em></p>");
            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, currentThinkingMessage));
        }

        resetSendButtonState();
    }

    private void resetSendButtonState() {
        this.isProcessing = false;
        this.sendButton.setText("Send");
        this.sendButton.setEnabled(true);
        this.sendButton.removeThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        this.sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.messageInput.setReadOnly(false);
        var ui = UI.getCurrent();
        if (ui != null) {
            ui.push();
        }
    }

    private void setProcessingState() {
        this.isProcessing = true;
        this.sendButton.setText("Stop");
        this.sendButton.setEnabled(true);
        this.sendButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.sendButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        this.messageInput.setReadOnly(true);
    }

    private void configActionsButton() {
        Image icon = new Image();
        icon.setSrc(CHAT_ACTIONS_ICON);
        icon.setHeight("10px");
        icon.setWidth("8px");

        this.actionsBtn = new Button(icon);
        this.actionsBtn.setHeight(messageInput.getHeight());

        Popover popover = new Popover();
        popover.setTarget(actionsBtn);
        popover.setWidth("90px");
        popover.addThemeVariants(PopoverVariant.ARROW,
                PopoverVariant.LUMO_NO_PADDING);
        popover.setPosition(PopoverPosition.TOP);
        popover.setOpenOnClick(true);
        popover.add(new Button("Clear chat", event -> {
            eventPublisher.publishEvent(new ClearChatEvent(this));
            aiRagService.clearConversationHistory(uuid_conversation_id);

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
        logger.info("### Message sent (Typewriter streaming mode)...");

        // 1. Send User message (appears immediately)
        AIChatMessage userMessage = AIChatMessage.Builder()
                .text(text)
                .userDetails(SecurityUtils.getAuthenticatedUser())
                .aiMessageType(AIMessageType.USER)
                .time(java.time.Instant.now())
                .build();

        eventPublisher.publishEvent(new ChatMessageSentEvent(this, userMessage));

        // 2. Send Assistant placeholder message with blinking prompt cursor
        AIChatMessage streamingMessage = AIChatMessage.Builder()
                .text("<span class='cursor-blink'>▌</span>")
                .aiMessageType(AIMessageType.ASSISTANT)
                .userDetails(createAIUser())
                .time(java.time.Instant.now())
                .build();

        this.currentThinkingMessage = streamingMessage;
        eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, streamingMessage));

        setProcessingState();

        var ui = UI.getCurrent();
        if (ui != null) {
            ui.push();
        }

        StringBuilder targetBuffer = new StringBuilder();
        int[] displayedPos = new int[]{0};
        boolean[] streamFinished = new boolean[]{false};

        cancelTypewriterTask();
        if (typewriterExecutor == null || typewriterExecutor.isShutdown()) {
            typewriterExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        activeSubscription = asyncRagQueryService.queryRagStream(text, Optional.ofNullable(uuid_conversation_id))
                .subscribe(
                        chunk -> {
                            synchronized (targetBuffer) {
                                targetBuffer.append(chunk);
                            }
                        },
                        error -> {
                            cancelTypewriterTask();
                            if (ui != null) {
                                ui.access(() -> handleError(streamingMessage, error));
                            }
                        },
                        () -> {
                            synchronized (targetBuffer) {
                                streamFinished[0] = true;
                            }
                        }
                );

        // Schedule smooth typewriter pacing loop (ticks every 20ms)
        typewriterTask = typewriterExecutor.scheduleAtFixedRate(() -> {
            if (!isProcessing) {
                cancelTypewriterTask();
                return;
            }

            String fullText;
            boolean finished;
            synchronized (targetBuffer) {
                fullText = targetBuffer.toString();
                finished = streamFinished[0];
            }

            if (fullText.trim().isEmpty() && !finished) {
                return; // Wait for initial chunk
            }

            int currentLen = fullText.length();
            int pos = displayedPos[0];

            if (pos < currentLen) {
                // Adaptive step size based on buffer backlog for fluid typing feel
                int remaining = currentLen - pos;
                int step = 3; // default typing speed (chars per 20ms frame)
                if (remaining > 120) {
                    step = 14;
                } else if (remaining > 50) {
                    step = 8;
                } else if (remaining > 20) {
                    step = 5;
                }

                pos = Math.min(currentLen, pos + step);
                displayedPos[0] = pos;

                String subText = fullText.substring(0, pos);
                String currentHtml = subText + " <span class='cursor-blink'>▌</span>";

                if (ui != null && ui.isAttached()) {
                    ui.access(() -> {
                        if (!isProcessing) return;
                        boolean patched = htmlMessageList.updateLastMessageContent(currentHtml);
                        if (!patched) {
                            streamingMessage.setText(currentHtml);
                            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, streamingMessage));
                        }
                        ui.push();
                    });
                }
            }

            // Check if stream finished and all text has been rendered
            if (finished && displayedPos[0] >= currentLen) {
                String finalAnswer = fullText.trim();
                if (finalAnswer.isEmpty()) {
                    finalAnswer = """
                        <div style="background: var(--lumo-contrast-5pct); border-left: 4px solid #d97706; border-radius: 8px; padding: 12px 14px; margin-top: 6px;">
                            <h4 style="margin: 0 0 6px 0; color: #d97706; font-weight: 700; font-size: 0.88rem;">⚠️ Unable to Evaluate Solution with Current Context Data</h4>
                            <p style="margin: 0 0 8px 0; font-size: 0.78rem; line-height: 1.45; color: var(--lumo-body-text-color);">No direct scientific evidence was found in the RAG knowledge base for the exact combination of selected building blocks. To assist the AI engine in performing a precise diagnostic, architects are requested to perform the following checks:</p>
                            <ul style="margin: 0; padding-left: 18px; font-size: 0.76rem; line-height: 1.5; color: var(--lumo-body-text-color);">
                                <li><strong>Domain & Pattern Alignment:</strong> Verify whether the selected domain (e.g., <em>Generic</em>) is using a domain-specific architecture pattern from another domain (e.g., <em>Healthcare</em>, <em>Smart Farming</em>, or <em>Industry 4.0</em>).</li>
                                <li><strong>Layer Technology Allocation:</strong> Ensure heavy cloud infrastructures (e.g., <em>AWS Core</em>, <em>Big Data Analytics Engine</em>) are not assigned to the Edge or Fog layers.</li>
                                <li><strong>Component Variation:</strong> Try varying solution components or protocols to align with standard practices for the chosen pattern.</li>
                            </ul>
                        </div>
                        """;
                }

                String finalText = finalAnswer;
                if (ui != null && ui.isAttached()) {
                    ui.access(() -> {
                        try {
                            streamingMessage.setText(finalText);
                            boolean patched = htmlMessageList.updateLastMessageContent(finalText);
                            if (!patched) {
                                eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, streamingMessage));
                            }
                        } finally {
                            resetSendButtonState();
                            cancelTypewriterTask();
                            logger.info("### Typewriter streaming complete for conversation ID: {}", uuid_conversation_id);
                        }
                    });
                } else {
                    this.isProcessing = false;
                    cancelTypewriterTask();
                }
            }
        }, 60, 20, TimeUnit.MILLISECONDS);
    }

    private void handleError(AIChatMessage thinkingMessage, Throwable error) {
        getUI().ifPresent(ui -> ui.access(() -> {
            if (!isProcessing) return; // Already cancelled
            logger.info("### Error received....");
            String msg = error != null && error.getMessage() != null ? error.getMessage() : "";
            if (msg.isEmpty()) {
                msg = "<p>Desculpe, ocorreu um erro ao consultar o serviço de IA. Por favor, tente novamente.</p>";
            } else if (!msg.startsWith("<p>")) {
                msg = "<p>" + msg + "</p>";
            }

            thinkingMessage.setText(msg);
            eventPublisher.publishEvent(new ChatMessageReceivedEvent(this, thinkingMessage));

            resetSendButtonState();
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



    /**
     * Scrolls the message scroller to the bottom using JavaScript.
     */
    public void scrollToBottom() {
        getUI().ifPresent(ui -> {
            // Target the MessageList's own element, which is the scrollable container.
            Element messageListElement = messageList.getElement();

            if (messageListElement.getNode().isAttached()) {
                // The JS is correct. setTimeout ensures it runs after the DOM update.
                String script = "var list = $0; setTimeout(function() { list.scrollTop = list.scrollHeight; }, 100);";
                ui.getPage().executeJs(script, messageListElement);
            } else {
                logger.warn("MessageList element is not attached, cannot execute JS scroll.");
            }
        });
    }
}
