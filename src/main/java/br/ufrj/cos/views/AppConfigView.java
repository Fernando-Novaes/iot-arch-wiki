package br.ufrj.cos.views;

import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.ScrapWebSite;
import br.ufrj.cos.domain.ServiceName;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

@PageTitle("Application Config")
@Route(value = "appconfig-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@CssImport("./styles/app-styles.css")
public class AppConfigView extends BaseView {

    private AppConfig appConfig;
    private Grid<ScrapWebSite> scrapWebsiteGrid = new Grid<>(ScrapWebSite.class);
    private TextField urlField = new TextField("URL");
    private TextField descriptionField = new TextField("Description");
    private Button addButton = new Button("Add");
    private Grid<ServiceName> serviceNameGrid = new Grid<>(ServiceName.class);
    private TextField serviceNameField = new TextField("Service Name");
    private TextField serviceDescriptionField = new TextField("Service Description");
    private Button addServiceButton = new Button("Add");
    private final AppConfigService appConfigService;
    private Button saveButton = new Button("Save Configuration");
    private FlexLayout contentLayout = new FlexLayout();
    private TextField apiAddressField = new TextField("API Address");
    private Button testConnectionButton = new Button("Test Connection");

    public AppConfigView(AppConfigService appConfigService) {
        this.appConfig = appConfigService.getAppConfig();
        this.appConfigService = appConfigService;
        this.createHeader("Application Config");
        configureApiAddressBlock();
        configureScrapWebsiteBlock();
        configureServiceNameBlock();
        configureSaveButton();
        configureContentLayout();
    }

    private void configureApiAddressBlock() {
        if (appConfig.getApiAddress() != null) {
            apiAddressField.setValue(appConfig.getApiAddress());
        }
        apiAddressField.setWidthFull();
        apiAddressField.setRequired(true);

        testConnectionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        testConnectionButton.addClickListener(event -> {
            try {
                InetAddress.getByName(apiAddressField.getValue());
                NotificationUtils.showSuccessNotification("Connection successful.");
            } catch (UnknownHostException e) {
                NotificationUtils.showErrorNotification("Connection failed: Invalid IP Address.");
            }
        });

        HorizontalLayout apiAddressLayout = new HorizontalLayout(apiAddressField, testConnectionButton);
        apiAddressLayout.setWidthFull();
        apiAddressLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Div apiAddressDiv = new Div(new H3("API Address Configuration"), apiAddressLayout);
        apiAddressDiv.addClassName("block-container");
        apiAddressDiv.setWidth("80%");
        apiAddressDiv.setMaxWidth("1200px");
        apiAddressDiv.getStyle().set("margin", "0 auto");

        contentLayout.add(apiAddressDiv); // Add at the beginning
    }

    private void configureScrapWebsiteBlock() {
        if (appConfig.getScrapWebSites() != null) {
            scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());
        }
        scrapWebsiteGrid.setColumns("url", "description");
        scrapWebsiteGrid.setWidthFull();
        scrapWebsiteGrid.setMinHeight("5%");

        urlField.setWidthFull();
        urlField.setRequired(true);
        descriptionField.setWidthFull();

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> {
            ScrapWebSite website = ScrapWebSite.builder()
                    .url(urlField.getValue())
                    .description(descriptionField.getValue())
                    .build();

            if (appConfig.getScrapWebSites() == null) {
                appConfig.setScrapWebSites(new ArrayList<>());
            }
            appConfig.getScrapWebSites().add(website);
            scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());

            urlField.clear();
            descriptionField.clear();
        });

        HorizontalLayout inputLayout = new HorizontalLayout(urlField, descriptionField, addButton);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, urlField, descriptionField);
        inputLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Div scrapWebsiteDiv = new Div(new H3("Scrap Websites"), inputLayout, scrapWebsiteGrid);
        scrapWebsiteDiv.addClassName("block-container");
        scrapWebsiteDiv.setWidth("80%");
        scrapWebsiteDiv.setMaxWidth("1200px");
        scrapWebsiteDiv.getStyle().set("margin", "0 auto");

        contentLayout.add(scrapWebsiteDiv);
    }

    private void configureServiceNameBlock() {
        if (appConfig.getServiceNames() != null) {
            serviceNameGrid.setItems(appConfig.getServiceNames());
        }
        serviceNameGrid.setColumns("name", "description");
        serviceNameGrid.setWidthFull();

        serviceNameField.setWidthFull();
        serviceNameField.setRequired(true);
        serviceDescriptionField.setWidthFull();

        addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addServiceButton.addClickListener(event -> {
            ServiceName service = ServiceName.builder()
                    .name(serviceNameField.getValue())
                    .description(serviceDescriptionField.getValue())
                    .build();

            if (appConfig.getServiceNames() == null) {
                appConfig.setServiceNames(new ArrayList<>());
            }
            appConfig.getServiceNames().add(service);
            serviceNameGrid.setItems(appConfig.getServiceNames());

            serviceNameField.clear();
            serviceDescriptionField.clear();
        });

        HorizontalLayout serviceInputLayout = new HorizontalLayout(serviceNameField, serviceDescriptionField, addServiceButton);
        serviceInputLayout.setWidthFull();
        serviceInputLayout.setFlexGrow(1, serviceNameField, serviceDescriptionField);
        serviceInputLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Div serviceNameDiv = new Div(new H3("AI Service Names"), serviceInputLayout, serviceNameGrid);
        serviceNameDiv.addClassName("block-container");
        serviceNameDiv.setWidth("80%");
        serviceNameDiv.setMaxWidth("1200px");
        serviceNameDiv.getStyle().set("margin", "0 auto");

        contentLayout.add(serviceNameDiv);
    }

    private void configureSaveButton() {
        saveButton.addClickListener(event -> {
            appConfig.setApiAddress(apiAddressField.getValue());
            appConfigService.save(appConfig);
            NotificationUtils.showSuccessNotification("Configurations saved.");
        });
    }

    private void configureContentLayout() {
        contentLayout.setWidthFull();
        contentLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        saveButton.setWidthFull();
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        VerticalLayout saveLayout = new VerticalLayout(saveButton);
        saveLayout.setWidthFull();
        saveLayout.setAlignItems(FlexComponent.Alignment.END);

        contentLayout.add(saveLayout);
        getContent().add(contentLayout);
    }
}