package br.ufrj.cos.components.annotation;

import br.ufrj.cos.domain.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class AnnotationData {

        String text;
        UserApplication userApplication;
        Date lastUpdate;
        DomainBase domainBase;

        public Annotation getAnnotation() {
            Annotation annotation = new Annotation();
            annotation.setText(text);
            annotation.setUserApplication(userApplication);
            annotation.setLastUpdate(lastUpdate);

            if (domainBase instanceof IoTDomain domain) {
                if (annotation.getIoTDomains() == null) {
                    List<IoTDomain> ioTDomains = new ArrayList<>();
                    ioTDomains.add(domain);
                    annotation.setIoTDomains(ioTDomains);
                } else {
                    annotation.getIoTDomains().add(domain);
                }
            } else if (domainBase instanceof Architecture architecture) {
                if (annotation.getIoTDomains() == null) {
                    List<Architecture> archs = new ArrayList<>();
                    archs.add(architecture);
                    annotation.setArchitectures(archs);
                } else {
                    annotation.getArchitectures().add(architecture);
                }
                annotation.getArchitectures().add(architecture);
            } else if (domainBase instanceof QualityRequirement qr) {
                if (annotation.getQualityRequirements() == null) {
                    List<QualityRequirement> qrs = new ArrayList<>();
                    qrs.add(qr);
                    annotation.setQualityRequirements(qrs);
                } else {
                    annotation.getQualityRequirements().add(qr);
                }
            } else if (domainBase instanceof Technology tech) {
                if (annotation.getTechnologies() == null) {
                    List<Technology> techs = new ArrayList<>();
                    techs.add(tech);
                    annotation.setTechnologies(techs);
                } else {
                    annotation.getTechnologies().add(tech);
                }
            }

            return annotation;
        }

}
