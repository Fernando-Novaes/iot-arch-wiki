package br.ufrj.cos.views.mynotes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

/**
 * Native, lightweight Vaadin 3D Flip Card layout component.
 */
public class FlipLayout extends Div {

    private final Div cardInner = new Div();
    private final Div frontWrapper = new Div();
    private final Div backWrapper = new Div();
    private Component frontComponent;
    private Component backComponent;
    private boolean isFlipped = false;

    public FlipLayout() {
        getStyle()
                .set("perspective", "1000px")
                .set("display", "inline-block")
                .set("width", "fit-content")
                .set("height", "fit-content");

        cardInner.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("height", "100%")
                .set("transition", "transform 0.6s cubic-bezier(0.4, 0.2, 0.2, 1)")
                .set("transform-style", "preserve-3d");

        frontWrapper.getStyle()
                .set("backface-visibility", "hidden")
                .set("-webkit-backface-visibility", "hidden");

        backWrapper.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("backface-visibility", "hidden")
                .set("-webkit-backface-visibility", "hidden")
                .set("transform", "rotateY(180deg)");

        cardInner.add(frontWrapper, backWrapper);
        add(cardInner);
    }

    public void setFrontComponent(Component component) {
        frontWrapper.removeAll();
        this.frontComponent = component;
        if (component != null) {
            frontWrapper.add(component);
        }
    }

    public Component getFrontComponent() {
        return frontComponent != null ? frontComponent : frontWrapper;
    }

    public void setBackComponent(Component component) {
        backWrapper.removeAll();
        this.backComponent = component;
        if (component != null) {
            backWrapper.add(component);
        }
    }

    public Component getBackComponent() {
        return backComponent != null ? backComponent : backWrapper;
    }

    public void flip() {
        isFlipped = !isFlipped;
        if (isFlipped) {
            cardInner.getStyle().set("transform", "rotateY(180deg)");
        } else {
            cardInner.getStyle().set("transform", "rotateY(0deg)");
        }
    }

    public boolean isFlipped() {
        return isFlipped;
    }
}
