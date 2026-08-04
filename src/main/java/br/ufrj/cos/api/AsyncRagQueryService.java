package br.ufrj.cos.api;

import br.ufrj.cos.service.ai.AiRagService;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

@UIScope
@Service
public class AsyncRagQueryService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRagQueryService.class);

    private final AiRagService aiRagService;

    public AsyncRagQueryService(AiRagService aiRagService) {
        this.aiRagService = aiRagService;
    }

    public Mono<String> queryRag(String query, Optional<UUID> conversation_id) {
        logger.info("Executing native Java RAG query directly in-memory for conversation: {}", conversation_id.orElse(null));
        return Mono.fromCallable(() -> aiRagService.query(query, conversation_id.orElse(null)))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("Error executing native RAG query: {}", e.getMessage(), e);
                    return Mono.just("<p>Erro ao consultar o serviço de IA: " + e.getMessage() + "</p>");
                });
    }

    public Flux<String> queryRagStream(String query, Optional<UUID> conversation_id) {
        logger.info("Executing streaming native Java RAG query for conversation: {}", conversation_id.orElse(null));
        return aiRagService.queryStream(query, conversation_id.orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    logger.error("Error executing streaming RAG query: {}", msg);

                    if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED")) {
                        return Flux.just("<p>⚠️ <strong>Cota do serviço de IA temporariamente excedida (HTTP 429).</strong><br>Por favor, aguarde alguns segundos e tente novamente ou insira uma nova chave de API do Google Gemini no menu <strong>Application Config</strong>.</p>");
                    }
                    if (msg.contains("503") || msg.contains("UNAVAILABLE")) {
                        return Flux.just("<p>⏳ <strong>O serviço de IA está temporariamente sobrecarregado (HTTP 503).</strong><br>Por favor, aguarde alguns segundos e tente novamente.</p>");
                    }

                    return Flux.just("<p>Erro ao consultar o serviço de IA: " + msg + "</p>");
                });
    }
}
