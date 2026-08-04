package br.ufrj.cos.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "uploaded_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedDocument extends DomainBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentTitle;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String category;
    private String version;
    private String checksum;
    private Instant uploadDate;
    private Integer totalChunks;

    @Lob
    private String notes;
}
