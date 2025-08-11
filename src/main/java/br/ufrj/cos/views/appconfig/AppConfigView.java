package br.ufrj.cos.views.appconfig;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.api.TextToRagStoreRequest;
import br.ufrj.cos.components.taskscheduler.TaskConfig;
import br.ufrj.cos.components.taskscheduler.TaskRegister;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.service.TaskScheduleConfigService;
import br.ufrj.cos.tasks.RAGDataUpdate;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.EndpointRecord;
import br.ufrj.cos.views.record.PeriodRecord;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.Html;
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
public class AppConfigView extends BaseView implements HasTour {
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
    private final Button apiAddressSaveButton = new Button("Save");
    private final RAGService ragService;
    private TaskScheduleConfig taskScheduleConfig = null;
    private Text statusTxt;
    private Button taskButton;
    private TaskRegister taskRegister;
    private RAGDataUpdate ragDataUpdate;
    private TaskScheduleConfigService taskScheduleConfigService;
    private ComboBox<PeriodRecord> periodCombo;
    private ComboBox<TaskScheduleConfig> taskNamesCombo;

    private HorizontalLayout apiAddressLayout;

    private final APIServiceConnection apiServiceConnection;
    private final List<EndpointRecord> endpoints;

    public AppConfigView(AppConfigService appConfigService, RAGService ragService, TaskRegister taskRegister, RAGDataUpdate ragDataUpdate, TaskScheduleConfigService taskScheduleConfigService, APIServiceConnection apiServiceConnection, List<EndpointRecord> endpoints) {
        this.appConfig = appConfigService.getAppConfig();
        this.appConfigService = appConfigService;
        this.ragService = ragService;
        this.taskRegister = taskRegister;
        this.ragDataUpdate = ragDataUpdate;
        this.taskScheduleConfigService = taskScheduleConfigService;
        this.apiServiceConnection = apiServiceConnection;
        this.endpoints = endpoints;
        this.createHeader("Application Config");
        loadEndpoints();
        configureApiAddressBlock();
        //configureScrapWebsiteBlock();
        configureServiceNameBlock();
        //configureSaveButton();
        configureLastUpdateLabels(); // Add this line
        configureContentLayout();
    }

