package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.domain.Architecture;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.repository.ArchitectureRepository;
import br.ufrj.cos.repository.ArchitectureSolutionRepository;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchitectureService {

    private final ArchitectureRepository architectureRepository;

    @Autowired
    public ArchitectureService(ArchitectureRepository architectureRepository) {
        this.architectureRepository = architectureRepository;
    }

    public List<Architecture> findAll() {
        return this.architectureRepository.findAllOrderByName();
    }

    public Architecture saveAndFlush(Architecture architecture) {
        return this.architectureRepository.saveAndFlush(architecture);
    }

    public Architecture saveAndUpdate(Architecture architecture) {
        return this.architectureRepository.saveAndFlush(architecture);
    }

    public void delete(Architecture architecture) {
        this.architectureRepository.delete(architecture);
    }
}
