package br.ufrj.cos.views.datamanager;

import br.ufrj.cos.domain.UploadedDocument;
import br.ufrj.cos.service.DocumentIngestionService;
import br.ufrj.cos.service.UploadedDocumentService;
import br.ufrj.cos.utils.NotificationUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReferenceDocumentDataManager {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceDocumentDataManager.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final UploadedDocumentService uploadedDocumentService;
    private final DocumentIngestionService documentIngestionService;

    private Grid<UploadedDocument> grid;
    private Runnable onDataChangedCallback;

    public ReferenceDocumentDataManager(UploadedDocumentService uploadedDocumentService,
                                        DocumentIngestionService documentIngestionService) {
        this.uploadedDocumentService = uploadedDocumentService;
        this.documentIngestionService = documentIngestionService;
    }

    public void setOnDataChangedCallback(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    public Component createDocumentCrud() {
        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(false);
        container.setSpacing(true);

        // Header / Action Bar
        Button btnUpload = new Button("Upload New Document / Standard", VaadinIcon.UPLOAD.create());
        btnUpload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnUpload.addClickListener(e -> openUploadDialog(null));

        Button btnReindexAll = new Button("Re-index Vector Store", VaadinIcon.REFRESH.create());
        btnReindexAll.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnReindexAll.addClickListener(e -> {
            try {
                documentIngestionService.rebuildVectorStoreWithCustomDocs();
                NotificationUtils.showSuccessNotification("Vector store re-indexed successfully!");
                refreshGrid();
            } catch (Exception ex) {
                NotificationUtils.showErrorNotification("Error re-indexing vector store: " + ex.getMessage());
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(btnUpload, btnReindexAll);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        toolbar.getStyle().set("margin-bottom", "0.5rem");

        // Grid
        grid = new Grid<>(UploadedDocument.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_COMPACT);
        grid.setHeight("100%");
        grid.setWidthFull();

        grid.addColumn(UploadedDocument::getDocumentTitle)
                .setHeader("Document Title")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addComponentColumn(doc -> {
            Span badge = new Span(doc.getCategory() != null ? doc.getCategory() : "General Reference");
            badge.getElement().getThemeList().add("badge pill small primary");
            return badge;
        }).setHeader("Category").setAutoWidth(true);

        grid.addComponentColumn(doc -> {
            Span badge = new Span(doc.getFileName() + " (" + (doc.getFileType() != null ? doc.getFileType().toUpperCase() : "TXT") + ")");
            badge.getElement().getThemeList().add("badge pill small contrast");
            return badge;
        }).setHeader("File Name").setAutoWidth(true);

        grid.addColumn(doc -> doc.getVersion() != null ? doc.getVersion() : "1.0")
                .setHeader("Version")
                .setAutoWidth(true);

        grid.addColumn(doc -> doc.getUploadDate() != null ? DATE_FORMATTER.format(doc.getUploadDate()) : "-")
                .setHeader("Upload Date")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActionsCell)
                .setHeader("Actions")
                .setAutoWidth(true);

        refreshGrid();

        container.add(toolbar, grid);
        return container;
    }

    private Component createActionsCell(UploadedDocument doc) {
        Button btnDownload = new Button(VaadinIcon.DOWNLOAD.create());
        btnDownload.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnDownload.setTooltipText("Download Document File");

        StreamResource resource = new StreamResource(
                doc.getFileName() != null ? doc.getFileName() : "document.pdf",
                () -> {
                    try {
                        return documentIngestionService.getDocumentInputStream(doc.getId());
                    } catch (Exception ex) {
                        logger.error("Error creating download stream for doc ID {}: {}", doc.getId(), ex.getMessage());
                        NotificationUtils.showErrorNotification("Could not download file: " + ex.getMessage());
                        return InputStream.nullInputStream();
                    }
                }
        );

        Anchor downloadAnchor = new Anchor(resource, "");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.add(btnDownload);

        Button btnEdit = new Button(VaadinIcon.EDIT.create());
        btnEdit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        btnEdit.setTooltipText("Edit Metadata / Update File Version");
        btnEdit.addClickListener(e -> openUploadDialog(doc));

        Button btnDelete = new Button(VaadinIcon.TRASH.create());
        btnDelete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnDelete.setTooltipText("Delete Document & Purge Vectors");
        btnDelete.addClickListener(e -> confirmDeleteDocument(doc));

        HorizontalLayout actions = new HorizontalLayout(downloadAnchor, btnEdit, btnDelete);
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        return actions;
    }

    private void openUploadDialog(UploadedDocument existingDoc) {
        boolean isEdit = existingDoc != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "✏️ Edit Document & Update Version" : "📄 Upload New Document / Standard");

        TextField titleField = new TextField("Document Title");
        titleField.setRequired(true);
        titleField.setPlaceholder("e.g. ISO/IEC 25010:2023 Systems Quality Model");
        titleField.setWidthFull();

        ComboBox<String> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems("ISO Standard", "Technical Specification", "Architecture Reference Model", "Scientific Reference Paper", "User Manual / Guide");
        categoryCombo.setAllowCustomValue(true);
        categoryCombo.setValue("ISO Standard");
        categoryCombo.setWidthFull();

        TextField versionField = new TextField("Version");
        versionField.setPlaceholder("e.g. 2023.1 or v1.0");
        versionField.setValue(isEdit && existingDoc.getVersion() != null ? existingDoc.getVersion() : "1.0");
        versionField.setWidthFull();

        TextArea notesField = new TextArea("Notes / Description");
        notesField.setPlaceholder("Optional notes regarding document scope or applicability...");
        notesField.setWidthFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".pdf", ".txt", ".md");
        upload.setMaxFileSize(50 * 1024 * 1024); // 50 MB
        upload.setDropLabel(new Span("Drop file here (.pdf, .txt, .md - max 50MB)"));
        upload.setWidthFull();
        upload.addFileRejectedListener(event -> {
            NotificationUtils.showErrorNotification("File rejected: " + event.getErrorMessage() + ". Maximum allowed size is 50MB.");
        });

        if (isEdit) {
            titleField.setValue(existingDoc.getDocumentTitle() != null ? existingDoc.getDocumentTitle() : "");
            if (existingDoc.getCategory() != null) categoryCombo.setValue(existingDoc.getCategory());
            notesField.setValue(existingDoc.getNotes() != null ? existingDoc.getNotes() : "");
        }

        VerticalLayout formLayout = new VerticalLayout(titleField, categoryCombo, versionField, notesField, upload);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        dialog.add(formLayout);

        Button saveButton = new Button(isEdit ? "Update Document & Re-Index" : "Upload & Index Document");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveButton.addClickListener(e -> {
            String title = titleField.getValue().trim();
            if (title.isEmpty()) {
                NotificationUtils.showErrorNotification("Please enter a valid document title.");
                return;
            }

            try {
                InputStream inputStream = buffer.getInputStream();
                String fileName = buffer.getFileName();

                if (!isEdit && (fileName == null || fileName.isEmpty())) {
                    NotificationUtils.showErrorNotification("Please select a file to upload.");
                    return;
                }

                saveButton.setEnabled(false);
                saveButton.setText("Processing & Indexing RAG Chunks...");

                if (isEdit) {
                    InputStream fileStream = (fileName != null && !fileName.isEmpty()) ? inputStream : null;
                    documentIngestionService.updateDocumentVersion(
                            existingDoc.getId(),
                            fileStream,
                            fileName,
                            title,
                            categoryCombo.getValue(),
                            versionField.getValue(),
                            notesField.getValue()
                    );
                    NotificationUtils.showSuccessNotification("Document '" + title + "' updated and re-indexed successfully!");
                } else {
                    documentIngestionService.processAndSaveDocument(
                            inputStream,
                            fileName,
                            title,
                            categoryCombo.getValue(),
                            versionField.getValue(),
                            notesField.getValue()
                    );
                    NotificationUtils.showSuccessNotification("Document '" + title + "' uploaded and indexed into RAG store!");
                }

                dialog.close();
                refreshGrid();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (Exception ex) {
                logger.error("Error saving document: {}", ex.getMessage(), ex);
                NotificationUtils.showErrorNotification("Error indexing document: " + ex.getMessage());
                saveButton.setEnabled(true);
                saveButton.setText(isEdit ? "Update Document & Re-Index" : "Upload & Index Document");
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void confirmDeleteDocument(UploadedDocument doc) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Document & Purge Vectors");
        confirmDialog.setText("Are you sure you want to delete '" + doc.getDocumentTitle() + "'? All associated vector chunks will be permanently purged from the offline RAG store.");
        confirmDialog.setConfirmText("Delete & Purge");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancel");

        confirmDialog.addConfirmListener(e -> {
            try {
                documentIngestionService.deleteDocument(doc.getId());
                NotificationUtils.showSuccessNotification("Document '" + doc.getDocumentTitle() + "' deleted and vectors purged.");
                refreshGrid();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (Exception ex) {
                logger.error("Error deleting document: {}", ex.getMessage(), ex);
                NotificationUtils.showErrorNotification("Failed to delete document: " + ex.getMessage());
            }
        });

        confirmDialog.open();
    }

    public void refreshGrid() {
        if (grid != null) {
            List<UploadedDocument> list = uploadedDocumentService.findAll();
            grid.setItems(list);
        }
    }
}
