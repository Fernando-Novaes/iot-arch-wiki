package br.ufrj.cos.views.board;

import br.ufrj.cos.components.chart.ChartComponent;
import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.components.chart.data.QualityRequirementChartRecord;
import br.ufrj.cos.components.chart.data.TechnologyChartRecord;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.ReferenceRecord;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "board-view", layout = MainLayout.class)
@PageTitle("Body of Knowledge")
@PermitAll
public class BoardView extends BaseView {

    private final IoTDomainService domainService;
    private final QualityRequirementService qualityReqService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final TechnologyService technologyService;
    private final PaperReferenceService paperReferenceService;
    private final ChartComponent chart;

    private static final String IOTDOMAIN_GRAPH_TITLE = "Architectures per IoT Domain";
    private static final String ARCHITECTURE_GRAPH_TITLE = "Solutions per Architecture Pattern";
    private static final String QUALITYREQUIREMENT_GRAPH_TITLE = "Solutions per Quality Requirement";
    private static final String REFERENCE_TITLE = "Primary Sources by Year";

    public BoardView(
            IoTDomainService domainService,
            QualityRequirementService qualityReqService,
            ArchitectureSolutionService architectureSolutionService,
            TechnologyService technologyService,
            PaperReferenceService paperReferenceService,
            ChartComponent chart) {

        this.domainService = domainService;
        this.qualityReqService = qualityReqService;
        this.architectureSolutionService = architectureSolutionService;
        this.technologyService = technologyService;
        this.paperReferenceService = paperReferenceService;
        this.chart = chart;

        VerticalLayout root = getContent();
        root.setSpacing(false);
        root.setPadding(false);
        root.setSizeFull();
        root.addClassName("board-view-root");
        root.setAlignItems(FlexComponent.Alignment.CENTER);

        createHeaderBanner();
    }

    private void createHeaderBanner() {
        Div banner = new Div();
        banner.addClassName("bv-header-banner");

        H1 title = new H1("Body of Knowledge");
        title.addClassName("bv-header-title");

        Paragraph subtitle = new Paragraph(
                String.format("Curated insights and statistical metrics extracted from %d primary scientific sources.",
                        this.paperReferenceService.findAll().size())
        );
        subtitle.addClassName("bv-header-subtitle");

        banner.add(title, subtitle);
        getContent().add(banner);
    }

    @PostConstruct
    private void init() {
        createChartsGrid();
        createBarGraphSummary();
    }

    private void createChartsGrid() {
        Div grid = new Div();
        grid.addClassName("bv-charts-grid");

        String[] titles = {
                IOTDOMAIN_GRAPH_TITLE,
                ARCHITECTURE_GRAPH_TITLE,
                QUALITYREQUIREMENT_GRAPH_TITLE,
                REFERENCE_TITLE
        };

        for (String title : titles) {
            VerticalLayout card = createChartCard(title);
            grid.add(card);
        }

        getContent().add(grid);
    }

    private VerticalLayout createChartCard(String title) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(false);
        card.addClassName("bv-chart-card");
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        // Header bar inside card
        Div headerBar = new Div();
        headerBar.addClassName("bv-card-header");

        Text titleText = new Text(title);
        Button expandBtn = createExpandButton(title);

        headerBar.add(titleText, expandBtn);
        card.add(headerBar);

        // Render appropriate chart
        ApexCharts chartInstance = switch (title) {
            case IOTDOMAIN_GRAPH_TITLE -> createIoTDomainChart("IoT Domains");
            case ARCHITECTURE_GRAPH_TITLE -> createArchitectureChart("Architectures");
            case QUALITYREQUIREMENT_GRAPH_TITLE -> createQualityRequirementChart("Quality Requirements");
            case REFERENCE_TITLE -> createReferencesChart("Primary Sources");
            default -> createIoTDomainChart("Chart");
        };

