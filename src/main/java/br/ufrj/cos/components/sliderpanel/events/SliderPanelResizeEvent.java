package br.ufrj.cos.components.sliderpanel.events;

import org.springframework.context.ApplicationEvent;

public class SliderPanelResizeEvent extends ApplicationEvent {

    private double newWidth;

    public SliderPanelResizeEvent(Object source, double newWidth) {
        super(source);
        this.newWidth = newWidth;
    }

    public double getNewWidth() {
        return newWidth;
    }
}