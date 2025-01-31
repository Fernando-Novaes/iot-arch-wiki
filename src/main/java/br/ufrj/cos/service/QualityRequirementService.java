package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.QualityRequirementChartRecord;
import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.QualityRequirement;
import br.ufrj.cos.repository.QualityRequirementRepository;
import br.ufrj.cos.views.record.QualityRequirementRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
public class QualityRequirementService {

    private final QualityRequirementRepository qualityRequirementRepository;

    @Autowired
    public QualityRequirementService(QualityRequirementRepository qualityRequirementRepository) {
        this.qualityRequirementRepository = qualityRequirementRepository;
    }

    public List<QualityRequirementChartRecord> getQualityRequirementCountGroupedByName() {
        return qualityRequirementRepository.countQualityRequirementGroupedByName();
    }

    public List<QualityRequirementRecord> findAllQualityRequirementGroupedByName() {
        return qualityRequirementRepository.findAllQualityRequirementGroupedByName();
    }

    public List<QualityRequirement> findAllOrderedByName() {
        return this.qualityRequirementRepository.findAll(Sort.by("name"));
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

    public List<QualityRequirement> findAllByName(String name) {
        return this.qualityRequirementRepository.findAllByNameContainingIgnoreCase(name);
    }

    public void delete(QualityRequirement qualityRequirement) {
        this.qualityRequirementRepository.delete(qualityRequirement);
    }

    public List<QualityRequirement> listAllByNameDistinct() {
        return this.findAll().stream()
                .filter(distinctByKey(QualityRequirement::getName))
                .sorted((qr1, qr2) -> qr1.getName().compareToIgnoreCase(qr2.getName()))
                .collect(Collectors.toList());
    }

    // Helper method to create a distinct filter based on a key
    private <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Map<Object, Boolean> seen = new java.util.concurrent.ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public List<QualityRequirement> findByArchitectureSolution_NameContainingIgnoreCaseOOrderByNameAsc(String name){
        return this.qualityRequirementRepository.findByAssociations_ArchitectureSolution_Architecture_Name_ContainingIgnoreCaseOrderByNameAsc(name);
    }

    public List<QualityRequirement> findByIoTDomainAndArchitecture(String domainName, String architectureName) {
        return this.qualityRequirementRepository.findByIoTDomainAndArchitecture(domainName, architectureName);
    }
}
