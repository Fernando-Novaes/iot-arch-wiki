package br.ufrj.cos.components.annotation.events;

import br.ufrj.cos.components.annotation.AnnotationAction;
import br.ufrj.cos.domain.DomainBase;
import br.ufrj.cos.domain.UserApplication;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AnnotationDialogRequestedEvent extends ApplicationEvent {

    private final DomainBase domainBase;
    private final UserApplication user;
    private final AnnotationAction action; // Use the enum

    public AnnotationDialogRequestedEvent(Object source,
                                          UserApplication user,
                                          DomainBase domainBase,
                                          AnnotationAction action) {
        super(source);
        this.user = user;
        this.domainBase = domainBase;
        this.action = action;
    }


}