package br.ufrj.cos.components.diagram;

import lombok.*;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class NodeDiagram {

    private String id;
    private String label;
    private String color;

    @Override
    public String toString() {
        return String.format("{ id: %s, label: '%s', group: 0, color: '%s', font: { multi: true, size: 30, face: 'georgia'}}", id, label, color);
    }

}
