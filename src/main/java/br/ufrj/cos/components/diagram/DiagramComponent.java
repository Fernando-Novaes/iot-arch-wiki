package br.ufrj.cos.components.diagram;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@UIScope
//@JavaScript(value = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js", loadMode = LoadMode.EAGER)
public class DiagramComponent extends VerticalLayout {

    @Setter
    private List<NodeDiagram> nodes;
    @Setter
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

//     "  { id: 0, label: '<b>IoT Domain</b>', group: 0, color: 'lightblue', font: { multi: true, size: 50, face: 'georgia'} }," +
//             "  { id: 1, label: 'Architectural Solution', group: 0, color: 'yellow', font: { multi: true, size: 50, face: 'georgia'}  }," +
//             "  { id: 2, label: 'Quality Requirement', group: 0, color: 'green', font: { multi: true, size: 50, face: 'georgia'}  }," +
//             "  { id: 3, label: 'Technology/Feature', group: 0, color: 'orange', font: { multi: true, size: 50, face: 'georgia'}  }" +
//
//             "]);" +
//             "var edges = new vis.DataSet([" +
//             "  { from: 0, to: 1, arrows: 'to', shadow: { color: 'black' }}," +
//             "  { from: 1, to: 2, arrows: 'to', shadow: { color: 'black' } }," +
//             "  { from: 2, to: 3, arrows: 'to', shadow: { color: 'black' } }" +
//
//             "]);" +

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
                "var options = {" +
                "  nodes: {" +
                "    shape: 'box'," +
                "    size: 22," +
                "    font: { size: 18 }," +
                "    borderWidth: 2," +
                "    zoomView: false,"+
                "    shadow: false" +
                "  }," +
                "  edges: {" +
                "    width: 2," +
                "    shadow: false" +
                "  }," +
                "  physics: {" +
                "    enabled: false," +
                        "stabilization: {" +
                        "                fit: true // Ensure the network fits within the container" +
                        "            }" +
                "  }," +
                "};" +
                "var network = new vis.Network(container, data, options);" +
                "// Fit the network within the container" +
                "    network.once('stabilizationIterationsDone', function () {" +
                "        network.fit();";;

        UI.getCurrent().getPage().executeJs(js);
        System.out.println("#JS: " + js);
    }

    public void execute(String window) {
        //Add the custom JavaScript to initialize the network (https://visjs.github.io/vis-network/examples/)
        String js =
                "var nodes = new vis.DataSet([" +
                        this.getNodes() +
                        "]);" +

                        "var edges = new vis.DataSet([" +
                        this.getEdges() +
                        "]);" +

                        "var container = document.getElementById('"+ window +"');" +
                        "var data = { nodes: nodes, edges: edges };" +
                        "var options = {" +
                        "  nodes: {" +
                        "    shape: 'box'," +
                        "    size: 22," +
                        "    font: { size: 18 }," +
                        "    borderWidth: 2," +
                        "    shadow: false" +
                        "    zoomView: false,"+
                        "  }," +
                        "  edges: {" +
                        "    width: 2," +
                        "    shadow: false" +
                        "  }," +
                        "  physics: {" +
                        "    enabled: false," +
                        "           stabilization: {" +
                        "                fit: true // Ensure the network fits within the container" +
                        "            }" +
                        "  }," +
                        "};" +
                        "var network = new vis.Network(container, data, options);" +
                        "// Fit the network within the container" +
                        "    network.once('stabilizationIterationsDone', function () {" +
                        "        network.fit();";

        UI.getCurrent().getPage().executeJs(js);
        System.out.println("#JS: " + js);
    }

}
