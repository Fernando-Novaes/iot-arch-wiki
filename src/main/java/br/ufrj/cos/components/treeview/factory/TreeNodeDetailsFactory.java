package br.ufrj.cos.components.treeview.factory;

import br.ufrj.cos.components.sliderpanel.DataDetailsUpdateEvent;
import br.ufrj.cos.components.sliderpanel.SliderPanel;
import br.ufrj.cos.components.treeview.TreeNode;
import br.ufrj.cos.components.treeview.record.DataDetails;
import br.ufrj.cos.domain.*;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Setter
@Getter
@Component
public class TreeNodeDetailsFactory {

//    @Setter
//    private SliderPanel detailsSliderPanel;

    private DataDetails dataDetails;

    private final ApplicationEventPublisher eventPublisher;

    public TreeNodeDetailsFactory(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

//    @Autowired
//    public TreeNodeDetailsFactory(SliderPanel detailsSliderPanel) {
//        this.detailsSliderPanel = detailsSliderPanel;
//    }

    public Button createButtonForNode(TreeNode<?> node, String className) {
        //detailsSliderPanel.clearContents();

        Button btn = new Button();
        btn.addClassNames("button-base", className);

        Object data = node.getData();

        if (data instanceof ArchitectureSolution solution) {
            btn.setText(solution.getArchitecture().getName());
            btn.getElement().addEventListener("mouseover", event -> setArchitectureSolutionDetails(solution));
        } else if (data instanceof IoTDomain domain) {
            btn.setText(domain.getName());
            btn.getElement().addEventListener("mouseover", event -> setIoTDomainDetails(domain));
        } else if (data instanceof QualityRequirement qr) {
            btn.setText(qr.getName());
            btn.getElement().addEventListener("mouseover", event -> setQualityRequirementDetails(node, qr));
        } else if (data instanceof Technology tech) {
            btn.setText(tech.getDescription());
            btn.getElement().addEventListener("mouseover", event -> setTechnologyDetails(node, tech));
        }

        btn.getElement().addEventListener("mouseout", event -> this.publishEmptyDetails());

        return btn;
    }

    private void publishEmptyDetails() {
        eventPublisher.publishEvent(new DataDetailsUpdateEvent(new DataDetails()));
    }

    private void setIoTDomainDetails(IoTDomain domain) {
//        detailsSliderPanel.setIoTDomainContent(new HorizontalLayout(
//                new Html(formatSlidePanelDetails("IoT Domain", domain.getName(), domain.getDescription()))
//        ));

        this.dataDetails = new DataDetails();
        this.dataDetails.setIotDomain(domain);
        eventPublisher.publishEvent(new DataDetailsUpdateEvent(this.dataDetails));
    }

    private void setArchitectureSolutionDetails(ArchitectureSolution solution) {
//        setIoTDomainDetails(solution.getIoTDomain());
//
//        detailsSliderPanel.setArchitectureContent(new HorizontalLayout(
//                new Html(formatSlidePanelDetails("Architecture", solution.getArchitecture().getName(), solution.getDescription()))
//        ));
//
//        setReferenceDetails(solution.getPaperReference());

        this.dataDetails = new DataDetails();

        this.dataDetails.setIotDomain(solution.getIoTDomain());
        this.dataDetails.setArchitectureSolution(solution);
        this.dataDetails.setReference(solution.getPaperReference());

        eventPublisher.publishEvent(new DataDetailsUpdateEvent(this.dataDetails));
    }

    private void setQualityRequirementDetails(TreeNode<?> node, QualityRequirement qr) {
        ArchitectureSolution solution = (ArchitectureSolution) node.getParent().getData();
//        setIoTDomainDetails(solution.getIoTDomain());
//        setArchitectureSolutionDetails(solution);
//
        QualityRequirementTechnology assoc = solution.getQualityRequirementTechnologies()
                .stream()
                .filter(assocs -> assocs.getQualityRequirement().getId().equals(qr.getId()))
                .findFirst()
                .orElse(null);
//
//        detailsSliderPanel.setQualityRequirementContent(new HorizontalLayout(
//                new Html(formatSlidePanelDetails("Quality Requirement", qr.getName(),
//                        Optional.ofNullable(assoc.getQualityRequirement()).map(QualityRequirement::getDescription).orElse("No description")))
//        ));
//
//        setReferenceDetails(solution.getPaperReference());

        this.dataDetails = new DataDetails();
        this.dataDetails.setIotDomain(solution.getIoTDomain());
        this.dataDetails.setArchitectureSolution(solution);
        this.dataDetails.setQualityRequirement(qr);
        this.dataDetails.setReference(solution.getPaperReference());

        eventPublisher.publishEvent(new DataDetailsUpdateEvent(this.dataDetails));
    }

    private void setTechnologyDetails(TreeNode<?> node, Technology tech) {
        ArchitectureSolution solution = (ArchitectureSolution) node.getParent().getParent().getData();
//        setIoTDomainDetails(solution.getIoTDomain());
//        setArchitectureSolutionDetails(solution);

        this.dataDetails = new DataDetails();
        this.dataDetails.setIotDomain(solution.getIoTDomain());
        this.dataDetails.setArchitectureSolution(solution);

        QualityRequirementTechnology assoc = solution.getQualityRequirementTechnologies()
                .stream()
                .filter(assocs -> assocs.getTechnology().getId().equals(tech.getId()))
                .findFirst()
                .orElse(null);

        this.dataDetails.setQualityRequirement(assoc.getQualityRequirement());
        this.dataDetails.setTechnology(tech);
        this.dataDetails.setReference(solution.getPaperReference());

//        detailsSliderPanel.setQualityRequirementContent(new HorizontalLayout(
//                new Html(formatSlidePanelDetails("Quality Requirement",
//                        Optional.ofNullable(assoc).map(QualityRequirementTechnology::getQualityRequirement).map(QualityRequirement::getName).orElse("Unknown"),
//                        Optional.ofNullable(assoc).map(QualityRequirementTechnology::getQualityRequirement).map(QualityRequirement::getDescription).orElse("No description")))
//        ));
//
//        detailsSliderPanel.setTechnologyContent(new HorizontalLayout(
//                new Html(formatSlidePanelDetails("Technology", tech.getDescription(),
//                        Optional.ofNullable(assoc).map(QualityRequirementTechnology::getNotes).orElse("No description")))
//        ));
//
//        setReferenceDetails(solution.getPaperReference());

        eventPublisher.publishEvent(new DataDetailsUpdateEvent(this.dataDetails));
    }

//    private String formatSlidePanelDetails(String title, String name, String description) {
//        return String.format("<div><h3>%s:</h3><b>%s</b></br><div style='font-style: italic; margin-bottom: 5px;'>%s</div></div>",
//                title, name, Optional.ofNullable(description).orElse("No description"));
//    }

//    private void setReferenceDetails(PaperReference reference) {
//        if (reference != null) {
//            detailsSliderPanel.setReferenceDetails(new Html(
//                    String.format("<div style='font-style: italic;'><center><b>%s, %s</b></center></div>",
//                            reference.getTitle(), reference.getPublishYear())));
//        }
//    }
}

