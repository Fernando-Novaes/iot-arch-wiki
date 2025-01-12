package br.ufrj.cos.service;

import br.ufrj.cos.domain.QualityRequirementTechnology;
import br.ufrj.cos.repository.QualityRequirementTechnologyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityRequirementTechnologyService {

    private final QualityRequirementTechnologyRepository qualityRequirementTechnologyRepository;

    @Autowired
    public QualityRequirementTechnologyService(QualityRequirementTechnologyRepository qualityRequirementTechnologyRepository) {
        this.qualityRequirementTechnologyRepository = qualityRequirementTechnologyRepository;
    }

    public void saveAndUpdate(QualityRequirementTechnology qualityRequirementTechnology) {
        this.qualityRequirementTechnologyRepository.saveAndFlush(qualityRequirementTechnology);
    }

    public void delete(QualityRequirementTechnology qualityRequirementTechnology) {
        this.qualityRequirementTechnologyRepository.delete(qualityRequirementTechnology);
    }
}
