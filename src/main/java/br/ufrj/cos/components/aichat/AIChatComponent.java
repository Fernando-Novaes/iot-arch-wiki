package br.ufrj.cos.components.aichat;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class AIChatComponent extends VerticalLayout {

    // Use final fields and constructor injection (good practice)
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

        // Configure layout after dependencies are injected
        configureLayout();
    }

    // Or use @PostConstruct if you prefer field injection (though constructor is generally better)
    // @PostConstruct
    private void configureLayout() {
        // 1. Configure the main layout (this VerticalLayout)
        setSizeFull(); // Make this layout take full available space
        setPadding(false);
        setSpacing(false); // No space between message area and input area

        // 2. Configure the message display area (already contains the Scroller)
        // Ensure messageDisplay itself is set to take up available space
        messageDisplay.setSizeFull(); // Important for the scroller inside it to work correctly

        // 3. Configure the input component area
        // No need for an extra VerticalLayout unless inputComponent itself needs complex arrangement
        inputComponent.setWidthFull(); // Make input component take full width

        // 4. Add components to the main layout
        add(messageDisplay); // Add the message display (which has the scroller)
        add(inputComponent); // Add the input component below it

        // 5. Set flex grow
        // Make the messageDisplay (and its internal scroller) expand to fill space
        expand(messageDisplay);
        // Input component should take its natural height, so no setFlexGrow needed or setFlexGrow(0)

        // Optional: Add some styling class if needed
        addClassName("ai-chat-component");
    }

    // Keep the clearMessages method if needed externally
    public void clearMessages() {
        messageService.clearMessages();
        // Optionally trigger a UI update if messageDisplay doesn't automatically clear
        // messageDisplay.clearDisplay(); // You might need a method like this in AIChatMessageDisplay
    }
}