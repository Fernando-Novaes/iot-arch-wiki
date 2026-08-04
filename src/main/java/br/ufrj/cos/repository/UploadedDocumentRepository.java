package br.ufrj.cos.repository;

import br.ufrj.cos.domain.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {
    Optional<UploadedDocument> findByFileName(String fileName);
    List<UploadedDocument> findByCategory(String category);
}
