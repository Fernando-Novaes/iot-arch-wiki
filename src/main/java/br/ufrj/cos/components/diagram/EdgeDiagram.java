package br.ufrj.cos.components.diagram;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class EdgeDiagram {

    private String from;
    private String to;

    @Override
    public String toString() {
        return String.format("{ from: %s, to: %s, arrows: 'to', shadow: { color: 'black' }}", from, to);
    }
}
