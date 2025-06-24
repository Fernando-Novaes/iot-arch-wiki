package br.ufrj.cos.views.aichat;

import br.ufrj.cos.components.aichat.AIChatComponent;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.dependency.CssImport;

@PageTitle("AI-Assistant")
@Route(value = "aichat", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/chat-view-styles.css")
@UIScope
public class AiChatView extends VerticalLayout {

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
}