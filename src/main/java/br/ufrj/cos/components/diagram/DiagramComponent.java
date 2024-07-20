package br.ufrj.cos.components.diagram;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.ui.LoadMode;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@UIScope
//@JavaScript(value = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js", loadMode = LoadMode.EAGER)
public class DiagramComponent {

    public DiagramComponent() {
        UI.getCurrent().getPage().addJavaScript("https://unpkg.com/vis-network/standalone/umd/vis-network.min.js");
    }

    public void execute() {
        //Add the custom JavaScript to initialize the network (https://visjs.github.io/vis-network/examples/)
        UI.getCurrent().getPage().executeJs(
                "var nodes = new vis.DataSet([" +
                        "  { id: 0, label: '<b>IoT Domain</b>', group: 0, color: 'lightblue', font: { multi: true, size: 50, face: 'georgia'} }," +
                        "  { id: 1, label: 'Architectural Solution', group: 0, color: 'yellow', font: { multi: true, size: 50, face: 'georgia'}  }," +
                        "  { id: 2, label: 'Quality Requirement', group: 0, color: 'green', font: { multi: true, size: 50, face: 'georgia'}  }," +
                        "  { id: 3, label: 'Technology/Feature', group: 0, color: 'orange', font: { multi: true, size: 50, face: 'georgia'}  }" +

                        "]);" +
                        "var edges = new vis.DataSet([" +
                        "  { from: 0, to: 1, arrows: 'to', shadow: { color: 'black' }}," +
                        "  { from: 1, to: 2, arrows: 'to', shadow: { color: 'black' } }," +
                        "  { from: 2, to: 3, arrows: 'to', shadow: { color: 'black' } }" +

                        "]);" +
                        "var container = document.getElementById('mynetwork');" +
                        "var data = { nodes: nodes, edges: edges };" +
                        "var options = {" +
                        "  nodes: {" +
                        "    shape: 'box'," +
                        "    size: 42," +
                        "    font: { size: 32 }," +
                        "    borderWidth: 2," +
                        "    shadow: true" +
                        "  }," +
                        "  edges: {" +
                        "    width: 2," +
                        "    shadow: true" +
                        "  }," +
                        "  physics: {" +
                        "    enabled: false," +
                        "  }," +
                        "};" +
                        "var network = new vis.Network(container, data, options);"
        );
    }

}
