package br.ufrj.cos.components.sliderpanel;

import lombok.Getter;
import lombok.Setter;

public class OpenCloseEvent {

    public enum Action {
        OPEN, CLOSE;
    }

    @Getter
    private Action action;

    public OpenCloseEvent(Action action) {
        this.action = action;
    }

}
