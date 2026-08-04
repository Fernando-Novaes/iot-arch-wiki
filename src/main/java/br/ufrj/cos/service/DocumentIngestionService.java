package br.ufrj.cos.service;

import br.ufrj.cos.domain.UploadedDocument;
import br.ufrj.cos.service.ai.AiRagService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);
    private static final String UPLOAD_DIR = "database/uploaded_docs";

    private final UploadedDocumentService uploadedDocumentService;
    private final AiRagService aiRagService;
    private final RAGService ragService;

    public DocumentIngestionService(UploadedDocumentService uploadedDocumentService,
                                    AiRagService aiRagService,
                                    RAGService ragService) {
        this.uploadedDocumentService = uploadedDocumentService;
        this.aiRagService = aiRagService;
        this.ragService = ragService;
        ensureStorageDirectory();
    }

    private void ensureStorageDirectory() {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            logger.error("Failed to create uploaded_docs directory: {}", e.getMessage());
        }
    }

    public UploadedDocument processAndSaveDocument(InputStream inputStream,
                                                  String originalFileName,
                                                  String title,
                                                  String category,
                                                  String version,
                                                  String notes) throws Exception {
        ensureStorageDirectory();

        byte[] fileBytes = inputStream.readAllBytes();
        long fileSize = fileBytes.length;
        String fileType = extractFileType(originalFileName);
        String checksum = calculateSha256(fileBytes);

        String extractedText = extractTextContent(fileBytes, fileType);
        if (extractedText.trim().isEmpty()) {
            throw new IllegalArgumentException("Extracted text from document is empty or unreadable.");
        }

        // Count chunks
        var splitter = DocumentSplitters.recursive(1000, 200);
        Document tempDoc = Document.from(extractedText);
        List<TextSegment> segments = splitter.split(tempDoc);
        int totalChunks = segments.size();

        // Check if updating an existing document with same title or filename
        Optional<UploadedDocument> existingOpt = uploadedDocumentService.findByFileName(originalFileName);
        UploadedDocument doc;

        if (existingOpt.isPresent()) {
            doc = existingOpt.get();
            doc.setDocumentTitle(title);
            doc.setCategory(category);
            doc.setVersion(version);
            doc.setFileSize(fileSize);
            doc.setChecksum(checksum);
            doc.setUploadDate(Instant.now());
            doc.setTotalChunks(totalChunks);
            doc.setNotes(notes);
        } else {
            doc = UploadedDocument.builder()
                    .documentTitle(title)
                    .fileName(originalFileName)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .category(category)
                    .version(version)
                    .checksum(checksum)
                    .uploadDate(Instant.now())
                    .totalChunks(totalChunks)
                    .notes(notes)
                    .build();
        }

        doc = uploadedDocumentService.save(doc);

        // Save raw file on disk
        Path destination = Paths.get(UPLOAD_DIR, doc.getId() + "_" + originalFileName);
        Files.write(destination, fileBytes);

        // Rebuild vector store with custom docs
        rebuildVectorStoreWithCustomDocs();

        return doc;
    }

    public void updateDocumentVersion(Long docId,
                                      InputStream newInputStream,
                                      String newFileName,
                                      String title,
                                      String category,
                                      String version,
                                      String notes) throws Exception {
        UploadedDocument doc = uploadedDocumentService.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + docId));

        if (newInputStream != null && newFileName != null && !newFileName.isEmpty()) {
            // Delete old file on disk
            Path oldPath = Paths.get(UPLOAD_DIR, doc.getId() + "_" + doc.getFileName());
            Files.deleteIfExists(oldPath);

            byte[] fileBytes = newInputStream.readAllBytes();
            String fileType = extractFileType(newFileName);
            String extractedText = extractTextContent(fileBytes, fileType);

            var splitter = DocumentSplitters.recursive(1000, 200);
            List<TextSegment> segments = splitter.split(Document.from(extractedText));

            doc.setFileName(newFileName);
            doc.setFileType(fileType);
            doc.setFileSize((long) fileBytes.length);
            doc.setChecksum(calculateSha256(fileBytes));
            doc.setTotalChunks(segments.size());

            Path newPath = Paths.get(UPLOAD_DIR, doc.getId() + "_" + newFileName);
            Files.write(newPath, fileBytes);
        }

        doc.setDocumentTitle(title);
        doc.setCategory(category);
        doc.setVersion(version);
        doc.setNotes(notes);
        doc.setUploadDate(Instant.now());

        uploadedDocumentService.save(doc);
        rebuildVectorStoreWithCustomDocs();
    }

    public void deleteDocument(Long docId) throws Exception {
        UploadedDocument doc = uploadedDocumentService.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + docId));

        Path filePath = Paths.get(UPLOAD_DIR, doc.getId() + "_" + doc.getFileName());
        Files.deleteIfExists(filePath);

        uploadedDocumentService.deleteById(docId);
        rebuildVectorStoreWithCustomDocs();
    }

    public InputStream getDocumentInputStream(Long docId) throws Exception {
        UploadedDocument doc = uploadedDocumentService.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + docId));

        Path filePath = Paths.get(UPLOAD_DIR, doc.getId() + "_" + doc.getFileName());
        if (!Files.exists(filePath)) {
            throw new java.io.FileNotFoundException("File not found on disk for ID: " + docId);
        }
        return Files.newInputStream(filePath);
    }

    public void rebuildVectorStoreWithCustomDocs() {
        try {
            logger.info("Rebuilding RAG Vector Store with Base System Data and Uploaded Reference Documents...");
            List<Document> allDocs = new ArrayList<>();

            // 1. System Base Documents from RAGService
            List<Document> systemDocs = ragService.generateStructuredDocuments();
            if (systemDocs != null) {
                allDocs.addAll(systemDocs);
            }

            // 2. Uploaded Custom Documents
            List<UploadedDocument> customDocs = uploadedDocumentService.findAll();
            for (UploadedDocument customDoc : customDocs) {
                Path filePath = Paths.get(UPLOAD_DIR, customDoc.getId() + "_" + customDoc.getFileName());
                if (Files.exists(filePath)) {
                    byte[] bytes = Files.readAllBytes(filePath);
                    String text = extractTextContent(bytes, customDoc.getFileType());

                    if (!text.isBlank()) {
                        Metadata meta = new Metadata();
                        meta.add("doc_id", customDoc.getId().toString());
                        meta.add("title", customDoc.getDocumentTitle());
                        meta.add("category", customDoc.getCategory());
                        meta.add("version", customDoc.getVersion());
                        meta.add("fileName", customDoc.getFileName());
                        meta.add("source", customDoc.getDocumentTitle() + " (v" + customDoc.getVersion() + ")");

                        allDocs.add(Document.from(text, meta));
                    }
                }
            }

            // Force index in AiRagService
            aiRagService.forceReindexDocuments(allDocs);
            logger.info("Vector Store rebuilt successfully with {} total documents (System + Custom).", allDocs.size());
        } catch (Exception e) {
            logger.error("Error rebuilding Vector Store with custom documents: {}", e.getMessage(), e);
        }
    }

    private String extractTextContent(byte[] bytes, String fileType) throws Exception {
        if ("pdf".equalsIgnoreCase(fileType)) {
            try (PDDocument pdfDocument = PDDocument.load(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(pdfDocument);
            }
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private String extractFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "TXT";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String calculateSha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
