package br.ufrj.cos.views.board;

import br.ufrj.cos.components.chart.ChartComponent;
import br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord;
import br.ufrj.cos.components.chart.data.IoTDomainRecord;
import br.ufrj.cos.components.chart.data.QualityRequirementRecord;
import br.ufrj.cos.components.chart.data.TechnologyRecord;
import br.ufrj.cos.components.diagram.DiagramComponent;
import br.ufrj.cos.service.ArchitectureSolutionService;
import br.ufrj.cos.service.IoTDomainService;
import br.ufrj.cos.service.QualityRequirementService;
import br.ufrj.cos.service.TechnologyService;
import br.ufrj.cos.views.BaseView;
import br.ufrj.cos.views.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.vaadin.flow.component.dependency.JavaScript;
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

@UIScope
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Iot-Arch Wiki - Board")
public class BoardView extends BaseView {

    private final IoTDomainService domainService;
    private final QualityRequirementService qualityReqService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final TechnologyService technologyService;
    private final ChartComponent chart;
    private final DiagramComponent diagramComponent;

    public BoardView(
            IoTDomainService domainService,
            QualityRequirementService qualityReqService,
            ArchitectureSolutionService architectureSolutionService,
            TechnologyService technologyService,
            ChartComponent chart,
            DiagramComponent diagramComponent) {

        this.domainService = domainService;
        this.qualityReqService = qualityReqService;
        this.architectureSolutionService = architectureSolutionService;
        this.technologyService = technologyService;
        this.chart = chart;
        this.diagramComponent = diagramComponent;

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        //Header
        this.createHeader("Board");
        //Content
        HorizontalLayout content = this.createContentLayout();
        getContent().add(content);
    }

    @PostConstruct
    private void init() {
        this.createChartsLayout();
        this.createDiagramLayout();
    }

    /***
     *
     * @return VerticalLayout
     */
    private VerticalLayout createVerticalContainer() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.getStyle().setBorder("1px solid grey");

        return verticalLayout;
    }

    /***
     * Create the container to show boxes
     * @return HorizontalLayout
     */
    private HorizontalLayout createContainer() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.setWidthFull();

        return horizontalLayout;
    }

    private void createDiagramLayout() {
        HorizontalLayout container = createContainer();
        VerticalLayout box01 = createVerticalContainer();

        Div diagram = new Div();
        diagram.setId("mynetwork");
        box01.add(diagram);
        container.add(box01);
        getContent().add(container);

        this.diagramComponent.execute();
    }

    /***
     * Create the layout to show all graphs
     * @throws IOException
     */
    private void createChartsLayout() {
        HorizontalLayout container = this.createContainer();
        VerticalLayout box01 = this.createVerticalContainer();
        VerticalLayout box02 = this.createVerticalContainer();
        VerticalLayout box03 = this.createVerticalContainer();
        VerticalLayout box04 = this.createVerticalContainer();

        box01.add(this.createArchitectureSolutionChart("Architectural Solutions"));
        box02.add(this.createIoTDomainChart("IoT Domains"));
        box03.add(this.createQualityRequirementChart("Quality Requirements"));
        box04.add(this.createTechnologyChart("Technologies"));

        container.add(box01, box02, box03, box04);

        getContent().add(container);
    }

    private void chartInitialConfig() {
        this.chart.cleanDataset();
        this.chart.withLegend(LegendBuilder.get().withShow(Boolean.FALSE).build()).build();
    }

    /***
     * Create the IoT Domains chart
     * @param chartTitle
     * @return Image
     * @throws IOException
     */
    private ApexCharts createIoTDomainChart(String chartTitle) {
        List<IoTDomainRecord> recordData = this.domainService.getIoTDomainCountGroupedByName();

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
}
