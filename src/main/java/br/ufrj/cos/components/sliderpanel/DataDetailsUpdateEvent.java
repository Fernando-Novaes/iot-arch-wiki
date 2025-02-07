package br.ufrj.cos.components.sliderpanel;

import br.ufrj.cos.components.treeview.record.DataDetails;
import lombok.Getter;

@Getter
public class DataDetailsUpdateEvent {
    private final DataDetails dataDetails;

    public DataDetailsUpdateEvent(DataDetails dataDetails) {
        this.dataDetails = dataDetails;
    }

}
