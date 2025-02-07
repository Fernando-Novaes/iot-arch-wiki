package br.ufrj.cos;

import br.ufrj.cos.components.qrcode.QRCodeComponent;
import org.springframework.context.annotation.*;

@Configuration
public class ComponentConfiguration {

    @Bean
    @Primary
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public QRCodeComponent qrCodeComponent() {
        return new QRCodeComponent();
    }
}
