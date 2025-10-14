package br.ufrj.cos.utils;

import com.vaadin.componentfactory.Popup;
import com.vaadin.componentfactory.PopupPosition;
import com.vaadin.componentfactory.PopupVariant;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.function.SerializableConsumer;
import lombok.Getter;

import java.util.Optional;

public class TourUtils {

    @Getter
    private Onboarding onboarding;

    public TourUtils build() {
        this.onboarding = new Onboarding();

        return this;
    }

    public TourUtils addStep(Component targetComponent,
                        String headerTitle,
                        Component content,
                        PopupPosition position,
                        Optional<SerializableConsumer<Popup>> listener) {
        OnboardingStep step = new OnboardingStep(targetComponent);

        step.setPosition(position);

        HorizontalLayout header = new HorizontalLayout();
        header.getStyle().setBackgroundColor("#006af5");
        header.getStyle().setColor("white");
        header.getStyle().setFontSize("16px");
        header.getStyle().setFontWeight(Style.FontWeight.BOLD);
        header.setSpacing(true);
        header.add(new Html(String.format("<div>%s</div>", headerTitle)));
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setAlignItems(FlexComponent.Alignment.END);
        header.setWidthFull();
        header.setHeight("50%");
        header.getStyle().setBorder("solid 1px #006af5");

        HorizontalLayout contentBox = new HorizontalLayout();
        contentBox.setSpacing(true);
        contentBox.setAlignItems(FlexComponent.Alignment.END);
        contentBox.setWidthFull();
        contentBox.setHeight("100%");
        contentBox.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        contentBox.add(content);

        step.setContent(contentBox);

        step.addBeforePopupShownListener(l ->
        {
            l.getHeader().removeAll();
            l.getHeader().add(header);

            l.setModeless(true);
            l.setHighlightTarget(true);
            l.setFocusTrap(false);
            l.addThemeVariants(PopupVariant.LUMO_POINTER_ARROW);
            l.setIgnoreTargetClick(true);

            l.getStyle().setBorder("solid 1px gray");
            l.getStyle().setFontWeight("bold");
            l.getStyle().setBoxShadow("0 4px 6px rgba(0, 0, 0, 2.0)");
            l.addPopupOpenChangedEventListener(e -> {
                if (e.isOpened()) {
                    // Ensure opacity is within the valid range [0, 1]
                    double clampedOpacity = Math.max(0.0, Math.min(1.0, 0.6));

                    UI.getCurrent().getPage().executeJs(
                            String.format("const style = document.createElement('style');" +
                            "style.textContent = 'vcf-popup-overlay::part(backdrop) { background-color: rgba(0, 0, 0, %f); }';" +
                            "document.head.appendChild(style);", clampedOpacity));

                    System.out.println("Opacity applied: " + clampedOpacity);
                }
            });

            listener.ifPresent(c -> c.accept(l));
        });

        this.onboarding.addStep(step);

        return this;
    }

    public TourUtils addStep(Component targetComponent,
                              String headerTitle,
                              Component content,
                              PopupPosition position) {
        // Call the more complex method with an empty Optional.
        return addStep(targetComponent, headerTitle, content, position, Optional.empty());
    }

    public void startTour() {
        if (!this.onboarding.isRunning()) {
            this.onboarding.start();
        }
    }

}
