package br.ufrj.cos.components.annotation;

import br.ufrj.cos.components.annotation.events.AnnotationDialogRequestedEvent;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.AnnotationService;
import br.ufrj.cos.service.UserApplicationService;
import br.ufrj.cos.utils.SecurityUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;
@UIScope
@Component
public class AnnotationComponent {

    private final UserApplicationService userApplicationService;
    private final AnnotationService annotationService;

    private final ApplicationEventPublisher eventPublisher; // Inject Event Publisher

    @Autowired
    public AnnotationComponent(AnnotationDialog annotationDialog, UserApplicationService userApplicationService, AnnotationService annotationService, ApplicationEventPublisher eventPublisher) {
        this.userApplicationService = userApplicationService;
        this.annotationService = annotationService;
        this.eventPublisher = eventPublisher;
    }

    public Popover create(com.vaadin.flow.component.Component component, com.vaadin.flow.component.UI layout, DomainBase domainBase) {
        Popover popover = new Popover();
        popover.setTarget(component);
        popover.setWidth("120px");
        popover.addThemeVariants(PopoverVariant.ARROW,
                PopoverVariant.LUMO_NO_PADDING);
        popover.setPosition(PopoverPosition.END);
        popover.setAriaLabelledBy("notifications-heading");
        popover.setOpenOnClick(true);

        popover.add(this.createLayout(domainBase));
        layout.add(popover);

        return popover;
    }

    private Div createLayout(DomainBase domainBase) {
        Div div = new Div();
        div.addClassName("annotation-box"); // For CSS styling (see below)

        // Title
        H4 title = new H4("Annotation");

        // Options
        Button addAnnotationButton = new Button("Take notes", new Icon(VaadinIcon.FILE_TEXT));
        addAnnotationButton.setWidth("120px");

        // Actions when you click the buttons
        addAnnotationButton.addClickListener(event -> {
            UserApplication user = userApplicationService.findByUserName(SecurityUtils.getUsername());

            if (domainBase instanceof IoTDomain domain) {
                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, user, domainBase,
                        (this.annotationService.getAnnotationsByUserApplicationAndIoTDomain(user, (IoTDomain) domainBase).isEmpty())? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (domainBase instanceof Architecture architecture) {
                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, user, domainBase,
                        (this.annotationService.getAnnotationsByUserApplicationAndArchitecture(user, (Architecture) domainBase).isEmpty())? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (domainBase instanceof QualityRequirement qr) {
                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, user, domainBase,
                        (this.annotationService.getAnnotationsByUserApplicationAndQualityRequirement(user, (QualityRequirement) domainBase).isEmpty())? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (domainBase instanceof Technology tech) {
                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, user, domainBase,
                        (this.annotationService.getAnnotationsByUserApplicationAndTechnology(user, (Technology) domainBase).isEmpty())? AnnotationAction.ADD : AnnotationAction.EDIT));
            }
        });

        // Layout
        VerticalLayout layout = new VerticalLayout(addAnnotationButton); //add title here
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().setAlignItems(Style.AlignItems.SELF_START); // Buttons fill the width

        div.add(layout);

        return div;
    }

}
