package br.ufrj.cos.api;


import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@UIScope
@Service
public class AsyncRagQueryService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRagQueryService.class);

    private final WebClient.Builder webClientBuilder;
    private final Duration TIMEOUT = Duration.ofSeconds(30);
    private final AppConfigService appConfigService;

    public AsyncRagQueryService(WebClient.Builder webClientBuilder, AppConfigService appConfigService) {
        this.webClientBuilder = webClientBuilder;
        this.appConfigService = appConfigService;
    }

    public Mono<String> queryRag(String query) {
        if (this.appConfigService.getAppConfig().getApiAddress() == null) {
            NotificationUtils.showErrorNotification("There is no API address configured.");
            return Mono.empty();
        }

        if (this.appConfigService.getServiceNameByType(APIServiceType.CHAT_QUERY) == null) {
            NotificationUtils.showErrorNotification("There is no AI Chat service name configured.");
            return Mono.empty();
        }

        String conn = String.format("%s/%s",
                this.appConfigService.getAppConfig().getApiAddress(),
                this.appConfigService.getServiceNameByType(APIServiceType.CHAT_QUERY).getName());
        logger.info(String.format("Connecting to API service: %s", conn));

        WebClient webClient = webClientBuilder.baseUrl(this.appConfigService.getAppConfig().getApiAddress()).build();
        logger.info(String.format("Base URL: %s", this.appConfigService.getAppConfig().getApiAddress()));

        QueryRequest request = new QueryRequest();
        request.setQuery(query);

        return webClient
                .post()
                .uri(String.format("/%s", this.appConfigService.getServiceNameByType(APIServiceType.CHAT_QUERY).getName()))
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
