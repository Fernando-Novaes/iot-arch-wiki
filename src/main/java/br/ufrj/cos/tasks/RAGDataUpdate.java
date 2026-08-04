package br.ufrj.cos.tasks;

import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.repository.TaskScheduleConfigRepository;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.service.ai.AiRagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RAGDataUpdate implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RAGDataUpdate.class);
    public static final String SERVICE_NAME = "RAG_KNOWLEDGE_UPDATE";

    private final AppConfigService appConfigService;
    private final RAGService ragService;
    private final AiRagService aiRagService;

    public RAGDataUpdate(AppConfigService appConfigService, RAGService ragService, AiRagService aiRagService, TaskScheduleConfigRepository taskScheduleConfigRepository) {
        this.appConfigService = appConfigService;
        this.ragService = ragService;
        this.aiRagService = aiRagService;
    }

    public void startRAGDataUpdateAutomatic() {
        AppConfig appConfig = this.appConfigService.getAppConfig();

        logger.info("Verifying RAG database for updates...");

        if (isUpdateNeeded(appConfig)) {
            logger.info("Update required. Starting native Java RAG database update process...");

            try {
                var structuredDocs = ragService.generateStructuredDocuments();
                boolean updated = aiRagService.indexStructuredDocuments(structuredDocs);

                appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                appConfigService.save(appConfig);
                logger.info("Native Structured RAG Update completed successfully (Updated: {}).", updated);
            } catch (Exception e) {
                logger.error("Error during native RAG update process: {}", e.getMessage(), e);
            }
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

    @Override
    public void run() {
        this.startRAGDataUpdateAutomatic();
    }
}
