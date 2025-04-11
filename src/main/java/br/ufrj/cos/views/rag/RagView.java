package br.ufrj.cos.views.rag;

import br.ufrj.cos.components.aichat.AIChatMessageService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Route(value = "rag-view", layout = MainLayout.class)
@PermitAll
public class RagView extends BaseView {

    private static final Logger logger = LoggerFactory.getLogger(AIChatMessageService.class);

    private RAGService ragService;
    private com.vaadin.flow.component.button.Button generateDataBtn;

    public RagView(RAGService ragService) {
        this.ragService = ragService;
        this.generateDataBtn = new com.vaadin.flow.component.button.Button("Generate Data");

        this.generateDataBtn.addClickListener(click -> {
            try {
                logger.info("### Starting data generator....");
                this.ragService.generateDocumentData();
                logger.info("### Data generator done.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        getContent().add(generateDataBtn);
    }

}