package br.ufrj.cos;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.flow.server.AppShellSettings;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@PWA(name = "ArchIoTect", shortName = "ArchIoTect", iconPath = "icons/blue-square.png")
@SpringBootApplication
@Theme(value = "iot-arch-wiki", variant = Material.LIGHT)
@Push(PushMode.AUTOMATIC)
@EnableScheduling
public class Application implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "icons/blue-square.png", "32x32");
        settings.addLink("shortcut icon", "icons/blue-square.png");
        settings.addLink("icon", "icons/blue-square.png");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