    private void configureApiAddressBlock() {
        if (appConfig.getApiAddress() != null) {
            apiAddressField.setValue(appConfig.getApiAddress());
        }
        apiAddressField.setWidthFull();
        apiAddressField.setRequired(true);
        apiAddressField.setPlaceholder("XXX.XXX.XXX.XXX:0000");

        testConnectionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        testConnectionButton.addClickListener(event -> {
            apiServiceConnection.testConnection(apiAddressField.getValue());
            logger.info("Testing connection: " + apiAddressField.getValue());

        });

        apiAddressSaveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apiAddressSaveButton.addClickListener(click -> {
            if (apiAddressField.getValue() != null) {
                appConfig.setApiAddress(apiAddressField.getValue());
                appConfigService.save(appConfig);
                NotificationUtils.showSuccessNotification("API address saved.");
            }
        });

        apiAddressLayout = new HorizontalLayout(apiAddressField, testConnectionButton, apiAddressSaveButton);
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
                            logger.info("Getting list of endpoints... done. Total: " + endpoints.size());
                            loadComboEndpoints();
                        },
                        error -> {
                            logger.info("Getting list of endpoints error..." + error.getMessage());
                        }
                );
    }

    private void configureServiceNameBlock() {
        if (appConfig.getServiceNames() != null) {
            serviceNameGrid.setItems(appConfig.getServiceNames());
        }
        serviceNameGrid.setColumns("name", "description");
        serviceNameGrid.addColumn(ServiceName::getType).setHeader("Service Type");
        serviceNameGrid.setWidthFull();

        serviceNameGrid.addComponentColumn(item -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            // Delete service name
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

                // Cancel deletion service name
                Button cancelButton = new Button("Cancel", cancelEvent -> dialog.close());

                dialog.getFooter().add(confirmButton, cancelButton);
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

        loadComboEndpoints();

        addServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addServiceButton.addClickListener(event -> {
            if (serviceNameField.getValue() != null) {
                ServiceName service = ServiceName.builder()
                        .name(serviceNameField.getValue().endpoint())
                        .description(serviceDescriptionField.getValue())
                        .type(serviceTypeComboBox.getValue())
                        .appConfig(appConfig)
                        .build();

                if (appConfig.getServiceNames() == null) {
                    appConfig.setServiceNames(new ArrayList<>());
                }
                appConfig.getServiceNames().add(service);
                appConfigService.saveServiceName(service);
                appConfig = appConfigService.getAppConfig();
                serviceNameGrid.getDataProvider().refreshAll();

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

    /***
     * The endpoints names are loaded from the API service
     */
    private void loadComboEndpoints() {
        if (!endpoints.isEmpty()) {
            serviceNameField.setItems(endpoints);
        }
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
                appConfig.getKnowledgeDatabaseLastUpdate() != null? formatInstant(appConfig.getKnowledgeDatabaseLastUpdate()) : "-");

        Text ragDocumentsLabel = new Text("AI Knowledge: ");
        Text ragDocumentsValueLabel = new Text(
                appConfig.getAiRagDocumentsLastUpdate() != null? formatInstant(appConfig.getAiRagDocumentsLastUpdate()) : "-");
        Span alert = new Span();
        alert.add(ragDocumentsValueLabel);

        if ((appConfig.getKnowledgeDatabaseLastUpdate() != null && appConfig.getAiRagDocumentsLastUpdate() != null) &&
                appConfig.getKnowledgeDatabaseLastUpdate().isAfter(appConfig.getAiRagDocumentsLastUpdate())) {
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

        // Add an Update Only button
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



        Div div = new Div(new H3("Last update Knowledge Database and AI Knowledge"), lastUpdateLayout, this.createTaskConfigLayout(), btnSaveRagData);
        div.addClassName("block-container");
        div.setWidth("95%");
        div.setMaxWidth("1200px");
        div.getStyle().set("margin", "0 auto");

        contentLayout.add(div);
    }

    private HorizontalLayout createTaskConfigLayout() {
        // 1. Create all UI components ONCE and assign them to member fields
        taskNamesCombo = new ComboBox<>("Service Name");
        periodCombo = new ComboBox<>("Execution Period");
        statusTxt = new Text("-"); // Already a field
        taskButton = new Button("Run"); // Already a field

        // 2. Configure the components
        configureTaskNamesCombo();
        configurePeriodCombo();
        taskButton.setEnabled(false); // Initially disabled

        // 3. Add event listeners that call dedicated handler methods
        taskNamesCombo.addValueChangeListener(event -> onTaskSelectionChange(event.getValue()));
        periodCombo.addValueChangeListener(event -> updateButtonState());
        taskButton.addClickListener(event -> onTaskActionButtonClick());

        // 4. Assemble the layout
        HorizontalLayout taskConfigLayout = new HorizontalLayout(taskNamesCombo, periodCombo, statusTxt, taskButton);
        taskConfigLayout.setWidth("98%");
        taskConfigLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        taskNamesCombo.setWidth("50%");
        periodCombo.setWidth("30%");

        return taskConfigLayout;
    }

    private void configureTaskNamesCombo() {
        List<TaskScheduleConfig> taskConfigs = this.appConfigService.getTaskConfigs();
        taskNamesCombo.setItems(taskConfigs);
        taskNamesCombo.setItemLabelGenerator(TaskScheduleConfig::getTaskName);
    }

    private void configurePeriodCombo() {
        List<PeriodRecord> periodOptions = List.of(
                new PeriodRecord("10 sec", 10000L),
                new PeriodRecord("30 sec", 30000L),
                new PeriodRecord("1 min", 60000L),
                new PeriodRecord("10 min", 600000L),
                new PeriodRecord("30 min", 1800000L),
                new PeriodRecord("1 hour", 3600000L)
        );
        periodCombo.setItems(periodOptions);
        periodCombo.setItemLabelGenerator(PeriodRecord::label);
    }

    // --- Event Handler and UI Update Logic ---

    private void onTaskSelectionChange(TaskScheduleConfig selectedConfig) {
        this.taskScheduleConfig = selectedConfig; // Update the view's state
        updateUiState();
    }

    private void onTaskActionButtonClick() {
        if (this.taskScheduleConfig == null) return;

        boolean isCurrentlyActive = this.taskScheduleConfig.isActive();

        if (isCurrentlyActive) {
            // --- ACTION: STOP THE TASK ---
            logger.info("Stopping Task...");
            this.taskScheduleConfig.setActive(false);
            this.taskScheduleConfigService.save(this.taskScheduleConfig); // Persist the change
            this.taskRegister.cancelTask(this.taskScheduleConfig.getTaskName());
            NotificationUtils.showSuccessNotification("Task '" + this.taskScheduleConfig.getTaskName() + "' stopped successfully.");
        } else {
            // --- ACTION: START THE TASK ---
            if (periodCombo.getValue() == null) {
                NotificationUtils.showErrorNotification("Please select an execution period.");
                return;
            }
            logger.info("Starting Task...");

            // Update the config object with the new period from the UI
            this.taskScheduleConfig.setActive(true);
            this.taskScheduleConfig.setFixedRateMilliseconds(periodCombo.getValue().milliseconds());
            this.taskScheduleConfigService.save(this.taskScheduleConfig); // Persist the change

            // Create a config DTO for the scheduler
            TaskConfig runConfig = TaskConfig.builder()
                    .taskName(this.taskScheduleConfig.getTaskName())
                    .fixedRateMilliseconds(this.taskScheduleConfig.getFixedRateMilliseconds())
                    .initialDelayMilliseconds(0L) // Start immediately for manual trigger
                    .active(true)
                    .build();

            this.taskRegister.scheduleTask(ragDataUpdate, runConfig); // Use the generic scheduler
            NotificationUtils.showSuccessNotification("Task '" + runConfig.getTaskName() + "' started successfully.");
        }

        // Refresh the UI to reflect the new state
        updateUiState();
    }

    /**
     * A single, central method to update the UI based on the current state of 'taskScheduleConfig'.
     */
    private void updateUiState() {
        if (this.taskScheduleConfig == null) {
            // Reset to initial state if nothing is selected
            statusTxt.setText("-");
            statusTxt.getStyle().clear(); // Remove any custom color
            taskButton.setText("Run");
            periodCombo.clear();
            periodCombo.setEnabled(false);
            taskButton.setEnabled(false);
        } else {
            boolean isActive = this.taskScheduleConfig.isActive();

            // *** THE FIX IS HERE ***
            // Update the properties of the EXISTING statusTxt component.
            if (isActive) {
                statusTxt.setText("Running"); // Use .set("property", "value")
            } else {
                statusTxt.setText("Stopped");
            }

            taskButton.setText(isActive ? "Stop" : "Run");
            periodCombo.setEnabled(!isActive); // Can only change period when stopped

            // Automatically select the period currently stored in the database
            findAndSelectPeriod(this.taskScheduleConfig.getFixedRateMilliseconds());

            updateButtonState();
        }
    }

    /**
     * Enables or disables the main action button based on the current selections.
     */
    private void updateButtonState() {
        if (this.taskScheduleConfig == null) {
            taskButton.setEnabled(false);
        } else {
            boolean isActive = this.taskScheduleConfig.isActive();
            // Button is enabled if the task is active (to stop it) OR if a period is selected (to run it).
            taskButton.setEnabled(isActive || periodCombo.getValue() != null);
        }
    }

    private void findAndSelectPeriod(Long milliseconds) {
        // Helper to find and select the matching PeriodRecord in the combo box
        periodCombo.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                .filter(p -> p.milliseconds().equals(milliseconds))
                .findFirst()
                .ifPresent(periodCombo::setValue);
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

    @Override
    public Onboarding createTour() {
        return new TourUtils().build()
                .addStep(apiAddressLayout,
                        "AI Chat API endpoint address",
                        new Html("<p>This is the component that register the API address of the AI endpoint.</br>Enter here the correct address and check if it is valid.</p>"),
                        PopupPosition.BOTTOM)
                .addStep(serviceNameField,
                        "AI Chat Services - Service Name selection",
                        new Html("<p>Select the name of the API endpoint that will be invoked.</p>"),
                        PopupPosition.END,
                        Optional.of(l -> serviceNameField.setOpened(true)))
                .addStep(serviceDescriptionField,
                        "AI Chat Services - Service Description",
                        new Html("<p>Enter a description for the service that will be invoked.</p>"),
                        PopupPosition.END,
                        Optional.of(l -> serviceNameField.setOpened(false)))
                .addStep(serviceTypeComboBox,
                        "AI Chat Services - Service Type selection",
                        new Html("<p>Select the type of the service that will handler the response in the application.</p>"),
                        PopupPosition.END,
                        Optional.of(l -> serviceTypeComboBox.setOpened(true)))
                .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}