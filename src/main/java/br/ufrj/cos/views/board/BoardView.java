package br.ufrj.cos.views.board;

import br.ufrj.cos.components.chart.ChartComponent;
import br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord;
import br.ufrj.cos.components.chart.data.IoTDomainRecord;
import br.ufrj.cos.components.chart.data.QualityRequirementRecord;
import br.ufrj.cos.components.chart.data.TechnologyRecord;
import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.service.*;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Route(value = "board-view", layout = MainLayout.class)
@PageTitle("Iot-Arch Wiki - Board")
public class BoardView extends BaseView {

    private final IoTDomainService domainService;
    private final QualityRequirementService qualityReqService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final TechnologyService technologyService;
    private final PaperReferenceService paperReferenceService;
    private final ChartComponent chart;
    private final DiagramComponent diagramComponent;

    private final HorizontalLayout pageContent;

    public BoardView(
            IoTDomainService domainService,
            QualityRequirementService qualityReqService,
            ArchitectureSolutionService architectureSolutionService,
            TechnologyService technologyService, PaperReferenceService paperReferenceService,
            ChartComponent chart,
            DiagramComponent diagramComponent) {

        this.domainService = domainService;
        this.qualityReqService = qualityReqService;
        this.architectureSolutionService = architectureSolutionService;
        this.technologyService = technologyService;
        this.paperReferenceService = paperReferenceService;
        this.chart = chart;
        this.diagramComponent = diagramComponent;

        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");

        //Header
        this.createHeader("Board of Knowledge");
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
    private HorizontalLayout createContainer() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        horizontalLayout.setSizeFull();

        return horizontalLayout;
    }

    private void createDiagramLayout() {
        HorizontalLayout container = createContainer();
        VerticalLayout box01 = createVerticalContainer();

        Div diagram = new Div();
        diagram.setId("diagram");
        box01.add(diagram);
        container.add(box01);
        getContent().add(container);

        this.diagramComponent.execute();
    }

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
        VerticalLayout box01 = this.createVerticalContainer();
        box01.setClassName("box");
        box01.getStyle().set("flex-grow", "1");
        VerticalLayout box02 = this.createVerticalContainer();
        box02.setClassName("box");
        box02.getStyle().set("flex-grow", "1");
        VerticalLayout box03 = this.createVerticalContainer();
        box03.setClassName("box");
        box03.getStyle().set("flex-grow", "1");
        VerticalLayout box04 = this.createVerticalContainer();
        box04.setClassName("box");
        box04.getStyle().set("flex-grow", "1");

        box01.add(this.createIoTDomainChart("IoT Domains"));
        box02.add(this.createQualityRequirementChart("Quality Requirements"));
        box03.add(this.createArchitectureSolutionChart("Architectural Solutions"));
        box04.add(this.createTechnologyChart("Technologies"));
        //box05.add(this.createCountRegistersBarChart());

        container.add(box01, box02, box03, box04);

        getContent().add(container);
    }

    private void chartInitialConfig() {
        this.chart.cleanDataset();
        this.chart.withLegend(LegendBuilder.get().withShow(Boolean.FALSE).build()).build();
    }

    private ApexCharts createCountRegistersBarChart() {
        this.chartInitialConfig();

        this.chart.addData("IoT Domains", (long) this.domainService.findAll().size(), 0L);
        this.chart.addData("Architecture Solutions", (long) this.architectureSolutionService.findAll().size(), 0L);
        this.chart.addData("Quality Requirements", (long) this.qualityReqService.getQualityRequirementCountGroupedByName().size(), 0L);
        this.chart.addData("Technologies", (long) this.technologyService.findAll().size(), 0L);
        this.chart.addData("References", (long) this.paperReferenceService.findAll().size(), 0L);

        return chart.createBarChart("Data Summary", false);
    }

    /***
     * Create the IoT Domains chart
     * @param chartTitle
     * @return Image
     * @throws IOException
     */
    private ApexCharts createIoTDomainChart(String chartTitle) {
        //List<IoTDomainRecord> recordData = this.domainService.getIoTDomainCountGroupedByName();
        List<IoTDomainRecord> recordData = this.domainService.countIoTDomainByArchitectureSolution();

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
        List<QualityRequirementRecord> recordData = this.qualityReqService.getQualityRequirementCountGroupedByName();

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
    private ApexCharts createArchitectureSolutionChart(String chartTitle) {
        List<ArchitectureSolutionRecord> recordData = this.architectureSolutionService.geArchitectureSolutionCountGroupedByName();

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
        List<TechnologyRecord> recordData = this.technologyService.getTechnologyCountGroupedByName();

        this.chartInitialConfig();
        recordData.forEach(d -> {
            this.chart.addData(d.description(), d.qtd(), d.total());
        });

        return chart.createPieChart(String.format("%s [%s]", chartTitle, recordData.size()));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
    }
}
