package br.ufrj.cos.components.chart;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.bar.Ranges;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.subtitle.Style;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.xaxis.labels.builder.StyleBuilder;
import com.github.appreciated.apexcharts.config.yaxis.builder.AxisTicksBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import com.github.appreciated.apexcharts.config.plotoptions.bar.Colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ChartComponent extends ApexChartsBuilder {

    private List<ChartData> dataSet;

    @PostConstruct
    private void init() {
        dataSet = new ArrayList<>();
    }

    /***
     * Add data to the chartdataset
     * @param description
     * @param value
     */
    public void addData(String description, Long value, Long totalRecords) {
        dataSet.add(new ChartData(description, value, totalRecords));
    }

    /***
     * Clear the dataset
     */
    public void cleanDataset() {
        dataSet.clear();
    }

    public ApexCharts createPieChart(String title) {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        this.dataSet.forEach(d -> {
            labels.add(d.getDescription());
            values.add(d.getValue().doubleValue());
        });

        TitleSubtitle titleChart = createChartTitle(title, "white");

        ApexChartsBuilder b = withChart(ChartBuilder.get().withType(Type.PIE).build())
                .withLabels(labels.toArray(value ->
                        new String[value]))
                .withTitle(titleChart)
//                .withLegend(LegendBuilder.get()
//                        .withPosition(Position.BOTTOM)
//                        .build())
                .withSeries(values.toArray(new Double[values.size()]))
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .withOptions(OptionsBuilder.get()
                                .withLegend(LegendBuilder.get()
                                        .withPosition(Position.BOTTOM)
                                        .build())
                                .build())
                        .build());

        return b.build();
    }

    private static TitleSubtitle createChartTitle(String title, String color) {
        TitleSubtitle titleChart = new TitleSubtitle();
        titleChart.setText(title);
        titleChart.setAlign(Align.CENTER);
        Style style = new Style();
        style.setColor(color);
        titleChart.setStyle(style);
        return titleChart;
    }

    public ApexCharts createBarChart(String title, Boolean horizontalBar) {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        List<String> labelColors = new ArrayList<>();

        this.dataSet.forEach(d -> {
            labels.add(d.getDescription());
            values.add(d.getValue().doubleValue());
            labelColors.add("white");
        });

        TitleSubtitle titleChart = createChartTitle(title, "white");

        com.github.appreciated.apexcharts.config.xaxis.labels.Style labelXStyle = StyleBuilder.get()
                .withColors(labelColors)
                .build();

        com.github.appreciated.apexcharts.config.yaxis.labels.Style labelYStyle = com.github.appreciated.apexcharts.config.yaxis.labels.builder.StyleBuilder.get()
                .withColor("white")
                .build();

        Ranges ranges = new Ranges();
        ranges.setColor(generateRandomColor());
        Colors colorsBar = new Colors();
        colorsBar.setRanges(ranges);

        ApexCharts chart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).withHeight("400px").withWidth("100%").build())
                .withTitle(titleChart)
                .withLegend(LegendBuilder.get().withShow(false).build())
                .withSeries(new Series<>("Total", values.toArray(new Double[0])))
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(horizontalBar)
                                .withDistributed(true) // Distribute colors across bars
                                .withColors(colorsBar) // Set colors for the bars
                                .build())
                        .build())
                .withXaxis(XAxisBuilder.get()
                        .withCategories(labels.toArray(new String[0]))
                        .withLabels(LabelsBuilder.get().withStyle(labelXStyle).build()) // Set x-axis labels to white
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withLabels(com.github.appreciated.apexcharts.config.yaxis.builder.LabelsBuilder.get().withStyle(labelYStyle).build())
                        .withAxisTicks(AxisTicksBuilder.get()
                                .withColor("white")
                                .build())
                        .build())
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(100.0)
                        .withOptions(OptionsBuilder.get()
                                .withLegend(LegendBuilder.get()
                                        .withPosition(Position.BOTTOM)
                                        .build())
                                .build())
                        .build())
                .build();

        return chart;
    }

    /***
     * Generate random color for graph bars
     * @return String code color
     */
    private String generateRandomColor() {
            // Generate a random hex color code
            int r = (int) (Math.random() * 256);
            int g = (int) (Math.random() * 256);
            int b = (int) (Math.random() * 256);

            return String.format("#%02x%02x%02x", r, g, b);
    }

}
