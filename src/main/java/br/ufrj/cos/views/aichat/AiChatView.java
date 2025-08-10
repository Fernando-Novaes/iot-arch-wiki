package br.ufrj.cos.views.aichat;

import br.ufrj.cos.components.aichat.AIChatComponent;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.dependency.CssImport;

import java.util.Optional;

@PageTitle("AI-Assistant")
@Route(value = "aichat", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/chat-view-styles.css")
@UIScope
public class AiChatView extends VerticalLayout implements HasTour {

    private final AIChatComponent chatComponent;

    public AiChatView(AIChatComponent chatComponent) {
        this.chatComponent = chatComponent;

        // Configure this view to be the main container
        setSizeFull();
        setPadding(true);
        setSpacing(false);

        add(chatComponent);
        expand(chatComponent);
    }

    @Override
    public Onboarding createTour() {
        return new TourUtils().build()
                .addStep(chatComponent.getInputComponent().getMessageInput(),
                        "Input Message",
                        new Html("""
                                    <div>
                                        <p> In this field, you will write your questions for the assistant. To ensure a rich response, follow these tips: </p>
                                        <ul>
                                            <li>Use the English language primarily.</li>
                                            <li>Provide maximum detail about your challenge. Be Specific in Design Scenarios.</li>
                                            <li>Specify the quality requirements that should be addressed.</li>
                                            <li>Ask for Comparisons</li>
                                            <li>You can ask for the Assistant to provide examples of effective questions</li>
                                        </ul>
                                    </div>
                                """),
                        PopupPosition.BOTTOM)
                .addStep(chatComponent.getInputComponent().getSendButton(),
                        "Send Button",
                        new Html("""
                                   <div>
                                        <p><strong>This button sends your question to the assistant.</strong></p>
                                        <p>When you ask a question, the button's label will change to '<b>Thinking...</b>'. This means your request is being processed and a solution is on its way.</p>
                                        <p>While the assistant is '<b>Thinking...</b>', this button will be disabled to prevent new questions during the execution.</p>
                                    </div>
                                """),
                        PopupPosition.BOTTOM)
                .addStep(chatComponent.getInputComponent().getActionsBtn(),
                        "Clear Messages",
                        new Html("""
                                   <div>
                                        <p>This button clears all messages in the screen.</p>
                                    </div>
                                """),
                        PopupPosition.BOTTOM,
                        Optional.of(e -> chatComponent.getInputComponent().getActionsBtn().click()))
                .addStep(chatComponent.getHtmlMessageList(),
                        "The Message List",
                        new Html("""
                                   <div>
                                        <p>This is the message list where the conversation is displayed.</p>
                                        <strong>Icon Legend:</strong>
                                        <ul>
                                            <li><strong><img src="icons/robo.png" alt="Assistant Icon" style="height: 18px; vertical-align: middle; margin-right: 5px;"> (Assistant Icon):</strong> Indicates a message sent by the Assistant.</li>
                                            <li><strong><img src="images/homem.png" alt="User Icon" style="height: 18px; vertical-align: middle; margin-right: 5px;"> (User Icon):</strong> Indicates a message sent by you.</li>
                                            <li><strong><img src="images/copy.png" alt="User Icon" style="height: 18px; vertical-align: middle; margin-right: 5px;"> (Copy Icon):</strong> Click to copy the message content to your clipboard.</li>
                                        </ul>
                                    </div>
                                """),
                        PopupPosition.BOTTOM).getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}