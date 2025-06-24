package br.ufrj.cos.views.board;

import br.ufrj.cos.components.avatar.AvatarComponent;
import br.ufrj.cos.components.chart.ChartComponent;
import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.components.chart.data.QualityRequirementChartRecord;
import br.ufrj.cos.components.chart.data.TechnologyChartRecord;
import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import br.ufrj.cos.views.record.ReferenceRecord;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

import java.io.IOException;
import java.util.List;

@Route(value = "board-view", layout = MainLayout.class)
@PageTitle("Board")
@PermitAll
public class BoardView extends BaseView {

    private final IoTDomainService domainService;
    private final QualityRequirementService qualityReqService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final TechnologyService technologyService;
    private final PaperReferenceService paperReferenceService;
    private final ChartComponent chart;
    //private final DiagramComponent diagramComponent;

    private final HorizontalLayout pageContent;

    private final String IOTDOMAIN_GRAPH_TITLE = "How many Architectures for each IoT Domain?";
    private final String ARCHITETCTURE_GRAPH_TITLE = "How many Solutions for each Architecture?";
    private final String QUALITYREQUIREMENT_GRAPH_TITLE = "How many each Quality Requirement is addressed by each Solution?";
    private final String TECHNOLOGY_GRAPH_TITLE = "How many each Technology is addressed to each Quality Requirement?";
    private final String REFERENCE_TITLE = "Primary Sources by Year";

    public BoardView(
            IoTDomainService domainService,
            QualityRequirementService qualityReqService,
            ArchitectureSolutionService architectureSolutionService,
            TechnologyService technologyService, PaperReferenceService paperReferenceService,
            ChartComponent chart,
            DiagramComponent diagramComponent, AvatarComponent avatarComponent) {

        this.domainService = domainService;
        this.qualityReqService = qualityReqService;
        this.architectureSolutionService = architectureSolutionService;
        this.technologyService = technologyService;
        this.paperReferenceService = paperReferenceService;
        this.chart = chart;
        //this.diagramComponent = diagramComponent;

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        //Header
        this.createHeaderHTML(
                String.format("<div><h1>Body of Knowledge</h1><p>Extracted from %s primary sources.</p></div>", this.paperReferenceService.findAll().size()));
        //Content
        pageContent = this.createContentLayout();
        getContent().add(pageContent);
    }

    @PostConstruct
    private void init() {
        this.createChartsLayout();
        //this.createDiagramLayout();
        this.createBarGraphLayout();
    }

    /***
     *
     * @return VerticalLayout
     */
    private VerticalLayout createVerticalContainer() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        verticalLayout.getStyle().setBorder("1px solid grey");
        verticalLayout.setSizeFull();

        return verticalLayout;
    }

    /***
     * Create the container to show boxes
     * @return HorizontalLayout
     */
//    private HorizontalLayout createContainer() {
//        HorizontalLayout horizontalLayout = new HorizontalLayout();
//        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
//        horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
//        horizontalLayout.setSizeFull();
//
//        return horizontalLayout;
//    }

//    private void createDiagramLayout() {
//        HorizontalLayout container = createContainer();
//        VerticalLayout box01 = createVerticalContainer();
//
//        Div diagram = new Div();
//        diagram.setId("diagram");
//        box01.add(diagram);
//        container.add(box01);
//        getContent().add(container);
//
//        //this.diagramComponent.execute();
//    }

    private void createBarGraphLayout() {
        HorizontalLayout container = createContainer();

        VerticalLayout box01 = createVerticalContainer();
        box01.setClassName("bar_graph");

        box01.add(this.createCountRegistersBarChart());
        container.add(box01);
        getContent().add(container);
    }

    /***
     * Create the layout to show all graphs
     * @throws IOException
     */
    private void createChartsLayout() {
        HorizontalLayout container = this.createContainer();

        // Define the titles for each box
        String[] boxTitles =
                {IOTDOMAIN_GRAPH_TITLE, ARCHITETCTURE_GRAPH_TITLE, QUALITYREQUIREMENT_GRAPH_TITLE, REFERENCE_TITLE};

        // Create boxes using a loop for better maintainability
        for (int i = 0; i < 4; i++) {
            VerticalLayout box = createBox(boxTitles[i]);
            container.add(box);
        }

        getContent().add(container);
    }

    /**
     * Creates a vertical layout "box" with a title label and adds a chart.
     * @param title The title to display above the chart.
     * @return The created VerticalLayout.
     */
    private VerticalLayout createBox(String title) {
        VerticalLayout box = this.createVerticalContainer();
        box.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        box.getStyle().set("flex-grow", "1");
        box.getStyle().setPosition(Style.Position.RELATIVE); // For absolute positioning within
        box.setClassName("box");
        box.getStyle().set("flex-grow", "1"); // Allow boxes to fill available space

        ApexCharts apexCharts = new ApexCharts();
        ApexCharts dialogChart = new ApexCharts();

        // Add the appropriate chart based on the title
        switch (title) {
            case IOTDOMAIN_GRAPH_TITLE -> {
                apexCharts = this.createIoTDomainChart("IoT Domains");
                dialogChart = this.createIoTDomainChart("IoT Domains");
                box.add(apexCharts);
            }
            case ARCHITETCTURE_GRAPH_TITLE -> {
                apexCharts = this.createArchitectureChart("Architectures");
                dialogChart = this.createArchitectureChart("Architectures");
                box.add(apexCharts);
            }
            case QUALITYREQUIREMENT_GRAPH_TITLE -> {
                apexCharts = this.createQualityRequirementChart("Quality Requirements");
                dialogChart = this.createQualityRequirementChart("Quality Requirements");
                box.add(apexCharts);
            }
            case TECHNOLOGY_GRAPH_TITLE -> {
                apexCharts = this.createTechnologyChart("Technologies");
                dialogChart = this.createTechnologyChart("Technologies");
                box.add(apexCharts);
            }
            case REFERENCE_TITLE -> {
                apexCharts = this.createReferencesChart("Primary Sources");
                dialogChart = this.createReferencesChart("Primary Sources");
                box.add(apexCharts);
            }
        }


        Button expand = createChartExpandDialog(dialogChart, title);
        Div div = new Div();
        //div.setWidthFull();
        div.getStyle().setDisplay(Style.Display.FLEX); // Use flexbox
        div.getStyle().setAlignItems(Style.AlignItems.CENTER);
        div.add(new Text(title), expand);

        // Add a title label
        box.add(div);

        return box;
    }

    private static Button createChartExpandDialog(ApexCharts chart, String title) {
        Icon icon = new Icon(VaadinIcon.EXPAND_FULL);
        icon.setSize("24px");
        Button expand = new Button(icon);
        expand.setTooltipText("Expand graph");

        expand.getStyle().set("cursor", "pointer");
        //ApexCharts finalApexCharts = apexCharts;
        expand.addClickListener(click -> {
                    ChartDialog chartDialog = new ChartDialog(chart, title);
                    chartDialog.setHeight("90%");
                    chartDialog.setWidth("60%");
                    chartDialog.open();
                }
        );
        return expand;
    }


