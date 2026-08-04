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

@UIScope
@Setter
@Component
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
        String js =
                "var nodes = new vis.DataSet([" +
                this.getNodes() +
                "]);" +

                "var edges = new vis.DataSet([" +
                this.getEdges() +
                "]);" +

                "var container = document.getElementById('diagram');" +
                "if (container) {" +
                "   var data = { nodes: nodes, edges: edges };" +

                "   var options = { " +
                        "layout: { hierarchical: false }, " +
                        "nodes: { borderWidth: 0, shadow: { enabled: true, color: 'rgba(0,0,0,0.18)', x: 0, y: 4, size: 8 } }, " +
                        "edges: { arrows: { to: { enabled: true, scaleFactor: 0.85 } }, smooth: { type: 'continuous', roundness: 0.4 }, width: 3, color: { color: '#3b82f6', highlight: '#2563eb', hover: '#1d4ed8' } }, " +
                        "physics: { enabled: true, solver: 'forceAtlas2Based', forceAtlas2Based: { gravitationalConstant: -70, centralGravity: 0.005, springLength: 170, springConstant: 0.06, damping: 0.4 }, stabilization: { iterations: 150 } }, " +
                        "interaction: { dragNodes: true, dragView: true, zoomView: true, hover: true, navigationButtons: false, keyboard: true } };" +

                "   var network = new vis.Network(container, data, options);" +

                "   network.on('hoverNode', function () { container.style.cursor = 'pointer'; });" +
                "   network.on('blurNode', function () { container.style.cursor = 'default'; });" +

                "   network.on('selectNode', function(params) {" +
                "       if (params.nodes.length > 0) {" +
                "           network.focus(params.nodes[0], { scale: 1.15, animation: { duration: 400, easingFunction: 'easeInOutQuad' } });" +
                "       }" +
                "   });" +

                "   network.on('doubleClick', function(params) {" +
                "       network.fit({ animation: { duration: 400, easingFunction: 'easeInOutQuad' } });" +
                "   });" +

                "   setTimeout(function () { network.fit(); }, 200);" +
                "}";

        UI.getCurrent().getPage().executeJs(js);
    }

}
