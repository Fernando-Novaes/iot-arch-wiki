package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.QualityRequirementRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.repository.QualityRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualityRequirementService {

    private final QualityRequirementRepository qualityRequirementRepository;

    @Autowired
    public QualityRequirementService(QualityRequirementRepository qualityRequirementRepository) {
        this.qualityRequirementRepository = qualityRequirementRepository;
    }

    public List<QualityRequirementRecord> getQualityRequirementCountGroupedByName() {
        return qualityRequirementRepository.countQualityRequirementGroupedByName();
    }

    public List<QualityRequirement> findAll() {
        return this.qualityRequirementRepository.findAll();
    }

    public QualityRequirement saveAndFlush(QualityRequirement qualityRequirement) {
        return qualityRequirementRepository.saveAndFlush(qualityRequirement);
    }

    public QualityRequirement saveAndUpdate(QualityRequirement qualityRequirement) {
        return qualityRequirementRepository.saveAndFlush(qualityRequirement);
    }

    public List<QualityRequirement> findAllByArchitectureSolution(ArchitectureSolution architectureSolution) {
        return this.qualityRequirementRepository.findByArchitectureSolutionId(architectureSolution.getId());
    }
}
