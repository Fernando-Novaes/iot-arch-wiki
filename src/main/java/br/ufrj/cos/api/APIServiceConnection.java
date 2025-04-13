package br.ufrj.cos.api;

import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@UIScope
@Component
public class APIServiceConnection {
    private static final Logger logger = LoggerFactory.getLogger(APIServiceConnection.class);

    private final WebClient.Builder webClientBuilder;
    private final Duration TIMEOUT = Duration.ofSeconds(30);
    private final AppConfigService appConfigService;

    public APIServiceConnection(WebClient.Builder webClientBuilder, AppConfigService appConfigService) {
        this.webClientBuilder = webClientBuilder;
        this.appConfigService = appConfigService;
    }

    public void testConnection() {
        var ui = UI.getCurrent();

        if (this.appConfigService.getAppConfig().getApiAddress() == null) {
            NotificationUtils.showErrorNotification("There is no API address configured.");
            return;
        }

        String conn = String.format("%s/%s",
                this.appConfigService.getAppConfig().getApiAddress(),
                this.appConfigService.getServiceNameByType(APIServiceType.TEST_CONNECTION).getName());
        logger.info(String.format("Connecting to API service: %s", conn));

        WebClient webClient = webClientBuilder.baseUrl(conn).build();

        webClient
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .subscribe(
                        ok -> {
                            ui.access(() -> {
                                NotificationUtils.showSuccessNotification("AI Chat API successful connected.");
                                ui.push();
                            });
                        },
                        error -> {
                            ui.access(() -> {
                                NotificationUtils.showErrorNotification("Failed to connect: " + error.getMessage());
                                ui.push();
                            });
                        }
                );
    }

    public Mono<String> callWebScrapingAPI(WebScrapingRequest scrapingUrls) {
        if (this.appConfigService.getAppConfig().getApiAddress() == null) {
            return Mono.error(new RuntimeException("There is no API address configured."));
        }

        if (this.appConfigService.getServiceNameByType(APIServiceType.WEB_SCRAPING) == null) {
            return Mono.error(new RuntimeException("There is no AI Chat service name configured."));
        }

        String baseUrl = this.appConfigService.getAppConfig().getApiAddress();
        String serviceName = this.appConfigService.getServiceNameByType(APIServiceType.WEB_SCRAPING).getName();

        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(serviceName)
                .build()
                .toUriString();

        logger.info("Connecting to API service: {}", uri);

        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        return webClient
                .post()
                .uri(uri)
                .bodyValue(scrapingUrls)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> { // Corrected line using method reference
                    logger.error("API returned error status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("API error: " + errorBody)));
                })
                .bodyToMono(String.class)
                .map(String::toString)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // retry 3 times.
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClient error: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to query API: " + e.getMessage()));
                })
                .onErrorResume(e -> {
                    logger.error("General error: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("An unexpected error occurred: " + e.getMessage()));
                });
    }

    public Mono<String> callTextToRAGAndStore(TextToRagStoreRequest data) {
        if (this.appConfigService.getAppConfig().getApiAddress() == null) {
            return Mono.error(new RuntimeException("There is no API address configured."));
        }

        if (this.appConfigService.getServiceNameByType(APIServiceType.RAG_TEXT_AND_STORE) == null) {
            return Mono.error(new RuntimeException("There is no AI Chat service name configured."));
        }

        String baseUrl = this.appConfigService.getAppConfig().getApiAddress();
        String serviceName = this.appConfigService.getServiceNameByType(APIServiceType.RAG_TEXT_AND_STORE).getName();

        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(serviceName)
                .build()
                .toUriString();

        logger.info("Connecting to API service: {}", uri);

        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        return webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> { // Corrected line using method reference
                    logger.error("API returned error status: {}", response.statusCode());
                    return response.bodyToMono(TextToRagStoreResponse.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("API error: " + errorBody)));
                })
                .bodyToMono(String.class)
                .map(String::toString)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // retry 3 times.
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClient error: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to query API: " + e.getMessage()));
                })
                .onErrorResume(e -> {
                    logger.error("General error: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("An unexpected error occurred: " + e.getMessage()));
                });
    }

    public Mono<String> callClearVectorStore() {
        // Ensure necessary configurations are present (similar checks as other methods)
        if (this.appConfigService.getAppConfig().getApiAddress() == null) {
            return Mono.error(new RuntimeException("There is no API address configured."));
        }

        String baseUrl = this.appConfigService.getAppConfig().getApiAddress();
        String serviceName = this.appConfigService.getServiceNameByType(APIServiceType.CLEAR_AI_DATA).getName();

        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path(serviceName)
                .build()
                .toUriString();

        logger.info("Connecting to API service to clear vector store: {}", uri);

        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        ClearDataRequest data = new ClearDataRequest();
        data.setMessage("Clear data");

        // Use POST as defined in the Flask endpoint
        return webClient
                .post() // Keep POST as defined in Flask
                .uri(uri) // Use the corrected URI
                // .contentType(MediaType.APPLICATION_JSON) // Probably not needed if body is empty
                .bodyValue(data) // Sending empty body is likely fine for this endpoint
                .retrieve()

                .bodyToMono(String.class)
                .defaultIfEmpty("{\"message\": \"Vector store cleared successfully.\"}") // Default success if body is empty
                .map(String::toString)
                .timeout(TIMEOUT) // Use appropriate timeout
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClient error (clear store): Status {}, Body {}", e.getStatusCode(), e.getResponseBodyAsString(), e); // Log more details
                    return Mono.error(new RuntimeException("Failed to call clear API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()));
                })
                .onErrorResume(e -> {
                    logger.error("General error (clear store): {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("An unexpected error occurred during clear: " + e.getMessage()));
                });
    }
}