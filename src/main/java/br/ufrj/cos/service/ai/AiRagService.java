package br.ufrj.cos.service.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRagService {

    private static final Logger logger = LoggerFactory.getLogger(AiRagService.class);

    private static final String DEFAULT_API_KEY = "";
    private static final String STORE_FILE_PATH = "database/rag_vector_store.json";
    private static final int MAX_HISTORY_MESSAGES = 6;

    private GoogleRestChatService chatService;
    private EmbeddingModel embeddingModel;
    private InMemoryEmbeddingStore<TextSegment> embeddingStore;

    // Chat Conversation Memory Map (conversationId -> List of ChatHistoryTurn)
    private final Map<UUID, List<ChatTurn>> conversationHistoryMap = new ConcurrentHashMap<>();

    public record ChatTurn(String role, String text) {}

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private br.ufrj.cos.service.AppConfigService appConfigService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private br.ufrj.cos.service.UserApplicationService userApplicationService;

    @Value("${google.api.key:}")
    private String configuredApiKey;

    public synchronized void updateGoogleApiKey(String newKey) {
        if (newKey != null && !newKey.trim().isEmpty()) {
            logger.info("Updating Google Gemini API Key dynamically in AiRagService...");
            this.chatService = new GoogleRestChatService(newKey.trim());
        }
    }

    @PostConstruct
    public void init() {
        String apiKey = resolveApiKey();
        logger.info("Initializing Native Java AI RAG Service with Google Gemini API...");

        try {
            this.chatService = new GoogleRestChatService(apiKey);
            this.embeddingModel = new HashingEmbeddingModel();

            // Load vector store from disk if exists, or create fresh
            File storeFile = new File(STORE_FILE_PATH);
            if (storeFile.exists()) {
                try {
                    this.embeddingStore = InMemoryEmbeddingStore.fromFile(storeFile.toPath());
                    logger.info("Loaded persisted vector store from {}", STORE_FILE_PATH);
                } catch (Exception e) {
                    logger.warn("Failed to load vector store file ({}), creating fresh store", e.getMessage());
                    this.embeddingStore = new InMemoryEmbeddingStore<>();
                }
            } else {
                this.embeddingStore = new InMemoryEmbeddingStore<>();
            }
        } catch (Exception e) {
            logger.error("Error initializing LangChain4j Gemini Models: {}", e.getMessage(), e);
        }
    }

    private String resolveApiKey() {
        if (appConfigService != null) {
            try {
                br.ufrj.cos.domain.AppConfig config = appConfigService.getAppConfig();
                if (config != null && config.getGoogleApiKey() != null && !config.getGoogleApiKey().trim().isEmpty()) {
                    logger.info("Key source: Loaded Google Gemini API Key from Database.");
                    return config.getGoogleApiKey().trim();
                }
            } catch (Exception e) {
                logger.warn("Could not load API Key from Database: {}", e.getMessage());
            }
        }

        if (configuredApiKey != null && !configuredApiKey.trim().isEmpty()) {
            logger.info("Key source: Loaded Google Gemini API Key from application.properties.");
            return configuredApiKey.trim();
        }
        String envKey = System.getenv("GOOGLE_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        List<String> envPaths = List.of(".env", "../iot-arch-ai-assistant/.env", "c:/Users/Fernando/Antigravity Projects/iot-arch-ai-assistant/.env");
        for (String p : envPaths) {
            try {
                Path path = Paths.get(p);
                if (Files.exists(path)) {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        if (line.startsWith("GOOGLE_API_KEY=") || line.startsWith("GEMINI_API_KEY=")) {
                            String key = line.split("=", 2)[1].trim().replace("\"", "").replace("'", "");
                            if (!key.isEmpty()) {
                                return key;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return DEFAULT_API_KEY;
    }

    public synchronized void clearVectorStore() {
        logger.info("Clearing vector store...");
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        try {
            File storeFile = new File(STORE_FILE_PATH);
            if (storeFile.exists()) {
                storeFile.delete();
            }
        } catch (Exception e) {
            logger.warn("Failed to delete store file: {}", e.getMessage());
        }
    }

    public void clearConversationHistory(UUID conversationId) {
        if (conversationId != null) {
            conversationHistoryMap.remove(conversationId);
            logger.info("Cleared conversation history for conversation ID: {}", conversationId);
        }
    }

    public synchronized void indexTextContent(String textContent) {
        indexTextContent(textContent, null);
    }

    public synchronized void indexTextContent(String textContent, Map<String, String> metadataMap) {
        if (textContent == null || textContent.trim().isEmpty()) {
            logger.warn("Received empty text content for indexing. Skipping.");
            return;
        }

        logger.info("Indexing text content into RAG vector store...");
        try {
            Document doc = Document.from(textContent);
            var splitter = DocumentSplitters.recursive(1000, 300);
            List<TextSegment> segments = splitter.split(doc);

            if (segments.isEmpty()) {
                logger.warn("No text segments produced by splitter.");
                return;
            }

            Metadata metadata = (metadataMap != null && !metadataMap.isEmpty())
                    ? Metadata.from(metadataMap)
                    : new Metadata();

            for (TextSegment segment : segments) {
                TextSegment segmentWithMeta = TextSegment.from(segment.text(), metadata);
                Embedding embedding = embeddingModel.embed(segmentWithMeta).content();
                embeddingStore.add(embedding, segmentWithMeta);
            }

            // Persist to file
            try {
                File dir = new File("database");
                if (!dir.exists()) dir.mkdirs();
                this.embeddingStore.serializeToFile(Paths.get(STORE_FILE_PATH));
                logger.info("Successfully indexed {} text chunks with metadata into vector store and persisted to {}", segments.size(), STORE_FILE_PATH);
            } catch (Exception e) {
                logger.warn("Successfully indexed {} text chunks, but file persistence failed: {}", segments.size(), e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error during text indexing: {}", e.getMessage(), e);
            throw new RuntimeException("Error indexing text into RAG store: " + e.getMessage(), e);
        }
    }

    public synchronized boolean indexStructuredDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            logger.warn("Received empty document list for indexing. Skipping.");
            return false;
        }

        String checksum = calculateChecksum(documents);
        File checksumFile = new File("database/rag_checksum.txt");

        if (checksumFile.exists()) {
            try {
                String existingChecksum = Files.readString(checksumFile.toPath()).trim();
                if (existingChecksum.equals(checksum) && new File(STORE_FILE_PATH).exists()) {
                    logger.info("RAG vector store is already up to date (checksum matches). Incremental index skipped.");
                    return false;
                }
            } catch (Exception ignored) {}
        }

        logger.info("Indexing {} structured document chunks into RAG vector store...", documents.size());
        this.clearVectorStore();

        int totalSegments = 0;
        for (Document doc : documents) {
            String text = doc.text();
            Metadata meta = doc.metadata();

            if (text.length() <= 1500) {
                TextSegment segment = TextSegment.from(text, meta);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                totalSegments++;
            } else {
                var splitter = DocumentSplitters.recursive(1000, 200);
                List<TextSegment> segments = splitter.split(doc);
                for (TextSegment seg : segments) {
                    Embedding embedding = embeddingModel.embed(seg).content();
                    embeddingStore.add(embedding, seg);
                    totalSegments++;
                }
            }
        }

        try {
            File dir = new File("database");
            if (!dir.exists()) dir.mkdirs();
            this.embeddingStore.serializeToFile(Paths.get(STORE_FILE_PATH));
            Files.writeString(checksumFile.toPath(), checksum);
            logger.info("Successfully indexed {} structured text segments into vector store and persisted to {}", totalSegments, STORE_FILE_PATH);
            return true;
        } catch (Exception e) {
            logger.warn("Successfully indexed segments, but file persistence failed: {}", e.getMessage());
            return true;
        }
    }

    public synchronized boolean forceReindexDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            logger.warn("Received empty document list for force reindexing. Skipping.");
            return false;
        }

        logger.info("Force reindexing {} documents into RAG vector store...", documents.size());
        this.clearVectorStore();

        int totalSegments = 0;
        for (Document doc : documents) {
            String text = doc.text();
            Metadata meta = doc.metadata() != null ? doc.metadata() : new Metadata();

            if (text.length() <= 1500) {
                TextSegment segment = TextSegment.from(text, meta);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                totalSegments++;
            } else {
                var splitter = DocumentSplitters.recursive(1000, 200);
                List<TextSegment> segments = splitter.split(doc);
                for (TextSegment seg : segments) {
                    Embedding embedding = embeddingModel.embed(seg).content();
                    embeddingStore.add(embedding, seg);
                    totalSegments++;
                }
            }
        }

        try {
            File dir = new File("database");
            if (!dir.exists()) dir.mkdirs();
            this.embeddingStore.serializeToFile(Paths.get(STORE_FILE_PATH));
            String checksum = calculateChecksum(documents);
            File checksumFile = new File("database/rag_checksum.txt");
            Files.writeString(checksumFile.toPath(), checksum);
            logger.info("Successfully force-indexed {} text segments into vector store and persisted to {}", totalSegments, STORE_FILE_PATH);
            return true;
        } catch (Exception e) {
            logger.warn("Successfully force-indexed segments, but file persistence failed: {}", e.getMessage());
            return true;
        }
    }

    private String calculateChecksum(List<Document> documents) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            for (Document doc : documents) {
                md.update(doc.text().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if (doc.metadata() != null) {
                    md.update(doc.metadata().toMap().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(documents.hashCode());
        }
    }

    public String query(String userQuery) {
        return query(userQuery, null);
    }

    private static final String FALLBACK_INSTRUCTIVE_MESSAGE = """
        <div style="background: var(--lumo-contrast-5pct); border-left: 4px solid #d97706; border-radius: 8px; padding: 12px 14px; margin-top: 6px;">
            <h4 style="margin: 0 0 6px 0; color: #d97706; font-weight: 700; font-size: 0.88rem;">
                ⚠️ Unable to Evaluate Solution with Current Context Data
            </h4>
            <p style="margin: 0 0 8px 0; font-size: 0.78rem; line-height: 1.45; color: var(--lumo-body-text-color);">
                No direct scientific evidence was found in the RAG knowledge base for the exact combination of selected building blocks. To assist the AI engine in performing a precise diagnostic, architects are requested to perform the following checks:
            </p>
            <ul style="margin: 0; padding-left: 18px; font-size: 0.76rem; line-height: 1.5; color: var(--lumo-body-text-color);">
                <li><strong>Domain & Pattern Alignment:</strong> Verify whether the selected domain (e.g., <em>Generic</em>) is using a domain-specific architecture pattern from another domain (e.g., <em>Healthcare</em>, <em>Smart Farming</em>, or <em>Industry 4.0</em>).</li>
                <li><strong>Layer Technology Allocation:</strong> Ensure heavy cloud infrastructures (e.g., <em>AWS Core</em>, <em>Big Data Analytics Engine</em>) are not assigned to the Edge or Fog layers.</li>
                <li><strong>Component Variation:</strong> Try varying solution components or protocols to align with standard practices for the chosen pattern.</li>
            </ul>
        </div>
        """;

    private String extractCleanSearchQuery(String fullQuery) {
        if (fullQuery == null || fullQuery.trim().isEmpty()) return "";
        if (fullQuery.contains("Target IoT Domain:") || fullQuery.contains("Selected Architecture Pattern:")) {
            StringBuilder sb = new StringBuilder();
            for (String line : fullQuery.split("\n")) {
                if (line.contains("Domain:") || line.contains("Pattern:") || line.contains("Layer") || line.contains("Requirements") || line.contains("Notes")) {
                    sb.append(line.replaceAll("[\\*#\\-]", "").trim()).append(" ");
                }
            }
            if (sb.length() > 0) {
                return sb.toString().trim();
            }
        }
        if (fullQuery.length() > 250) {
            return fullQuery.substring(0, 250);
        }
        return fullQuery;
    }

    public String query(String userQuery, UUID conversationId) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return "<p>Por favor, forneça uma pergunta válida.</p>";
        }

        logger.info("Executing Native RAG query (ConversationId: {}): {}", conversationId, userQuery);

        String context = fetchRAGContext(userQuery);
        String historyText = getFormattedHistory(conversationId);
        String prompt = buildPromptWithFewShot(context, historyText, userQuery);

        try {
            String response = chatService.generate(prompt);

            if (conversationId != null && response != null && !response.isEmpty()) {
                recordTurn(conversationId, userQuery, response);
            }

            return response;
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";

            if (msg.contains("503") || msg.contains("UNAVAILABLE")) {
                logger.warn("AI service temporarily unavailable (503): {}", msg);
                return "<p>⏳ <strong>O serviço de IA está temporariamente sobrecarregado.</strong> "
                        + "Isso é um problema temporário. "
                        + "Por favor, <strong>aguarde alguns segundos e tente novamente</strong>.</p>";
            }

            if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED")) {
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("retry in (\\d+)")
                        .matcher(msg);
                String waitMsg = m.find()
                        ? "Tente novamente em aproximadamente <strong>" + m.group(1) + " segundos</strong>."
                        : "Tente novamente em alguns instantes.";
                logger.warn("AI service quota exceeded: {}", msg);
                return "<p>⚠️ <strong>Cota do serviço de IA esgotada.</strong> " + waitMsg
                        + "<br>Se o problema persistir, você pode cadastrar uma nova chave de API do Google Gemini no menu <strong>Application Config</strong> (campo <em>Google Gemini API Key</em>).</p>";
            }

            logger.error("Error calling AI LLM service: {}", msg, e);
            return "<p>Erro ao consultar o serviço de IA: " + msg + "</p>";
        }
    }

    public Flux<String> queryStream(String userQuery, UUID conversationId) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return Flux.just("<p>Por favor, forneça uma pergunta válida.</p>");
        }

        logger.info("Executing Native RAG streaming query (ConversationId: {}): {}", conversationId, userQuery);

        String context = fetchRAGContext(userQuery);
        String historyText = getFormattedHistory(conversationId);
        String prompt = buildPromptWithFewShot(context, historyText, userQuery);

        return chatService.generateStream(prompt);
    }

    private String fetchRAGContext(String userQuery) {
        String context = "";
        try {
            String searchTerm = extractCleanSearchQuery(userQuery);
            Embedding queryEmbedding = embeddingModel.embed(searchTerm).content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(8)
                    .minScore(0.08)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            context = buildContextFromMatches(searchResult.matches());

            String lower = userQuery.toLowerCase();
            if (lower.contains("dominio") || lower.contains("domain") || lower.contains("requisito") || lower.contains("quality") || lower.contains("qr") || lower.contains("tecnologia") || lower.contains("technology") || lower.contains("padrao") || lower.contains("pattern") || lower.contains("iso 25010")) {
                Embedding catalogEmbedding = embeddingModel.embed("Master Knowledge Base Catalog Index ISO 25010 Quality Requirements IoT Domains Technologies").content();
                EmbeddingSearchRequest catalogRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(catalogEmbedding)
                        .maxResults(5)
                        .minScore(0.02)
                        .build();
                EmbeddingSearchResult<TextSegment> catalogResult = embeddingStore.search(catalogRequest);
                String catalogContext = buildContextFromMatches(catalogResult.matches());
                if (!catalogContext.isEmpty()) {
                    context = catalogContext + "\n\n" + context;
                }
            }
        } catch (Exception e) {
            logger.warn("Similarity search warning: {}", e.getMessage());
        }

        if (context.trim().isEmpty()) {
            context = "[Context Note: Evaluating using general IoT architectural knowledge and ISO 25010 standards.]\n\n";
        }
        return context;
    }

    private String buildContextFromMatches(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches == null || matches.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            if (match.embedded() != null) {
                TextSegment seg = match.embedded();
                String archName = seg.metadata() != null ? seg.metadata().get("architecture") : null;
                String docName = seg.metadata() != null ? seg.metadata().get("document") : null;
                String paperTitle = seg.metadata() != null ? seg.metadata().get("paper") : null;
                String paperDoi = seg.metadata() != null ? seg.metadata().get("doi") : null;
                String paperRef = seg.metadata() != null ? seg.metadata().get("reference") : null;
                String paperYear = seg.metadata() != null ? seg.metadata().get("year") : null;

                sb.append("[Match Score: ").append(String.format("%.2f", match.score()));
                if (archName != null) sb.append(" | Architecture: ").append(archName);
                if (paperTitle != null && !paperTitle.equals("N/A")) sb.append(" | Paper Title: ").append(paperTitle);
                if (paperRef != null && !paperRef.equals("N/A")) sb.append(" | Reference: ").append(paperRef);
                if (paperYear != null && !paperYear.equals("N/A")) sb.append(" | Year: ").append(paperYear);
                if (paperDoi != null && !paperDoi.equals("N/A")) sb.append(" | DOI: ").append(paperDoi);
                if (docName != null) sb.append(" | Document: ").append(docName);
                sb.append("]\n");
                sb.append(seg.text()).append("\n\n---\n\n");
            }
        }
        return sb.toString();
    }

    private String getFormattedHistory(UUID conversationId) {
        if (conversationId == null) return "";
        List<ChatTurn> history = conversationHistoryMap.get(conversationId);
        if (history == null || history.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("Previous Conversation History:\n");
        for (ChatTurn turn : history) {
            sb.append("[").append(turn.role()).append("]: ").append(turn.text()).append("\n\n");
        }
        return sb.toString();
    }

    private void recordTurn(UUID conversationId, String userQuery, String assistantResponse) {
        List<ChatTurn> history = conversationHistoryMap.computeIfAbsent(conversationId, k -> Collections.synchronizedList(new ArrayList<>()));
        history.add(new ChatTurn("User", userQuery));
        history.add(new ChatTurn("Assistant", assistantResponse));

        while (history.size() > MAX_HISTORY_MESSAGES) {
            history.remove(0);
        }
    }

    private boolean isStackEvaluationQuery(String query) {
        return query != null && (query.contains("Target IoT Domain:") || query.contains("Selected Architecture Pattern:") || query.contains("Evaluated Stack Prompt:"));
    }

    private boolean isExternalContextAllowed() {
        if (appConfigService != null) {
            try {
                br.ufrj.cos.domain.AppConfig config = appConfigService.getAppConfig();
                if (config != null) {
                    Boolean allowOverride = config.getAllowUserContextOverride();
                    if (Boolean.FALSE.equals(allowOverride)) {
                        // Admin has enforced system policy, user override is disabled
                        return config.getAllowExternalContext() != null ? config.getAllowExternalContext() : true;
                    }

                    // User override is permitted by Admin, check current logged-in user preference
                    String currentUsername = br.ufrj.cos.utils.SecurityUtils.getUsername();
                    if (currentUsername != null && userApplicationService != null) {
                        br.ufrj.cos.domain.UserApplication userApp = userApplicationService.findByUserName(currentUsername);
                        if (userApp != null && userApp.getAllowExternalContext() != null) {
                            return userApp.getAllowExternalContext();
                        }
                    }

                    return config.getAllowExternalContext() != null ? config.getAllowExternalContext() : true;
                }
            } catch (Exception e) {
                logger.warn("Could not load allowExternalContext setting: {}", e.getMessage());
            }
        }
        return true;
    }

    private String buildPromptWithFewShot(String context, String historyText, String question) {
        boolean allowExternal = isExternalContextAllowed();
        String strictRule = !allowExternal ? """

        CRITICAL STRICT CONTEXT RULE (STRICT APPLICATION DATA MODE IS ACTIVE):
        The system governance setting is configured to STRICT APPLICATION CONTEXT ONLY.
        - You MUST answer EXCLUSIVELY using the retrieved application context documents provided below (which encompass all cataloged knowledge base articles, domain data, and uploaded Standards & Reference Documents).
        - DO NOT fetch, generate, or incorporate external internet knowledge, ungrounded web assumptions, or external training facts beyond the application's internal data.
        - Synthesize your evaluation directly from the provided application context documents (cataloged articles and uploaded reference PDFs).
        """ : "";

        if (!isStackEvaluationQuery(question)) {
            return """
            First, use clean HTML to format the output with headers (<h2>, <h3>), bullet lists (<ul>, <li>), and bold text. You are an expert IoT System Architect.
            """ + strictRule + """

            RULES FOR GENERAL CONVERSATIONAL INQUIRIES & LISTINGS:
            1. DIRECT RESPONSE: Answer the user's question directly, clearly, and concisely in the same language as the question (e.g. Portuguese).
            2. COMPLETE CANONICAL LISTINGS: When asked to list cataloged items:
               - For Quality Requirements (QR / ISO 25010): List ALL ISO 25010 Quality Characteristics cataloged in the knowledge base (Security, Performance / Low Latency, High Availability, Scalability, Fault Tolerance, Interoperability, Compatibility, Flexibility, Functional Suitability, Maintainability, Reliability, Safety) with brief explanations of how they apply to IoT systems.
               - For IoT Domains: List ALL cataloged domains (Smart Farming, Industry 4.0, Healthcare, Smart City, Generic).
               - For Architectural Patterns: List ALL cataloged patterns.
            3. NO UNNECESSARY DIAGNOSTIC HEADERS: DO NOT output architectural evaluation report headers (like Evidence Confidence Indicator, Layer Placement Check, or Quality Requirements Adaptability) unless the user explicitly requested a full stack trade-off analysis.
            4. ARCHITECTURAL SOLUTION RECOMMENDATION STACK:
               ONLY IF the user asks for an architecture recommendation, suggestion, design proposal, or stack recommendation (e.g., "sugira uma arquitetura...", "qual arquitetura usar...", "recomende uma solução...", "design an architecture for..."):
               - First provide your detailed explanation in HTML.
               - THEN, at the VERY END of your response, append a structured JSON payload inside `<script type="application/json" class="suggested-arch-stack">` with the exact stack components matching cataloged names.
               Example format:
               <script type="application/json" class="suggested-arch-stack">
               {
                 "domain": "Smart Farming",
                 "pattern": "3-Tier (Edge-Fog-Cloud)",
                 "edgeTechs": ["Edge Sensors", "LoRaWAN"],
                 "fogTechs": ["Node-RED", "Docker Container"],
                 "cloudTechs": ["AWS IoT Core", "Apache Kafka", "Time-Series DB"],
                 "qualityReqs": ["High Availability", "Performance / Low Latency"]
               }
               </script>
               - DO NOT append this script tag for simple questions, listings, definitions, or general non-architectural inquiries.

            --- FEW-SHOT EXAMPLE ---
            Question: "liste os dominios de iot"
            Answer:
            <h2>Domínios de IoT Catalogados</h2>
            <p>Os domínios de Internet das Coisas (IoT) catalogados na base de conhecimento são:</p>
            <ul>
              <li><strong>Smart Farming:</strong> Agricultura de precisão, gestão de irrigação e sensores de campo.</li>
              <li><strong>Industry 4.0:</strong> Automação industrial, manufatura inteligente e manutenção preditiva.</li>
              <li><strong>Healthcare:</strong> Saúde conectada, monitoramento remoto de pacientes e dispositivos médicos.</li>
              <li><strong>Smart City:</strong> Mobilidade urbana, gestão de iluminação e monitoramento ambiental urbano.</li>
              <li><strong>Genérico:</strong> Padrões e soluções arquiteturais transversais reutilizáveis.</li>
            </ul>
            --- END OF EXAMPLE ---

            """ + (historyText.isEmpty() ? "" : historyText + "\n---\n\n") + """
            Context Documents & Articles:
            """ + context + """

            User Question: """ + question;
        }

        return """
        First, use clean HTML to format the output with headers (<h2>, <h3>), bullet lists (<ul>, <li>), and bold text. Highlight key architectural components. URLs shall open in new browser tabs. You are an expert IoT System Architect.
        """ + strictRule + """

        CRITICAL RULES FOR ACCURACY, ISOLATION & RELIABILITY:
        1. CONTEXT SYNTHESIS & BROAD EVIDENCE ANALYSIS:
           - Evaluate the requested architecture based on the retrieved application context documents (which contain all cataloged knowledge base data and uploaded reference PDFs/standards).
           - Synthesize findings from individual component matches across cataloged papers and uploaded reference documents.
           - DO NOT output rigid fallback disclaimer blocks unless no relevant content exists across all application context documents. Provide a comprehensive, tailored architectural evaluation!
        2. EVIDENCE CONFIDENCE INDICATOR:
           At the start of your evaluation report (under section 1), explicitly state the Evidence Level based on retrieved context:
           - **High Evidence**: Direct scientific paper matches exist for multiple components.
           - **Medium Evidence**: Partial paper matches exist for specific layers or components.
           - **General Evidence**: Synthesized evaluation based on core IoT architectural standards & ISO 25010.
        3. NATIVE VS. ADAPTABILITY DISTINCTION (QUALITY REQUIREMENTS):
           Whenever explaining Quality Requirements, structure into:
           a) "a) Native Architecture Strengths:" (List ONLY quality requirements explicitly documented in the provided context for this architecture).
           b) "b) Adaptability & Extension Recommendations:" (If a quality requirement was not explicitly defined in the base paper, state it and provide concrete recommended extension patterns/good practices).
        4. SCOPE TRANSPARENCY:
           If the user asks about a Quality Requirement not covered by the target architecture, reply explicitly:
           "This architecture originally does not specify [Requirement X] in the base paper. However, to satisfy it and guarantee desired system behavior, the recommended adaptation practice is [...]".
        5. LAYER COMPONENT PLACEMENT & ALLOCATION CHECK:
           When evaluating an IoT architecture composition, explicitly inspect component layer placement:
           - Check if Cloud ecosystems (e.g., general AWS, Azure, GCP, or Big Data Analytics) are placed in Edge/Fog layers without edge runtime qualifiers (like AWS Greengrass).
           - Check if Quality Requirements or Security Mechanisms (e.g., Authentication, Access Control) are mislabeled as physical Edge technologies.
           - Highlight layer misplacements under a dedicated heading and provide concrete reallocation guidance.
        6. MANDATORY SCIENTIFIC CITATIONS (REFERENCES):
           At the end of the answer, ALWAYS add a section titled "<h3>References</h3>".
           List the real scientific paper details from the retrieved context. If direct matches were sparse, cite general domain reference standards (e.g. ISO 25010, IEEE IoT Architecture Reference Model).

        Follow these few-shot structural guidelines for your response format:

        --- FEW-SHOT EXAMPLE 1 ---
        Question: "Como a arquitetura 3-camadas atende Segurança e Desempenho?"
        Answer:
        <h2>Análise de Requisitos de Qualidade: Arquitetura 3-Camadas</h2>

        <h3>a) Requisitos Nativos da Arquitetura:</h3>
        <ul>
          <li><b>Desempenho / Baixa Latência:</b> Atendido pela Camada Fog através do pré-processamento local de dados antes do envio à nuvem.</li>
        </ul>

        <h3>b) Recomendações de Adaptabilidade para Requisitos Ausentes / Não Especificados:</h3>
        <p><i>Nota: O artigo base desta arquitetura originalmente não especifica o requisito de <b>Segurança</b>. Contudo, para atendê-lo e garantir a integridade do sistema, as seguintes boas práticas de adaptação são recomendadas:</i></p>
        <ul>
          <li><b>Criptografia de Enlace (Edge-Fog):</b> Implementação de protocolos TLS 1.3 / DTLS para comunicação entre nós sensores e gateways.</li>
          <li><b>Autenticação na Nuvem:</b> Uso de tokens OAuth2 / JWT nas APIs da camada Cloud.</li>
          <li><b>Hardware Seguro:</b> Uso de módulos TPM (Trusted Platform Module) nos gateways da camada Fog.</li>
        </ul>

        <h3>Referências / References:</h3>
        <ul>
          <li><b>A Reference Architecture for Internet of Things</b> - Silva et al. (2023). DOI: <a href="https://doi.org/10.1109/ACCESS.2023.123456" target="_blank">https://doi.org/10.1109/ACCESS.2023.123456</a></li>
        </ul>

        --- FEW-SHOT EXAMPLE 2 ---
        Question: "Quais domínios de IoT você conhece?"
        Answer:
        <h2>Domínios de IoT Catalogados</h2>
        <p>Os domínios de Internet das Coisas (IoT) que constam na base de conhecimento são:</p>
        <ul>
          <li><b>Smart City:</b> Gestão urbana inteligente, iluminação pública e mobilidade urbana.</li>
          <li><b>Healthcare:</b> Dispositivos médicos conectados, telessaúde e monitoramento de pacientes.</li>
          <li><b>Industry 4.0:</b> Automação industrial, manufatura inteligente e manutenção preditiva.</li>
          <li><b>Smart Farming:</b> Agricultura de precisão, gestão de recursos hídricos e sensores agrícolas.</li>
          <li><b>Genérico:</b> Modelos e padrões arquiteturais transversais aplicáveis a diversos cenários.</li>
        </ul>

        <h3>Referências / References:</h3>
        <ul>
          <li><b>Visão Geral dos Domínios IoT</b> - Mapeamento da Base de Conhecimento ArchIoTect.</li>
        </ul>

        --- END OF FEW-SHOT EXAMPLES ---

        """ + (historyText.isEmpty() ? "" : historyText + "\n---\n\n") + """
        Context Documents & Articles:
        """ + context + """

        Question: """ + question + """

        Answer (REMINDER: Strict Context Only, Architecture Isolation, and Mandatory Scientific Paper Citations in Referências / References):
        """;
    }
}
