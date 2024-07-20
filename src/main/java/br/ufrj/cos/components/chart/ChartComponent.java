package br.ufrj.cos.components.chart;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

        TitleSubtitle titleChart = new TitleSubtitle();
        titleChart.setText(title);
        titleChart.setAlign(Align.CENTER);

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


}
