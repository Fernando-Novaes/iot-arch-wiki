package br.ufrj.cos.views.appconfig;

import br.ufrj.cos.api.APIServiceConnection;
import br.ufrj.cos.components.taskscheduler.TaskConfig;
import br.ufrj.cos.components.taskscheduler.TaskRegister;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.AppConfigService;
import br.ufrj.cos.service.DocumentIngestionService;
import br.ufrj.cos.service.RAGService;
import br.ufrj.cos.service.TaskScheduleConfigService;
import br.ufrj.cos.service.ai.AiRagService;
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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.vaadin.flow.component.checkbox.Checkbox;
import br.ufrj.cos.utils.SecurityUtils;

@PageTitle("Application Config")
@Route(value = "appconfig-view", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
@CssImport("./styles/app-styles.css")
public class AppConfigView extends BaseView implements HasTour {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigView.class);

    private AppConfig appConfig;
    private final Grid<ServiceName> serviceNameGrid = new Grid<>(ServiceName.class);
    private final ComboBox<EndpointRecord> serviceNameField = new ComboBox<>("Service Name");
    private final TextField serviceDescriptionField = new TextField("Service Description");
    private final ComboBox<APIServiceType> serviceTypeComboBox = new ComboBox<>("Service Type", APIServiceType.values());
    private final Button addServiceButton = new Button("Add");
    private final AppConfigService appConfigService;
    private final FlexLayout contentLayout = new FlexLayout();
    private final TextField apiAddressField = new TextField("API Address");
    private final Button testConnectionButton = new Button("Test Connection");
    private final Button apiAddressSaveButton = new Button("Save Address");

    private final RAGService ragService;
    private final AiRagService aiRagService;
    private TaskScheduleConfig taskScheduleConfig = null;
    private Span statusSpan;
    private Button taskButton;
    private TaskRegister taskRegister;
    private RAGDataUpdate ragDataUpdate;
    private TaskScheduleConfigService taskScheduleConfigService;
    private ComboBox<PeriodRecord> periodCombo;
    private ComboBox<TaskScheduleConfig> taskNamesCombo;

    private HorizontalLayout apiAddressLayout;

    private final APIServiceConnection apiServiceConnection;
    private final List<EndpointRecord> endpoints;

    private final PasswordField googleApiKeyField = new PasswordField("Google Gemini API Key");
    private final Button googleApiKeySaveButton = new Button("Save Key");
    private final Button googleApiKeyNewButton = new Button("New");
    private final Button googleApiKeyCancelButton = new Button("Cancel");
    private final DocumentIngestionService documentIngestionService;
    private final br.ufrj.cos.service.UserApplicationService userApplicationService;

    public AppConfigView(AppConfigService appConfigService, RAGService ragService, TaskRegister taskRegister, RAGDataUpdate ragDataUpdate, TaskScheduleConfigService taskScheduleConfigService, APIServiceConnection apiServiceConnection, List<EndpointRecord> endpoints, AiRagService aiRagService, DocumentIngestionService documentIngestionService, br.ufrj.cos.service.UserApplicationService userApplicationService) {
        this.appConfig = appConfigService.getAppConfig();
        this.appConfigService = appConfigService;
        this.ragService = ragService;
        this.aiRagService = aiRagService;
        this.documentIngestionService = documentIngestionService;
        this.userApplicationService = userApplicationService;
        this.taskRegister = taskRegister;
        this.ragDataUpdate = ragDataUpdate;
        this.taskScheduleConfigService = taskScheduleConfigService;
        this.apiServiceConnection = apiServiceConnection;
        this.endpoints = endpoints;

        boolean isAdmin = SecurityUtils.hasRole("ADMIN");
        configureHeroBanner(isAdmin);
        configureAiContextSettingsBlock();

        if (isAdmin) {
            configureGoogleApiKeyBlock();
            configureLastUpdateLabels();
        }
        configureContentLayout();
    }

    private void configureHeroBanner(boolean isAdmin) {
        Div heroCard = new Div();
        heroCard.addClassName("appconfig-hero-card");
        heroCard.setWidth("95%");
        heroCard.setMaxWidth("1200px");
        heroCard.getStyle()
                .set("margin", "0 auto 1.5rem auto")
                .set("box-sizing", "border-box");

        Span badge = new Span(isAdmin ? "Admin Control Center" : "User Preferences");
        badge.addClassName("appconfig-badge");

        H3 heroTitle = new H3(isAdmin ? "⚙️ Application Configuration & System Governance" : "⚙️ Application Configuration");
        heroTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "1.5rem")
                .set("font-weight", "800")
                .set("color", "white");

        Paragraph heroSubtitle = new Paragraph(isAdmin
                ? "Manage core API endpoints, Google Gemini LLM credentials, service routing, and vector knowledge base synchronization."
                : "Manage AI Assistant knowledge context scope and governance preferences.");
        heroSubtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9rem")
                .set("color", "rgba(255, 255, 255, 0.85)");

        VerticalLayout heroContent = new VerticalLayout(badge, heroTitle, heroSubtitle);
        heroContent.setPadding(false);
        heroContent.setSpacing(false);
        heroCard.add(heroContent);

        contentLayout.add(heroCard);
    }

    private void configureAiContextSettingsBlock() {
        boolean isAdmin = SecurityUtils.hasRole("ADMIN");
        boolean allowUserOverride = appConfig.getAllowUserContextOverride() == null || appConfig.getAllowUserContextOverride();

        Checkbox allowExternalCheckbox = new Checkbox("Allow AI to consult general IoT knowledge beyond application context");
        Checkbox allowUserOverrideCheckbox = new Checkbox("Allow individual users to override this AI context setting in their preferences");
        
        Paragraph lockNotice = new Paragraph();
        lockNotice.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("color", "var(--lumo-error-color, #ef4444)")
                .set("font-size", "0.85rem")
                .set("font-weight", "600");
        lockNotice.setVisible(false);

        if (isAdmin) {
            allowExternalCheckbox.setValue(appConfig.getAllowExternalContext() == null || appConfig.getAllowExternalContext());
            allowExternalCheckbox.getStyle().set("font-weight", "600");

            allowUserOverrideCheckbox.setValue(allowUserOverride);
            allowUserOverrideCheckbox.getStyle().set("font-weight", "600");
        } else {
            String username = SecurityUtils.getUsername();
            UserApplication userApp = username != null ? userApplicationService.findByUserName(username) : null;
            
            if (allowUserOverride) {
                boolean userVal = (userApp != null && userApp.getAllowExternalContext() != null) 
                        ? userApp.getAllowExternalContext() 
                        : (appConfig.getAllowExternalContext() == null || appConfig.getAllowExternalContext());
                allowExternalCheckbox.setValue(userVal);
                allowExternalCheckbox.setEnabled(true);
            } else {
                allowExternalCheckbox.setValue(appConfig.getAllowExternalContext() == null || appConfig.getAllowExternalContext());
                allowExternalCheckbox.setEnabled(false);
                lockNotice.setText("🔒 The administrator has enforced a system-wide AI context policy. Individual user overrides are currently disabled.");
                lockNotice.setVisible(true);
            }
        }

        Paragraph desc = new Paragraph("Configure the context constraint level for AI Assistant (Gemini) queries:");
        desc.getStyle().set("margin", "0 0 0.5rem 0").set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        UnorderedList list = new UnorderedList();
        list.getStyle().set("margin", "0 0 1rem 0").set("padding-left", "1.2rem").set("font-size", "0.8rem").set("color", "var(--lumo-secondary-text-color)");
        list.add(new ListItem(new Html("<span><strong>Checked (Hybrid / Open):</strong> The AI uses the application's internal knowledge base and uploaded reference documents (PDFs/ISO standards) as primary sources, but MAY supplement responses with general external web/internet IoT architectural knowledge when needed.</span>")));
        list.add(new ListItem(new Html("<span><strong>Unchecked (Strict / Offline):</strong> The AI operates in strict offline mode, responding EXCLUSIVELY based on the application's internal knowledge base and uploaded reference documents (PDFs/ISO standards), strictly prohibiting any external internet or ungrounded web lookup.</span>")));

        Button saveBtn = new Button("Save Preference");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (!isAdmin && !allowUserOverride) {
            saveBtn.setEnabled(false);
        }

        saveBtn.addClickListener(click -> {
            if (isAdmin) {
                appConfig.setAllowExternalContext(allowExternalCheckbox.getValue());
                appConfig.setAllowUserContextOverride(allowUserOverrideCheckbox.getValue());
                appConfigService.save(appConfig);
                NotificationUtils.showSuccessNotification("AI Context governance master policy saved successfully.");
            } else {
                String username = SecurityUtils.getUsername();
                if (username != null) {
                    UserApplication userApp = userApplicationService.findByUserName(username);
                    if (userApp != null) {
                        userApp.setAllowExternalContext(allowExternalCheckbox.getValue());
                        userApplicationService.saveAndUpdate(userApp);
                        NotificationUtils.showSuccessNotification("Your personal AI Context preference saved successfully.");
                    }
                }
            }
        });

        HorizontalLayout btnLayout = new HorizontalLayout(saveBtn);
        btnLayout.setWidthFull();
        btnLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout boxLayout;
        if (isAdmin) {
            boxLayout = new VerticalLayout(allowExternalCheckbox, allowUserOverrideCheckbox, desc, list, btnLayout);
        } else {
            boxLayout = new VerticalLayout(allowExternalCheckbox, lockNotice, desc, list, btnLayout);
        }
        boxLayout.setPadding(false);
        boxLayout.setSpacing(true);

        Div container = new Div(new H3("🤖 AI Knowledge Context & Governance Settings"), boxLayout);
        container.addClassName("block-container");
        container.setWidth("95%");
        container.setMaxWidth("1200px");
        container.getStyle().set("margin", "0 auto 1.5rem auto");

        contentLayout.add(container);
    }

    private void configureGoogleApiKeyBlock() {
        googleApiKeyField.setWidthFull();
        googleApiKeyField.setPlaceholder("Paste new Gemini API key here...");

        googleApiKeySaveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        googleApiKeyNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        googleApiKeyCancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        boolean hasKey = appConfig.getGoogleApiKey() != null && !appConfig.getGoogleApiKey().trim().isEmpty();
        updateApiKeyUiState(hasKey);

        googleApiKeyNewButton.addClickListener(click -> {
            googleApiKeyField.setValue("");
            googleApiKeyField.setEnabled(true);
            googleApiKeyField.focus();
            googleApiKeySaveButton.setVisible(true);
            googleApiKeyCancelButton.setVisible(true);
            googleApiKeyNewButton.setVisible(false);
        });

        googleApiKeyCancelButton.addClickListener(click -> {
            boolean keyExists = appConfig.getGoogleApiKey() != null && !appConfig.getGoogleApiKey().trim().isEmpty();
            updateApiKeyUiState(keyExists);
        });

        googleApiKeySaveButton.addClickListener(click -> {
            String newKey = googleApiKeyField.getValue();
            if (newKey != null && !newKey.trim().isEmpty()) {
                appConfig.setGoogleApiKey(newKey.trim());
                appConfigService.save(appConfig);
                aiRagService.updateGoogleApiKey(newKey.trim());
                NotificationUtils.showSuccessNotification("Google Gemini API Key saved successfully.");
                updateApiKeyUiState(true);
            } else {
                NotificationUtils.showErrorNotification("Please enter a valid Google Gemini API Key.");
            }
        });

        HorizontalLayout keyLayout = new HorizontalLayout(googleApiKeyField, googleApiKeySaveButton, googleApiKeyCancelButton, googleApiKeyNewButton);
        keyLayout.setWidthFull();
        keyLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        Paragraph desc = new Paragraph("Configure your Google Gemini API Key for AI Assistant queries and real-time streaming.");
        desc.getStyle().set("margin", "0 0 1rem 0").set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        Div keyDiv = new Div(new H3("🔑 AI Assistant - Google Gemini API Key"), desc, keyLayout);
        keyDiv.addClassName("block-container");
        keyDiv.setWidth("95%");
        keyDiv.setMaxWidth("1200px");
        keyDiv.getStyle().set("margin", "0 auto 1.5rem auto");

        contentLayout.add(keyDiv);
    }

    private void updateApiKeyUiState(boolean hasKey) {
        if (hasKey) {
            googleApiKeyField.setValue("••••••••••••••••••••••••••••••••••••••••••••••••••••");
            googleApiKeyField.setEnabled(false);
            googleApiKeySaveButton.setVisible(false);
            googleApiKeyCancelButton.setVisible(false);
            googleApiKeyNewButton.setVisible(true);
        } else {
            googleApiKeyField.setValue("");
            googleApiKeyField.setEnabled(true);
            googleApiKeySaveButton.setVisible(true);
            googleApiKeyCancelButton.setVisible(false);
            googleApiKeyNewButton.setVisible(false);
        }
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "N/A";
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    private void configureLastUpdateLabels() {
        String BTN_DEFAULT_TEXT = "Update AI Knowledge Data";
        String BTN_UPDATING_TEXT = "Updating AI Knowledge Data...";
        String BTN_CLEARING_TEXT = "Clearing & Updating AI Knowledge Data...";

        Span kbTimeSpan = new Span(appConfig.getKnowledgeDatabaseLastUpdate() != null ? formatInstant(appConfig.getKnowledgeDatabaseLastUpdate()) : "-");
        kbTimeSpan.getStyle().set("font-weight", "700");

        Span ragTimeSpan = new Span(appConfig.getAiRagDocumentsLastUpdate() != null ? formatInstant(appConfig.getAiRagDocumentsLastUpdate()) : "-");
        ragTimeSpan.getStyle().set("font-weight", "700");

        if ((appConfig.getKnowledgeDatabaseLastUpdate() != null && appConfig.getAiRagDocumentsLastUpdate() != null) &&
                appConfig.getKnowledgeDatabaseLastUpdate().isAfter(appConfig.getAiRagDocumentsLastUpdate())) {
            ragTimeSpan.getStyle().setColor("var(--lumo-error-color, #ef4444)");
        }

        HorizontalLayout kbRow = new HorizontalLayout(new Span("Knowledge Database:"), kbTimeSpan);
        kbRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kbRow.setWidthFull();

        HorizontalLayout ragRow = new HorizontalLayout(new Span("AI Vector Knowledge:"), ragTimeSpan);
        ragRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        ragRow.setWidthFull();

        VerticalLayout statusCard = new VerticalLayout(kbRow, ragRow);
        statusCard.setPadding(true);
        statusCard.setSpacing(true);
        statusCard.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "12px")
                .set("margin-bottom", "1.25rem");

        Button btnSaveRagData = new Button(BTN_DEFAULT_TEXT);
        btnSaveRagData.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSaveRagData.setWidthFull();

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Update Strategy");
        dialog.setText("Clear existing AI knowledge vector store before indexing new documents?");
        dialog.setConfirmText("Clear & Update");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            var ui = getUI().orElse(null);
            btnSaveRagData.setEnabled(false);
            btnSaveRagData.setText(BTN_CLEARING_TEXT);

            new Thread(() -> {
                try {
                    documentIngestionService.rebuildVectorStoreWithCustomDocs();

                    if (ui != null) {
                        ui.access(() -> {
                            appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                            appConfigService.save(appConfig);
                            ragTimeSpan.setText(formatInstant(appConfig.getAiRagDocumentsLastUpdate()));
                            NotificationUtils.showSuccessNotification("AI knowledge cleared and updated successfully!");
                            btnSaveRagData.setEnabled(true);
                            btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                            ui.push();
                        });
                    }
                } catch (Exception error) {
                    logger.error("Error during Clear and Update process: {}", error.getMessage(), error);
                    if (ui != null) {
                        ui.access(() -> {
                            NotificationUtils.showErrorNotification("Error during clear/update: " + error.getMessage());
                            btnSaveRagData.setEnabled(true);
                            btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                            ui.push();
                        });
                    }
                }
            }).start();
        });

        dialog.setRejectable(true);
        dialog.setRejectText("Update Only");
        dialog.addRejectListener(event -> {
            var ui = getUI().orElse(null);
            btnSaveRagData.setEnabled(false);
            btnSaveRagData.setText(BTN_UPDATING_TEXT);

            new Thread(() -> {
                try {
                    documentIngestionService.rebuildVectorStoreWithCustomDocs();

                    if (ui != null) {
                        ui.access(() -> {
                            appConfig.setAiRagDocumentsLastUpdate(Instant.now());
                            appConfigService.save(appConfig);
                            ragTimeSpan.setText(formatInstant(appConfig.getAiRagDocumentsLastUpdate()));
                            NotificationUtils.showSuccessNotification("AI knowledge updated successfully!");
                            btnSaveRagData.setEnabled(true);
                            btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                            ui.push();
                        });
                    }
                } catch (Exception error) {
                    logger.error("Error during Update process: {}", error.getMessage(), error);
                    if (ui != null) {
                        ui.access(() -> {
                            NotificationUtils.showErrorNotification("Error during update: " + error.getMessage());
                            btnSaveRagData.setEnabled(true);
                            btnSaveRagData.setText(BTN_DEFAULT_TEXT);
                            ui.push();
                        });
                    }
                }
            }).start();
        });

        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");

        btnSaveRagData.addClickListener(click -> dialog.open());

        Paragraph desc = new Paragraph("Synchronize vector embeddings with current knowledge database and manage background execution tasks.");
        desc.getStyle().set("margin", "0 0 1rem 0").set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        Div div = new Div(
                new H3("🧠 AI Knowledge Base & Scheduled Tasks"),
                desc,
                statusCard,
                this.createTaskConfigLayout(),
                btnSaveRagData
        );
        div.addClassName("block-container");
        div.setWidth("95%");
        div.setMaxWidth("1200px");
        div.getStyle().set("margin", "0 auto 1.5rem auto");

        contentLayout.add(div);
    }

    private HorizontalLayout createTaskConfigLayout() {
        taskNamesCombo = new ComboBox<>("Service Name");
        periodCombo = new ComboBox<>("Execution Period");
        statusSpan = new Span("-");
        statusSpan.setClassName("status-badge-stopped");
        taskButton = new Button("Run");

        configureTaskNamesCombo();
        configurePeriodCombo();
        taskButton.setEnabled(false);

        taskNamesCombo.addValueChangeListener(event -> onTaskSelectionChange(event.getValue()));
        periodCombo.addValueChangeListener(event -> updateButtonState());
        taskButton.addClickListener(event -> onTaskActionButtonClick());

        HorizontalLayout taskConfigLayout = new HorizontalLayout(taskNamesCombo, periodCombo, statusSpan, taskButton);
        taskConfigLayout.setWidthFull();
        taskConfigLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        taskConfigLayout.getStyle().set("margin-bottom", "1rem");
        taskNamesCombo.setWidth("45%");
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

    private void onTaskSelectionChange(TaskScheduleConfig selectedConfig) {
        this.taskScheduleConfig = selectedConfig;
        updateUiState();
    }

    private void onTaskActionButtonClick() {
        if (this.taskScheduleConfig == null) return;

        boolean isCurrentlyActive = this.taskScheduleConfig.isActive();

        if (isCurrentlyActive) {
            logger.info("Stopping Task...");
            this.taskScheduleConfig.setActive(false);
            this.taskScheduleConfigService.save(this.taskScheduleConfig);
            this.taskRegister.cancelTask(this.taskScheduleConfig.getTaskName());
            NotificationUtils.showSuccessNotification("Task '" + this.taskScheduleConfig.getTaskName() + "' stopped successfully.");
        } else {
            if (periodCombo.getValue() == null) {
                NotificationUtils.showErrorNotification("Please select an execution period.");
                return;
            }
            logger.info("Starting Task...");

            this.taskScheduleConfig.setActive(true);
            this.taskScheduleConfig.setFixedRateMilliseconds(periodCombo.getValue().milliseconds());
            this.taskScheduleConfigService.save(this.taskScheduleConfig);

            TaskConfig runConfig = TaskConfig.builder()
                    .taskName(this.taskScheduleConfig.getTaskName())
                    .fixedRateMilliseconds(this.taskScheduleConfig.getFixedRateMilliseconds())
                    .initialDelayMilliseconds(0L)
                    .active(true)
                    .build();

            this.taskRegister.scheduleTask(ragDataUpdate, runConfig);
            NotificationUtils.showSuccessNotification("Task '" + runConfig.getTaskName() + "' started successfully.");
        }

        updateUiState();
    }

    private void updateUiState() {
        if (this.taskScheduleConfig == null) {
            statusSpan.setText("-");
            statusSpan.setClassName("status-badge-stopped");
            taskButton.setText("Run");
            periodCombo.clear();
            periodCombo.setEnabled(false);
            taskButton.setEnabled(false);
        } else {
            boolean isActive = this.taskScheduleConfig.isActive();

            if (isActive) {
                statusSpan.setText("RUNNING");
                statusSpan.setClassName("status-badge-running");
            } else {
                statusSpan.setText("STOPPED");
                statusSpan.setClassName("status-badge-stopped");
            }

            taskButton.setText(isActive ? "Stop" : "Run");
            periodCombo.setEnabled(!isActive);

            findAndSelectPeriod(this.taskScheduleConfig.getFixedRateMilliseconds());

            updateButtonState();
        }
    }

    private void updateButtonState() {
        if (this.taskScheduleConfig == null) {
            taskButton.setEnabled(false);
        } else {
            boolean isActive = this.taskScheduleConfig.isActive();
            taskButton.setEnabled(isActive || periodCombo.getValue() != null);
        }
    }

    private void findAndSelectPeriod(Long milliseconds) {
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
                .addStep(googleApiKeyField,
                        "AI Assistant - Google Gemini API Key",
                        new Html("<p>Configure your Google Gemini API Key here for RAG queries and real-time streaming.</p>"),
                        PopupPosition.BOTTOM)
                .addStep(taskNamesCombo,
                        "AI Knowledge Synchronization Scheduler",
                        new Html("<p>Schedule periodic vector store indexing and background update tasks.</p>"),
                        PopupPosition.BOTTOM)
                .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}