        card.add(chartInstance);
        return card;
    }

    private Button createExpandButton(String title) {
        Icon icon = new Icon(VaadinIcon.EXPAND_FULL);
        Button expand = new Button(icon);
        expand.addClassName("bv-expand-btn");
        expand.setTooltipText("Expand graph");

        expand.addClickListener(click -> {
            ApexCharts dialogChart = switch (title) {
                case IOTDOMAIN_GRAPH_TITLE -> createIoTDomainChart("IoT Domains");
                case ARCHITECTURE_GRAPH_TITLE -> createArchitectureChart("Architectures");
                case QUALITYREQUIREMENT_GRAPH_TITLE -> createQualityRequirementChart("Quality Requirements");
                case REFERENCE_TITLE -> createReferencesChart("Primary Sources");
                default -> createIoTDomainChart("Chart");
            };
            ChartDialog chartDialog = new ChartDialog(dialogChart, title);
            chartDialog.setHeight("85vh");
            chartDialog.setWidth("75vw");
            chartDialog.open();
        });
        return expand;
    }

    private void createBarGraphSummary() {
        VerticalLayout summaryCard = new VerticalLayout();
        summaryCard.setSpacing(false);
        summaryCard.setPadding(false);
        summaryCard.addClassName("bv-summary-card");
        summaryCard.setAlignItems(FlexComponent.Alignment.CENTER);

        ApexCharts barChart = createCountRegistersBarChart();
        summaryCard.add(barChart);

        getContent().add(summaryCard);
    }

    private void chartInitialConfig() {
        this.chart.cleanDataset();
        this.chart.withLegend(LegendBuilder.get().withShow(Boolean.FALSE).build()).build();
    }

    private ApexCharts createCountRegistersBarChart() {
        this.chartInitialConfig();

        this.chart.addData("IoT Domains", (long) this.domainService.findAll().size(), 0L);
        this.chart.addData("Architectures", (long) this.architectureSolutionService.geArchitectureSolutionCountGroupedByName().size(), 0L);
        this.chart.addData("Quality Requirements", (long) this.qualityReqService.getQualityRequirementCountGroupedByName().size(), 0L);
        this.chart.addData("Primary Sources", (long) this.paperReferenceService.findAll().size(), 0L);

        return chart.createBarChart(
                String.format("Data Summary (Totaling %d solutions cataloged)", this.architectureSolutionService.findAll().size()),
                false
        );
    }

    private ApexCharts createIoTDomainChart(String chartTitle) {
        List<IoTDomainChartRecord> recordData = this.domainService.countIoTDomainByArchitectureSolution();

        this.chartInitialConfig();
        recordData.forEach(d -> this.chart.addData(d.description(), d.qtd(), d.total()));

        return chart.createPieChart(String.format("%s [%d]", chartTitle, recordData.size()));
    }

    private ApexCharts createQualityRequirementChart(String chartTitle) {
        List<QualityRequirementChartRecord> recordData = this.qualityReqService.getQualityRequirementCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> this.chart.addData(d.description(), d.qtd(), d.total()));

        return chart.createPieChart(String.format("%s [%d]", chartTitle, recordData.size()));
    }

    private ApexCharts createArchitectureChart(String chartTitle) {
        List<ArchitectureSolutionChartRecord> recordData = this.architectureSolutionService.geArchitectureSolutionCountGroupedByName();

        this.chartInitialConfig();
        List<ArchitectureSolutionChartRecord> topData = recordData.stream()
                .sorted((a, b) -> Long.compare(b.qtd(), a.qtd()))
                .limit(7)
                .toList();

        topData.forEach(d -> this.chart.addData(d.description(), d.qtd(), d.total()));

        return chart.createBarChart(String.format("%s [Top %d of %d]", chartTitle, topData.size(), recordData.size()), false);
    }

    private ApexCharts createTechnologyChart(String chartTitle) {
        List<TechnologyChartRecord> recordData = this.technologyService.getTechnologyCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> this.chart.addData(d.description(), d.qtd(), d.total()));

        return chart.createPieChart(String.format("%s [%d]", chartTitle, recordData.size()));
    }

    private ApexCharts createReferencesChart(String chartTitle) {
        List<ReferenceRecord> recordData = this.paperReferenceService.findPaperCountsByYear();

        this.chartInitialConfig();
        recordData.forEach(paper -> this.chart.addData(paper.publishYear().toString(), paper.qtd(), paper.total()));

        return chart.createBarChart(String.format("%s [%d]", chartTitle, this.paperReferenceService.findAll().size()), false);
    }
}
