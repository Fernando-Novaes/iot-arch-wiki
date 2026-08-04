package br.ufrj.cos.views.builder;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.service.ai.AiRagService;
import br.ufrj.cos.utils.NotificationUtils;
import br.ufrj.cos.utils.SecurityUtils;
import br.ufrj.cos.utils.TourUtils;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.HasTour;
import br.ufrj.cos.views.MainLayout;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.data.value.ValueChangeMode;

@PageTitle("Architecture Builder")
@Route(value = "builder", layout = MainLayout.class)
@PermitAll
@CssImport("./styles/chat-view-styles.css")
public class ArchitectureBuilderView extends BaseView implements HasTour {

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static record CommunicationArrowRecord(
            @com.fasterxml.jackson.annotation.JsonProperty("id") String id,
            @com.fasterxml.jackson.annotation.JsonProperty("sourceName") String sourceName,
            @com.fasterxml.jackson.annotation.JsonProperty("targetName") String targetName,
            @com.fasterxml.jackson.annotation.JsonProperty("protocol") String protocol,
            @com.fasterxml.jackson.annotation.JsonProperty("security") String security,
            @com.fasterxml.jackson.annotation.JsonProperty("pattern") String pattern,
            @com.fasterxml.jackson.annotation.JsonProperty("colorHex") String colorHex,
            @com.fasterxml.jackson.annotation.JsonProperty("annotation") String annotation
    ) {
        public CommunicationArrowRecord(String id, String sourceName, String targetName, String protocol, String security, String pattern, String colorHex) {
            this(id, sourceName, targetName, protocol, security, pattern, colorHex, "");
        }

        public CommunicationArrowRecord withAnnotation(String newAnnotation) {
            return new CommunicationArrowRecord(id, sourceName, targetName, protocol, security, pattern, colorHex, newAnnotation);
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public String getFormattedSummary() {
            String noteSuffix = (annotation != null && !annotation.isBlank()) ? " | 📝 Note: " + annotation : "";
            return String.format("%s ➔ %s: %s (%s | %s)%s",
                    sourceName != null ? sourceName : "Origin",
                    targetName != null ? targetName : "Destination",
                    protocol != null ? protocol : "MQTT",
                    security != null ? security : "TLS 1.3",
                    pattern != null ? pattern : "Real-time",
                    noteSuffix);
        }

        public static String determineColor(String protocol, String security) {
            if ("Unencrypted (Plaintext)".equalsIgnoreCase(security) || (protocol != null && "HTTP".equalsIgnoreCase(protocol))) {
                return "#ef4444"; // Red - Plaintext Warning
            }
            if (protocol == null) return "#10b981";
            String p = protocol.toLowerCase();
            if (p.contains("mqtt") || p.contains("coap") || p.contains("websocket")) {
                return "#10b981"; // Emerald Green - Real-time Streaming
            } else if (p.contains("http") || p.contains("grpc") || p.contains("rest")) {
                return "#2563eb"; // Royal Blue - Sync Request-Response
            } else if (p.contains("kafka") || p.contains("amqp") || p.contains("rabbit")) {
                return "#8b5cf6"; // Violet Purple - Event-Driven Broker
            } else if (p.contains("modbus") || p.contains("opc") || p.contains("lora") || p.contains("zigbee")) {
                return "#f59e0b"; // Amber Orange - Industrial & Fieldbus
            }
            return "#059669";
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureBuilderView.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Category Color Palette
    private static final String COLOR_DOMAIN = "#059669";   // Emerald / Green
    private static final String COLOR_PATTERN = "#7c3aed";  // Violet / Purple
    private static final String COLOR_EDGE = "#2563eb";     // Royal Blue
    private static final String COLOR_FOG = "#d97706";      // Amber / Orange
    private static final String COLOR_CLOUD = "#0284c7";    // Cyan / Indigo
    private static final String COLOR_QUALITY = "#dc2626";  // Red / Rose

    private final SavedArchitectureService savedArchitectureService;
    private final AiRagService aiRagService;
    private final IoTDomainService ioTDomainService;
    private final ArchitectureService architectureService;
    private final TechnologyService technologyService;
    private final QualityRequirementService qualityRequirementService;

    // Cached Master Data
    private List<String> cachedDomains = null;
    private List<String> cachedPatterns = null;
    private List<String> cachedTechnologies = null;
    private List<String> cachedQualityRequirements = null;

    // State of active design
    private SavedArchitecture currentArchitecture = new SavedArchitecture();
    private String selectedDomain = null;
    private String selectedPattern = null;
    private final Set<String> edgeTechs = new LinkedHashSet<>();
    private final Set<String> fogTechs = new LinkedHashSet<>();
    private final Set<String> cloudTechs = new LinkedHashSet<>();
    private final Set<String> qualityReqs = new LinkedHashSet<>();
    private final List<CommunicationArrowRecord> activeFlowArrows = new ArrayList<>();
    private String lastSavedSnapshot = "";

    // Interactive Arrow Tool State (Click 1 = Origem, Click 2 = Destino)
    private boolean isArrowConnectionActive = false;
    private String activeToolProtocol = null;
    private String activeToolSecurity = null;
    private String activeToolPattern = null;
    private String activeToolColor = null;
    private String selectedOriginName = null;

    // UI Containers
    private final VerticalLayout canvasContentLayout = new VerticalLayout();
    private final Div domainContainer = new Div();
    private final Div patternContainer = new Div();
    private final Div edgeContainer = new Div();
    private final Div fogContainer = new Div();
    private final Div cloudContainer = new Div();
    private final Div qualityReqContainer = new Div();
    private final Div aiReportDiv = new Div();
    private final Div rightPanelContainer = new Div();
    private boolean isRightPanelOpen = true;

    private boolean isDrawerOpen = true;
    private int openAccordionIndex = 0;
    private final VerticalLayout paletteDrawerPanel = new VerticalLayout();
    private final Div accordionContainer = new Div();

    // Floating AI Report Widget State
    private boolean isAiReportWindowOpen = false;
    private final Div aiReportFloatingWindow = new Div();
    private final Button aiReportFab = new Button();
    private String lastAiEvaluationReport = null;
    private final Map<String, String> arrowPositionsMap = new LinkedHashMap<>();
    private final Button exportPdfBtn = new Button("Export PDF", VaadinIcon.FILE_TEXT.create());

    // Floating Architectural Notes Widget State
    private boolean isNotesWindowOpen = false;
    private final Div notesFloatingWindow = new Div();
    private final Button notesFab = new Button();
    private final TextArea notesTextArea = new TextArea();

    // Buttons
    private final Button evaluateButton = new Button("Evaluate with AI", VaadinIcon.MAGIC.create());
    private final Button saveButton = new Button("Save", VaadinIcon.DISC.create());
    private final Button openButton = new Button("Open", VaadinIcon.FOLDER_OPEN_O.create());
    private final Button exportImageButton = new Button("Export Image", VaadinIcon.CAMERA.create());
    private final Button newButton = new Button("New", VaadinIcon.PLUS.create());

    private Disposable activeEvaluationSubscription;

    public ArchitectureBuilderView(
            SavedArchitectureService savedArchitectureService,
            AiRagService aiRagService,
            IoTDomainService ioTDomainService,
            ArchitectureService architectureService,
            TechnologyService technologyService,
            QualityRequirementService qualityRequirementService) {

        this.savedArchitectureService = savedArchitectureService;
        this.aiRagService = aiRagService;
        this.ioTDomainService = ioTDomainService;
        this.architectureService = architectureService;
        this.technologyService = technologyService;
        this.qualityRequirementService = qualityRequirementService;

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidthFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // 1. Hero Header
        mainLayout.add(createHeroCard());

        // 2. Action Bar
        mainLayout.add(createActionBar());

        // 3. Main Builder Grid with Left Collapsible Vertical Drawer & Canvas
        HorizontalLayout builderGrid = new HorizontalLayout();
        builderGrid.setWidthFull();
        builderGrid.setSpacing(true);

        createLeftDrawerPanel();
        Component canvas = createCanvas();
        createRightPanel();

        builderGrid.add(paletteDrawerPanel, canvas, rightPanelContainer);
        builderGrid.setFlexGrow(0, paletteDrawerPanel);
        builderGrid.setFlexGrow(1, canvas);
        builderGrid.setFlexGrow(0, rightPanelContainer);

        mainLayout.add(builderGrid);

        // 4. Floating AI Diagnostic Report & Notes Widgets
        createFloatingAiReportWidget();
        createFloatingNotesWidget();

        // Start with empty clean composition
        initDefaultComposition();
        this.lastSavedSnapshot = createStateSnapshot();
        refreshAllViews();

        getContent().add(mainLayout);
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkAndLoadSuggestedStack();
    }

    private void checkAndLoadSuggestedStack() {
        com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
        if (session != null) {
            br.ufrj.cos.domain.SuggestedArchitectureStack stack = (br.ufrj.cos.domain.SuggestedArchitectureStack) session.getAttribute("SUGGESTED_ARCH_STACK");
            if (stack != null) {
                session.setAttribute("SUGGESTED_ARCH_STACK", null);

                if (stack.getDomain() != null && !stack.getDomain().isEmpty()) {
                    this.selectedDomain = stack.getDomain();
                }
                if (stack.getPattern() != null && !stack.getPattern().isEmpty()) {
                    this.selectedPattern = stack.getPattern();
                }
                if (stack.getEdgeTechs() != null) {
                    this.edgeTechs.clear();
                    this.edgeTechs.addAll(stack.getEdgeTechs());
                }
                if (stack.getFogTechs() != null) {
                    this.fogTechs.clear();
                    this.fogTechs.addAll(stack.getFogTechs());
                }
                if (stack.getCloudTechs() != null) {
                    this.cloudTechs.clear();
                    this.cloudTechs.addAll(stack.getCloudTechs());
                }
                if (stack.getQualityReqs() != null) {
                    this.qualityReqs.clear();
                    this.qualityReqs.addAll(stack.getQualityReqs());
                }

                refreshAllViews();
                NotificationUtils.showSuccessNotification("🚀 Suggested Architecture Stack loaded from AI Assistant!");
            }
        }
    }

    private Component createHeroCard() {
        Div heroCard = new Div();
        heroCard.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("border-radius", "16px")
                .set("padding", "1.5rem 2rem")
                .set("color", "#ffffff")
                .set("box-shadow", "0 10px 25px rgba(37, 99, 235, 0.2)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);
        titleLayout.getStyle().set("margin-bottom", "0.3rem");

        H2 title = new H2("IoT Architecture Builder & AI Auditor");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.7rem")
                .set("font-weight", "700")
                .set("color", "#ffffff");

        Span betaBadge = new Span("BETA");
        betaBadge.getStyle()
                .set("background", "linear-gradient(135deg, #f59e0b, #d97706)")
                .set("color", "#ffffff")
                .set("font-size", "0.68rem")
                .set("font-weight", "800")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("letter-spacing", "0.8px")
                .set("box-shadow", "0 2px 8px rgba(245, 158, 11, 0.4)")
                .set("text-transform", "uppercase")
                .set("user-select", "none");

        titleLayout.add(title, betaBadge);

        Paragraph subtitle = new Paragraph("Compose your custom IoT system using the collapsible left Block Palette, save/load your designs, and evaluate trade-offs using AI Assistant.");
        subtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "0.92rem")
                .set("opacity", "0.9")
                .set("line-height", "1.4");

        heroCard.add(titleLayout, subtitle);
        return heroCard;
    }

    private Component createActionBar() {
        Div actionBar = new Div();
        actionBar.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "14px")
                .set("padding", "0.75rem 1.25rem")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftGroup = new HorizontalLayout();

        openButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        openButton.addClickListener(e -> openGalleryDialog());

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("background", "linear-gradient(135deg, #1e293b, #2563eb)");
        saveButton.addClickListener(e -> openSaveDialog());

        exportImageButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        exportImageButton.setTooltipText("Export high-resolution PNG image of architecture canvas");
        exportImageButton.addClickListener(e -> exportCanvasAsImage());

        newButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        newButton.addClickListener(e -> resetCanvas());
        leftGroup.add(openButton, saveButton, exportImageButton, newButton);

        notesFab.setIcon(VaadinIcon.NOTEBOOK.create());
        notesFab.setText("Notes");
        notesFab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        notesFab.getStyle()
                .set("background", "linear-gradient(135deg, #7c3aed, #6d28d9)")
                .set("font-weight", "700");
        notesFab.addClickListener(e -> toggleNotesWindow());

        evaluateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        evaluateButton.getStyle()
                .set("background", "linear-gradient(135deg, #059669, #10b981)")
                .set("font-weight", "700");
        evaluateButton.addClickListener(e -> runAiEvaluation());

        HorizontalLayout rightGroup = new HorizontalLayout(notesFab, evaluateButton);
        rightGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        rightGroup.setSpacing(true);

        layout.add(leftGroup, rightGroup);
        actionBar.add(layout);
        return actionBar;
    }

    private void createLeftDrawerPanel() {
        paletteDrawerPanel.removeAll();
        paletteDrawerPanel.setPadding(false);
        paletteDrawerPanel.setSpacing(true);

        if (!isDrawerOpen) {
            paletteDrawerPanel.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border", "1px solid var(--lumo-contrast-15pct)")
                    .set("border-radius", "16px")
                    .set("padding", "0.75rem 0.5rem")
                    .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)")
                    .set("width", "56px")
                    .set("min-width", "56px")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("box-sizing", "border-box")
                    .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)");

            Button expandBtn = new Button(VaadinIcon.ANGLE_DOUBLE_RIGHT.create(), e -> toggleDrawerState());
            expandBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            expandBtn.setTooltipText("Show Architecture Solution Palette");

            Span verticalLabel = new Span("🧩 Architecture Solution Palette");
            verticalLabel.getStyle()
                    .set("writing-mode", "vertical-rl")
                    .set("transform", "rotate(180deg)")
                    .set("font-weight", "700")
                    .set("font-size", "0.82rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "1rem")
                    .set("cursor", "pointer");

            verticalLabel.addClickListener(e -> toggleDrawerState());

            paletteDrawerPanel.add(expandBtn, verticalLabel);
            return;
        }

        paletteDrawerPanel.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("padding", "1.25rem")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)")
                .set("width", "380px")
                .set("min-width", "380px")
                .set("max-height", "calc(100vh - 210px)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("box-sizing", "border-box")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)");

        // Header with title and Hide Toggle Button
        HorizontalLayout drawerHeader = new HorizontalLayout();
        drawerHeader.setWidthFull();
        drawerHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        drawerHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 title = new H3("🧩 Architecture Solution Palette");
        title.getStyle().set("margin", "0").set("font-size", "1.05rem");

        Button hideBtn = new Button(VaadinIcon.ANGLE_DOUBLE_LEFT.create(), e -> toggleDrawerState());
        hideBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        hideBtn.getStyle().set("font-weight", "600");
        hideBtn.setTooltipText("Hide Architecture Solution Palette");

        drawerHeader.add(title, hideBtn);
        paletteDrawerPanel.add(drawerHeader);

        // Accordion Container with internal scroll
        accordionContainer.setWidthFull();
        accordionContainer.getStyle()
                .set("overflow-y", "auto")
                .set("max-height", "calc(100vh - 275px)")
                .set("padding-right", "4px");

        paletteDrawerPanel.add(accordionContainer);
    }

    private void toggleDrawerState() {
        isDrawerOpen = !isDrawerOpen;
        refreshAllViews();
    }

