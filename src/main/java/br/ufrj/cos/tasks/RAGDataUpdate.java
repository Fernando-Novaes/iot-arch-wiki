package br.ufrj.cos.tasks;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.api.TextToRagStoreRequest;
import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.TaskScheduleConfig;
import br.ufrj.cos.repository.TaskScheduleConfigRepository;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.service.TaskScheduleConfigService;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RAGDataUpdate implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RAGDataUpdate.class);
    public static final String SERVICE_NAME = "RAG_KNOWLEDGE_UPDATE";

    private final AppConfigService appConfigService;
    private final APIServiceConnection apiServiceConnection;
    private final RAGService ragService;

    public RAGDataUpdate(AppConfigService appConfigService, APIServiceConnection apiServiceConnection, RAGService ragService, TaskScheduleConfigRepository taskScheduleConfigRepository) {
        this.appConfigService = appConfigService;
        this.apiServiceConnection = apiServiceConnection;
        this.ragService = ragService;
    }

    public void startRAGDataUpdateAutomatic() {
        AppConfig appConfig = this.appConfigService.getAppConfig();

        logger.info("Verifying RAG database for updates...");

        // Check if an update is needed
        if (isUpdateNeeded(appConfig)) {
            logger.info("Update required. Starting RAG database update process...");

            //Clear and update
            String allData = ragService.generateStringData();
            logger.info(allData);

            TextToRagStoreRequest textToRagStoreRequest = new TextToRagStoreRequest();
            textToRagStoreRequest.setText(allData);
            textToRagStoreRequest.setChunk_separator("###CHUNK###");

            // Chain the API calls: Clear first, then Update
            apiServiceConnection.callClearVectorStore()
                    .doOnSubscribe(s ->
                        // Already set to clearing, could refine if needed
                        logger.info("Clear operation subscribed.")
                    )
                    .flatMap(clearResponse -> {
                        // Clear succeeded, now proceed to update
                        logger.info("Clear API call successful: {}", clearResponse);
                        // Update button text for the next stage
                        return apiServiceConnection.callTextToRAGAndStore(textToRagStoreRequest);
                    })
                    .doFinally(signalType ->
                        logger.info("Clear and Update sequence finished (Signal: {}).", signalType)
                      )
                    .subscribe( // Handle final success or error
                            updateAnswer -> {
                                // Both clear and update succeeded
                               logger.info("Clear and Update successful: {}", updateAnswer);
                               appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                               appConfigService.save(appConfig);
                            },
                            error -> {
                                // An error occurred during either clear OR update
                                logger.error("Error during Clear and Update process: {}", error.getMessage(), error);
                            }
                    );
            // End clear and update
        } else {
            logger.info("RAG database is already up-to-date. No action taken.");
        }
    }

    private boolean isUpdateNeeded(AppConfig appConfig) {
        if (appConfig == null) return false;
        Instant knowledgeDbLastUpdate = appConfig.getKnowledgeDatabaseLastUpdate();
        Instant aiRagLastUpdate = appConfig.getAiRagDocumentsLastUpdate();
        return knowledgeDbLastUpdate != null && (aiRagLastUpdate == null || knowledgeDbLastUpdate.isAfter(aiRagLastUpdate));
    }

    private TextToRagStoreRequest createRequest() {
        String ragData = this.ragService.generateStringData();
        TextToRagStoreRequest request = new TextToRagStoreRequest();
        request.setText(ragData);
        request.setChunk_separator("###CHUNK###");
        return request;
    }

    @Override
    public void run() {
        this.startRAGDataUpdateAutomatic();
    }
}
