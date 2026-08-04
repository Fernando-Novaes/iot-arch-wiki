package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.components.richtext.RichTextField;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.ThemeVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.wontlost.ckeditor.Config;
import com.wontlost.ckeditor.Constants;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;
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
    private RichTextField archNotes;

    @Autowired
    public ArchitectureSolutionDataManager(
            ArchitectureSolutionService architectureSolutionService,
            IoTDomainService ioTDomainService,
            PaperReferenceService paperReferenceService, QualityRequirementService qualityRequirementService,
            TechnologyService technologyService, ArchitectureService architectureService, QualityRequirementTechnologyService qualityRequirementTechnologyService, RichTextField archNotes) {

        this.architectureSolutionService = architectureSolutionService;
        this.ioTDomainService = ioTDomainService;
        this.paperReferenceService = paperReferenceService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;
        this.architectureService = architectureService;
        this.qualityRequirementTechnologyService = qualityRequirementTechnologyService;
        this.archNotes = archNotes;
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
        vl.setPadding(false);
        vl.setSpacing(true);

        ComboBox<Architecture> comboBoxArch = new ComboBox<>("Architecture", this.architectureService.findAll());
        comboBoxArch.setWidth("60%");
        comboBoxArch.setEnabled(false);
        comboBoxArch.setPlaceholder("Select the Architecture");

        ComboBox<IoTDomain> comboBoxDomain = new ComboBox<>("IoT Domain", this.ioTDomainService.findAllOrderByName());
        comboBoxDomain.setWidth("60%");
        comboBoxDomain.setEnabled(false);
        comboBoxDomain.setPlaceholder("Select the IoT Domain");

        ComboBox<PaperReference> comboPaper = new ComboBox<>("Paper Reference (APA 7)", this.paperReferenceService.finAllOrderByPaperReferenceTitle());
        comboPaper.setWidth("60%");
        comboPaper.setPlaceholder("Select the Paper Reference (APA 7)");
        comboPaper.setClearButtonVisible(true);

        Grid<QualityRequirementTechnology> qualityRequirementGrid = new Grid<>(QualityRequirementTechnology.class);
        qualityRequirementGrid.setWidthFull();
        qualityRequirementGrid.getColumnByKey("id").setVisible(false);

//        TextArea archNotes = new TextArea("Architecture Solution notes");
//        archNotes.setClearButtonVisible(true);
//        archNotes.setWidth("90%");

        archNotes.addDefaultToolBar();
        archNotes.setDefaultLayout();
        archNotes.get().setWidth("95%");
        archNotes.get().setMinHeight("200px");
        archNotes.get().setMaxHeight("200px");
        archNotes.get().getStyle().setOverflow(Style.Overflow.AUTO);
        archNotes.get().getStyle().setBorder("1px solid lightgrey");
        archNotes.get().setLabel("Architecture Solution Notes");

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


        // Add actions column (Edit + Delete)
        qualityRequirementGrid.addComponentColumn(knowledge -> {
            Button editButton = new Button("", event -> {
                this.openQRAndTechDialog(knowledge, qualityRequirementGrid).open();
            });
            editButton.setIcon(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            editButton.setTooltipText("Edit Quality Requirement & Technology");

            Button deleteButton = new Button("", event -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Confirm Deletion");
                confirmDialog.setText(String.format("Are you sure you want to delete this Quality Requirement [%s] and Technology [%s]?", knowledge.getQualityRequirement(), knowledge.getTechnology()));
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(confirmEvent -> {
                    this.deleteQualityRequirement(knowledge);
                    qualityRequirementGrid.setItems(this.architectureSolution.getQualityRequirementTechnologies());
                    qualityRequirementGrid.getDataProvider().refreshAll();
                });

                confirmDialog.open();
            });
            deleteButton.setIcon(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.setTooltipText(String.format("Delete %s - %s", knowledge.getQualityRequirement(), knowledge.getTechnology()));

            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setKey("actions");

        // Create a dedicated toolbar above the Grid
        Span gridTitle = new Span("Quality Requirements & Technologies");
        gridTitle.getStyle().set("font-weight", "600").set("font-size", "1.05rem").set("color", "var(--lumo-header-text-color)");

        Button addButton = new Button("Add Requirement and Technology", new Icon(VaadinIcon.PLUS), event -> {
            this.openQRAndTechDialog(null, qualityRequirementGrid).open();
        });
        addButton.setEnabled(false);
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout gridToolbar = new HorizontalLayout(gridTitle, addButton);
        gridToolbar.setWidthFull();
        gridToolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        gridToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        gridToolbar.getStyle().set("margin-top", "1rem").set("margin-bottom", "0.5rem");

        VerticalLayout vlGrid = new VerticalLayout();
        vlGrid.setPadding(false);
        vlGrid.setSpacing(false);
        vlGrid.setWidthFull();
        vlGrid.add(gridToolbar, qualityRequirementGrid);

        //Save Architecture Solution button
        Button saveBtn = new Button("Save");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(save -> {
            this.architectureSolution.setIoTDomain(comboBoxDomain.getValue());
            this.architectureSolution.setPaperReference(comboPaper.getValue());
            this.architectureSolution.setArchitecture(comboBoxArch.getValue());
            this.architectureSolution.setDescription(archNotes.get().getValue());
            this.architectureSolutionService.saveAndUpdate(this.architectureSolution);

            NotificationUtils.showSuccessNotification("Architecture Solution saved.");
        });

        //Cancel Architecture Solution button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addClickListener(click -> {
            this.paperReference = null;
            this.architectureSolution = null;
            this.prepareRegisterForm(comboBoxArch, comboBoxDomain, comboPaper, archNotes, qualityRequirementGrid, buttons, addButton);
        });

        comboPaper.addValueChangeListener(ref -> {
            if (ref.getValue() != null) {
                this.paperReference = ref.getValue();
                this.architectureSolution = ref.getValue().getArchitectureSolution();
            } else {
                this.paperReference = null;
                this.architectureSolution = null;
            }
            this.prepareRegisterForm(comboBoxArch, comboBoxDomain, comboPaper, archNotes, qualityRequirementGrid, buttons, addButton);
        });

        buttons.add(saveBtn, cancelBtn);

        if (archNotes != null && archNotes.get() != null) {
            archNotes.get().getElement().removeFromTree();
        }

        this.paperReference = null;
        this.architectureSolution = null;
        this.prepareRegisterForm(comboBoxArch, comboBoxDomain, comboPaper, archNotes, qualityRequirementGrid, buttons, addButton);

        vl.add(comboPaper, comboBoxArch, comboBoxDomain, archNotes.get(), vlGrid, buttons);

        return vl;
    }

    private void prepareRegisterForm(ComboBox comboBoxArch, ComboBox iotDomainComboBox, ComboBox paperReferenceComboBox,RichTextField archNotes,  Grid qualityRequirementGrid, HorizontalLayout buttons, Button addButton) {
        iotDomainComboBox.setEnabled(this.paperReference != null);
        comboBoxArch.setEnabled(this.paperReference != null);

        if (this.paperReference != null) {
            if (this.paperReference.getArchitectureSolution() != null) {
                iotDomainComboBox.setValue(this.paperReference.getArchitectureSolution().getIoTDomain());
                qualityRequirementGrid.setItems(new ArrayList());
                qualityRequirementGrid.setItems(this.paperReference.getArchitectureSolution().getQualityRequirementTechnologies());
                comboBoxArch.setValue(this.paperReference.getArchitectureSolution().getArchitecture());
                this.architectureSolution = this.paperReference.getArchitectureSolution();
                if (this.architectureSolution.getDescription() != null) archNotes.get().setValue(this.architectureSolution.getDescription());
                else archNotes.get().setValue("");
            } else {
                this.architectureSolution = new ArchitectureSolution();
                comboBoxArch.setItems(this.architectureService.findAll());
                iotDomainComboBox.setItems(this.ioTDomainService.findAllOrderByName());
                qualityRequirementGrid.setItems(new ArrayList<>());
                archNotes.get().setValue("");
            }
            addButton.setEnabled(true);
            buttons.setVisible(true);
        } else {
            comboBoxArch.setItems(this.architectureService.findAll());
            iotDomainComboBox.setItems(this.ioTDomainService.findAllOrderByName());
            paperReferenceComboBox.setItems(this.paperReferenceService.finAllOrderByPaperReferenceTitle());
            qualityRequirementGrid.setItems(new ArrayList<>());
            if (archNotes != null && archNotes.get() != null) {
                archNotes.get().setValue("");
            }
            buttons.setVisible(false);
            addButton.setEnabled(false);
            this.architectureSolution = new ArchitectureSolution();
            this.paperReference = null;
        }
    }

    private void deleteQualityRequirement(QualityRequirementTechnology qualityRequirementTechnology) {
        if (qualityRequirementTechnology == null) return;

        if (this.architectureSolution != null && this.architectureSolution.getQualityRequirementTechnologies() != null) {
            if (qualityRequirementTechnology.getId() != null) {
                this.architectureSolution.getQualityRequirementTechnologies().removeIf(q -> 
                    q != null && q.getId() != null && q.getId().equals(qualityRequirementTechnology.getId()));
            } else {
                this.architectureSolution.getQualityRequirementTechnologies().removeIf(q -> q == qualityRequirementTechnology);
            }
        }

        if (qualityRequirementTechnology.getId() != null) {
            try {
                this.qualityRequirementTechnologyService.delete(qualityRequirementTechnology);
            } catch (Exception e) {
                // Ignore if cascade or orphan removal handles it
            }
        }
        NotificationUtils.showSuccessNotification("Quality Requirement and Technology Deleted.");
    }

    private Dialog openQRAndTechDialog(QualityRequirementTechnology itemToEdit, Grid<QualityRequirementTechnology> qualityRequirementGrid) {
        Dialog dialog = new Dialog();
        boolean isEdit = itemToEdit != null;
        dialog.setHeaderTitle(isEdit ? "Edit Requirement & Technology" : "Add Requirement & Technology");

        VerticalLayout dialogLayout = new VerticalLayout();

        ComboBox<QualityRequirement> qualityRequirementComboDialog = new ComboBox<>("Select Quality Requirement", this.qualityRequirementService.findAllOrderedByName());
        qualityRequirementComboDialog.setWidthFull();

        ComboBox<Technology> technologyComboDialog = new ComboBox<>("Select Technology", this.technologyService.findAllOrderedByDescription());
        technologyComboDialog.setWidthFull();

        TextArea technoNotes = new TextArea("Notes");
        technoNotes.setWidthFull();

        if (isEdit) {
            qualityRequirementComboDialog.setValue(itemToEdit.getQualityRequirement());
            technologyComboDialog.setValue(itemToEdit.getTechnology());
            technoNotes.setValue(itemToEdit.getNotes() != null ? itemToEdit.getNotes() : "");
        }

        dialogLayout.add(qualityRequirementComboDialog, technologyComboDialog, technoNotes);
        dialog.add(dialogLayout);

        Button saveButton = new Button(isEdit ? "Update" : "Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(s -> {
            QualityRequirement qualityRequirement = qualityRequirementComboDialog.getValue();
            Technology technology = technologyComboDialog.getValue();

            if (qualityRequirement == null || technology == null) {
                NotificationUtils.showErrorNotification("Quality Requirement and Technology are required.");
                return;
            }

            if (isEdit) {
                itemToEdit.setQualityRequirement(qualityRequirement);
                itemToEdit.setTechnology(technology);
                itemToEdit.setNotes(technoNotes.getValue());
                if (itemToEdit.getId() != null) {
                    this.qualityRequirementTechnologyService.saveAndUpdate(itemToEdit);
                }
                NotificationUtils.showSuccessNotification("Quality Requirement and Technology updated.");
            } else {
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
                NotificationUtils.showSuccessNotification("Quality Requirement and Technology added.");
            }

            qualityRequirementGrid.setItems(this.architectureSolution.getQualityRequirementTechnologies());
            qualityRequirementGrid.getDataProvider().refreshAll();

            dialog.close();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }

}
