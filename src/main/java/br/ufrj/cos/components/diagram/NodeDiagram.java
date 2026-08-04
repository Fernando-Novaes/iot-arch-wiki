package br.ufrj.cos.components.diagram;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class NodeDiagram {

    private String id;
    private String label;
    private String color;
    private String tooltip;

    @Override
    public String toString() {
        String safeLabel = label != null ? label.replace("'", "\\'").replace("\n", " ") : "";
        String safeTooltip = tooltip != null ? tooltip.replace("'", "\\'").replace("\n", " ") : "";

        return String.format(
                "{ id: %s, label: '%s', shape: 'box', margin: { top: 10, bottom: 10, left: 22, right: 22 }, " +
                "color: { background: '%s', border: '%s', highlight: { background: '%s', border: '%s' } }, " +
                "font: { color: '#ffffff', size: 14, face: 'system-ui, -apple-system, sans-serif', bold: true }, " +
                "borderWidth: 0, shadow: { enabled: true, color: 'rgba(0,0,0,0.18)', x: 0, y: 4, size: 8 }, " +
                "shapeProperties: { borderRadius: 22 }, title: '%s' }",
                id, safeLabel, color, color, color, color, safeTooltip
        );
    }
}
