package br.ufrj.cos.views.board;

import com.github.appreciated.apexcharts.ApexCharts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class ChartDialog extends Dialog {  // Extend Dialog to create a custom dialog component
    private ApexCharts chart;  // The ApexCharts chart
    private String title;

    /**
     * Constructor for the ChartDialog.
     * @param chart The ApexCharts chart to display.
     * @param title The title of the chart.
     */
    public ChartDialog(ApexCharts chart, String title) {
        this.chart = chart;
        this.title = title;

        setHeaderTitle(title);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        dialogLayout.setAlignSelf(FlexComponent.Alignment.CENTER);

        dialogLayout.setWidth("80%"); // Set the width of the dialog
        dialogLayout.setHeight("70%"); // Set the height of the dialog

        // Add the chart to the dialog layout, after converting to a Vaadin Component
        dialogLayout.add(chart);

        Button closeButton = new Button("Close", event -> close());
        getFooter().add(closeButton);

        add(dialogLayout); // Add the layout to the Dialog
    }
}