package br.ufrj.cos.views.aichat;

import br.ufrj.cos.components.aichat.AIChatComponent;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.dependency.CssImport;

@PageTitle("AI Chat")
@Route(value = "aichat", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/chat-view-styles.css")
@UIScope
public class AiChatView extends BaseView {

    private final AIChatComponent chatComponent;

    public AiChatView(AIChatComponent chatComponent) {
        this.chatComponent = chatComponent;
        initializeView();
    }

    private void initializeView() {
        // Clear any existing content
        getContent().removeAll();

        // Create main container
        Div chatContainer = createChatContainer();

        // Add the chat component to the container
        chatContainer.add(chatComponent);

        // Add container to the view's content
        chatContainer.setSizeFull();

        getContent().getStyle().set("flex-grow", "1");
        getContent().add(chatContainer);
    }

    private Div createChatContainer() {
        Div container = new Div();
        container.addClassName("chat-container");

        // Set container styles
        container.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("padding", "var(--lumo-space-m)")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column");

        return container;
    }
}