package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.repository.ArchitectureSolutionRepository;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchitectureSolutionService {

    private final ArchitectureSolutionRepository architectureSolutionRepository;

    @Autowired
    public ArchitectureSolutionService(ArchitectureSolutionRepository architectureSolutionRepository) {
        this.architectureSolutionRepository = architectureSolutionRepository;
    }

    public List<ArchitectureSolutionChartRecord> geArchitectureSolutionCountGroupedByName() {
        return this.architectureSolutionRepository.countArchitectureSolutionGroupedByName();
    }

    public List<ArchitectureSolutionRecord> findAllArchitectureSolutionGroupedByName() {
        return this.architectureSolutionRepository.findAllArchitectureSolutionGroupedByName();
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

    public List<ArchitectureSolution> findByQualityRequirementId(Long id) {
        return this.architectureSolutionRepository.findByQualityRequirementId(id);
    }

    public List<ArchitectureSolution> findByNameContainingIgnoreCase(String name) {
        return this.architectureSolutionRepository.findByArchitecture_NameContainingIgnoreCase(name);
    }

    public List<ArchitectureSolution> findAllOrderedByName() {
        return this.architectureSolutionRepository.findAll(Sort.by("architecture.name"));
    }

    public List<ArchitectureSolution> findAllByIoTDomain(IoTDomain domain) {
        return this.architectureSolutionRepository.findArchitectureSolutionByIoTDomain(domain);
    }

    public void delete(ArchitectureSolution architectureSolution) {
        this.architectureSolutionRepository.delete(architectureSolution);
    }
}
