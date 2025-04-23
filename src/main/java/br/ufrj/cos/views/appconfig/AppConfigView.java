package br.ufrj.cos.views.appconfig;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.api.TextToRagStoreRequest;
import br.ufrj.cos.domain.APIServiceType;
import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.ScrapWebSite;
import br.ufrj.cos.domain.ServiceName;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.EndpointRecord;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@PageTitle("Application Config")
@Route(value = "appconfig-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@CssImport("./styles/app-styles.css")
public class AppConfigView extends BaseView {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigView.class);

    private AppConfig appConfig;
    private final Grid<ScrapWebSite> scrapWebsiteGrid = new Grid<>(ScrapWebSite.class);
    private final TextField urlField = new TextField("URL");
    private final TextField descriptionField = new TextField("Description");
    private final Button addButton = new Button("Add");
    private final Grid<ServiceName> serviceNameGrid = new Grid<>(ServiceName.class);
    private final ComboBox<EndpointRecord> serviceNameField = new ComboBox<EndpointRecord>("Service Name");
    private final TextField serviceDescriptionField = new TextField("Service Description");
    private final ComboBox<APIServiceType> serviceTypeComboBox = new ComboBox<>("Service Type", APIServiceType.values());
    private final Button addServiceButton = new Button("Add");
    private final AppConfigService appConfigService;
    private final Button saveButton = new Button("Save Configuration");
    private final FlexLayout contentLayout = new FlexLayout();
    private final TextField apiAddressField = new TextField("API Address");
    private final Button testConnectionButton = new Button("Test Connection");
    private final RAGService ragService;

    private final APIServiceConnection apiServiceConnection;
    private final List<EndpointRecord> endpoints;

    public AppConfigView(AppConfigService appConfigService, RAGService ragService, APIServiceConnection apiServiceConnection, List<EndpointRecord> endpoints) {
        this.appConfig = appConfigService.getAppConfig();
        this.appConfigService = appConfigService;
        this.ragService = ragService;
        this.apiServiceConnection = apiServiceConnection;
        this.endpoints = endpoints;
        this.createHeader("Application Config");
        configureApiAddressBlock();
        //configureScrapWebsiteBlock();
        configureServiceNameBlock();
        //configureSaveButton();
        configureLastUpdateLabels(); // Add this line
        configureContentLayout();
        loadEndpoints();
    }

    private void configureApiAddressBlock() {
        if (appConfig.getApiAddress() != null) {
            apiAddressField.setValue(appConfig.getApiAddress());
        }
        apiAddressField.setWidthFull();
        apiAddressField.setRequired(true);
        apiAddressField.setPlaceholder("XXX.XXX.XXX.XXX:0000");

        testConnectionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        testConnectionButton.addClickListener(event -> {
            apiServiceConnection.testConnection();
        });

        HorizontalLayout apiAddressLayout = new HorizontalLayout(apiAddressField, testConnectionButton);
        apiAddressLayout.setWidthFull();
        apiAddressLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Div apiAddressDiv = new Div(new H3("AI Chat - API Address Configuration"), apiAddressLayout);
        apiAddressDiv.addClassName("block-container");
        apiAddressDiv.setWidth("95%");
        apiAddressDiv.setMaxWidth("1200px");
        apiAddressDiv.getStyle().set("margin", "0 auto");

        contentLayout.add(apiAddressDiv); // Add at the beginning
    }

    private void loadEndpoints() {
        var ui = UI.getCurrent();
        apiServiceConnection.callListEndpoints()
                .subscribe(
                        answer -> {
                            logger.info("Getting list of endpoints...");
                            answer.getEndpoints().forEach(endpoint -> endpoints.add(new EndpointRecord(endpoint.getEndpoint(),endpoint.getDescription())));
                        },
                        error -> {
                            logger.info("Getting list of endpoints error..." + error.getMessage());
                        }
                );
    }

//    private void configureScrapWebsiteBlock() {
//        if (appConfig.getScrapWebSites() != null) {
//            scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());
//        }
//        scrapWebsiteGrid.setColumns("url", "description");
//        scrapWebsiteGrid.setWidthFull();
//        scrapWebsiteGrid.setMinHeight("5%");
//
//        Map<ScrapWebSite, Checkbox> checkboxMap = new HashMap<>();
//
//        Grid.Column<ScrapWebSite> checkboxColumn = scrapWebsiteGrid.addColumn(new ComponentRenderer<>(item -> {
//            Checkbox checkbox = new Checkbox();
//            checkboxMap.put(item, checkbox);
//            return checkbox;
//        })).setHeader("Select");
//
//        Button selectAllButton = new Button("Select");
//        selectAllButton.addClickListener(event -> {
//            boolean anyUnchecked = checkboxMap.values().stream().anyMatch(checkbox -> !checkbox.getValue());
//            checkboxMap.values().forEach(checkbox -> checkbox.setValue(anyUnchecked));
//            selectAllButton.setText((selectAllButton.getText().equals("Select"))? "Deselect" : "Select");
//        });
//
//        var ui = UI.getCurrent();
//        Button getSelectedButton = new Button("Run");
//        getSelectedButton.addClickListener(event -> {
//            List<String> selectedUrls = new ArrayList<>();
//            checkboxMap.forEach((item, checkbox) -> {
//                if (checkbox.getValue()) {
//                    selectedUrls.add(item.getUrl());
//                }
//
//                if (!selectedUrls.isEmpty()) {
//                    logger.info("URLs: " + selectedUrls.toString());
//
//                    getSelectedButton.setText("Running");
//                    getSelectedButton.setEnabled(false);
//
//                    WebScrapingRequest scraping = new WebScrapingRequest();
//                    scraping.setUrls(selectedUrls);
//                    apiServiceConnection.callWebScrapingAPI(scraping)
//                            .subscribe(
//                                    answer -> {
//                                        ui.access(() ->
//                                                NotificationUtils.showSuccessNotification(answer.toString()));
//                                    },
//                                    error -> {
//                                        ui.access(() ->
//                                                NotificationUtils.showErrorNotification(error.getMessage()));
//
//                                    }
//                            );
//                    getSelectedButton.setText("Run");
//                    getSelectedButton.setEnabled(true);
//                }
//            });
//        });
//        ui.push();
//
//        HorizontalLayout headerActions = new HorizontalLayout(selectAllButton, getSelectedButton);
//        scrapWebsiteGrid.getHeaderRows().getFirst().getCell(checkboxColumn).setComponent(headerActions);
//
//        scrapWebsiteGrid.addComponentColumn(item -> {
//            HorizontalLayout actionsLayout = new HorizontalLayout();
//
//            Button deleteButton = new Button("Delete");
//            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
//            deleteButton.addClickListener(event -> {
//                Dialog dialog = new Dialog();
//                dialog.setHeaderTitle("Confirm Deletion");
//                dialog.add(String.format("Are you sure you want to delete the item [%s]?", item.getUrl()));
//
//                Button confirmButton = new Button("Confirm", confirmEvent -> {
//                    appConfig.getScrapWebSites().remove(item);
//                    scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());
//                    dialog.close();
//                });
//                confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
//
//                Button cancelButton = new Button("Cancel", cancelEvent -> dialog.close());
//
//                dialog.getFooter().add(confirmButton, cancelButton);
//                dialog.open();
//            });
//
//            Button editButton = new Button("Edit");
//            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
//            editButton.addClickListener(event -> {
//                Dialog dialog = new Dialog();
//                dialog.setHeaderTitle("Edit Item");
//
//                TextField urlEditorField = new TextField("URL");
//                urlEditorField.setValue(item.getUrl());
//                TextField descriptionEditorField = new TextField("Description");
//                descriptionEditorField.setValue(item.getDescription());
//
//                VerticalLayout v1 = new VerticalLayout(urlEditorField);
//                VerticalLayout v2 = new VerticalLayout(descriptionEditorField);
//
//                dialog.add(v1, v2);
//
//                Button saveButton = new Button("Save", saveEvent -> {
//                    item.setUrl(urlEditorField.getValue());
//                    item.setDescription(descriptionEditorField.getValue());
//                    scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());
//                    dialog.close();
//                });
//                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//                Button cancelButton = new Button("Cancel", cancelEvent -> dialog.close());
//
//                dialog.getFooter().add(saveButton, cancelButton);
//                dialog.open();
//            });
//
//            actionsLayout.add(editButton, deleteButton);
//            return actionsLayout;
//        }).setHeader("Actions");
//
//        urlField.setWidthFull();
//        urlField.setRequired(true);
//
//        descriptionField.setWidthFull();
//
//        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        addButton.addClickListener(event -> {
//            ScrapWebSite website = ScrapWebSite.builder()
//                    .url(urlField.getValue())
//                    .description(descriptionField.getValue())
//                    .build();
//
//            if (appConfig.getScrapWebSites() == null) {
//                appConfig.setScrapWebSites(new ArrayList<>());
//            }
//            appConfig.getScrapWebSites().add(website);
//            scrapWebsiteGrid.setItems(appConfig.getScrapWebSites());
//
//            urlField.clear();
//            descriptionField.clear();
//        });
//
//        HorizontalLayout inputLayout = new HorizontalLayout(urlField, descriptionField, addButton);
//        inputLayout.setWidthFull();
//        inputLayout.setFlexGrow(1, urlField, descriptionField);
//        inputLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
//
//        Div scrapWebsiteDiv = new Div(new H3("AI Chat - Web scraping"), inputLayout, scrapWebsiteGrid);
//        scrapWebsiteDiv.addClassName("block-container");
//        scrapWebsiteDiv.setWidth("80%");
//        scrapWebsiteDiv.setMaxWidth("1200px");
//        scrapWebsiteDiv.getStyle().set("margin", "0 auto");
//
//        scrapWebsiteGrid.getColumnByKey("url").setAutoWidth(true);
//        scrapWebsiteGrid.getColumnByKey("description").setAutoWidth(true);
//
//        contentLayout.add(scrapWebsiteDiv);
//    }

    private void configureServiceNameBlock() {
        if (appConfig.getServiceNames() != null) {
            serviceNameGrid.setItems(appConfig.getServiceNames());
        }
        serviceNameGrid.setColumns("name", "description");
        serviceNameGrid.addColumn(ServiceName::getType).setHeader("Service Type");
        serviceNameGrid.setWidthFull();

        serviceNameGrid.addComponentColumn(item -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Confirm Deletion");
                dialog.add(String.format("Are you sure you want to delete the item [%s]?", item.getName()));

                Button confirmButton = new Button("Confirm", confirmEvent -> {
                    appConfig = appConfigService.getAppConfig();
                    appConfig.getServiceNames().remove(item);

                    appConfigService.save(appConfig);
                    serviceNameGrid.setItems(appConfig.getServiceNames());
                    dialog.close();
                });
                confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

                Button cancelButton = new Button("Cancel", cancelEvent -> dialog.close());

                dialog.getFooter().add(confirmButton, cancelButton);
                dialog.open();
            });

            Button editButton = new Button("Edit");
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editButton.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Edit Item");

                TextField nameEditorField = new TextField("Name");
                nameEditorField.setValue(item.getName());

                TextField descriptionEditorField = new TextField("Description");
                descriptionEditorField.setValue(item.getDescription());

                ComboBox<APIServiceType> serviceTypeComboBoxField = new ComboBox<>("Service Type");
                serviceTypeComboBoxField.setItems(APIServiceType.values());
                serviceTypeComboBoxField.setValue(item.getType());

                VerticalLayout v1 = new VerticalLayout(nameEditorField);
                VerticalLayout v2 = new VerticalLayout(descriptionEditorField);
                VerticalLayout v3 = new VerticalLayout(serviceTypeComboBoxField);

                dialog.add(v1, v2, v3);

                Button saveButton = new Button("Save", saveEvent -> {
                    item.setName(nameEditorField.getValue());
                    item.setDescription(descriptionEditorField.getValue());
                    item.setType(serviceTypeComboBoxField.getValue());
                    serviceNameGrid.setItems(appConfig.getServiceNames());
                    appConfigService.save(appConfig);
                    dialog.close();
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelButton = new Button("Cancel", cancelEvent -> dialog.close());

                dialog.getFooter().add(saveButton, cancelButton);
                dialog.open();
            });

            //actionsLayout.add(editButton, deleteButton);
            actionsLayout.add(deleteButton);
            return actionsLayout;
        }).setHeader("Actions");

        serviceNameField.setWidthFull();
        serviceNameField.setRequired(true);
        serviceDescriptionField.setWidthFull();

        serviceNameField.setAllowCustomValue(true);
        serviceNameField.setClearButtonVisible(true);
        serviceDescriptionField.setClearButtonVisible(true);

        serviceTypeComboBox.setRequired(true);

        serviceNameField.setItems(endpoints);
        serviceNameField.addValueChangeListener(value -> {
            EndpointRecord selectedRecord = value.getValue();
            if (selectedRecord != null) {
                // Defensive check (shouldn't be necessary if 'if' works)
                String description = selectedRecord.description();
                if (description != null) { // Check if description itself could be null?
                    serviceDescriptionField.setValue(description);
                } else {
                    serviceDescriptionField.setValue(""); // Handle null description
                }
            } else {
                serviceDescriptionField.setValue("");
            }
        });

        addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addServiceButton.addClickListener(event -> {
            if (serviceNameField.getValue() != null) {
                ServiceName service = ServiceName.builder()
                        .name(serviceNameField.getValue().endpoint())
                        .description(serviceDescriptionField.getValue())
                        .type(serviceTypeComboBox.getValue()) // Default enum value
                        .build();

                if (appConfig.getServiceNames() == null) {
                    appConfig.setServiceNames(new ArrayList<>());
                }
                appConfig.getServiceNames().add(service);
                serviceNameGrid.setItems(appConfig.getServiceNames());
                appConfigService.save(appConfig);
                serviceNameField.clear();
                serviceDescriptionField.clear();
                serviceTypeComboBox.clear();
            }
        });

        HorizontalLayout serviceInputLayout = new HorizontalLayout(serviceNameField, serviceDescriptionField, serviceTypeComboBox, addServiceButton);
        serviceInputLayout.setWidthFull();
        serviceInputLayout.setFlexGrow(1, serviceNameField, serviceDescriptionField);
        serviceInputLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Div serviceNameDiv = new Div(new H3("AI Chat - API Service Names"), serviceInputLayout, serviceNameGrid);
        serviceNameDiv.addClassName("block-container");
        serviceNameDiv.setWidth("95%");
        serviceNameDiv.setMaxWidth("1200px");
        serviceNameDiv.getStyle().set("margin", "0 auto");

        contentLayout.add(serviceNameDiv);
    }

