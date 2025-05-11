package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.service.*;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.Text;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@UIScope
@Component
class ArchitectureSolutionDataManager {

    @Setter
    private ArchitectureSolution architectureSolution;
    @Setter
    private PaperReference paperReference;
    private final ArchitectureSolutionService architectureSolutionService;
    private final IoTDomainService ioTDomainService;
    private final PaperReferenceService paperReferenceService;
    private final QualityRequirementService qualityRequirementService;
    private final TechnologyService technologyService;
    private final ArchitectureService architectureService;
    private final QualityRequirementTechnologyService qualityRequirementTechnologyService;

    @Autowired
    public ArchitectureSolutionDataManager(
            ArchitectureSolutionService architectureSolutionService,
            IoTDomainService ioTDomainService,
            PaperReferenceService paperReferenceService, QualityRequirementService qualityRequirementService,
            TechnologyService technologyService, ArchitectureService architectureService, QualityRequirementTechnologyService qualityRequirementTechnologyService) {

        this.architectureSolutionService = architectureSolutionService;
        this.ioTDomainService = ioTDomainService;
        this.paperReferenceService = paperReferenceService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;
        this.architectureService = architectureService;
        this.qualityRequirementTechnologyService = qualityRequirementTechnologyService;
    }

    /***
     * Creates the Grid to correlates all data
     * Arch Solution -> Iot Domain -> Paper Reference and more
     *
     * @return GridCrud
     */
    public VerticalLayout createKnowledgeCrud() {

        VerticalLayout vl = new VerticalLayout();
        vl.setWidthFull();
        vl.setHeight("80%");
        vl.setSpacing(true);
        vl.getStyle().setOverflow(Style.Overflow.AUTO);

        ComboBox<Architecture> comboBoxArch = new ComboBox<>("Architecture", this.architectureService.findAll());
        comboBoxArch.setWidth("60%");
        comboBoxArch.setEnabled(false);
        comboBoxArch.setPlaceholder("Select the Architecture");

        ComboBox<IoTDomain> comboBoxDomain = new ComboBox<>("IoT Domain", this.ioTDomainService.findAllOrderByName());
        comboBoxDomain.setWidth("60%");
        comboBoxDomain.setEnabled(false);
        comboBoxDomain.setPlaceholder("Select the IoT Domain");

        ComboBox<PaperReference> comboPaper = new ComboBox<>("Paper Reference", this.paperReferenceService.finAllOrderByPaperReferenceTitle());
        comboPaper.setWidth("60%");
        comboPaper.setPlaceholder("Select the Paper Reference");
        comboPaper.setClearButtonVisible(true);

        Grid<QualityRequirementTechnology> qualityRequirementGrid = new Grid<>(QualityRequirementTechnology.class);
        qualityRequirementGrid.setWidthFull();
        qualityRequirementGrid.getColumnByKey("id").setVisible(false);

        TextArea archNotes = new TextArea("Architecture Solution notes");
        archNotes.setClearButtonVisible(true);
        archNotes.setWidth("90%");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setVisible(false);
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.END);
        buttons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        buttons.setWidthFull();

        HorizontalLayout hlGrid = new HorizontalLayout();
        buttons.setVisible(true);
        hlGrid.setSpacing(true);
        hlGrid.setAlignItems(FlexComponent.Alignment.CENTER);
        hlGrid.setWidthFull();
        hlGrid.add(qualityRequirementGrid);


