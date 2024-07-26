package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.TechnologyRecord;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.repository.TechnologyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnologyService {

    private final TechnologyRepository technologyRepository;

    @Autowired
    public TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    public List<TechnologyRecord> getTechnologyCountGroupedByName() {
        return technologyRepository.countTechnologyGroupedByName();
    }

    public Technology saveAndFlush(Technology technology) {
        return technologyRepository.saveAndFlush(technology);
    }

    public Technology saveAndUpdate(Technology technology) {
        return technologyRepository.saveAndFlush(technology);
    }

    public List<Technology> findAll(){
        return technologyRepository.findAll();
    }

    public List<Technology> findByArchitectureSolutionName(String architectureSolutionName) {
        return this.technologyRepository.findByArchitectureSolutionNameContainingIgnoreCase(architectureSolutionName);
    }
}
