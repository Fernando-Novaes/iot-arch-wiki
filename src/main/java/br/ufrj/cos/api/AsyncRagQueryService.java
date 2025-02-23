package br.ufrj.cos.api;


import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@UIScope
@Service
public class AsyncRagQueryService {

    private final WebClient webClient;
    private final Duration TIMEOUT = Duration.ofSeconds(30);

    public AsyncRagQueryService(
            WebClient.Builder webClientBuilder,
            @Value("${api.url}") String host) {

        this.webClient = webClientBuilder
                .baseUrl(host)
                .build();
    }

    public Mono<String> queryRag(String query) {
        QueryRequest request = new QueryRequest();
        request.setQuery(query);

        return webClient
                .post()
                .uri("/query")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QueryResponse.class)
                .map(QueryResponse::getAnswer)
                .timeout(TIMEOUT)
                .onErrorResume(e -> Mono.error(
                        new RuntimeException("Failed to query RAG service: " + e.getMessage(), e)
                ));
    }
}
