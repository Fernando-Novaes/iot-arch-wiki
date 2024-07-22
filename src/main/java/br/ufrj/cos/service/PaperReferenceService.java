package br.ufrj.cos.service;

import br.ufrj.cos.domain.PaperReference;
import br.ufrj.cos.repository.PaperReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaperReferenceService {

    private final PaperReferenceRepository paperReferenceRepository;

    @Autowired
    public PaperReferenceService(PaperReferenceRepository paperReferenceRepository) {
        this.paperReferenceRepository = paperReferenceRepository;
    }

    public List<PaperReference> findAll() {
        return paperReferenceRepository.findAll();
    }

    public PaperReference saveAndFlush(PaperReference paper) {
        return paperReferenceRepository.saveAndFlush(paper);
    }
}
