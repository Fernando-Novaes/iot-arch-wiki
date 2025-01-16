package br.ufrj.cos.components.diagram;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.ui.LoadMode;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Setter
@Component
@UIScope
@JavaScript(value = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js", loadMode = LoadMode.EAGER)
public class DiagramComponent extends VerticalLayout {

    private List<NodeDiagram> nodes;
    private List<EdgeDiagram> edges;

    public DiagramComponent() {
        UI.getCurrent().getPage().addJavaScript("https://unpkg.com/vis-network/standalone/umd/vis-network.min.js");
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    private String getNodes() {
        StringBuilder nodesStr = new StringBuilder();
        this.nodes.forEach(n -> {
            nodesStr.append(n.toString());
            nodesStr.append(",");
        });
        nodesStr.append("#");

        return nodesStr.toString().replace(",#", "");
    }

    private String getEdges() {
        StringBuilder edgesStr = new StringBuilder();
        this.edges.forEach(n -> {
            edgesStr.append(n.toString());
            edgesStr.append(",");
        });
        edgesStr.append("#");

        return edgesStr.toString().replace(",#", "");
    }

    public void execute() {
        //Add the custom JavaScript to initialize the network (https://visjs.github.io/vis-network/examples/)
       String js =
                "var nodes = new vis.DataSet([" +
                this.getNodes() +
                "]);" +

                "var edges = new vis.DataSet([" +
                this.getEdges() +
                "]);" +

                "var container = document.getElementById('diagram');" +
                "var data = { nodes: nodes, edges: edges };" +

                "var options = { " +
                        "nodes: { shape: 'box', size: 16, font: { size: 12 }, borderWidth: 2, shadow: true }, " +
                        "edges: { smooth: { type: 'vertical', forceDirection: 'vertical', roundness: 0 }, width: 2, shadow: false }, " +
                        "physics: { hierarchicalRepulsion: { centralGravity: 0, avoidOverlap: null }, " +
                                    "solver: 'hierarchicalRepulsion', enabled: true, stabilization: true }, interaction: { zoomView: true } };" +

                "var network = new vis.Network(container, data, options);"+
                "network.once('stabilizationIterationsDone', function () {"+
                "    network.fit();"+
                "});"+
                "network.fit();";

        UI.getCurrent().getPage().executeJs(js);
        System.out.println("#JS: " + js);
    }

}
