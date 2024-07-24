package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.repository.ArchitectureSolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchitectureSolutionService {

    private final ArchitectureSolutionRepository architectureSolutionRepository;

    @Autowired
    public ArchitectureSolutionService(ArchitectureSolutionRepository architectureSolutionRepository) {
        this.architectureSolutionRepository = architectureSolutionRepository;
    }

    public List<ArchitectureSolutionRecord> geArchitectureSolutionCountGroupedByName() {
        return this.architectureSolutionRepository.countArchitectureSolutionGroupedByName();
    }

    public List<ArchitectureSolution> findAll() {
        return this.architectureSolutionRepository.findAll();
    }

    public ArchitectureSolution saveAndFlush(ArchitectureSolution architectureSolution) {
        return this.architectureSolutionRepository.saveAndFlush(architectureSolution);
    }

    public ArchitectureSolution saveAndUpdate(ArchitectureSolution architectureSolution) {
        return this.architectureSolutionRepository.saveAndFlush(architectureSolution);
    }
}
