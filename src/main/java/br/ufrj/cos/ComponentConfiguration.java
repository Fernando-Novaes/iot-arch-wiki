package br.ufrj.cos;


import br.ufrj.cos.components.qrcode.QRCodeComponent;
import com.vaadin.flow.component.messages.MessageList;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ComponentConfiguration {

    @Bean
    @Primary
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public QRCodeComponent qrCodeComponent() {
        return new QRCodeComponent();
    }

    @Bean
    public MessageList messageList() {
        return new MessageList();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