// Helper methods (assuming these are already defined):

    /**
     * Creates a horizontal layout container.
     * @return The created HorizontalLayout.
     */
    private HorizontalLayout createContainer() {
        // Implementation details...
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        return layout; // Replace with actual implementation.
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
        //this.chart.addData("Technologies", (long) this.technologyService.findAll().size(), 0L);

        //this.chart.addData("References", (long) this.paperReferenceService.findAll().size(), 0L);

        return chart.createBarChart(String.format("Data Summary (Totaling %s solutions found)", this.architectureSolutionService.findAll().size()), false);
    }

    /***
     * Create the IoT Domains chart
     * @param chartTitle
     * @return Image
     * @throws IOException
     */
    private ApexCharts createIoTDomainChart(String chartTitle) {
        //List<IoTDomainChartRecord> recordData = this.domainService.getIoTDomainCountGroupedByName();
        List<IoTDomainChartRecord> recordData = this.domainService.countIoTDomainByArchitectureSolution();

        this.chartInitialConfig();
        recordData.forEach(d -> {
            this.chart.addData(d.description(), d.qtd(), d.total());
        });

        return chart.createPieChart(String.format("%s [%s]", chartTitle, recordData.size()));
    }

    /***
     * Create the Quality Requirement chart
     * @param chartTitle
     * @throws IOException
     */
    private ApexCharts createQualityRequirementChart(String chartTitle) {
        List<QualityRequirementChartRecord> recordData = this.qualityReqService.getQualityRequirementCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> {
            this.chart.addData(d.description(), d.qtd(), d.total());
        });

        return chart.createPieChart(String.format("%s [%s]", chartTitle, recordData.size()));
    }

    /***
     * Create the Quality Requirement chart
     * @param chartTitle
     * @throws IOException
     */
    private ApexCharts createArchitectureChart(String chartTitle) {
        List<ArchitectureSolutionChartRecord> recordData = this.architectureSolutionService.geArchitectureSolutionCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> {
            this.chart.addData(d.description(), d.qtd(), d.total());
        });

        return chart.createPieChart(String.format("%s [%s]", chartTitle, recordData.size()));
    }

    /***
     * Create the Technology chart
     * @param chartTitle
     * @throws IOException
     */
    private ApexCharts createTechnologyChart(String chartTitle) {
        List<TechnologyChartRecord> recordData = this.technologyService.getTechnologyCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> {
            this.chart.addData(d.description(), d.qtd(), d.total());
        });

        return chart.createPieChart(String.format("%s [%s]", chartTitle, recordData.size()));
    }

    /***
     * Create the References chart
     * @param chartTitle
     * @throws IOException
     */
    private ApexCharts createReferencesChart(String chartTitle) {
        List<ReferenceRecord> recordData = this.paperReferenceService.findPaperCountsByYear();

        this.chartInitialConfig();
        recordData.forEach(paper -> {
            this.chart.addData(paper.publishYear().toString(), paper.qtd(), paper.total());
        });

        return chart.createBarChart(String.format("%s [%s]", chartTitle, this.paperReferenceService.findAll().size()), false);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
    }
}
