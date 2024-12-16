package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@UIScope
@Component
class KnowledegeDataMananger {

    @Setter
    private ArchitectureSolution architectureSolution;
    private final ArchitectureSolutionService architectureSolutionService;
    private final IoTDomainService ioTDomainService;
    private final PaperReferenceService paperReferenceService;
    private final QualityRequirementService qualityRequirementService;
    private final TechnologyService technologyService;

    @Autowired
    public KnowledegeDataMananger(
            ArchitectureSolutionService architectureSolutionService,
            IoTDomainService ioTDomainService,
            PaperReferenceService paperReferenceService, QualityRequirementService qualityRequirementService, TechnologyService technologyService) {

        this.architectureSolutionService = architectureSolutionService;
        this.ioTDomainService = ioTDomainService;
        this.paperReferenceService = paperReferenceService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;
    }

    /***
     * Creates the Grid to correlates all data
     * Arch Solution -> Iot Domain -> Paper Reference and more
     *
     * @return GridCrud
     */
    public VerticalLayout createRegisterCrud() {

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);

        ComboBox<ArchitectureSolution> comboBoxArch = new ComboBox<>("Architecture", this.architectureSolutionService.findAllOrderedByName());
        comboBoxArch.setWidth("60%");

        ComboBox<IoTDomain> comboBoxDomain = new ComboBox<>("IoT Domain", this.ioTDomainService.findAllOrderByName());
        comboBoxDomain.setWidth("60%");
        comboBoxDomain.setEnabled(false);
        ComboBox<PaperReference> comboPaper = new ComboBox<>("Paper Reference", this.paperReferenceService.finAllOrderByPaperReferenceTitle());
        comboPaper.setWidth("60%");
        comboPaper.setEnabled(false);
        comboBoxArch.setPlaceholder("Select the Architecture");

        Grid<QualityRequirement> qualityRequirementGrid = new Grid<>(QualityRequirement.class);
        qualityRequirementGrid.setWidthFull();
        qualityRequirementGrid.getColumnByKey("id").setVisible(false);
        qualityRequirementGrid.getColumnByKey("architectureSolution").setVisible(false);
        qualityRequirementGrid.getColumnByKey("name").setVisible(false);
        qualityRequirementGrid.getColumnByKey("technology").setVisible(false);

        // Column for QualityRequirement name
        qualityRequirementGrid.addColumn(QualityRequirement::getName)
                .setHeader("Quality Requirement")
                .setSortable(true);

