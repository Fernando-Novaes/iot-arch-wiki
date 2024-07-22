package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.QualityRequirementRecord;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.repository.QualityRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
