package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.ArchitectureSolutionRecord;
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
}
