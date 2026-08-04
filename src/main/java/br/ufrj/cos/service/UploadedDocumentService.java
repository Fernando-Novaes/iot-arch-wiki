package br.ufrj.cos.service;

import br.ufrj.cos.domain.UploadedDocument;
import br.ufrj.cos.repository.UploadedDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UploadedDocumentService {

    private final UploadedDocumentRepository repository;

    public UploadedDocumentService(UploadedDocumentRepository repository) {
        this.repository = repository;
    }

    public List<UploadedDocument> findAll() {
        return repository.findAll();
    }

    public Optional<UploadedDocument> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<UploadedDocument> findByFileName(String fileName) {
        return repository.findByFileName(fileName);
    }

    public UploadedDocument save(UploadedDocument document) {
        return repository.save(document);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
}
