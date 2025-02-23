package br.ufrj.cos.components.sliderpanel.listeners;

import br.ufrj.cos.components.sliderpanel.events.SliderPanelResizeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class ResizeListener {
    @EventListener
    public void handleSliderPanelResize(SliderPanelResizeEvent event) {
        double newWidth = event.getNewWidth();
        // Do something with the new width
        System.out.println("SliderPanel resized to: " + newWidth);
    }
}