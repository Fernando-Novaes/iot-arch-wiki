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

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@PWA(name = "", shortName = "")
@SpringBootApplication
@Theme(value = "iot-arch-wiki", variant = Material.LIGHT)
@Push(PushMode.MANUAL)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

}