//    private void configureSaveButton() {
//        saveButton.addClickListener(event -> {
//            appConfig.setApiAddress(apiAddressField.getValue());
//            appConfigService.save(appConfig);
//            NotificationUtils.showSuccessNotification("Configurations saved.");
//        });
//    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "N/A";
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    private void configureLastUpdateLabels() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String BTN_DEFAULT_TEXT = "Update AI Knowledge data";
        String BTN_UPDATING_TEXT = "Updating AI Knowledge data...";
        String BTN_CLEARING_TEXT = "Clearing and Updating AI Knowledge data...";


        Text knowledgeDatabaseLabel = new Text("Knowledge Database: ");
        Text knowledgeDatabaseValueLabel = new Text(
               formatInstant(appConfig.getKnowledgeDatabaseLastUpdate()));

        Text ragDocumentsLabel = new Text("AI Knowledge: ");
        Text ragDocumentsValueLabel = new Text(formatInstant(appConfig.getAiRagDocumentsLastUpdate()));
        Span alert = new Span();
        alert.add(ragDocumentsValueLabel);

        if (appConfig.getKnowledgeDatabaseLastUpdate().isAfter(appConfig.getAiRagDocumentsLastUpdate())) {
            alert.getStyle().setColor("red");
        }

        HorizontalLayout knowledgeLayout = new HorizontalLayout(knowledgeDatabaseLabel, knowledgeDatabaseValueLabel);
        HorizontalLayout ragLayout = new HorizontalLayout(ragDocumentsLabel, alert);

        VerticalLayout lastUpdateLayout = new VerticalLayout(knowledgeLayout, ragLayout);
        lastUpdateLayout.setWidthFull();
        lastUpdateLayout.setAlignItems(FlexComponent.Alignment.END);

        // Update button
        Button btnSaveRagData = new Button(BTN_DEFAULT_TEXT);

        // Confirm clear and update dialog
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Update Strategy");
        dialog.setText("Clear existing AI knowledge data before adding new ones?");
        dialog.setConfirmText("Clear and Update");

        // Add a Clear and Update button
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            var ui = getUI().get();
            String allData = ragService.generateStringData();
            logger.info(allData);

            TextToRagStoreRequest textToRagStoreRequest = new TextToRagStoreRequest();
            textToRagStoreRequest.setText(allData);
            textToRagStoreRequest.setChunk_separator("###CHUNK###");

            btnSaveRagData.setEnabled(false);
            btnSaveRagData.setText(BTN_CLEARING_TEXT);

            // Chain the API calls: Clear first, then Update
            apiServiceConnection.callClearVectorStore()
                    .doOnSubscribe(s -> ui.access(() -> {
                        // Already set to clearing, could refine if needed
                        logger.info("Clear operation subscribed.");
                    }))
                    .flatMap(clearResponse -> {
                        // Clear succeeded, now proceed to update
                        logger.info("Clear API call successful: {}", clearResponse);
                        // Update button text for the next stage
                        ui.access(() -> {
                            btnSaveRagData.setText(BTN_UPDATING_TEXT);
                            //NotificationUtils.showSuccessNotification("Existing data cleared. Updating knowledge...");
                            ui.push();
                        });
                        return apiServiceConnection.callTextToRAGAndStore(textToRagStoreRequest);
                    })
                    .doFinally(signalType -> ui.access(() -> { // Re-enable button regardless of outcome
                        logger.info("Clear and Update sequence finished (Signal: {}). Re-enabling button.", signalType);
                        btnSaveRagData.setEnabled(true);
                        btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                        ui.push();
                    }))
                    .subscribe( // Handle final success or error
                            updateAnswer -> {
                                // Both clear and update succeeded
                                ui.access(() -> {
                                    logger.info("Clear and Update successful: {}", updateAnswer);
                                    appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                                    appConfigService.save(appConfig);
                                    ragDocumentsValueLabel.setText(formatInstant(appConfig.getAiRagDocumentsLastUpdate()));
                                    NotificationUtils.showSuccessNotification("AI knowledge cleared and updated successfully!");
                                    ui.push();
                                });
                            },
                            error -> {
                                // An error occurred during either clear OR update
                                ui.access(() -> {
                                    logger.error("Error during Clear and Update process: {}", error.getMessage(), error);
                                    NotificationUtils.showErrorNotification("Error during clear/update: " + error.getMessage());
                                    ui.push();
                                });
                            }
                    );
        });

        // Add a Update Only button
        dialog.setRejectable(true); // Allow rejecting
        dialog.setRejectText("Update Only");
        dialog.addRejectListener(event -> {
            var ui = getUI();
            String allData = ragService.generateStringData();
            logger.info(allData);
            TextToRagStoreRequest textToRagStoreRequest = new TextToRagStoreRequest();
            textToRagStoreRequest.setText(allData);
            textToRagStoreRequest.setChunk_separator("###CHUNK###");

            btnSaveRagData.setEnabled(false);
            btnSaveRagData.setText(BTN_UPDATING_TEXT);

            apiServiceConnection.callTextToRAGAndStore(textToRagStoreRequest)
                    .subscribe(
                            answer -> {
                                ui.get().access(() -> {
                                    logger.info("AI knowledge updating...");

                                    appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                                    appConfigService.save(appConfig);
                                    NotificationUtils.showSuccessNotification("AI knowledge updated.");
                                    btnSaveRagData.setEnabled(true);
                                    btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                                    ui.get().push();
                                });
                            },
                            error -> {
                                ui.get().access(() -> {
                                    logger.info("AI knowledge error..." + error.getMessage());

                                    NotificationUtils.showErrorNotification(error.getMessage());
                                    btnSaveRagData.setEnabled(true);
                                    btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                                    ui.get().push();
                                });

                            }
                    );
        });

        // Add a cancel button
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.addCancelListener(event -> logger.info("User cancelled update operation."));

        btnSaveRagData.setWidthFull();
        btnSaveRagData.addClickListener(click -> {
            dialog.open();
        });

        Div div = new Div(new H3("Last update Knowledge Database and AI Knowledge"), lastUpdateLayout, btnSaveRagData);
        div.addClassName("block-container");
        div.setWidth("95%");
        div.setMaxWidth("1200px");
        div.getStyle().set("margin", "0 auto");

        contentLayout.add(div);
    }

    private void configureContentLayout() {
        contentLayout.setWidthFull();
        contentLayout.getStyle().setPadding("0");
        contentLayout.getStyle().setMargin("0");
        contentLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().add(contentLayout);
        getContent().setSizeFull();
    }
}