        // Add a delete button column
        qualityRequirementGrid.addComponentColumn(knowledge -> {
            Button deleteButton = new Button("", event -> {
                // Confirm the deletion
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Confirm Deletion");
                confirmDialog.setText(String.format("Are you sure you want to delete this Quality Requirement [%s] and Technology [%s]?", knowledge.getQualityRequirement(), knowledge.getTechnology()));
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(confirmEvent -> {
                    // Perform deletion logic
                    this.deleteQualityRequirement(knowledge);
                    qualityRequirementGrid.setItems(this.architectureSolution.getQualityRequirementTechnologies());
                });

                confirmDialog.open();
            });
            deleteButton.setIcon(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.setTooltipText(String.format("Delete %s - %s", knowledge.getQualityRequirement(), knowledge.getTechnology()));

            return deleteButton;
        }).setHeader("Actions").setKey("actions");

        // Add a header row with a button in the header
        HeaderRow headerRow = qualityRequirementGrid.prependHeaderRow();

        // Create a button
        Button addButton = new Button("Add Requirement and Technology", event -> {
            this.openAddQRAndTech(qualityRequirementGrid).open();
        });
        addButton.setEnabled(false);

        // Customize button style
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        //Save Architecture Solution button
        Button saveBtn = new Button("Save");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(save -> {
            this.architectureSolution.setIoTDomain(comboBoxDomain.getValue());
            this.architectureSolution.setPaperReference(comboPaper.getValue());
            this.architectureSolution.setArchitecture(comboBoxArch.getValue());
            this.architectureSolution.setDescription(archNotes.getValue());
            this.architectureSolutionService.saveAndUpdate(this.architectureSolution);

            NotificationUtils.showSuccessNotification("Architecture Solution saved.");
        });

        //Cancel Architecture Solution button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addClickListener(click -> {
            this.paperReference = null;
            this.architectureSolution = null;
            this.prepareRegisterForm(comboBoxArch, comboBoxDomain, comboPaper, qualityRequirementGrid, archNotes, buttons, addButton);
        });

        comboPaper.addValueChangeListener(ref -> {
            if (ref.getValue() != null) {
                this.paperReference = ref.getValue();
                this.architectureSolution = ref.getValue().getArchitectureSolution();
                this.prepareRegisterForm(comboBoxArch, comboBoxDomain, comboPaper, qualityRequirementGrid, archNotes, buttons, addButton);
            }
        });

        // Add the button to the header of the QualityRequirement column
        headerRow.getCell(qualityRequirementGrid.getColumnByKey("actions")).setComponent(addButton);
        headerRow.getCell(qualityRequirementGrid.getColumnByKey("actions")).getComponent().getStyle().setAlignItems(Style.AlignItems.END);

        buttons.add(saveBtn, cancelBtn);

        vl.add(comboPaper, comboBoxArch, comboBoxDomain, archNotes, hlGrid, buttons);

        return vl;
    }

    /***
     * Method to set enabled or disabled the elements of the form
     *
     * @param iotDomainComboBox
     * @param paperReferenceComboBox
     * @param qualityRequirementGrid
     */
    private void prepareRegisterForm(ComboBox comboBoxArch, ComboBox iotDomainComboBox, ComboBox paperReferenceComboBox, Grid qualityRequirementGrid, TextArea archNotes, HorizontalLayout buttons, Button addButton) {
        iotDomainComboBox.setEnabled(this.paperReference != null);
        //paperReferenceComboBox.setEnabled(this.paperReference != null);
        comboBoxArch.setEnabled(this.paperReference != null);

        if (this.paperReference != null) {
            if (this.paperReference.getArchitectureSolution() != null) {
                iotDomainComboBox.setValue(this.paperReference.getArchitectureSolution().getIoTDomain());
                //paperReferenceComboBox.setValue(this.architectureSolution.getPaperReference());
                qualityRequirementGrid.setItems(new ArrayList());
                qualityRequirementGrid.setItems(this.paperReference.getArchitectureSolution().getQualityRequirementTechnologies());
                comboBoxArch.setValue(this.paperReference.getArchitectureSolution().getArchitecture());
                this.architectureSolution = this.paperReference.getArchitectureSolution();
                if (this.architectureSolution.getDescription() != null) archNotes.setValue(this.architectureSolution.getDescription());
                else archNotes.setValue("");
            } else {
                this.architectureSolution = new ArchitectureSolution();
                comboBoxArch.setItems(this.architectureService.findAll());
                iotDomainComboBox.setItems(this.ioTDomainService.findAllOrderByName());
                qualityRequirementGrid.setItems(new ArrayList<>());
                archNotes.clear();
            }
            addButton.setEnabled(true);
            buttons.setVisible(true);
        } else {
            comboBoxArch.setItems(this.architectureService.findAll());
            iotDomainComboBox.setItems(this.ioTDomainService.findAllOrderByName());
            paperReferenceComboBox.setItems(this.paperReferenceService.finAllOrderByPaperReferenceTitle());
            qualityRequirementGrid.setItems(new ArrayList<>());
            archNotes.clear();
            buttons.setVisible(false);
            addButton.setEnabled(false);
            this.architectureSolution = new ArchitectureSolution();
            this.paperReference = new PaperReference();
        }
    }

    /***
     * Deleting logically ArchitectureSolutionQualityRequirementTechnology
     *
     * @param
     * @return
     */
    private void deleteQualityRequirement(QualityRequirementTechnology qualityRequirementTechnology) {
        this.architectureSolution.getQualityRequirementTechnologies().stream().filter(
                q -> q.getId().equals(qualityRequirementTechnology.getId())).findAny().ifPresent(q -> {
                    this.architectureSolution.getQualityRequirementTechnologies().remove(q);
                    this.qualityRequirementTechnologyService.delete(qualityRequirementTechnology);
                });

        NotificationUtils.showSuccessNotification("Quality Requirement and Technology Deleted.");
    }

    /***
     * Creates the dialog window to add quality requirement and technology
     *
     * @return Dialog
     */
    private Dialog openAddQRAndTech(Grid<QualityRequirementTechnology> qualityRequirementGrid) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Add");

        VerticalLayout dialogLayout = new VerticalLayout();

        ComboBox<QualityRequirement> qualityRequirementComboDialog = new ComboBox<>("Select Quality Requirement", this.qualityRequirementService.findAllOrderedByName());
        ComboBox<Technology> technologyComboDialog = new ComboBox<>("Select Technology", this.technologyService.findAllOrderedByDescription());
        TextArea technoNotes = new TextArea();

        dialogLayout.add(qualityRequirementComboDialog, technologyComboDialog, technoNotes);
        dialog.add(dialogLayout);

        //Save Quality Requirement and Technology
        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(s -> {
            QualityRequirement qualityRequirement = qualityRequirementComboDialog.getValue();
            Technology technology = technologyComboDialog.getValue();

            QualityRequirementTechnology requirementTechnology = new QualityRequirementTechnology();
            requirementTechnology.setQualityRequirement(qualityRequirement);
            requirementTechnology.setTechnology(technology);
            requirementTechnology.setArchitectureSolution(this.architectureSolution);
            requirementTechnology.setNotes(technoNotes.getValue());

            if (this.architectureSolution.getQualityRequirementTechnologies() != null) {
                this.architectureSolution.getQualityRequirementTechnologies().add(requirementTechnology);
            } else {
                this.architectureSolution.setQualityRequirementTechnologies(new ArrayList<>());
                this.architectureSolution.getQualityRequirementTechnologies().add(requirementTechnology);
            }

            qualityRequirementGrid.setItems(this.architectureSolution.getQualityRequirementTechnologies());
            qualityRequirementGrid.getDataProvider().refreshAll();

            dialog.close();
            NotificationUtils.showSuccessNotification("Quality Requirement added.");
        });

        //Cancel Quality Requirement and Technology
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

}