        // Column for associated Technologies
        qualityRequirementGrid.addColumn(qr ->
                        qr.getTechnology() != null
                                ? qr.getTechnology()
                                : "No Technology")
                .setHeader("Technologies")
                .setSortable(true);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setVisible(false);
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.END);
        buttons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        buttons.setWidthFull();

        //Save button
        Button saveBtn = new Button("Save");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(save -> {
            this.architectureSolution.setIoTDomain(comboBoxDomain.getValue());
            this.architectureSolution.setPaperReference(comboPaper.getValue());
            this.architectureSolutionService.saveAndUpdate(this.architectureSolution);

            Notification.show("Architecture saved.");
        });

        //Cancel buttonm
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addClickListener(click -> {
            this.prepareRegisterForm(null, comboBoxArch, comboBoxDomain, comboPaper, qualityRequirementGrid, buttons);
        });

        comboBoxArch.addValueChangeListener(arc -> {
            this.architectureSolution = arc.getValue();
            this.prepareRegisterForm(arc.getValue(), comboBoxArch, comboBoxDomain, comboPaper, qualityRequirementGrid, buttons);
        });

        // Add a delete button column
        qualityRequirementGrid.addComponentColumn(qr -> {
            Button deleteButton = new Button("", event -> {
                // Confirm the deletion
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Confirm Deletion");
                confirmDialog.setText(String.format("Are you sure you want to delete this Quality Requirement [%s]?", qr.getName()));
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(confirmEvent -> {
                    // Perform deletion logic
                    this.deleteQualityRequirement(qr);
                    qualityRequirementGrid.setItems(this.architectureSolution.getQrs());
                });

                confirmDialog.open();
            });
            deleteButton.setIcon(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.setTooltipText(String.format("Delete %s", qr.getName()));

            return deleteButton;
        }).setHeader("Actions").setKey("actions");

        // Add a header row with a button in the header
        HeaderRow headerRow = qualityRequirementGrid.prependHeaderRow();

        // Create a button
        Button addButton = new Button("Add Requirement and Technology", event -> {
            this.openAddQRAndTech(qualityRequirementGrid).open();
        });

        // Customize button style
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        // Add the button to the header of the QualityRequirement column
        headerRow.getCell(qualityRequirementGrid.getColumnByKey("actions")).setComponent(addButton);
        headerRow.getCell(qualityRequirementGrid.getColumnByKey("actions")).getComponent().getStyle().setAlignItems(Style.AlignItems.END);

        buttons.add(saveBtn, cancelBtn);

        vl.add(comboBoxDomain, comboBoxArch, comboPaper, qualityRequirementGrid, buttons);

        return vl;
    }

    /***
     * Method to set enabled or disabled the elements of the form
     *
     * @param architectureSolution
     * @param iotDomainComboBox
     * @param paperReferenceComboBox
     * @param qualityRequirementGrid
     */
    private void prepareRegisterForm(ArchitectureSolution architectureSolution, ComboBox comboBoxArch, ComboBox iotDomainComboBox, ComboBox paperReferenceComboBox, Grid qualityRequirementGrid, HorizontalLayout buttons) {
        iotDomainComboBox.setEnabled(architectureSolution != null);
        paperReferenceComboBox.setEnabled(architectureSolution != null);

        if (architectureSolution != null) {
            iotDomainComboBox.setValue(architectureSolution.getIoTDomain());
            paperReferenceComboBox.setValue(architectureSolution.getPaperReference());
            qualityRequirementGrid.setItems(architectureSolution.getQrs());
            buttons.setVisible(true);
        } else {
            comboBoxArch.setItems(this.architectureSolutionService.findAllOrderedByName());
            iotDomainComboBox.setItems(this.ioTDomainService.findAllOrderByName());
            paperReferenceComboBox.setItems(this.paperReferenceService.finAllOrderByPaperReferenceTitle());
            qualityRequirementGrid.setItems(new ArrayList<>());
            buttons.setVisible(false);
            this.architectureSolution = new ArchitectureSolution();
        }
    }

    /***
     * Deleting logically quality requirement
     *
     * @param qr
     * @return
     */
    private void deleteQualityRequirement(QualityRequirement qr) {
        this.architectureSolution.getQrs().stream().filter(q -> q.getId().equals(qr.getId())).findAny().ifPresent(q -> {
            q.getTechnology().setArchitectureSolution(null);
            q.setTechnology(null);
            q.setArchitectureSolution(null);
            this.architectureSolution.getQrs().remove(q);
        });

        Notification.show("Quality Requirement and Technology Deleted.");
    }

    /***
     * Creates the dialog window to add quality requirement and technology
     *
     * @return Dialog
     */
    private Dialog openAddQRAndTech(Grid<QualityRequirement> qualityRequirementGrid) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Add");

        VerticalLayout dialogLayout = new VerticalLayout();

        ComboBox<QualityRequirement> qualityRequirementComboDialog = new ComboBox<>("Select Quality Requirement", this.qualityRequirementService.findAllOrderedByName());
        ComboBox<Technology> technologyComboDialog = new ComboBox<>("Select Technology", this.technologyService.findAllOrderedByDescription());

        dialogLayout.add(qualityRequirementComboDialog, technologyComboDialog);

        dialog.add(dialogLayout);

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(s -> {
            QualityRequirement qualityRequirement = qualityRequirementComboDialog.getValue();
            qualityRequirement.setArchitectureSolution(this.architectureSolution);

            Technology technology = technologyComboDialog.getValue();
            technology.setArchitectureSolution(this.architectureSolution);
            technology.setQualityRequirement(qualityRequirement);

            this.architectureSolution.getTechnologies().add(technology);

            technology = this.technologyService.saveAndFlush(technology);

            qualityRequirement.setTechnology(technology);
            qualityRequirement.getTechnology().setArchitectureSolution(this.architectureSolution);

            this.architectureSolution.getQrs().add(qualityRequirement);
            qualityRequirementGrid.setItems(this.architectureSolution.getQrs());

            dialog.close();
            Notification.show("Quality Requirement added.");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

}
