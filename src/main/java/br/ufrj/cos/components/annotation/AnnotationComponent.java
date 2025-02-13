package br.ufrj.cos.components.annotation;

import br.ufrj.cos.components.annotation.events.AnnotationDialogRequestedEvent;
import br.ufrj.cos.components.treeview.TreeNode;
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

import java.util.ArrayList;
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

    public Popover create(com.vaadin.flow.component.Component component, com.vaadin.flow.component.UI layout, TreeNode<?> node) {
        Popover popover = new Popover();
        popover.setTarget(component);
        popover.setWidth("120px");
        popover.addThemeVariants(PopoverVariant.ARROW,
                PopoverVariant.LUMO_NO_PADDING);
        popover.setPosition(PopoverPosition.END);
        popover.setAriaLabelledBy("notifications-heading");
        popover.setOpenOnClick(true);

        popover.add(this.createLayout(node));
        layout.add(popover);

        return popover;
    }

    private Div createLayout(TreeNode<?> node) {
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
            Annotation a;

            if (node.getData() instanceof IoTDomain domain) {
                a = this.annotationService.findMostRecentOnlyUserAppAndDomain(user, domain)
                        .orElse(Annotation.builder().userApplication(user).build());

                if (a.getAnnotationDomains() == null) {
                    a.setAnnotationDomains(new ArrayList<>());
                    a.getAnnotationDomains().add(AnnotationDomain.builder().ioTDomain(domain).annotation(a).build());
                }

                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, a,
                            (a.getAnnotationDomains() != null) ? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (node.getData() instanceof ArchitectureSolution architecture) {
                a = this.annotationService.findMostRecentAnnotation(user, architecture.getIoTDomain(), architecture.getArchitecture())
                        .orElse(Annotation.builder().userApplication(user).build());

                if (a.getAnnotationDomains() == null) {
                    a.setAnnotationDomains(new ArrayList<>());
                    a.getAnnotationDomains().add(AnnotationDomain.builder().ioTDomain(architecture.getIoTDomain()).architecture(architecture.getArchitecture()).annotation(a).build());
                }

                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, a,
                        (a.getAnnotationDomains() != null)? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (node.getData() instanceof QualityRequirement qr) {
                IoTDomain d = (IoTDomain) node.getParent().getParent().getData();
                Architecture arch = ((ArchitectureSolution) node.getParent().getData()).getArchitecture();

                a = this.annotationService.findMostRecentAnnotation(user, d, arch, qr)
                        .orElse(Annotation.builder().userApplication(user).build());

                if (a.getAnnotationDomains() == null) {
                    a.setAnnotationDomains(new ArrayList<>());
                    a.getAnnotationDomains().add(AnnotationDomain.builder().ioTDomain(d).architecture(arch).qualityRequirement(qr).annotation(a).build());
                }

                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, a,
                        (a.getAnnotationDomains() != null)? AnnotationAction.ADD : AnnotationAction.EDIT));
            } else if (node.getData() instanceof Technology tech) {
                IoTDomain d = (IoTDomain) node.getParent().getParent().getParent().getData();
                Architecture arch = ((ArchitectureSolution) node.getParent().getParent().getData()).getArchitecture();
                QualityRequirement qr = ((QualityRequirement) node.getParent().getData());

                a = this.annotationService.findMostRecentAnnotation(user, d, arch, qr, tech)
                        .orElse(Annotation.builder().userApplication(user).build());

                if (a.getAnnotationDomains() == null) {
                    a.setAnnotationDomains(new ArrayList<>());
                    a.getAnnotationDomains().add(AnnotationDomain.builder().ioTDomain(d).architecture(arch).qualityRequirement(qr).technology(tech).annotation(a).build());
                }

                eventPublisher.publishEvent(new AnnotationDialogRequestedEvent(this, a,
                                (a.getAnnotationDomains() != null)? AnnotationAction.ADD : AnnotationAction.EDIT));
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
