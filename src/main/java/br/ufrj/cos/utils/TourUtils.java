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
        header.getStyle()
                .set("background", "linear-gradient(135deg, #1e293b, #2563eb)")
                .set("color", "#ffffff")
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("padding", "0.75rem 1.25rem")
                .set("border-radius", "14px 14px 0 0")
                .set("box-sizing", "border-box");

        header.setSpacing(true);
        header.add(new Html(String.format("<div>%s</div>", headerTitle)));
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        HorizontalLayout contentBox = new HorizontalLayout();
        contentBox.getStyle()
                .set("padding", "1rem 1.25rem")
                .set("font-size", "0.9rem")
                .set("line-height", "1.55")
                .set("color", "var(--lumo-body-text-color)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "0 0 14px 14px");

        contentBox.setSpacing(true);
        contentBox.setWidthFull();
        contentBox.add(content);

        step.setContent(contentBox);

        step.addBeforePopupShownListener(l -> {
            l.getHeader().removeAll();
            l.getHeader().add(header);
            l.getHeader().getElement().getStyle().set("padding", "0").set("background", "transparent");

            l.setModeless(false);
            l.setHighlightTarget(true);
            l.setFocusTrap(false);
            l.addThemeVariants(PopupVariant.LUMO_POINTER_ARROW);
            l.setIgnoreTargetClick(true);

            l.getStyle()
                    .set("border", "1px solid var(--lumo-contrast-15pct)")
                    .set("border-radius", "14px")
                    .set("box-shadow", "0 12px 32px rgba(0, 0, 0, 0.25), 0 2px 8px rgba(0, 0, 0, 0.1)")
                    .set("background", "var(--lumo-base-color)")
                    .set("overflow", "hidden");

            l.addPopupOpenChangedEventListener(e -> {
                if (e.isOpened()) {
                    UI.getCurrent().getPage().executeJs(
                            "const styleId = 'tour-backdrop-style';" +
                            "if (!document.getElementById(styleId)) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = styleId;" +
                            "  style.textContent = 'vcf-popup-overlay::part(backdrop) { background-color: rgba(0, 0, 0, 0.5) !important; opacity: 1 !important; }';" +
                            "  document.head.appendChild(style);" +
                            "}");
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
        return addStep(targetComponent, headerTitle, content, position, Optional.empty());
    }

    public void startTour() {
        if (!this.onboarding.isRunning()) {
            this.onboarding.start();
        }
    }
}