    private void createFloatingAiReportWidget() {
        // 1. Floating Action Button (FAB)
        aiReportFab.setIcon(VaadinIcon.MAGIC.create());
        aiReportFab.setText("AI Report");
        aiReportFab.getStyle()
                .set("position", "fixed")
                .set("bottom", "28px")
                .set("right", "28px")
                .set("z-index", "1000")
                .set("height", "48px")
                .set("padding", "0 1.25rem")
                .set("border-radius", "24px")
                .set("background", "linear-gradient(135deg, #059669, #10b981)")
                .set("color", "#ffffff")
                .set("font-weight", "700")
                .set("font-size", "0.88rem")
                .set("box-shadow", "0 8px 24px rgba(5, 150, 105, 0.4)")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.5rem");

        aiReportFab.addClickListener(e -> toggleAiReportWindow());

        // 2. Floating Window Card
        aiReportFloatingWindow.getStyle()
                .set("position", "fixed")
                .set("bottom", "90px")
                .set("right", "28px")
                .set("z-index", "1000")
                .set("width", "520px")
                .set("max-height", "550px")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 12px 32px rgba(0, 0, 0, 0.18)")
                .set("padding", "1.25rem")
                .set("display", "none")
                .set("flex-direction", "column")
                .set("box-sizing", "border-box");

        // Header of floating window
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("margin-bottom", "0.75rem").set("padding-bottom", "0.5rem").set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        H3 title = new H3("🤖 AI Diagnostic Report");
        title.getStyle().set("margin", "0").set("font-size", "1.05rem").set("font-weight", "700");

        exportPdfBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        exportPdfBtn.getStyle()
                .set("background", "linear-gradient(135deg, #2563eb, #1d4ed8)")
                .set("font-weight", "600");
        exportPdfBtn.setTooltipText("Export high-resolution technical PDF report with architecture diagram");
        exportPdfBtn.addClickListener(e -> exportAiReportAsPdf());
        exportPdfBtn.setEnabled(lastAiEvaluationReport != null && !lastAiEvaluationReport.trim().isEmpty());

        Button closeBtn = new Button(VaadinIcon.CLOSE.create(), e -> toggleAiReportWindow());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeBtn.setTooltipText("Hide Report Window");

        HorizontalLayout headerActions = new HorizontalLayout(exportPdfBtn, closeBtn);
        headerActions.setAlignItems(FlexComponent.Alignment.CENTER);
        headerActions.setSpacing(true);

        header.add(title, headerActions);

        // Content Area inside floating window
        aiReportDiv.getStyle()
                .set("font-size", "0.8rem")
                .set("line-height", "1.5")
                .set("color", "var(--lumo-body-text-color)")
                .set("overflow-y", "auto")
                .set("max-height", "430px")
                .set("padding-right", "0.25rem");

        aiReportDiv.setText("Click 'Evaluate with AI' to generate an audit report for your active stack.");

        aiReportFloatingWindow.add(header, aiReportDiv);
        getContent().add(aiReportFab, aiReportFloatingWindow);
    }

    private void toggleAiReportWindow() {
        isAiReportWindowOpen = !isAiReportWindowOpen;
        if (isAiReportWindowOpen) {
            aiReportFloatingWindow.getStyle().set("display", "flex");
        } else {
            aiReportFloatingWindow.getStyle().set("display", "none");
        }
    }

    private void showAiReportWindow() {
        isAiReportWindowOpen = true;
        aiReportFloatingWindow.getStyle().set("display", "flex");
    }

    private void exportAiReportAsPdf() {
        if (lastAiEvaluationReport == null || lastAiEvaluationReport.trim().isEmpty()) {
            NotificationUtils.showErrorNotification("Please run AI Evaluation before exporting PDF.");
            return;
        }

        String username = SecurityUtils.getUsername();
        if (username == null || username.isEmpty()) {
            username = "Architect User";
        }
        String dateStr = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now());

        NotificationUtils.showSuccessNotification("Opening Technical PDF Report...");

        String script = """
            (function(appName, viewName, dateStr, username, reportContentHtml) {
                function loadScript(url, callback) {
                    if (window.html2pdf) { callback(true); return; }
                    var script = document.createElement('script');
                    script.src = url;
                    script.onload = function() { callback(true); };
                    script.onerror = function() { callback(false); };
                    document.head.appendChild(script);
                }

                var html2pdfCdn = 'https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js';
                var html2canvasCdn = 'https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js';

                loadScript(html2canvasCdn, function() {
                    loadScript(html2pdfCdn, function() {
                        generatePdfReport();
                    });
                });

                function generatePdfReport() {
                    var canvasContainer = document.getElementById('architecture-canvas-container');

                    if (window.html2canvas && canvasContainer) {
                        html2canvas(canvasContainer, {
                            scale: 2,
                            useCORS: true,
                            backgroundColor: '#ffffff'
                        }).then(function(canvas) {
                            var imgData = canvas.toDataURL('image/png');
                            buildAndSavePdf(imgData, reportContentHtml);
                        }).catch(function(err) {
                            console.error("html2canvas error:", err);
                            buildAndSavePdf(null, reportContentHtml);
                        });
                    } else {
                        buildAndSavePdf(null, reportContentHtml);
                    }
                }

                function buildAndSavePdf(imgDataUrl, reportHtmlContent) {
                    var reportContainer = document.createElement('div');
                    reportContainer.style.cssText = 'padding: 20px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; color: #1e293b; background: #ffffff; width: 100%; max-width: 680px; margin: 0 auto; box-sizing: border-box;';

                    var headerHtml = `
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #2563eb; padding-bottom: 12px; margin-bottom: 20px; width: 100%; box-sizing: border-box;">
                            <div style="flex-grow: 1;">
                                <h1 style="margin: 0; font-size: 24px; color: #0f172a; font-weight: 800; letter-spacing: -0.5px;">${appName}</h1>
                                <h2 style="margin: 4px 0 0 0; font-size: 13px; color: #2563eb; font-weight: 700;">${viewName} — Technical Audit & Evaluation Report</h2>
                            </div>
                            <div style="flex-shrink: 0; text-align: right; font-size: 11px; color: #475569; line-height: 1.6; background: #f8fafc; padding: 6px 14px; border-radius: 8px; border: 1px solid #cbd5e1; box-sizing: border-box; margin-left: 15px;">
                                <div><strong>Date:</strong> ${dateStr}</div>
                                <div><strong>User / Architect:</strong> ${username}</div>
                            </div>
                        </div>
                    `;

                    var imageHtml = imgDataUrl ? `
                        <div style="margin-bottom: 22px; page-break-inside: avoid;">
                            <h3 style="font-size: 13px; color: #0f172a; border-left: 4px solid #2563eb; padding-left: 8px; margin: 0 0 10px 0; font-weight: 700;">
                                📐 Architecture Stack Blueprint Diagram
                            </h3>
                            <div style="border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px; background: #f8fafc; text-align: center;">
                                <img src="${imgDataUrl}" style="max-width: 100%; height: auto; max-height: 350px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);" />
                            </div>
                        </div>
                    ` : '';

                    var contentHtml = `
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 13px; color: #0f172a; border-left: 4px solid #10b981; padding-left: 8px; margin: 0 0 12px 0; font-weight: 700;">
                                🤖 AI Architecture Evaluation & Trade-off Audit
                            </h3>
                            <div style="font-size: 11.5px; line-height: 1.6; color: #334155;">
                                ${reportHtmlContent}
                            </div>
                        </div>
                    `;

                    reportContainer.innerHTML = headerHtml + imageHtml + contentHtml;

                    if (window.html2pdf) {
                        var opt = {
                            margin:       [10, 10, 14, 10],
                            filename:     'ArchIoTect_Technical_Report.pdf',
                            image:        { type: 'jpeg', quality: 0.98 },
                            html2canvas:  { scale: 2, useCORS: true, logging: false },
                            jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' },
                            pagebreak:    { mode: ['avoid-all', 'css', 'legacy'] }
                        };

                        html2pdf().set(opt).from(reportContainer).toPdf().get('pdf').then(function(pdf) {
                            var totalPages = pdf.internal.getNumberOfPages();
                            for (var i = 1; i <= totalPages; i++) {
                                pdf.setPage(i);
                                pdf.setFontSize(8);
                                pdf.setTextColor(100, 116, 139);
                                pdf.text('Page ' + i + ' of ' + totalPages, pdf.internal.pageSize.getWidth() - 25, pdf.internal.pageSize.getHeight() - 6);
                                pdf.text('ArchIoTect — Architecture Builder Technical Report', 10, pdf.internal.pageSize.getHeight() - 6);
                            }
                        }).save();
                    } else {
                        var printWin = window.open('', '_blank');
                        printWin.document.write('<html><head><title>ArchIoTect Technical Report</title><style>@page{size:A4 portrait;margin:12mm;} body{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;background:#fff;color:#1e293b;margin:0;padding:0;}</style></head><body>' + reportContainer.innerHTML + '</body></html>');
                        printWin.document.close();
                        printWin.focus();
                        setTimeout(function(){ printWin.print(); printWin.close(); }, 400);
                    }
                }
            })($0, $1, $2, $3, $4);
            """;

