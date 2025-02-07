package br.ufrj.cos.components.treeview.record;

import br.ufrj.cos.domain.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataDetails {
    private IoTDomain iotDomain;
    private ArchitectureSolution architectureSolution;
    private QualityRequirement qualityRequirement;
    private Technology technology;
    private PaperReference reference;

    public DataDetails() {

    }
}
