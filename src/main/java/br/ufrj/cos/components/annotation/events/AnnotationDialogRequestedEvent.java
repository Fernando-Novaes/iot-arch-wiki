package br.ufrj.cos.components.annotation.events;

import br.ufrj.cos.components.annotation.AnnotationAction;
import br.ufrj.cos.domain.Annotation;
import br.ufrj.cos.domain.DomainBase;
import br.ufrj.cos.domain.UserApplication;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AnnotationDialogRequestedEvent extends ApplicationEvent {

    private final Annotation annotation;
    private final AnnotationAction action; // Use the enum

    public AnnotationDialogRequestedEvent(Object source,
                                          Annotation annotation,
                                          AnnotationAction action) {
        super(source);
        this.annotation = annotation;
        this.action = action;
    }


}