        UI.getCurrent().getPage().executeJs(script, "ArchIoTect", "Architecture Builder", dateStr, username, lastAiEvaluationReport);
    }

    private void createFloatingNotesWidget() {
        // Floating Window Card for Notes
        notesFloatingWindow.getStyle()
                .set("position", "fixed")
                .set("top", "140px")
                .set("right", "28px")
                .set("z-index", "1000")
                .set("width", "460px")
                .set("max-height", "480px")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 12px 32px rgba(0, 0, 0, 0.18)")
                .set("padding", "1.25rem")
                .set("display", "none")
                .set("flex-direction", "column")
                .set("box-sizing", "border-box");

        // Header of floating window
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("margin-bottom", "0.75rem").set("padding-bottom", "0.5rem").set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        H3 title = new H3("📝 Architectural Notes & Goals");
        title.getStyle().set("margin", "0").set("font-size", "1.05rem").set("font-weight", "700");

        Button closeBtn = new Button(VaadinIcon.CLOSE.create(), e -> toggleNotesWindow());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeBtn.setTooltipText("Hide Notes Window");

        header.add(title, closeBtn);

        // Content TextArea inside floating window
        notesTextArea.setPlaceholder("Enter system objectives, target SLAs, throughput goals, security policies, or custom architectural notes...");
        notesTextArea.setWidthFull();
        notesTextArea.setHeight("280px");
        notesTextArea.getStyle()
                .set("font-size", "0.82rem")
                .set("line-height", "1.5");

        Span helper = new Span("💡 Notes are automatically included in AI evaluations & saved with your blueprint.");
        helper.getStyle().set("font-size", "0.72rem").set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0.5rem");

        notesTextArea.addValueChangeListener(e -> updateSaveButtonState());

        notesFloatingWindow.add(header, notesTextArea, helper);
        getContent().add(notesFloatingWindow);
    }

    private void toggleNotesWindow() {
        isNotesWindowOpen = !isNotesWindowOpen;
        if (isNotesWindowOpen) {
            notesFloatingWindow.getStyle().set("display", "flex");
        } else {
            notesFloatingWindow.getStyle().set("display", "none");
        }
    }

    private void refreshPaletteView() {
        accordionContainer.removeAll();
        accordionContainer.add(createPaletteAccordion());
    }

    private void refreshAllViews() {
        createLeftDrawerPanel();
        refreshCanvasView();
        refreshPaletteView();
        createRightPanel();
        updateSaveButtonState();
        triggerSvgArrowRendering();
    }

    @ClientCallable
    public void deleteFlowArrowById(String arrowId) {
        if (arrowId != null) {
            activeFlowArrows.removeIf(a -> a.id().equalsIgnoreCase(arrowId));
            arrowPositionsMap.remove(arrowId);
            refreshAllViews();
            updateSaveButtonState();
            NotificationUtils.showSuccessNotification("Deleted flow arrow connection.");
        }
    }

    @ClientCallable
    public void triggerReRenderArrows() {
        triggerSvgArrowRendering();
    }

    private void triggerSvgArrowRendering() {
        try {
            String arrowsJson = objectMapper.writeValueAsString(activeFlowArrows);
            String script = """
                (function(canvasId, arrowsRaw) {
                    setTimeout(function() {
                        var canvas = document.getElementById(canvasId);
                        if (!canvas) return;
                        canvas.style.position = 'relative';

                        function getVaadinServer() {
                            var el = canvas;
                            while (el) {
                                if (el.$server) return el.$server;
                                el = el.parentElement || el.parentNode || (el.getRootNode ? el.getRootNode().host : null);
                            }
                            return null;
                        }

                        if (!canvas.hasAttribute('data-delete-listener')) {
                            canvas.setAttribute('data-delete-listener', 'true');
                            canvas.addEventListener('delete-arrow', function(e) {
                                var server = getVaadinServer();
                                if (server && server.deleteFlowArrowById) {
                                    server.deleteFlowArrowById(e.detail);
                                }
                            });
                            canvas.addEventListener('render-arrows', function(e) {
                                var server = getVaadinServer();
                                if (server && server.triggerReRenderArrows) {
                                    server.triggerReRenderArrows();
                                }
                            });
                            canvas.addEventListener('update-arrow-position', function(e) {
                                var server = getVaadinServer();
                                if (server && server.updateArrowPosition) {
                                    server.updateArrowPosition(e.detail.id, JSON.stringify(e.detail));
                                }
                            });
                        }

                        var oldSvg = document.getElementById('canvas-svg-arrows-overlay');
                        if (oldSvg) oldSvg.remove();

                        var arrows = typeof arrowsRaw === 'string' ? JSON.parse(arrowsRaw) : arrowsRaw;
                        if (!arrows || arrows.length === 0) return;

                        window.arrowControlPoints = window.arrowControlPoints || {};
                        window.arrowAnchors = window.arrowAnchors || {};

                        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
                        svg.id = 'canvas-svg-arrows-overlay';
                        svg.style.cssText = 'position:absolute; top:0; left:0; width:100%; height:100%; pointer-events:none; z-index:100; overflow:visible;';

                        var defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
                        svg.appendChild(defs);

                        var canvasRect = canvas.getBoundingClientRect();

                        function getNearestPerimeterPoint(mX, mY, rect) {
                            var minX = rect.left - canvasRect.left;
                            var maxX = rect.right - canvasRect.left;
                            var minY = rect.top - canvasRect.top;
                            var maxY = rect.bottom - canvasRect.top;

                            var dLeft = Math.abs(mX - minX);
                            var dRight = Math.abs(mX - maxX);
                            var dTop = Math.abs(mY - minY);
                            var dBottom = Math.abs(mY - maxY);

                            var minDist = Math.min(dLeft, dRight, dTop, dBottom);
                            var ptX, ptY;

                            if (minDist === dLeft) {
                                ptX = minX;
                                ptY = Math.max(minY, Math.min(maxY, mY));
                            } else if (minDist === dRight) {
                                ptX = maxX;
                                ptY = Math.max(minY, Math.min(maxY, mY));
                            } else if (minDist === dTop) {
                                ptX = Math.max(minX, Math.min(maxX, mX));
                                ptY = minY;
                            } else {
                                ptX = Math.max(minX, Math.min(maxX, mX));
                                ptY = maxY;
                            }
                            return { x: ptX, y: ptY };
                        }

                        arrows.forEach(function(arrow, idx) {
                            var cleanSrc = arrow.sourceName.replace(/[^a-zA-Z0-9_-]/g, '_').toLowerCase();
                            var cleanTgt = arrow.targetName.replace(/[^a-zA-Z0-9_-]/g, '_').toLowerCase();

                            var srcEl = document.getElementById('canvas-pill-' + cleanSrc) || document.getElementById('canvas-layer-' + cleanSrc);
                            var tgtEl = document.getElementById('canvas-pill-' + cleanTgt) || document.getElementById('canvas-layer-' + cleanTgt);

                            if (!srcEl || !tgtEl) return;

                            var sRect = srcEl.getBoundingClientRect();
                            var tRect = tgtEl.getBoundingClientRect();

                            var storedAnchor = window.arrowAnchors[arrow.id] || {};

                            var defaultStartX = sRect.right - canvasRect.left;
                            var defaultStartY = sRect.top + sRect.height / 2 - canvasRect.top;

                            var defaultEndX = tRect.left - canvasRect.left;
                            var defaultEndY = tRect.top + tRect.height / 2 - canvasRect.top;

                            var startX = typeof storedAnchor.startX === 'number' ? storedAnchor.startX : defaultStartX;
                            var startY = typeof storedAnchor.startY === 'number' ? storedAnchor.startY : defaultStartY;
                            var endX = typeof storedAnchor.endX === 'number' ? storedAnchor.endX : defaultEndX;
                            var endY = typeof storedAnchor.endY === 'number' ? storedAnchor.endY : defaultEndY;

                            var color = arrow.colorHex || '#10b981';
                            var markerId = 'marker-arrow-sm-' + idx + '-' + arrow.id.replace(/[^a-zA-Z0-9]/g, '');

                            // Sleek small arrowhead
                            var marker = document.createElementNS('http://www.w3.org/2000/svg', 'marker');
                            marker.setAttribute('id', markerId);
                            marker.setAttribute('viewBox', '0 0 8 8');
                            marker.setAttribute('refX', '6');
                            marker.setAttribute('refY', '4');
                            marker.setAttribute('markerWidth', '6');
                            marker.setAttribute('markerHeight', '6');
                            marker.setAttribute('orient', 'auto');

                            var pathMarker = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                            pathMarker.setAttribute('d', 'M 0 0 L 7 4 L 0 8 z');
                            pathMarker.setAttribute('fill', color);
                            marker.appendChild(pathMarker);
                            defs.appendChild(marker);

                            var defaultCtrlX = Math.min(canvasRect.width - 20, Math.max(startX, endX) + 50);
                            var defaultCtrlY = (startY + endY) / 2;

                            var stored = window.arrowControlPoints[arrow.id];
                            var ctrlX = (stored && typeof stored.x === 'number') ? stored.x : defaultCtrlX;
                            var ctrlY = (stored && typeof stored.y === 'number') ? stored.y : defaultCtrlY;

                            var arrowG = document.createElementNS('http://www.w3.org/2000/svg', 'g');
                            arrowG.style.cssText = 'pointer-events: auto;';

                            function getPathD(sX, sY, cX, cY, eX, eY) {
                                return 'M ' + sX + ',' + sY + ' Q ' + cX + ',' + cY + ' ' + eX + ',' + eY;
                            }

                            var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                            path.setAttribute('d', getPathD(startX, startY, ctrlX, ctrlY, endX, endY));
                            path.setAttribute('stroke', color);
                            path.setAttribute('stroke-width', '3.5');
                            path.setAttribute('fill', 'none');
                            path.setAttribute('stroke-linecap', 'round');
                            path.setAttribute('marker-end', 'url(#' + markerId + ')');
                            path.style.cssText = 'pointer-events: stroke; cursor: pointer; transition: stroke 0.15s ease, stroke-width 0.15s ease;';

                            arrowG.appendChild(path);

                            // Protocol Badge
                            var badgeG = document.createElementNS('http://www.w3.org/2000/svg', 'g');
                            badgeG.style.cssText = 'pointer-events: auto; cursor: pointer;';

                            function updateBadgePos(sX, sY, cX, cY, eX, eY) {
                                var bX = (sX + eX + cX * 2) / 4;
                                var bY = (sY + eY + cY * 2) / 4 - 6;
                                badgeG.setAttribute('transform', 'translate(' + bX + ',' + bY + ')');
                            }

                            var rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                            rect.setAttribute('x', '-32');
                            rect.setAttribute('y', '-11');
                            rect.setAttribute('width', '64');
                            rect.setAttribute('height', '20');
                            rect.setAttribute('rx', '10');
                            rect.setAttribute('fill', color);
                            rect.setAttribute('stroke', '#ffffff');
                            rect.setAttribute('stroke-width', '1.5');

                            var text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                            text.setAttribute('x', '0');
                            text.setAttribute('y', '3');
                            text.setAttribute('fill', '#ffffff');
                            text.setAttribute('font-size', '10');
                            text.setAttribute('font-weight', 'bold');
                            text.setAttribute('text-anchor', 'middle');
                            text.textContent = arrow.protocol || 'MQTT';

                            badgeG.appendChild(rect);
                            badgeG.appendChild(text);
                            updateBadgePos(startX, startY, ctrlX, ctrlY, endX, endY);

                            arrowG.appendChild(badgeG);

                            function notifyPosChange() {
                                canvas.dispatchEvent(new CustomEvent('update-arrow-position', {
                                    detail: { id: arrow.id, startX: startX, startY: startY, endX: endX, endY: endY, ctrlX: ctrlX, ctrlY: ctrlY },
                                    bubbles: true
                                }));
                            }

                            // Draggable Start Anchor Circle (Origin Element Edge Anchor)
                            var startAnchor = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                            startAnchor.setAttribute('cx', startX);
                            startAnchor.setAttribute('cy', startY);
                            startAnchor.setAttribute('r', '6.5');
                            startAnchor.setAttribute('fill', '#ffffff');
                            startAnchor.setAttribute('stroke', color);
                            startAnchor.setAttribute('stroke-width', '2.5');
                            startAnchor.style.cssText = 'opacity: 0; pointer-events: none; cursor: crosshair; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3)); transition: opacity 0.15s ease;';

                            // Draggable End Anchor Circle (Destination Element Edge Anchor)
                            var endAnchor = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                            endAnchor.setAttribute('cx', endX);
                            endAnchor.setAttribute('cy', endY);
                            endAnchor.setAttribute('r', '6.5');
                            endAnchor.setAttribute('fill', '#ffffff');
                            endAnchor.setAttribute('stroke', color);
                            endAnchor.setAttribute('stroke-width', '2.5');
                            endAnchor.style.cssText = 'opacity: 0; pointer-events: none; cursor: crosshair; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3)); transition: opacity 0.15s ease;';

                            // Draggable Handle Circle (Midpoint Curve Control Point)
                            var handleCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                            handleCircle.setAttribute('cx', ctrlX);
                            handleCircle.setAttribute('cy', ctrlY);
                            handleCircle.setAttribute('r', '7.5');
                            handleCircle.setAttribute('fill', color);
                            handleCircle.setAttribute('stroke', '#ffffff');
                            handleCircle.setAttribute('stroke-width', '2');
                            handleCircle.style.cssText = 'opacity: 0; pointer-events: none; cursor: move; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3)); transition: opacity 0.15s ease;';

                            arrowG.appendChild(startAnchor);
                            arrowG.appendChild(endAnchor);
                            arrowG.appendChild(handleCircle);

                            var isSelected = false;

                            function showHandles() {
                                startAnchor.style.opacity = '1';
                                startAnchor.style.pointerEvents = 'auto';
                                endAnchor.style.opacity = '1';
                                endAnchor.style.pointerEvents = 'auto';
                                handleCircle.style.opacity = '1';
                                handleCircle.style.pointerEvents = 'auto';
                            }

                            function hideHandles() {
                                if (!isSelected) {
                                    startAnchor.style.opacity = '0';
                                    startAnchor.style.pointerEvents = 'none';
                                    endAnchor.style.opacity = '0';
                                    endAnchor.style.pointerEvents = 'none';
                                    handleCircle.style.opacity = '0';
                                    handleCircle.style.pointerEvents = 'none';
                                }
                            }

                            arrowG.onmouseenter = function() {
                                showHandles();
                                path.setAttribute('stroke-width', '5');
                            };

                            arrowG.onmouseleave = function() {
                                if (!isSelected) {
                                    path.setAttribute('stroke-width', '3.5');
                                    hideHandles();
                                }
                            };

                            path.onclick = function(e) {
                                e.stopPropagation();
                                isSelected = !isSelected;
                                if (isSelected) {
                                    showHandles();
                                    path.setAttribute('stroke', '#2563eb');
                                    path.setAttribute('stroke-width', '5');
                                } else {
                                    path.setAttribute('stroke', color);
                                    path.setAttribute('stroke-width', '3.5');
                                    hideHandles();
                                }
                            };

                            badgeG.onclick = function(e) {
                                e.stopPropagation();
                                isSelected = !isSelected;
                                if (isSelected) {
                                    showHandles();
                                    path.setAttribute('stroke', '#2563eb');
                                    path.setAttribute('stroke-width', '5');
                                } else {
                                    path.setAttribute('stroke', color);
                                    path.setAttribute('stroke-width', '3.5');
                                    hideHandles();
                                }
                            };

                            startAnchor.onmousedown = function(e) {
                                e.preventDefault();
                                e.stopPropagation();

                                function onMove(evt) {
                                    var mX = evt.clientX - canvasRect.left;
                                    var mY = evt.clientY - canvasRect.top;

                                    var pt = getNearestPerimeterPoint(mX, mY, sRect);
                                    startX = pt.x;
                                    startY = pt.y;

                                    window.arrowAnchors[arrow.id] = window.arrowAnchors[arrow.id] || {};
                                    window.arrowAnchors[arrow.id].startX = startX;
                                    window.arrowAnchors[arrow.id].startY = startY;

                                    startAnchor.setAttribute('cx', startX);
                                    startAnchor.setAttribute('cy', startY);
                                    path.setAttribute('d', getPathD(startX, startY, ctrlX, ctrlY, endX, endY));
                                    updateBadgePos(startX, startY, ctrlX, ctrlY, endX, endY);
                                }

                                function onUp() {
                                    window.removeEventListener('mousemove', onMove);
                                    window.removeEventListener('mouseup', onUp);
                                    notifyPosChange();
                                }

                                window.addEventListener('mousemove', onMove);
                                window.addEventListener('mouseup', onUp);
                            };

                            endAnchor.onmousedown = function(e) {
                                e.preventDefault();
                                e.stopPropagation();

                                function onMove(evt) {
                                    var mX = evt.clientX - canvasRect.left;
                                    var mY = evt.clientY - canvasRect.top;

                                    var pt = getNearestPerimeterPoint(mX, mY, tRect);
                                    endX = pt.x;
                                    endY = pt.y;

                                    window.arrowAnchors[arrow.id] = window.arrowAnchors[arrow.id] || {};
                                    window.arrowAnchors[arrow.id].endX = endX;
                                    window.arrowAnchors[arrow.id].endY = endY;

                                    endAnchor.setAttribute('cx', endX);
                                    endAnchor.setAttribute('cy', endY);
                                    path.setAttribute('d', getPathD(startX, startY, ctrlX, ctrlY, endX, endY));
                                    updateBadgePos(startX, startY, ctrlX, ctrlY, endX, endY);
                                }

                                function onUp() {
                                    window.removeEventListener('mousemove', onMove);
                                    window.removeEventListener('mouseup', onUp);
                                    notifyPosChange();
                                }

                                window.addEventListener('mousemove', onMove);
                                window.addEventListener('mouseup', onUp);
                            };

                            handleCircle.onmousedown = function(e) {
                                e.preventDefault();
                                e.stopPropagation();

                                var startMouseX = e.clientX;
                                var startMouseY = e.clientY;
                                var origCtrlX = ctrlX;
                                var origCtrlY = ctrlY;

                                function onMove(evt) {
                                    var dx = evt.clientX - startMouseX;
                                    var dy = evt.clientY - startMouseY;

                                    ctrlX = origCtrlX + dx;
                                    ctrlY = origCtrlY + dy;

                                    window.arrowControlPoints[arrow.id] = { x: ctrlX, y: ctrlY };

                                    path.setAttribute('d', getPathD(startX, startY, ctrlX, ctrlY, endX, endY));
                                    handleCircle.setAttribute('cx', ctrlX);
                                    handleCircle.setAttribute('cy', ctrlY);
                                    updateBadgePos(startX, startY, ctrlX, ctrlY, endX, endY);
                                }

                                function onUp() {
                                    window.removeEventListener('mousemove', onMove);
                                    window.removeEventListener('mouseup', onUp);
                                    notifyPosChange();
                                }

                                window.addEventListener('mousemove', onMove);
                                window.addEventListener('mouseup', onUp);
                            };

                            svg.appendChild(arrowG);

                            handleCircle.ondblclick = function(e) {
                                e.stopPropagation();
                                delete window.arrowControlPoints[arrow.id];
                                ctrlX = defaultCtrlX;
                                ctrlY = defaultCtrlY;
                                path.setAttribute('d', getPathD(startX, startY, ctrlX, ctrlY, endX, endY));
                                handleCircle.setAttribute('cx', ctrlX);
                                handleCircle.setAttribute('cy', ctrlY);
                                updateBadgePos(startX, startY, ctrlX, ctrlY, endX, endY);
                            };

                            svg.appendChild(handleCircle);
                        });

                        canvas.appendChild(svg);
                    }, 120);
                })($0, $1);

                (function() {
                    if (window.__dnd_builder_initialized) return;
                    window.__dnd_builder_initialized = true;

                    function getVaadinServer() {
                        var el = document.getElementById('architecture-canvas-container');
                        while (el) {
                            if (el.$server) return el.$server;
                            el = el.parentElement || el.parentNode || (el.getRootNode ? el.getRootNode().host : null);
                        }
                        return null;
                    }

                    document.addEventListener('dragstart', function(e) {
                        var pill = e.target.closest('[data-pill-name]');
                        if (pill) {
                            var name = pill.getAttribute('data-pill-name');
                            var cat = pill.getAttribute('data-pill-category') || 'tech';
                            e.dataTransfer.setData('text/plain', name);
                            e.dataTransfer.setData('category', cat);
                            e.dataTransfer.effectAllowed = 'copy';
                        }
                    });

                    document.addEventListener('dragover', function(e) {
                        var dropBox = e.target.closest('[data-drop-layer]');
                        if (dropBox) {
                            e.preventDefault();
                            e.dataTransfer.dropEffect = 'copy';
                            dropBox.style.border = '2px dashed #2563eb';
                            dropBox.style.backgroundColor = 'rgba(37, 99, 235, 0.08)';
                        }
                    });

                    document.addEventListener('dragleave', function(e) {
                        var dropBox = e.target.closest('[data-drop-layer]');
                        if (dropBox) {
                            dropBox.style.border = '';
                            dropBox.style.backgroundColor = '';
                        }
                    });

                    document.addEventListener('drop', function(e) {
                        var dropBox = e.target.closest('[data-drop-layer]');
                        if (dropBox) {
                            e.preventDefault();
                            dropBox.style.border = '';
                            dropBox.style.backgroundColor = '';
                            var techName = e.dataTransfer.getData('text/plain');
                            var category = e.dataTransfer.getData('category') || 'tech';
                            var targetLayer = dropBox.getAttribute('data-drop-layer');
                            if (techName && targetLayer) {
                                var server = getVaadinServer();
                                if (server && server.dropPillOnLayer) {
                                    server.dropPillOnLayer(techName, category, targetLayer);
                                }
                            }
                        }
                    });

                    document.addEventListener('keydown', function(e) {
                        if (e.key === 'Escape' || e.key === 'Esc' || e.keyCode === 27) {
                            var shadow = document.getElementById('arrow-ghost-shadow');
                            if (shadow && shadow.style.display !== 'none') {
                                shadow.style.display = 'none';
                                var server = getVaadinServer();
                                if (server && server.cancelArrowTool) {
                                    server.cancelArrowTool();
                                }
                            }
                        }
                    });

                    document.addEventListener('click', function(e) {
                        var shadow = document.getElementById('arrow-ghost-shadow');
                        if (shadow && shadow.style.display !== 'none') {
                            var isCanvasBox = e.target.closest('[data-drop-layer]') || e.target.closest('.canvas-layer-box') || e.target.closest('.pill-badge');
                            var isArrowToolBtn = e.target.closest('[data-arrow-tool-btn]');
                            if (!isCanvasBox && !isArrowToolBtn) {
                                shadow.style.display = 'none';
                                var server = getVaadinServer();
                                if (server && server.cancelArrowTool) {
                                    server.cancelArrowTool();
                                }
                            }
                        }
                    }, true);
                })();
                """;
            UI.getCurrent().getPage().executeJs(script, "architecture-canvas-container", arrowsJson);
        } catch (Exception ex) {
            logger.error("Error triggering SVG arrow rendering", ex);
        }
    }

    private Component createPaletteAccordion() {
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        List<AccordionPanel> panels = new ArrayList<>();

        // Fetch DB data dynamically
        List<String> domainList = fetchDbDomains();
        List<String> patternList = fetchDbPatterns();
        List<String> techList = fetchDbTechnologies();
        List<String> qualityListRaw = fetchDbQualityRequirements();

        // Categorize technologies and quality requirements semantically
        List<String> edgeList = getCategorizedEdgeTechs(techList);
        List<String> fogList = getCategorizedFogTechs(techList);
        List<String> cloudList = getCategorizedCloudTechs(techList);
        List<String> qualityList = getCategorizedQualityReqs(qualityListRaw, techList);

        // 1. Domains (index 0)
        panels.add(accordion.add("🌐 IoT Domain (" + domainList.size() + ")", createPillFlexLayout(domainList, COLOR_DOMAIN, pill -> pill.equals(selectedDomain), pill -> {
            this.selectedDomain = pill;
            this.openAccordionIndex = 0;
            refreshAllViews();
            NotificationUtils.showSuccessNotification("Domain set to: " + pill);
        }, "domain")));

        // 2. Patterns (index 1)
        panels.add(accordion.add("🏛️ Architecture Pattern (" + patternList.size() + ")", createPillFlexLayout(patternList, COLOR_PATTERN, pill -> pill.equals(selectedPattern), pill -> {
            this.selectedPattern = pill;
            this.openAccordionIndex = 1;
            refreshAllViews();
            NotificationUtils.showSuccessNotification("Pattern set to: " + pill);
        }, "pattern")));

        // 3. Unified Technologies (index 2)
        Set<String> allTechsSet = new LinkedHashSet<>();
        allTechsSet.addAll(edgeList);
        allTechsSet.addAll(fogList);
        allTechsSet.addAll(cloudList);
        allTechsSet.addAll(techList);
        List<String> allTechsList = new ArrayList<>(allTechsSet);
        allTechsList.sort(String::compareToIgnoreCase);

        panels.add(accordion.add("⚡ Technologies (" + allTechsList.size() + ")", createUnifiedTechPanel(allTechsList)));

        // 4. Quality Reqs (index 3)
        panels.add(accordion.add("🛡️ Quality ISO 25010 (" + qualityList.size() + ")", createPillFlexLayout(qualityList, COLOR_QUALITY, qualityReqs::contains, pill -> {
            this.qualityReqs.add(pill);
            this.openAccordionIndex = 3;
            refreshAllViews();
            NotificationUtils.showSuccessNotification("Added Requirement: " + pill);
        }, "quality")));

        // 5. Custom Block Section (index 4)
        panels.add(accordion.add("➕ Add Custom Block / Pill", createCustomBlockSection()));

        accordion.addOpenedChangeListener(e -> {
            e.getOpenedPanel().ifPresent(panel -> {
                int idx = panels.indexOf(panel);
                if (idx >= 0) {
                    this.openAccordionIndex = idx;
                }
            });
        });

        // Keep active panel expanded
        if (openAccordionIndex >= 0 && openAccordionIndex < panels.size()) {
            accordion.open(panels.get(openAccordionIndex));
        }

        return accordion;
    }

    private String getColorForInitialLetter(String text) {
        if (text == null || text.trim().isEmpty()) return "#2563eb";
        char c = Character.toUpperCase(text.trim().charAt(0));
        int index = (c >= 'A' && c <= 'Z') ? (c - 'A') : Math.abs(c) % 16;
        String[] paletteColors = new String[]{
            "#2563eb", // A - Royal Blue
            "#059669", // B - Emerald Green
            "#d97706", // C - Amber Gold
            "#7c3aed", // D - Purple
            "#dc2626", // E - Crimson Red
            "#0891b2", // F - Cyan
            "#4f46e5", // G - Indigo
            "#ea580c", // H - Deep Orange
            "#9333ea", // I - Dark Violet
            "#0284c7", // J - Sky Blue
            "#be185d", // K - Rose Pink
            "#0d9488", // L - Teal
            "#16a34a", // M - Green
            "#c026d3", // N - Fuchsia
            "#2563eb", // O - Blue
            "#059669"  // P..Z fallback cycling
        };
        return paletteColors[index % paletteColors.length];
    }

    private void addTechToAppropriateLayer(String tech) {
        List<String> edgeList = getCategorizedEdgeTechs(fetchDbTechnologies());
        List<String> fogList = getCategorizedFogTechs(fetchDbTechnologies());
        List<String> cloudList = getCategorizedCloudTechs(fetchDbTechnologies());

        if (edgeList.contains(tech)) {
            this.edgeTechs.add(tech);
            NotificationUtils.showSuccessNotification("Added to Edge Layer: " + tech);
        } else if (fogList.contains(tech)) {
            this.fogTechs.add(tech);
            NotificationUtils.showSuccessNotification("Added to Fog / Gateway Layer: " + tech);
        } else if (cloudList.contains(tech)) {
            this.cloudTechs.add(tech);
            NotificationUtils.showSuccessNotification("Added to Cloud Layer: " + tech);
        } else {
            this.edgeTechs.add(tech);
            NotificationUtils.showSuccessNotification("Added to Edge Layer: " + tech);
        }
    }

    private Component createUnifiedTechPanel(List<String> allTechList) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(true);
        container.getStyle().set("padding", "0.5rem 0");

        TextField searchFilter = new TextField();
        searchFilter.setPlaceholder("Filter technologies by name...");
        searchFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchFilter.setClearButtonVisible(true);
        searchFilter.setWidthFull();
        searchFilter.setValueChangeMode(ValueChangeMode.LAZY);

        FlexLayout pillsFlex = new FlexLayout();
        pillsFlex.getStyle().set("gap", "0.45rem").set("flex-wrap", "wrap").set("padding", "0.5rem 0");

        List<String> sortedTechs = new ArrayList<>(allTechList);
        sortedTechs.sort(String::compareToIgnoreCase);

        Runnable renderPills = () -> {
            pillsFlex.removeAll();
            String filterText = searchFilter.getValue() != null ? searchFilter.getValue().trim().toLowerCase() : "";

            for (String tech : sortedTechs) {
                if (!filterText.isEmpty() && !tech.toLowerCase().contains(filterText)) {
                    continue;
                }

                boolean isSelected = edgeTechs.contains(tech) || fogTechs.contains(tech) || cloudTechs.contains(tech);
                String initialColor = getColorForInitialLetter(tech);
                Span pill = createPill(tech, initialColor, !isSelected, isSelected, "tech");

                if (!isSelected) {
                    pill.addClickListener(e -> {
                        addTechToAppropriateLayer(tech);
                        this.openAccordionIndex = 2;
                        refreshAllViews();
                    });
                } else {
                    pill.addClickListener(e -> {
                        edgeTechs.remove(tech);
                        fogTechs.remove(tech);
                        cloudTechs.remove(tech);
                        this.openAccordionIndex = 2;
                        refreshAllViews();
                    });
                }
                pillsFlex.add(pill);
            }
        };

        renderPills.run();
        searchFilter.addValueChangeListener(e -> renderPills.run());

        container.add(searchFilter, pillsFlex);
        return container;
    }

    private Component createPillFlexLayout(List<String> items, String colorHex, java.util.function.Predicate<String> isSelectedPredicate, java.util.function.Consumer<String> onSelect, String category) {
        FlexLayout pillsFlex = new FlexLayout();
        pillsFlex.getStyle().set("gap", "0.45rem").set("flex-wrap", "wrap").set("padding", "0.5rem 0");

        for (String item : items) {
            boolean isSelected = isSelectedPredicate.test(item);
            Span pill = createPill(item, colorHex, !isSelected, isSelected, category);
            if (!isSelected) {
                pill.addClickListener(e -> onSelect.accept(item));
            }
            pillsFlex.add(pill);
        }
        return pillsFlex;
    }

    private Component createPillFlexLayout(List<String> items, String colorHex, java.util.function.Predicate<String> isSelectedPredicate, java.util.function.Consumer<String> onSelect) {
        return createPillFlexLayout(items, colorHex, isSelectedPredicate, onSelect, "tech");
    }

    private static final Set<String> PURE_ISO_QUALITY_REQUIREMENTS = Set.of(
            "Compatibility",
            "Flexibility",
            "Functional Suitability",
            "Interaction Capability",
            "Maintainability",
            "Performance/Efficiency",
            "Performance / Low Latency",
            "Reliability",
            "Safety",
            "Security",
            "High Availability",
            "Scalability",
            "Fault Tolerance",
            "Interoperability",
            "Usability",
            "Portability"
    );

    private List<String> getCategorizedEdgeTechs(List<String> allTechs) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(List.of("Edge Sensors", "LoRaWAN", "MQTT", "CoAP", "Zigbee", "Modbus", "BLE", "TPM Module", "AWS IoT Greengrass", "FreeRTOS", "TLS 1.3 / DTLS", "IoT devices authentication and data encryption"));
        for (String t : allTechs) {
            String lower = t.toLowerCase();
            if ((lower.contains("sensor") || lower.contains("lora") || lower.contains("mqtt") || lower.contains("coap")
                    || lower.contains("zigbee") || lower.contains("ble") || lower.contains("tpm") || lower.contains("greengrass") || lower.contains("freertos")
                    || lower.contains("digital signature") || lower.contains("tls"))
                    && !lower.contains("aws core") && !PURE_ISO_QUALITY_REQUIREMENTS.contains(t)) {
                set.add(t);
            }
        }
        return new ArrayList<>(set);
    }

    private List<String> getCategorizedFogTechs(List<String> allTechs) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(List.of("Edge Gateway", "Node-RED", "Complex Event Processing (CEP)", "Stream Analytics", "SQLite Local Buffer", "Docker Container", "Local Edge Broker", "Data Origin Authentication"));
        for (String t : allTechs) {
            String lower = t.toLowerCase();
            if ((lower.contains("gateway") || lower.contains("node-red") || lower.contains("sqlite")
                    || lower.contains("docker") || lower.contains("stream") || lower.contains("cep") || lower.contains("local broker")
                    || lower.contains("data origin"))
                    && !PURE_ISO_QUALITY_REQUIREMENTS.contains(t)) {
                set.add(t);
            }
        }
        return new ArrayList<>(set);
    }

    private List<String> getCategorizedCloudTechs(List<String> allTechs) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(List.of("Amazon Web Services (AWS)", "AWS IoT Core", "Big Data Analytics Engine", "Apache Kafka", "Time-Series DB", "Kubernetes Cluster", "Blockchain Technology", "Cloud Data Warehouse", "Authentication, authorization, and data access control", "OAuth2 / JWT Auth", "Cloud-based biometric authentication", "User Identity Manager and Authentication"));
        for (String t : allTechs) {
            String lower = t.toLowerCase();
            if ((lower.contains("aws") || lower.contains("kafka") || lower.contains("big data")
                    || lower.contains("kubernetes") || lower.contains("blockchain") || lower.contains("cloud") || lower.contains("time-series")
                    || lower.contains("biometric") || lower.contains("user identity") || lower.contains("oauth"))
                    && !PURE_ISO_QUALITY_REQUIREMENTS.contains(t)) {
                set.add(t);
            }
        }
        return new ArrayList<>(set);
    }

    private List<String> getCategorizedQualityReqs(List<String> dbQrs, List<String> allTechs) {
        Set<String> set = new LinkedHashSet<>();
        // Pure ISO 25010 Quality Characteristics only!
        set.addAll(List.of("Security", "Performance / Low Latency", "High Availability", "Scalability", "Fault Tolerance", "Interoperability", "Compatibility", "Flexibility", "Functional Suitability", "Maintainability", "Reliability", "Safety"));

        for (String qr : dbQrs) {
            String lower = qr.toLowerCase();
            if (!lower.contains("authentication") && !lower.contains("tls") && !lower.contains("access control")
                    && !lower.contains("biometric") && !lower.contains("signature") && !lower.contains("encryption")) {
                if (PURE_ISO_QUALITY_REQUIREMENTS.contains(qr) || lower.contains("capability") || lower.contains("suitability")
                        || lower.contains("maintainability") || lower.contains("reliability") || lower.contains("scalability")
                        || lower.contains("compatibility") || lower.contains("flexibility") || lower.contains("performance")) {
                    set.add(qr);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private List<String> fetchDbDomains() {
        if (cachedDomains != null) {
            return cachedDomains;
        }
        try {
            List<String> domains = ioTDomainService.findAllOrderByName().stream()
                    .map(IoTDomain::getName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!domains.isEmpty()) {
                cachedDomains = domains;
                return cachedDomains;
            }
        } catch (Exception e) {
            logger.warn("Could not fetch DB domains", e);
        }
        cachedDomains = List.of("Smart Farming", "Industry 4.0", "Healthcare", "Smart City", "Generic");
        return cachedDomains;
    }

    private List<String> fetchDbPatterns() {
        if (cachedPatterns != null) {
            return cachedPatterns;
        }
        try {
            List<String> patterns = architectureService.findAll().stream()
                    .map(Architecture::getName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!patterns.isEmpty()) {
                cachedPatterns = patterns;
                return cachedPatterns;
            }
        } catch (Exception e) {
            logger.warn("Could not fetch DB patterns", e);
        }
        cachedPatterns = List.of("3-Tier (Edge-Fog-Cloud)", "4-Tier (Enterprise-Cloud-Fog-Edge)", "Microservices Architecture", "Event-Driven Architecture", "Lambda Architecture");
        return cachedPatterns;
    }

    private List<String> fetchDbTechnologies() {
        if (cachedTechnologies != null) {
            return cachedTechnologies;
        }
        try {
            List<String> techs = technologyService.findAllOrderedByDescription().stream()
                    .map(Technology::getDescription)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!techs.isEmpty()) {
                cachedTechnologies = techs;
                return cachedTechnologies;
            }
        } catch (Exception e) {
            logger.warn("Could not fetch DB technologies", e);
        }
        cachedTechnologies = List.of("LoRaWAN", "MQTT", "CoAP", "Zigbee", "Modbus", "BLE", "Edge Sensors", "Edge Gateway", "Node-RED", "TPM Module", "TLS 1.3 / DTLS", "SQLite Local Buffer", "Docker Container", "AWS IoT Core", "Apache Kafka", "Time-Series DB", "Kubernetes Cluster", "OAuth2 / JWT Auth");
        return cachedTechnologies;
    }

    private List<String> fetchDbQualityRequirements() {
        if (cachedQualityRequirements != null) {
            return cachedQualityRequirements;
        }
        try {
            List<String> qrs = qualityRequirementService.listAllByNameDistinct().stream()
                    .map(QualityRequirement::getName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!qrs.isEmpty()) {
                cachedQualityRequirements = qrs;
                return cachedQualityRequirements;
            }
        } catch (Exception e) {
            logger.warn("Could not fetch DB quality requirements", e);
        }
        cachedQualityRequirements = List.of("Security", "Performance / Low Latency", "High Availability", "Scalability", "Fault Tolerance", "Interoperability");
        return cachedQualityRequirements;
    }

    private Component createCustomBlockSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.getStyle().set("padding", "0.5rem 0");

        TextField customInput = new TextField();
        customInput.setPlaceholder("Enter custom technology or protocol...");
        customInput.setWidthFull();

        HorizontalLayout btnGroup = new HorizontalLayout();
        Button addEdge = new Button("+ Edge", e -> {
            if (!customInput.getValue().isBlank()) {
                String val = customInput.getValue().trim();
                this.edgeTechs.add(val);
                customInput.clear();
                refreshAllViews();
                NotificationUtils.showSuccessNotification("Added to Edge: " + val);
            }
        });
        addEdge.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addEdge.getStyle().set("background", COLOR_EDGE);

        Button addFog = new Button("+ Fog", e -> {
            if (!customInput.getValue().isBlank()) {
                String val = customInput.getValue().trim();
                this.fogTechs.add(val);
                customInput.clear();
                refreshAllViews();
                NotificationUtils.showSuccessNotification("Added to Fog: " + val);
            }
        });
        addFog.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addFog.getStyle().set("background", COLOR_FOG);

        Button addCloud = new Button("+ Cloud", e -> {
            if (!customInput.getValue().isBlank()) {
                String val = customInput.getValue().trim();
                this.cloudTechs.add(val);
                customInput.clear();
                refreshAllViews();
                NotificationUtils.showSuccessNotification("Added to Cloud: " + val);
            }
        });
        addCloud.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addCloud.getStyle().set("background", COLOR_CLOUD);

        btnGroup.add(addEdge, addFog, addCloud);
        section.add(customInput, btnGroup);
        return section;
    }

    private Component createCanvas() {
        VerticalLayout canvasWrapper = new VerticalLayout();
        canvasWrapper.setId("architecture-canvas-container");
        canvasWrapper.setWidthFull();
        canvasWrapper.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("padding", "1.25rem")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)");

        canvasContentLayout.setWidthFull();
        canvasContentLayout.setPadding(false);
        canvasContentLayout.setSpacing(false);

        canvasWrapper.add(canvasContentLayout);
        rebuildCanvasContent();
        return canvasWrapper;
    }

    private void rebuildCanvasContent() {
        canvasContentLayout.removeAll();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);
        titleLayout.getStyle().set("margin-bottom", "0.8rem");

        H3 title = new H3("🎨 Active Architecture Solution Canvas");
        title.getStyle().set("margin", "0").set("font-size", "1.05rem");

        titleLayout.add(title);
        canvasContentLayout.add(titleLayout);

        if (isArrowConnectionActive) {
            Div banner = new Div();
            banner.getStyle()
                    .set("width", "100%")
                    .set("background", activeToolColor != null ? activeToolColor : "#2563eb")
                    .set("color", "#ffffff")
                    .set("padding", "0.65rem 1rem")
                    .set("border-radius", "10px")
                    .set("margin-bottom", "0.8rem")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "space-between")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                    .set("font-weight", "700")
                    .set("font-size", "0.82rem");

            String originMsg = selectedOriginName == null ? "Click 1: Select ORIGIN Component/Layer" : "ORIGIN: [" + selectedOriginName + "] ➔ Click 2: Select DESTINATION";
            Span msg = new Span("🏹 ARROW TOOL ACTIVE: " + activeToolProtocol + " — " + originMsg);

            Button cancelBtn = new Button("Cancel Tool (Esc)", e -> deactivateArrowTool());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
            cancelBtn.getStyle().set("color", "#ffffff").set("text-decoration", "underline");

            banner.add(msg, cancelBtn);
            canvasContentLayout.add(banner);
        }

        canvasContentLayout.add(createCanvasLayerBox("🌐 Selected Domain", domainContainer));
        canvasContentLayout.add(createCanvasLayerBox("🏛️ Selected Architecture Pattern", patternContainer));
        canvasContentLayout.add(createCanvasLayerBox("⚡ Edge Layer (Devices & Sensors)", edgeContainer));
        canvasContentLayout.add(createCanvasLayerBox("🌉 Fog / Gateway Layer", fogContainer));
        canvasContentLayout.add(createCanvasLayerBox("☁️ Cloud & Enterprise Layer", cloudContainer));
        canvasContentLayout.add(createCanvasLayerBox("🛡️ Target Quality Requirements (ISO 25010)", qualityReqContainer));
    }

    private Component createInterLayerGapComponent(String sourceLayerLabel, String targetLayerLabel) {
        VerticalLayout gapContainer = new VerticalLayout();
        gapContainer.setWidthFull();
        gapContainer.setPadding(false);
        gapContainer.setSpacing(false);
        gapContainer.getStyle()
                .set("margin", "0.4rem 0")
                .set("align-items", "center");

        List<CommunicationArrowRecord> matchingArrows = activeFlowArrows.stream()
                .filter(a -> isArrowBetween(a, sourceLayerLabel, targetLayerLabel))
                .collect(Collectors.toList());

        if (!matchingArrows.isEmpty()) {
            for (CommunicationArrowRecord arrow : matchingArrows) {
                gapContainer.add(createGraphicalArrowElement(arrow));
            }
        }

        return gapContainer;
    }

    private boolean isArrowBetween(CommunicationArrowRecord arrow, String layer1, String layer2) {
        String src = arrow.sourceName().toLowerCase();
        String tgt = arrow.targetName().toLowerCase();
        String l1 = layer1.toLowerCase();
        String l2 = layer2.toLowerCase();
        return (src.contains(l1) && tgt.contains(l2));
    }

    private void openArrowAnnotationDialog(CommunicationArrowRecord arrow) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📝 Flow Link Annotation & Technical Notes");
        dialog.setWidth("540px");

        Div cardHeader = new Div();
        cardHeader.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("color", "#ffffff")
                .set("padding", "0.85rem 1.1rem")
                .set("border-radius", "12px")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 4px 12px rgba(37, 99, 235, 0.2)");

        Span flowTitle = new Span("🏹 " + arrow.sourceName() + " ━━━━► " + arrow.targetName());
        flowTitle.getStyle().set("font-weight", "800").set("font-size", "0.95rem").set("display", "block").set("margin-bottom", "0.3rem");

        Span flowDetails = new Span("Protocol: " + arrow.protocol() + " | Security: " + arrow.security() + " | Pattern: " + arrow.pattern());
        flowDetails.getStyle().set("font-size", "0.74rem").set("opacity", "0.9");

        cardHeader.add(flowTitle, flowDetails);

        boolean hasExistingAnnotation = arrow.annotation() != null && !arrow.annotation().isBlank();

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        contentLayout.add(cardHeader);

        if (hasExistingAnnotation) {
            Div viewCard = new Div();
            viewCard.getStyle()
                    .set("background", "rgba(245, 158, 11, 0.1)")
                    .set("border", "1px solid #f59e0b")
                    .set("border-left", "5px solid #f59e0b")
                    .set("border-radius", "10px")
                    .set("padding", "0.85rem 1rem")
                    .set("margin-bottom", "0.75rem")
                    .set("font-size", "0.82rem")
                    .set("color", "var(--lumo-header-text-color)");

            Span viewHeader = new Span("📌 Active Annotation:");
            viewHeader.getStyle().set("font-weight", "700").set("color", "#d97706").set("display", "block").set("margin-bottom", "0.4rem");

            Paragraph viewText = new Paragraph(arrow.annotation());
            viewText.getStyle().set("margin", "0").set("white-space", "pre-wrap").set("line-height", "1.4");

            viewCard.add(viewHeader, viewText);
            contentLayout.add(viewCard);
        }

        TextArea notesArea = new TextArea("Edit Technical Annotation / SLA Notes");
        notesArea.setWidthFull();
        notesArea.setHeight("120px");
        notesArea.setPlaceholder("e.g., Latency SLA < 100ms; TLS 1.3 mTLS client certs; Fallback over cellular 5G...");
        notesArea.setValue(arrow.annotation() != null ? arrow.annotation() : "");

        contentLayout.add(notesArea);
        dialog.add(contentLayout);

        Button saveBtn = new Button("Save Annotation", VaadinIcon.CHECK.create(), e -> {
            String newNote = notesArea.getValue().trim();
            int idx = activeFlowArrows.indexOf(arrow);
            if (idx != -1) {
                CommunicationArrowRecord updated = arrow.withAnnotation(newNote);
                activeFlowArrows.set(idx, updated);
                refreshAllViews();
                updateSaveButtonState();
                NotificationUtils.showSuccessNotification("Saved annotation for flow link: " + arrow.sourceName() + " ➔ " + arrow.targetName());
            }
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        if (hasExistingAnnotation) {
            Button removeBtn = new Button("Clear Note", VaadinIcon.TRASH.create(), e -> {
                int idx = activeFlowArrows.indexOf(arrow);
                if (idx != -1) {
                    CommunicationArrowRecord updated = arrow.withAnnotation("");
                    activeFlowArrows.set(idx, updated);
                    refreshAllViews();
                    updateSaveButtonState();
                    NotificationUtils.showSuccessNotification("Cleared annotation from flow link.");
                }
                dialog.close();
            });
            removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(removeBtn);
        }

        dialog.getFooter().add(new Button("Close", e -> dialog.close()), saveBtn);
        dialog.open();
    }

    private Component createGraphicalArrowElement(CommunicationArrowRecord arrow) {
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("margin", "0.35rem 0")
                .set("padding", "0.5rem 0.85rem")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "10px")
                .set("border", "1px dashed " + arrow.colorHex())
                .set("box-sizing", "border-box")
                .set("cursor", "pointer")
                .set("position", "relative")
                .set("transition", "all 0.2s ease");

        container.getElement().addEventListener("mouseenter", e -> {
            container.getStyle()
                    .set("border", "2px solid " + arrow.colorHex())
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("box-shadow", "0 2px 8px rgba(0,0,0,0.06)");
        });

        container.getElement().addEventListener("mouseleave", e -> {
            container.getStyle()
                    .set("border", "1px dashed " + arrow.colorHex())
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("box-shadow", "none");
        });

        // Click anywhere on container to open Annotation Popup
        container.addClickListener(e -> openArrowAnnotationDialog(arrow));

        // Origin Box
        Span originBox = new Span(arrow.sourceName());
        originBox.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-weight", "700")
                .set("font-size", "0.76rem")
                .set("padding", "4px 10px")
                .set("border-radius", "8px")
                .set("border-left", "4px solid " + arrow.colorHex())
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.04)");

        // Arrow Shaft Div with Protocol & Annotation Badges centered on line
        Div arrowShaftContainer = new Div();
        arrowShaftContainer.getStyle()
                .set("flex-grow", "1")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("position", "relative")
                .set("margin", "0 12px");

        HorizontalLayout badgeRow = new HorizontalLayout();
        badgeRow.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeRow.setSpacing(true);
        badgeRow.getStyle().set("margin-bottom", "3px");

        Span protoBadge = new Span(arrow.protocol() + ("Unencrypted (Plaintext)".equalsIgnoreCase(arrow.security()) ? " ⚠️ Plaintext" : " 🔒 " + arrow.security()));
        protoBadge.getStyle()
                .set("background", arrow.colorHex())
                .set("color", "#ffffff")
                .set("font-weight", "700")
                .set("font-size", "0.68rem")
                .set("padding", "2px 10px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)")
                .set("white-space", "nowrap");

        boolean hasNote = arrow.annotation() != null && !arrow.annotation().isBlank();
        Span noteBadge;
        if (hasNote) {
            String snippet = arrow.annotation().length() > 22 ? arrow.annotation().substring(0, 19) + "..." : arrow.annotation();
            noteBadge = new Span("📝 " + snippet);
            noteBadge.getStyle()
                    .set("background", "#f59e0b")
                    .set("color", "#ffffff")
                    .set("font-weight", "700")
                    .set("font-size", "0.65rem")
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px")
                    .set("box-shadow", "0 2px 6px rgba(245, 158, 11, 0.3)")
                    .set("cursor", "pointer")
                    .set("white-space", "nowrap");
            noteBadge.getElement().setAttribute("title", "Click to view annotation: " + arrow.annotation());
        } else {
            noteBadge = new Span("+ 📝 Add Note");
            noteBadge.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-weight", "600")
                    .set("font-size", "0.65rem")
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px")
                    .set("cursor", "pointer")
                    .set("white-space", "nowrap");
            noteBadge.getElement().setAttribute("title", "Click to add technical note to this flow link");
        }
        noteBadge.getElement().executeJs("this.addEventListener('click', function(e){ e.stopPropagation(); });");
        noteBadge.addClickListener(e -> openArrowAnnotationDialog(arrow));

        badgeRow.add(protoBadge, noteBadge);

        // Graphical Arrow Line and Pointer
        Div lineAndPointer = new Div();
        lineAndPointer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("width", "100%");

        Div shaftLine = new Div();
        shaftLine.getStyle()
                .set("height", "4px")
                .set("width", "100%")
                .set("background-color", arrow.colorHex())
                .set("border-radius", "2px");

        Div pointerHead = new Div();
        pointerHead.getStyle()
                .set("width", "0")
                .set("height", "0")
                .set("border-top", "6px solid transparent")
                .set("border-bottom", "6px solid transparent")
                .set("border-left", "10px solid " + arrow.colorHex());

        lineAndPointer.add(shaftLine, pointerHead);
        arrowShaftContainer.add(badgeRow, lineAndPointer);

        // Destination Box
        Span targetBox = new Span(arrow.targetName());
        targetBox.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-weight", "700")
                .set("font-size", "0.76rem")
                .set("padding", "4px 10px")
                .set("border-radius", "8px")
                .set("border-right", "4px solid " + arrow.colorHex())
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.04)");

        // Right side Action Buttons (Delete)
        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            activeFlowArrows.remove(arrow);
            refreshAllViews();
            updateSaveButtonState();
            NotificationUtils.showSuccessNotification("Deleted flow arrow link: " + arrow.sourceName() + " ➔ " + arrow.targetName());
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.setTooltipText("Delete arrow connection");
        deleteBtn.getElement().executeJs("this.addEventListener('click', function(e){ e.stopPropagation(); });");

        container.add(originBox, arrowShaftContainer, targetBox, deleteBtn);
        return container;
    }

    private void createRightPanel() {
        rightPanelContainer.removeAll();
        if (!isRightPanelOpen) {
            rightPanelContainer.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border", "1px solid var(--lumo-contrast-15pct)")
                    .set("border-radius", "16px")
                    .set("padding", "0.75rem 0.5rem")
                    .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)")
                    .set("width", "56px")
                    .set("min-width", "56px")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("box-sizing", "border-box")
                    .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)");

            Button expandBtn = new Button(VaadinIcon.ANGLE_DOUBLE_LEFT.create(), e -> {
                isRightPanelOpen = true;
                refreshAllViews();
            });
            expandBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            expandBtn.setTooltipText("Show Communication Palette");

            Span verticalLabel = new Span("🏹 Communication Palette");
            verticalLabel.getStyle()
                    .set("writing-mode", "vertical-rl")
                    .set("transform", "rotate(180deg)")
                    .set("font-weight", "700")
                    .set("font-size", "0.82rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "1rem")
                    .set("cursor", "pointer");

            verticalLabel.addClickListener(e -> {
                isRightPanelOpen = true;
                refreshAllViews();
            });

            rightPanelContainer.add(expandBtn, verticalLabel);
            return;
        }

        rightPanelContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "16px")
                .set("padding", "1.25rem")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.04)")
                .set("width", "360px")
                .set("min-width", "360px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "1rem")
                .set("box-sizing", "border-box");

        // Header with Hide Toggle Button & Icon
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 titleRight = new H3("🏹 Communication Palette");
        titleRight.getStyle().set("margin", "0").set("font-size", "1.02rem");

        Button hideBtn = new Button(VaadinIcon.ANGLE_DOUBLE_RIGHT.create(), e -> {
            isRightPanelOpen = false;
            refreshAllViews();
        });
        hideBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        hideBtn.getStyle().set("font-weight", "600");
        hideBtn.setTooltipText("Hide Communication Palette");

        header.add(titleRight, hideBtn);
        rightPanelContainer.add(header);

        // Independent Collapsible Accordion Panels (both can be opened at the same time)
        Div accordionContainer = new Div();
        accordionContainer.setWidthFull();
        accordionContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.5rem");

        // Panel 1: Arrow Tools
        Div paletteGrid = new Div();
        paletteGrid.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.5rem").set("padding", "0.4rem 0");

        paletteGrid.add(createArrowToolPill("MQTT", "TLS 1.3 / mTLS", "Real-time Streaming", "#10b981", "🟢 Real-Time Streaming (MQTT)"));
        paletteGrid.add(createArrowToolPill("HTTPS / REST", "TLS 1.3", "Request-Response", "#2563eb", "🔵 Request-Response (HTTPS)"));
        paletteGrid.add(createArrowToolPill("Kafka / AMQP", "mTLS", "Event-Driven", "#8b5cf6", "🟣 Event Broker (Kafka/AMQP)"));
        paletteGrid.add(createArrowToolPill("Modbus / OPC-UA", "TLS", "Industrial", "#f59e0b", "🟠 Industrial Fieldbus (Modbus)"));
        paletteGrid.add(createArrowToolPill("HTTP (Plaintext)", "Unencrypted (Plaintext)", "Real-time", "#ef4444", "🔴 Plaintext Warning Link"));
        paletteGrid.add(createCustomArrowToolPill());

        AccordionPanel panel1 = new AccordionPanel("🎯 Communication Arrow Tools", paletteGrid);
        panel1.setWidthFull();
        panel1.setOpened(true);

        // Panel 2: Active Flow Links
        Div flowListDiv = new Div();
        flowListDiv.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.6rem").set("max-height", "280px").set("overflow-y", "auto").set("padding", "0.4rem 0");

        if (activeFlowArrows.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("padding", "0.85rem")
                    .set("border", "1px dashed var(--lumo-contrast-20pct)")
                    .set("border-radius", "10px")
                    .set("font-size", "0.76rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("text-align", "center");
            emptyState.setText("No arrows connected yet. Select an Arrow Tool above, then click Origin ➔ Destination.");
            flowListDiv.add(emptyState);
        } else {
            for (CommunicationArrowRecord arrow : activeFlowArrows) {
                flowListDiv.add(createRightPanelActiveArrowRow(arrow));
            }
        }

        AccordionPanel panel2 = new AccordionPanel("⚡ Active Flow Links (" + activeFlowArrows.size() + ")", flowListDiv);
        panel2.setWidthFull();
        panel2.setOpened(true);

        accordionContainer.add(panel1, panel2);
        rightPanelContainer.add(accordionContainer);
    }

    private boolean isBlockName(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase();
        return n.contains("architecture pattern") || n.contains("edge layer") || n.contains("fog") || n.contains("cloud & enterprise") || n.contains("cloud layer") || n.contains("selected domain") || n.contains("quality requirements");
    }

    private Component createRightPanelActiveArrowRow(CommunicationArrowRecord arrow) {
        Div row = new Div();
        row.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-left", "5px solid " + arrow.colorHex())
                .set("border-radius", "8px")
                .set("padding", "0.45rem 0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease");

        boolean isSrcBlock = isBlockName(arrow.sourceName());
        boolean isTgtBlock = isBlockName(arrow.targetName());

        row.getElement().addEventListener("mouseenter", e -> {
            row.getStyle()
                    .set("background", "rgba(37, 99, 235, 0.08)")
                    .set("border-color", arrow.colorHex())
                    .set("box-shadow", "0 0 10px " + arrow.colorHex());

            UI.getCurrent().getPage().executeJs("""
                (function(src, tgt, color, isSrcBlock, isTgtBlock) {
                    function cleanName(n) {
                        if (!n) return '';
                        var s = n.endsWith(' ✕') ? n.substring(0, n.length - 2).trim() : n.trim();
                        return s.replace(/^Pattern:\\s*/i, '').replace(/^Domain:\\s*/i, '').trim();
                    }

                    var cleanSrc = cleanName(src).toLowerCase();
                    var cleanTgt = cleanName(tgt).toLowerCase();

                    function matchesTarget(el, isBlock, targetClean) {
                        if (!targetClean) return false;
                        var attr = isBlock ? 'data-layer-name' : 'data-pill-name';
                        var rawName = el.getAttribute(attr) || el.getAttribute('data-drop-layer') || el.innerText || '';
                        var cName = cleanName(rawName).toLowerCase();
                        if (!cName) return false;
                        return cName === targetClean || cName.includes(targetClean) || targetClean.includes(cName);
                    }

                    var srcSelector = isSrcBlock ? '[data-layer-name], [data-drop-layer]' : '[data-pill-name], [id^="canvas-pill-"]';
                    var tgtSelector = isTgtBlock ? '[data-layer-name], [data-drop-layer]' : '[data-pill-name], [id^="canvas-pill-"]';

                    var elementsToHighlight = [];

                    document.querySelectorAll(srcSelector).forEach(function(el) {
                        if (matchesTarget(el, isSrcBlock, cleanSrc)) {
                            elementsToHighlight.push(el);
                        }
                    });

                    document.querySelectorAll(tgtSelector).forEach(function(el) {
                        if (matchesTarget(el, isTgtBlock, cleanTgt)) {
                            elementsToHighlight.push(el);
                        }
                    });

                    elementsToHighlight.forEach(function(el) {
                        el.dataset.highlighted = 'true';
                        el.dataset.origBorder = el.style.border || '';
                        el.dataset.origShadow = el.style.boxShadow || '';
                        el.dataset.origTransform = el.style.transform || '';
                        el.style.border = '3px solid ' + (color || '#2563eb');
                        el.style.boxShadow = '0 0 16px ' + (color || '#2563eb');
                        el.style.transform = 'scale(1.06)';
                        el.style.transition = 'all 0.2s ease';
                    });
                })($0, $1, $2, $3, $4);
                """, arrow.sourceName(), arrow.targetName(), arrow.colorHex(), isSrcBlock, isTgtBlock);
        });

        row.getElement().addEventListener("mouseleave", e -> {
            row.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-color", "var(--lumo-contrast-15pct)")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

            UI.getCurrent().getPage().executeJs("""
                (function() {
                    document.querySelectorAll('[data-highlighted="true"]').forEach(function(el) {
                        if (el.dataset.origBorder !== undefined) {
                            el.style.border = el.dataset.origBorder;
                            el.style.boxShadow = el.dataset.origShadow;
                            el.style.transform = el.dataset.origTransform;
                            delete el.dataset.origBorder;
                            delete el.dataset.origShadow;
                            delete el.dataset.origTransform;
                            delete el.dataset.highlighted;
                        }
                    });
                })();
                """);
        });

        Span text = new Span(arrow.sourceName() + " ━━━━► " + arrow.targetName());
        text.getStyle().set("font-weight", "700").set("font-size", "0.76rem").set("color", "var(--lumo-header-text-color)");

        Span badge = new Span(arrow.protocol());
        badge.getStyle().set("background", arrow.colorHex()).set("color", "#ffffff").set("padding", "2px 8px").set("border-radius", "10px").set("font-size", "0.68rem").set("font-weight", "700");

        boolean hasNote = arrow.annotation() != null && !arrow.annotation().isBlank();
        Button noteBtn = new Button(VaadinIcon.NOTEBOOK.create(), e -> openArrowAnnotationDialog(arrow));
        noteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, hasNote ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        noteBtn.setTooltipText(hasNote ? "View Annotation: " + arrow.annotation() : "Add annotation to this flow link");

        Button delBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            activeFlowArrows.remove(arrow);
            refreshAllViews();
            updateSaveButtonState();
            NotificationUtils.showSuccessNotification("Removed flow arrow connection.");
        });
        delBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        delBtn.setTooltipText("Delete arrow connection");

        HorizontalLayout right = new HorizontalLayout(badge, noteBtn, delBtn);
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setSpacing(true);

        row.add(text, right);
        return row;
    }

    private Component createArrowToolPill(String protocol, String security, String pattern, String colorHex, String labelText) {
        Div tool = new Div();
        boolean isSelected = isArrowConnectionActive && protocol.equalsIgnoreCase(activeToolProtocol);
        tool.getStyle()
                .set("background", isSelected ? "rgba(37, 99, 235, 0.1)" : "var(--lumo-contrast-5pct)")
                .set("border", isSelected ? "2px solid " + colorHex : "1px solid var(--lumo-contrast-15pct)")
                .set("border-left", "6px solid " + colorHex)
                .set("border-radius", "10px")
                .set("padding", "0.5rem 0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.03)");

        Span text = new Span(labelText);
        text.getStyle().set("font-weight", "700").set("font-size", "0.76rem").set("color", "var(--lumo-header-text-color)");

        Span arrowSymbol = new Span("━━━━►");
        arrowSymbol.getStyle().set("font-weight", "900").set("color", colorHex).set("font-size", "0.85rem");

        tool.add(text, arrowSymbol);

        tool.addClickListener(e -> activateArrowTool(protocol, security, pattern, colorHex));
        return tool;
    }

    private Component createCustomArrowToolPill() {
        Div tool = new Div();
        tool.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px dashed var(--lumo-contrast-30pct)")
                .set("border-radius", "10px")
                .set("padding", "0.45rem 0.75rem")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("cursor", "pointer")
                .set("font-size", "0.74rem")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600");

        Span text = new Span("⚙️ Custom Protocol Arrow...");
        Span symbol = new Span("━━━━►");
        tool.add(text, symbol);

        tool.addClickListener(e -> openCustomArrowToolDialog());
        return tool;
    }

    private void openCustomArrowToolDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚙️ Configure Custom Arrow Tool");
        dialog.setWidth("400px");

        TextField protoField = new TextField("Protocol Name");
        protoField.setValue("gRPC Stream");
        protoField.setWidthFull();

        ComboBox<String> secCombo = new ComboBox<>("Transport Security");
        secCombo.setItems("TLS 1.3 / mTLS", "DTLS", "IPsec VPN", "Unencrypted (Plaintext)");
        secCombo.setValue("TLS 1.3 / mTLS");
        secCombo.setWidthFull();

        ComboBox<String> patCombo = new ComboBox<>("Transmission Pattern");
        patCombo.setItems("Real-time Streaming", "Sync Request-Response", "Event-Driven", "Industrial Fieldbus");
        patCombo.setValue("Real-time Streaming");
        patCombo.setWidthFull();

        Button startBtn = new Button("Activate Arrow Tool", e -> {
            String p = protoField.getValue().trim();
            if (p.isEmpty()) return;
            String sec = secCombo.getValue();
            String pat = patCombo.getValue();
            String color = CommunicationArrowRecord.determineColor(p, sec);

            dialog.close();
            activateArrowTool(p, sec, pat, color);
        });
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(protoField, secCombo, patCombo));
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), startBtn);
        dialog.open();
    }

    private void activateArrowTool(String protocol, String security, String pattern, String colorHex) {
        this.isArrowConnectionActive = true;
        this.activeToolProtocol = protocol;
        this.activeToolSecurity = security;
        this.activeToolPattern = pattern;
        this.activeToolColor = colorHex;
        this.selectedOriginName = null;

        UI.getCurrent().getPage().executeJs("""
            (function(proto, color) {
                var shadow = document.getElementById('arrow-ghost-shadow');
                if (!shadow) {
                    shadow = document.createElement('div');
                    shadow.id = 'arrow-ghost-shadow';
                    shadow.style.cssText = 'position:fixed; pointer-events:none; z-index:99999; padding:6px 14px; border-radius:20px; font-weight:800; font-size:12px; color:#ffffff; box-shadow:0 6px 18px rgba(0,0,0,0.4); transform:translate(14px, 14px); transition: transform 0.05s ease; border: 2px solid #ffffff;';
                    document.body.appendChild(shadow);
                }
                shadow.style.background = color;
                shadow.innerText = '🏹 ' + proto + ' ━━━━► (Click 1: Select Origin)';
                shadow.style.display = 'block';

                window.onmousemove = function(evt) {
                    shadow.style.left = evt.clientX + 'px';
                    shadow.style.top = evt.clientY + 'px';
                };
            })($0, $1);
            """, protocol, colorHex);

        refreshAllViews();
        NotificationUtils.showSuccessNotification("Activated Arrow Tool: " + protocol + ". Click 1 = ORIGIN, Click 2 = DESTINATION.");
    }

    @ClientCallable
    public void cancelArrowTool() {
        if (isArrowConnectionActive) {
            deactivateArrowTool();
            NotificationUtils.showWarningNotification("Arrow Tool cancelled.");
        }
    }

    private void deactivateArrowTool() {
        this.isArrowConnectionActive = false;
        this.activeToolProtocol = null;
        this.activeToolSecurity = null;
        this.activeToolPattern = null;
        this.activeToolColor = null;
        this.selectedOriginName = null;

        UI.getCurrent().getPage().executeJs("""
            var shadow = document.getElementById('arrow-ghost-shadow');
            if (shadow) shadow.style.display = 'none';
            """);

        refreshAllViews();
    }

    private boolean isArchitecturePatternBlock(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("architecture pattern") || n.startsWith("pattern:");
    }

    private boolean isTechLayerBlock(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("edge layer") || n.contains("fog") || n.contains("cloud & enterprise") || n.contains("cloud layer");
    }

    private boolean isOtherLayerBlock(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("selected domain") || n.startsWith("domain:") || n.contains("target quality requirements") || n.contains("quality requirements");
    }

    private boolean isAnyLayerBlock(String name) {
        return isArchitecturePatternBlock(name) || isTechLayerBlock(name) || isOtherLayerBlock(name);
    }

    private void handleCanvasItemClick(String name) {
        if (!isArrowConnectionActive) return;

        if (selectedOriginName == null) {
            selectedOriginName = name;
            UI.getCurrent().getPage().executeJs("""
                (function(name, proto) {
                    var shadow = document.getElementById('arrow-ghost-shadow');
                    if (shadow) {
                        shadow.innerText = '🏹 ' + proto + ' [' + name + '] ━━━━► (Click 2: Select Destination)';
                    }
                })($0, $1);
                """, selectedOriginName, activeToolProtocol);

            NotificationUtils.showSuccessNotification("Selected ORIGIN: '" + selectedOriginName + "'. Now click DESTINATION.");
            refreshCanvasView();
        } else {
            if (selectedOriginName.equalsIgnoreCase(name)) {
                NotificationUtils.showErrorNotification("Destination must be different from Origin.");
                return;
            }

            // Layer block arrow constraint validation:
            boolean isOriginBlock = isAnyLayerBlock(selectedOriginName);
            boolean isDestBlock = isAnyLayerBlock(name);

            if (isOriginBlock || isDestBlock) {
                boolean isOriginArch = isArchitecturePatternBlock(selectedOriginName);
                boolean isDestArch = isArchitecturePatternBlock(name);
                boolean isOriginTech = isTechLayerBlock(selectedOriginName);
                boolean isDestTech = isTechLayerBlock(name);

                boolean validBlockPair = (isOriginArch && isDestTech) || (isOriginTech && isDestArch);
                if (!validBlockPair) {
                    NotificationUtils.showErrorNotification("Layer block arrows are only allowed between Architecture Pattern and Edge / Fog / Cloud layers.");
                    return;
                }
            }

            CommunicationArrowRecord newArrow = new CommunicationArrowRecord(
                    UUID.randomUUID().toString(),
                    selectedOriginName,
                    name,
                    activeToolProtocol,
                    activeToolSecurity,
                    activeToolPattern,
                    activeToolColor
            );

            activeFlowArrows.add(newArrow);
            NotificationUtils.showSuccessNotification("Connected Flow Arrow: " + selectedOriginName + " ━━━━► " + name);
            deactivateArrowTool();
            updateSaveButtonState();
        }
    }

    private Component createFlowArrowCard(CommunicationArrowRecord arrow) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-15pct)")
                .set("border-radius", "10px")
                .set("padding", "0.65rem")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.4rem")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.03)");

        // Top Row: Delete Action
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span label = new Span("Arrow Flow Link:");
        label.getStyle().set("font-weight", "600").set("font-size", "0.72rem").set("color", "var(--lumo-secondary-text-color)");

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            activeFlowArrows.remove(arrow);
            refreshAllViews();
            NotificationUtils.showSuccessNotification("Removed flow arrow connection.");
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.setTooltipText("Remove connection");

        topRow.add(label, deleteBtn);

        // Graphical Visual Arrow inside Card
        Component arrowGraphic = createGraphicalArrowElement(arrow);

        card.add(topRow, arrowGraphic);
        return card;
    }

    private Component createLegendBox() {
        Div legendCard = new Div();
        legendCard.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "0.75rem 1rem")
                .set("font-size", "0.75rem")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.4rem");

        Span legendTitle = new Span("🎨 Arrow Color Legend");
        legendTitle.getStyle().set("font-weight", "700").set("color", "var(--lumo-primary-text-color)");

        legendCard.add(legendTitle);
        legendCard.add(createLegendItem("#10b981", "🟢 Real-time Streaming (MQTT / CoAP)"));
        legendCard.add(createLegendItem("#2563eb", "🔵 Request-Response (HTTPS / REST / gRPC)"));
        legendCard.add(createLegendItem("#8b5cf6", "🟣 Event-Driven Broker (Kafka / AMQP)"));
        legendCard.add(createLegendItem("#f59e0b", "🟠 Industrial Fieldbus (Modbus / OPC-UA)"));
        legendCard.add(createLegendItem("#ef4444", "🔴 Unencrypted Warning (Plaintext Link)"));

        return legendCard;
    }

    private Component createLegendItem(String colorHex, String labelText) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);

        Span dot = new Span();
        dot.getStyle()
                .set("width", "10px")
                .set("height", "10px")
                .set("border-radius", "50%")
                .set("background-color", colorHex)
                .set("display", "inline-block");

        Span label = new Span(labelText);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.72rem");

        item.add(dot, label);
        return item;
    }

    private void openAddArrowDialog(String initialSource, String initialTarget) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚡ Connect Flow Arrow (Origem ➔ Destino)");
        dialog.setWidth("520px");

        List<String> options = new ArrayList<>();
        options.add("Edge Layer (Devices & Sensors)");
        for (String t : edgeTechs) options.add("Edge: " + t);
        options.add("Fog / Gateway Layer");
        for (String t : fogTechs) options.add("Fog: " + t);
        options.add("Cloud & Enterprise Layer");
        for (String t : cloudTechs) options.add("Cloud: " + t);

        ComboBox<String> sourceCombo = new ComboBox<>("Source Component / Layer (Origem)");
        sourceCombo.setItems(options);
        sourceCombo.setValue(initialSource != null ? initialSource : options.get(0));
        sourceCombo.setWidthFull();

        ComboBox<String> targetCombo = new ComboBox<>("Target Component / Layer (Destino)");
        targetCombo.setItems(options);
        targetCombo.setValue(initialTarget != null ? initialTarget : (options.size() > 2 ? options.get(2) : options.get(0)));
        targetCombo.setWidthFull();

        ComboBox<String> protocolCombo = new ComboBox<>("Communication Protocol");
        protocolCombo.setItems("MQTT", "CoAP", "HTTPS / REST", "gRPC", "AMQP / RabbitMQ", "Modbus / Industrial", "WebSockets", "Kafka Stream");
        protocolCombo.setValue("MQTT");
        protocolCombo.setWidthFull();

        ComboBox<String> securityCombo = new ComboBox<>("Transport Security");
        securityCombo.setItems("TLS 1.3 / mTLS", "DTLS", "IPsec VPN", "Unencrypted (Plaintext)");
        securityCombo.setValue("TLS 1.3 / mTLS");
        securityCombo.setWidthFull();

        ComboBox<String> patternCombo = new ComboBox<>("Data Transmission Pattern");
        patternCombo.setItems("Real-time Streaming", "Periodic Batch (Buffered)", "Event-Driven (Pub/Sub)");
        patternCombo.setValue("Real-time Streaming");
        patternCombo.setWidthFull();

        Button saveBtn = new Button("Create Flow Arrow", VaadinIcon.PLUS.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            String src = sourceCombo.getValue();
            String tgt = targetCombo.getValue();
            if (src == null || tgt == null || src.equals(tgt)) {
                NotificationUtils.showErrorNotification("Please select distinct origin and destination components.");
                return;
            }

            String proto = protocolCombo.getValue();
            String sec = securityCombo.getValue();
            String pat = patternCombo.getValue();
            String color = CommunicationArrowRecord.determineColor(proto, sec);

            CommunicationArrowRecord arrow = new CommunicationArrowRecord(
                    UUID.randomUUID().toString(),
                    src,
                    tgt,
                    proto,
                    sec,
                    pat,
                    color
            );

            activeFlowArrows.add(arrow);
            refreshAllViews();
            updateSaveButtonState();
            NotificationUtils.showSuccessNotification("Added arrow: " + src + " ➔ " + tgt);
            dialog.close();
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                new Paragraph("Select origin, destination, and communication protocol to generate a color-coded flow arrow link for AI evaluation."),
                sourceCombo, targetCombo, protocolCombo, securityCombo, patternCombo
        );
        layout.setPadding(false);
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    @ClientCallable
    public void dropPillOnLayer(String pillName, String category, String targetLayer) {
        if (pillName == null || targetLayer == null || pillName.trim().isEmpty()) return;

        String lowerLayer = targetLayer.toLowerCase().trim();
        String cat = category != null ? category.toLowerCase().trim() : "tech";

        if (lowerLayer.contains("domain")) {
            if (!"domain".equals(cat)) {
                NotificationUtils.showWarningNotification("Only IoT Domain items can be added to Selected Domain.");
                return;
            }
            this.selectedDomain = pillName;
            NotificationUtils.showSuccessNotification("Domain set to: " + pillName);
        } else if (lowerLayer.contains("pattern")) {
            if (!"pattern".equals(cat)) {
                NotificationUtils.showWarningNotification("Only Architecture Pattern items can be added to Selected Pattern.");
                return;
            }
            this.selectedPattern = pillName;
            NotificationUtils.showSuccessNotification("Pattern set to: " + pillName);
        } else if (lowerLayer.contains("quality") || lowerLayer.contains("iso")) {
            if (!"quality".equals(cat)) {
                NotificationUtils.showWarningNotification("Only Quality Requirements can be added to Target Quality Requirements.");
                return;
            }
            if (!this.qualityReqs.contains(pillName)) {
                this.qualityReqs.add(pillName);
                NotificationUtils.showSuccessNotification("Added Requirement: " + pillName);
            }
        } else if (lowerLayer.contains("edge") || lowerLayer.contains("fog") || lowerLayer.contains("gateway") || lowerLayer.contains("cloud") || lowerLayer.contains("enterprise")) {
            // Target is a technology stack layer!
            if ("domain".equals(cat) || "pattern".equals(cat) || "quality".equals(cat)) {
                NotificationUtils.showWarningNotification("Domains, Patterns, and Quality Requirements cannot be added to Technology layers.");
                return;
            }

            if (lowerLayer.contains("edge")) {
                if (!this.edgeTechs.contains(pillName)) {
                    this.edgeTechs.add(pillName);
                    NotificationUtils.showSuccessNotification("Added to Edge Layer: " + pillName);
                }
            } else if (lowerLayer.contains("fog") || lowerLayer.contains("gateway")) {
                if (!this.fogTechs.contains(pillName)) {
                    this.fogTechs.add(pillName);
                    NotificationUtils.showSuccessNotification("Added to Fog / Gateway Layer: " + pillName);
                }
            } else if (lowerLayer.contains("cloud") || lowerLayer.contains("enterprise")) {
                if (!this.cloudTechs.contains(pillName)) {
                    this.cloudTechs.add(pillName);
                    NotificationUtils.showSuccessNotification("Added to Cloud Layer: " + pillName);
                }
            }
        }

        refreshAllViews();
        updateSaveButtonState();
    }

    private void exportCanvasAsImage() {
        String script = """
            (function(containerId, domainName) {
                var el = document.getElementById(containerId);
                if (!el) {
                    console.error("Canvas element not found:", containerId);
                    return;
                }

                function captureAndDownload() {
                    html2canvas(el, {
                        scale: 2,
                        backgroundColor: '#ffffff',
                        useCORS: true,
                        logging: false
                    }).then(function(canvas) {
                        var link = document.createElement('a');
                        var safeDomain = (domainName || 'architecture').toLowerCase().replace(/[^a-z0-9]/g, '_');
                        link.download = 'iot_blueprint_' + safeDomain + '_' + Date.now() + '.png';
                        link.href = canvas.toDataURL('image/png');
                        link.click();
                    }).catch(function(err) {
                        console.error("Error capturing blueprint canvas image:", err);
                    });
                }

                if (typeof html2canvas === 'undefined') {
                    var scriptEl = document.createElement('script');
                    scriptEl.src = 'https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js';
                    scriptEl.onload = captureAndDownload;
                    document.head.appendChild(scriptEl);
                } else {
                    captureAndDownload();
                }
            })($0, $1);
            """;

        UI.getCurrent().getPage().executeJs(script, "architecture-canvas-container", selectedDomain);
        NotificationUtils.showSuccessNotification("Generating architecture blueprint PNG image...");
    }

    private Component createCanvasLayerBox(String titleText, Div... containers) {
        Div box = new Div();
        String cleanLayerName = titleText
                .replace("⚡ ", "")
                .replace("🌉 ", "")
                .replace("☁️ ", "")
                .replace("🌐 ", "")
                .replace("🏛️ ", "")
                .replace("🛡️ ", "")
                .trim();
        String domId = "canvas-layer-" + cleanLayerName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        box.setId(domId);
        box.getElement().setAttribute("data-drop-layer", cleanLayerName);
        box.getElement().setAttribute("data-layer-name", cleanLayerName);

        boolean isConnectableBlock = isArchitecturePatternBlock(cleanLayerName) || isTechLayerBlock(cleanLayerName);
        boolean isSelectedOrigin = isArrowConnectionActive && cleanLayerName.equalsIgnoreCase(selectedOriginName);

        box.getStyle()
                .set("background", isSelectedOrigin ? "rgba(37, 99, 235, 0.08)" : "var(--lumo-contrast-5pct)")
                .set("border", isSelectedOrigin ? "2px solid " + (activeToolColor != null ? activeToolColor : "#2563eb") : (isArrowConnectionActive && isConnectableBlock ? "2px dashed #2563eb" : "1px dashed var(--lumo-contrast-20pct)"))
                .set("box-shadow", isSelectedOrigin ? "0 0 14px " + activeToolColor : "none")
                .set("border-radius", "12px")
                .set("padding", "0.75rem 1rem")
                .set("margin-bottom", "0.75rem")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("cursor", isArrowConnectionActive && isConnectableBlock ? "pointer" : "default")
                .set("transition", "all 0.2s ease");

        String hint = "";
        if (isArrowConnectionActive && isConnectableBlock) {
            hint = " 🎯 (Click block to select as " + (selectedOriginName == null ? "ORIGIN" : "DESTINATION") + ")";
        }
        Span header = new Span(titleText + hint);
        header.getStyle().set("font-size", "0.78rem").set("font-weight", "700").set("color", "var(--lumo-primary-text-color)").set("display", "block").set("margin-bottom", "0.4rem");

        box.add(header);
        for (Div container : containers) {
            container.getStyle().set("display", "flex").set("gap", "0.4rem").set("flex-wrap", "wrap");
            box.add(container);
        }

        if (isArrowConnectionActive && isConnectableBlock) {
            box.addClickListener(e -> handleCanvasItemClick(cleanLayerName));
        }

        return box;
    }

    private String createStateSnapshot() {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("domain", selectedDomain != null ? selectedDomain : "");
            map.put("pattern", selectedPattern != null ? selectedPattern : "");
            map.put("edgeTechs", new ArrayList<>(edgeTechs));
            map.put("fogTechs", new ArrayList<>(fogTechs));
            map.put("cloudTechs", new ArrayList<>(cloudTechs));
            map.put("qualityReqs", new ArrayList<>(qualityReqs));
            map.put("notes", notesTextArea.getValue() != null ? notesTextArea.getValue().trim() : "");
            map.put("activeFlowArrows", new ArrayList<>(activeFlowArrows));
            map.put("arrowPositions", new LinkedHashMap<>(arrowPositionsMap));
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "";
        }
    }

    @ClientCallable
    public void updateArrowPosition(String arrowId, String posJson) {
        if (arrowId != null && posJson != null) {
            arrowPositionsMap.put(arrowId, posJson);
            updateSaveButtonState();
        }
    }

    private void updateSaveButtonState() {
        String currentSnapshot = createStateSnapshot();
        boolean dirty = !currentSnapshot.equals(lastSavedSnapshot);
        saveButton.setEnabled(dirty);
        if (dirty) {
            saveButton.getStyle().remove("opacity");
            saveButton.setTooltipText("Save unsaved changes to your architecture blueprint");
        } else {
            saveButton.getStyle().set("opacity", "0.55");
            saveButton.setTooltipText("No unsaved changes");
        }
    }

    private void initDefaultComposition() {
        this.selectedDomain = null;
        this.selectedPattern = null;
        this.edgeTechs.clear();
        this.fogTechs.clear();
        this.cloudTechs.clear();
        this.qualityReqs.clear();
    }

    private void refreshCanvasView() {
        // Domain (Emerald Green)
        domainContainer.removeAll();
        if (selectedDomain != null && !selectedDomain.isEmpty()) {
            Span domainPill = createRemovablePill("Domain: " + selectedDomain, COLOR_DOMAIN, () -> {
                selectedDomain = null;
                refreshAllViews();
            });
            domainContainer.add(domainPill);
        } else {
            Span empty = new Span("No Domain selected (Click a domain pill in the palette)");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            domainContainer.add(empty);
        }

        // Pattern (Purple Violet)
        patternContainer.removeAll();
        if (selectedPattern != null && !selectedPattern.isEmpty()) {
            Span patternPill = createRemovablePill("Pattern: " + selectedPattern, COLOR_PATTERN, () -> {
                selectedPattern = null;
                refreshAllViews();
            });
            patternContainer.add(patternPill);
        } else {
            Span empty = new Span("No Pattern selected (Click a pattern pill in the palette)");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            patternContainer.add(empty);
        }

        // Edge Techs (Royal Blue)
        edgeContainer.removeAll();
        if (edgeTechs.isEmpty()) {
            Span empty = new Span("No Edge Layer components added");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            edgeContainer.add(empty);
        } else {
            for (String tech : edgeTechs) {
                Span pill = createRemovablePill(tech, COLOR_EDGE, () -> {
                    edgeTechs.remove(tech);
                    refreshAllViews();
                });
                edgeContainer.add(pill);
            }
        }

        // Fog Techs (Amber Orange)
        fogContainer.removeAll();
        if (fogTechs.isEmpty()) {
            Span empty = new Span("No Fog / Gateway components added");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            fogContainer.add(empty);
        } else {
            for (String tech : fogTechs) {
                Span pill = createRemovablePill(tech, COLOR_FOG, () -> {
                    fogTechs.remove(tech);
                    refreshAllViews();
                });
                fogContainer.add(pill);
            }
        }

        // Cloud Techs (Cyan Indigo)
        cloudContainer.removeAll();
        if (cloudTechs.isEmpty()) {
            Span empty = new Span("No Cloud & Enterprise components added");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            cloudContainer.add(empty);
        } else {
            for (String tech : cloudTechs) {
                Span pill = createRemovablePill(tech, COLOR_CLOUD, () -> {
                    cloudTechs.remove(tech);
                    refreshAllViews();
                });
                cloudContainer.add(pill);
            }
        }

        // Quality Reqs (Rose Red)
        qualityReqContainer.removeAll();
        if (qualityReqs.isEmpty()) {
            Span empty = new Span("No Quality Requirements added");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem").set("font-style", "italic");
            qualityReqContainer.add(empty);
        } else {
            for (String qr : qualityReqs) {
                Span pill = createRemovablePill(qr, COLOR_QUALITY, () -> {
                    qualityReqs.remove(qr);
                    refreshAllViews();
                });
                qualityReqContainer.add(pill);
            }
        }
        rebuildCanvasContent();
    }

    private Span createPill(String label, String bgColor, boolean clickable, boolean disabled, String category) {
        Span pill = new Span(label);
        if (disabled) {
            pill.getStyle()
                    .set("font-size", "0.72rem")
                    .set("font-weight", "600")
                    .set("padding", "4px 10px")
                    .set("border-radius", "12px")
                    .set("background", "#94a3b8")
                    .set("color", "#ffffff")
                    .set("opacity", "0.45")
                    .set("cursor", "not-allowed")
                    .set("user-select", "none")
                    .set("filter", "grayscale(70%)");
        } else {
            pill.getElement().setAttribute("draggable", "true");
            pill.getElement().setAttribute("data-pill-name", label);
            pill.getElement().setAttribute("data-pill-category", category != null ? category : "tech");
            pill.getStyle()
                    .set("font-size", "0.72rem")
                    .set("font-weight", "600")
                    .set("padding", "4px 10px")
                    .set("border-radius", "12px")
                    .set("background", bgColor)
                    .set("color", "#ffffff")
                    .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)")
                    .set("cursor", "grab")
                    .set("user-select", "none")
                    .set("transition", "transform 0.15s ease");

            if (clickable) {
                pill.getElement().addEventListener("mouseenter", e -> pill.getStyle().set("transform", "scale(1.05)"));
                pill.getElement().addEventListener("mouseleave", e -> pill.getStyle().set("transform", "scale(1)"));
            }
        }
        return pill;
    }

    private Span createPill(String label, String bgColor, boolean clickable, boolean disabled) {
        return createPill(label, bgColor, clickable, disabled, "tech");
    }

    private Span createRemovablePill(String label, String bgColor, Runnable onRemove) {
        String cleanName = label.endsWith(" ✕") ? label.substring(0, label.length() - 2) : label;
        String domId = "canvas-pill-" + cleanName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        boolean isSelectedOrigin = isArrowConnectionActive && cleanName.equalsIgnoreCase(selectedOriginName);

        Span pill = new Span(label + (onRemove != null && !isArrowConnectionActive ? " ✕" : ""));
        pill.setId(domId);
        pill.getElement().setAttribute("data-pill-name", cleanName);
        pill.getStyle()
                .set("font-size", "0.72rem")
                .set("font-weight", "600")
                .set("padding", "4px 10px")
                .set("border-radius", "12px")
                .set("background", isSelectedOrigin ? (activeToolColor != null ? activeToolColor : "#2563eb") : bgColor)
                .set("color", "#ffffff")
                .set("box-shadow", isSelectedOrigin ? "0 0 12px " + activeToolColor : "0 2px 6px rgba(0,0,0,0.1)")
                .set("border", isSelectedOrigin ? "2px solid #ffffff" : "none")
                .set("cursor", "pointer")
                .set("user-select", "none");

        pill.getElement().executeJs("this.addEventListener('click', function(e){ e.stopPropagation(); });");
        pill.addClickListener(e -> {
            if (isArrowConnectionActive) {
                handleCanvasItemClick(cleanName);
            } else if (onRemove != null) {
                onRemove.run();
            }
        });
        return pill;
    }

    private void resetCanvas() {
        this.currentArchitecture = new SavedArchitecture();
        this.notesTextArea.clear();
        this.activeFlowArrows.clear();
        initDefaultComposition();
        this.lastSavedSnapshot = createStateSnapshot();
        refreshAllViews();
        aiReportDiv.setText("Canvas reset. Select components from the palette and click 'Evaluate with AI'.");
        NotificationUtils.showSuccessNotification("Canvas reset to clean state.");
    }

    private void openSaveDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(currentArchitecture.getId() == null ? "Save Architecture Blueprint" : "Edit Saved Architecture");
        dialog.setWidth("500px");

        TextField nameField = new TextField("Architecture Name");
        nameField.setWidthFull();
        nameField.setValue(currentArchitecture.getName() != null ? currentArchitecture.getName() : "My IoT Architecture");

        TextArea descArea = new TextArea("Description / Architectural Notes");
        descArea.setWidthFull();
        descArea.setHeight("120px");
        descArea.setValue(notesTextArea.getValue() != null ? notesTextArea.getValue() : (currentArchitecture.getDescription() != null ? currentArchitecture.getDescription() : ""));

        Button confirmBtn = new Button("Save to Database", VaadinIcon.CHECK.create());
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmBtn.addClickListener(e -> {
            String name = nameField.getValue().trim();
            if (name.isEmpty()) {
                NotificationUtils.showErrorNotification("Please enter an architecture name.");
                return;
            }

            try {
                String notesVal = descArea.getValue();
                notesTextArea.setValue(notesVal != null ? notesVal : "");
                currentArchitecture.setName(name);
                currentArchitecture.setDescription(notesVal);
                currentArchitecture.setDomain(selectedDomain);
                currentArchitecture.setPattern(selectedPattern);

                Map<String, Object> stateMap = new LinkedHashMap<>();
                stateMap.put("domain", selectedDomain);
                stateMap.put("pattern", selectedPattern);
                stateMap.put("edgeTechs", new ArrayList<>(edgeTechs));
                stateMap.put("fogTechs", new ArrayList<>(fogTechs));
                stateMap.put("cloudTechs", new ArrayList<>(cloudTechs));
                stateMap.put("qualityReqs", new ArrayList<>(qualityReqs));
                stateMap.put("activeFlowArrows", activeFlowArrows);

                currentArchitecture.setLayersJson(objectMapper.writeValueAsString(stateMap));

                savedArchitectureService.save(currentArchitecture, SecurityUtils.getUsername());
                this.lastSavedSnapshot = createStateSnapshot();
                updateSaveButtonState();
                NotificationUtils.showSuccessNotification("Architecture '" + name + "' saved successfully!");
                dialog.close();
            } catch (Exception ex) {
                logger.error("Error saving architecture", ex);
                NotificationUtils.showErrorNotification("Error saving architecture. Please try again.");
            }
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(nameField, descArea));
        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }

    private void openGalleryDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📁 Saved Architectures Gallery");
        dialog.setWidth("750px");

        VerticalLayout listContainer = new VerticalLayout();
        listContainer.setPadding(false);
        listContainer.setSpacing(true);

        List<SavedArchitecture> list = savedArchitectureService.findByUserName(SecurityUtils.getUsername());

        if (list.isEmpty()) {
            listContainer.add(new Paragraph("No saved architectures found. Save your current canvas to view it here."));
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (SavedArchitecture arch : list) {
                Div itemCard = new Div();
                itemCard.getStyle()
                        .set("background", "var(--lumo-contrast-5pct)")
                        .set("border", "1px solid var(--lumo-contrast-15pct)")
                        .set("border-radius", "12px")
                        .set("padding", "0.85rem 1.1rem")
                        .set("width", "100%")
                        .set("box-sizing", "border-box");

                H4 archName = new H4(arch.getName());
                archName.getStyle().set("margin", "0 0 0.2rem 0");

                String dateStr = arch.getUpdatedAt() != null ? arch.getUpdatedAt().format(formatter) : "";
                Span meta = new Span("Domain: " + (arch.getDomain() != null ? arch.getDomain() : "None") + " | Pattern: " + (arch.getPattern() != null ? arch.getPattern() : "None") + " | Updated: " + dateStr);
                meta.getStyle().set("font-size", "0.72rem").set("color", "var(--lumo-secondary-text-color)").set("display", "block").set("margin-bottom", "0.4rem");

                Paragraph desc = new Paragraph(arch.getDescription() != null ? arch.getDescription() : "");
                desc.getStyle().set("font-size", "0.78rem").set("margin", "0 0 0.5rem 0");

                HorizontalLayout btnGroup = new HorizontalLayout();
                Button loadBtn = new Button("Load / Open", VaadinIcon.UPLOAD.create(), event -> {
                    loadSavedArchitecture(arch);
                    dialog.close();
                });
                loadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

                Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create(), event -> {
                    ConfirmDialog confirm = new ConfirmDialog();
                    confirm.setHeader("Delete Architecture: '" + arch.getName() + "'?");
                    confirm.setText("Are you sure you want to permanently delete this saved architecture?");
                    confirm.setConfirmText("Delete");
                    confirm.setConfirmButtonTheme("error primary");
                    confirm.addConfirmListener(c -> {
                        savedArchitectureService.deleteById(arch.getId());
                        NotificationUtils.showSuccessNotification("Architecture deleted.");
                        dialog.close();
                        openGalleryDialog();
                    });
                    confirm.open();
                });
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);

                btnGroup.add(loadBtn, deleteBtn);
                itemCard.add(archName, meta, desc, btnGroup);
                listContainer.add(itemCard);
            }
        }

        dialog.add(listContainer);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void loadSavedArchitecture(SavedArchitecture arch) {
        try {
            this.currentArchitecture = arch;
            this.selectedDomain = arch.getDomain();
            this.selectedPattern = arch.getPattern();
            this.notesTextArea.setValue(arch.getDescription() != null ? arch.getDescription() : "");

            if (arch.getLayersJson() != null && !arch.getLayersJson().isEmpty()) {
                Map<String, Object> stateMap = objectMapper.readValue(arch.getLayersJson(), new TypeReference<>() {});
                this.edgeTechs.clear();
                if (stateMap.containsKey("edgeTechs")) {
                    this.edgeTechs.addAll((List<String>) stateMap.get("edgeTechs"));
                } else if (stateMap.containsKey("edge")) {
                    this.edgeTechs.addAll((List<String>) stateMap.get("edge"));
                }

                this.fogTechs.clear();
                if (stateMap.containsKey("fogTechs")) {
                    this.fogTechs.addAll((List<String>) stateMap.get("fogTechs"));
                } else if (stateMap.containsKey("fog")) {
                    this.fogTechs.addAll((List<String>) stateMap.get("fog"));
                }

                this.cloudTechs.clear();
                if (stateMap.containsKey("cloudTechs")) {
                    this.cloudTechs.addAll((List<String>) stateMap.get("cloudTechs"));
                } else if (stateMap.containsKey("cloud")) {
                    this.cloudTechs.addAll((List<String>) stateMap.get("cloud"));
                }

                this.qualityReqs.clear();
                if (stateMap.containsKey("qualityReqs")) {
                    this.qualityReqs.addAll((List<String>) stateMap.get("qualityReqs"));
                } else if (stateMap.containsKey("quality")) {
                    this.qualityReqs.addAll((List<String>) stateMap.get("quality"));
                }

                this.activeFlowArrows.clear();
                if (stateMap.containsKey("activeFlowArrows")) {
                    List<CommunicationArrowRecord> arrows = objectMapper.convertValue(stateMap.get("activeFlowArrows"), new TypeReference<List<CommunicationArrowRecord>>() {});
                    if (arrows != null) {
                        this.activeFlowArrows.addAll(arrows);
                    }
                }
            }

            if (arch.getAiEvaluationReport() != null && !arch.getAiEvaluationReport().isEmpty()) {
                this.lastAiEvaluationReport = arch.getAiEvaluationReport();
                aiReportDiv.getElement().setProperty("innerHTML", arch.getAiEvaluationReport());
                exportPdfBtn.setEnabled(true);
            } else {
                this.lastAiEvaluationReport = null;
                exportPdfBtn.setEnabled(false);
            }

            refreshAllViews();
            this.lastSavedSnapshot = createStateSnapshot();
            updateSaveButtonState();
            NotificationUtils.showSuccessNotification("Loaded architecture: " + arch.getName());
        } catch (Exception e) {
            logger.error("Error parsing saved architecture state JSON", e);
            NotificationUtils.showErrorNotification("Error loading architecture state.");
        }
    }

    private void runAiEvaluation() {
        if (selectedDomain == null && selectedPattern == null && edgeTechs.isEmpty() && fogTechs.isEmpty() && cloudTechs.isEmpty() && qualityReqs.isEmpty()) {
            NotificationUtils.showErrorNotification("Please select a domain, pattern, or add components to your canvas before running AI evaluation.");
            return;
        }

        showAiReportWindow();

        String userNotes = notesTextArea.getValue() != null ? notesTextArea.getValue().trim() : "";

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Please provide a comprehensive architectural evaluation, gap analysis, and trade-off assessment in English for the following IoT system composition:\n\n");
        promptBuilder.append("- **Target IoT Domain:** ").append(selectedDomain).append("\n");
        promptBuilder.append("- **Selected Architecture Pattern:** ").append(selectedPattern).append("\n");
        promptBuilder.append("- **Edge Layer (Devices & Sensors):** ").append(String.join(", ", edgeTechs)).append("\n");
        promptBuilder.append("- **Fog / Gateway Layer (Local Processing & Security):** ").append(String.join(", ", fogTechs)).append("\n");
        promptBuilder.append("- **Cloud & Enterprise Layer:** ").append(String.join(", ", cloudTechs)).append("\n");
        promptBuilder.append("- **Target Quality Requirements (ISO 25010):** ").append(String.join(", ", qualityReqs)).append("\n");

        if (!activeFlowArrows.isEmpty()) {
            promptBuilder.append("- **Active Inter-Layer Communication Flow Arrows:**\n");
            for (CommunicationArrowRecord arrow : activeFlowArrows) {
                promptBuilder.append("  • ").append(arrow.getFormattedSummary()).append("\n");
            }
        } else {
            promptBuilder.append("- **Active Inter-Layer Communication Flow Arrows:** None configured.\n");
        }

        if (!userNotes.isEmpty()) {
            promptBuilder.append("- **Architect's Notes & Objectives:** ").append(userNotes).append("\n");
        }

        promptBuilder.append("\nStructure your evaluation report in English with the following sections:\n");
        promptBuilder.append("1. **Architecture Compatibility Score & Evidence Level** (e.g. Score: 85/100 | Evidence Confidence Level: High Evidence / Medium Evidence / General Domain Evidence based on Assistant context coverage)\n");
        promptBuilder.append("2. **Layer Placement & Component Allocation Check** (Inspect Edge, Fog, and Cloud layers for misplaced cloud ecosystems in Edge/Fog, heavy analytics in Fog, or security mechanisms placed as hardware tech)\n");
        promptBuilder.append("3. **Inter-Layer Communication & Protocol Security Diagnostics** (Inspect protocols like MQTT/CoAP/HTTPS/gRPC, transport encryption like TLS/DTLS, plaintext link warnings, and transmission patterns for latency, security risks, and throughput bottlenecks)\n");
        promptBuilder.append("4. **Native Strengths** (How the chosen pattern, flow protocols & technologies satisfy the quality requirements & goals)\n");
        promptBuilder.append("5. **Gaps & Vulnerabilities** (Identified security risks, plaintext in-transit communication, latency issues, missing buffer layers)\n");
        promptBuilder.append("6. **Adaptability Recommendations** (Actionable technical fixes, protocol upgrades, and layer component extensions)\n");
        promptBuilder.append("7. **Academic & Literature References** (Cite relevant papers from the knowledge base)\n\n");
        promptBuilder.append("CRITICAL: Write the entire response in English using clean HTML formatting (<h3>, <ul>, <li>, <strong>, <p>). Never refuse to evaluate or return static fallback blocks.");

        String notesSummaryLine = userNotes.isEmpty() ? "" : String.format("• <strong>Architect's Notes:</strong> %s<br/>", userNotes);
        String flowsSummaryLine = activeFlowArrows.isEmpty() ? "None" : activeFlowArrows.stream().map(CommunicationArrowRecord::getFormattedSummary).collect(Collectors.joining("<br/>• "));

        String promptSummaryHtml = String.format("""
            <div style="background: var(--lumo-contrast-5pct); border-left: 4px solid var(--lumo-primary-color); padding: 8px 12px; margin-bottom: 12px; border-radius: 8px; font-size: 0.76rem; color: var(--lumo-secondary-text-color);">
                <strong style="color: var(--lumo-primary-text-color);">📋 Evaluated Stack Prompt:</strong><br/>
                • <strong>Domain:</strong> %s | <strong>Pattern:</strong> %s<br/>
                • <strong>Edge:</strong> %s<br/>
                • <strong>Fog:</strong> %s<br/>
                • <strong>Cloud:</strong> %s<br/>
                • <strong>Quality Reqs:</strong> %s<br/>
                • <strong>Communication Arrows:</strong> %s<br/>
                %s
            </div>
            """,
            selectedDomain,
            selectedPattern,
            edgeTechs.isEmpty() ? "None" : String.join(", ", edgeTechs),
            fogTechs.isEmpty() ? "None" : String.join(", ", fogTechs),
            cloudTechs.isEmpty() ? "None" : String.join(", ", cloudTechs),
            qualityReqs.isEmpty() ? "None" : String.join(", ", qualityReqs),
            flowsSummaryLine,
            notesSummaryLine
        );

        String query = promptBuilder.toString();
        aiReportDiv.getElement().setProperty("innerHTML", promptSummaryHtml + "<p>⏳ <em>Evaluating your architecture stack with AI Assistant...</em> <span class='cursor-blink'>▌</span></p>");
        evaluateButton.setEnabled(false);
        exportPdfBtn.setEnabled(false);

        var ui = UI.getCurrent();
        StringBuilder responseBuf = new StringBuilder();

        if (activeEvaluationSubscription != null && !activeEvaluationSubscription.isDisposed()) {
            activeEvaluationSubscription.dispose();
        }

        activeEvaluationSubscription = aiRagService.queryStream(query, UUID.randomUUID())
                .subscribe(
                        chunk -> {
                            responseBuf.append(chunk);
                            if (ui != null) {
                                ui.access(() -> {
                                    aiReportDiv.getElement().setProperty("innerHTML", promptSummaryHtml + responseBuf.toString() + " <span class='cursor-blink'>▌</span>");
                                    ui.push();
                                });
                            }
                        },
                        error -> {
                            if (ui != null) {
                                ui.access(() -> {
                                    aiReportDiv.getElement().setProperty("innerHTML", promptSummaryHtml + "<p style='color: var(--lumo-error-text-color);'>Error evaluating architecture: " + error.getMessage() + "</p>");
                                    evaluateButton.setEnabled(true);
                                    exportPdfBtn.setEnabled(lastAiEvaluationReport != null && !lastAiEvaluationReport.trim().isEmpty());
                                    ui.push();
                                });
                            }
                        },
                        () -> {
                            if (ui != null) {
                                ui.access(() -> {
                                    String finalReport = promptSummaryHtml + responseBuf.toString().trim();
                                    this.lastAiEvaluationReport = finalReport;
                                    aiReportDiv.getElement().setProperty("innerHTML", finalReport);
                                    currentArchitecture.setAiEvaluationReport(finalReport);
                                    evaluateButton.setEnabled(true);
                                    exportPdfBtn.setEnabled(true);
                                    NotificationUtils.showSuccessNotification("AI Architecture Evaluation Complete!");
                                    ui.push();
                                });
                            }
                        }
                );
    }

    @Override
    public Onboarding createTour() {
        return new TourUtils().build()
                .addStep(paletteDrawerPanel, "Architecture Solution Palette", new Html("<div>Browse and drag & drop IoT Domains, Architectural Patterns, Edge/Fog/Cloud Technologies, and Quality ISO 25010 Requirements into your design canvas.</div>"), PopupPosition.END)
                .addStep(rightPanelContainer, "Communication Palette & Active Links", new Html("<div>Select interactive protocol arrow tools (MQTT, HTTPS, Kafka, Modbus, etc.) and click origin ➔ destination blocks/pills to connect communication flows. Manage active links in expandable accordion panels and hover over any link to dynamically highlight its connected components on the canvas!</div>"), PopupPosition.BOTTOM)
                .addStep(openButton, "Open Saved Work", new Html("<div>Click here to open a gallery of your saved architecture designs.</div>"), PopupPosition.BOTTOM)
                .addStep(saveButton, "Save Blueprint", new Html("<div>Save your active block layout and AI report directly to the database.</div>"), PopupPosition.BOTTOM)
                .addStep(exportImageButton, "Export Image PNG", new Html("<div>Export and download a high-resolution PNG image blueprint of your active architecture canvas.</div>"), PopupPosition.BOTTOM)
                .addStep(evaluateButton, "Evaluate with AI", new Html("<div>Request a real-time AI Assistant diagnostic review for your active architecture stack.</div>"), PopupPosition.BOTTOM)
                .addStep(notesFab, "Architectural Notes", new Html("<div>Add system goals, SLA constraints, or notes that are saved with your project & evaluated by AI.</div>"), PopupPosition.BOTTOM)
                .addStep(aiReportFab, "Floating AI Report", new Html("<div>Click this floating widget to show or hide the AI diagnostic evaluation report window anytime.</div>"), PopupPosition.BOTTOM)
                .getOnboarding();
    }

    @Override
    public Boolean startDemoTour() {
        return false;
    }
}
