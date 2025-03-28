package br.ufrj.cos.views.appconfig;

import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@UIScope
@Component
public class APIServiceConnection {

    private final WebClient.Builder webClientBuilder;
    private final Duration TIMEOUT = Duration.ofSeconds(30);

    public APIServiceConnection(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public void connectionTest(String address) {
        WebClient webClient = webClientBuilder.baseUrl(address).build();

        var ui = UI.getCurrent();

        webClient
                .get() //Use get, as the api uses get
                //.uri("/test_connection")
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
}