package br.ufrj.cos.components.chart;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ChartData {

    private String description;
    private Long value;
    private Long totalRecordsa;

}
