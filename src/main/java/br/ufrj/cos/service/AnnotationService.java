package br.ufrj.cos.service;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.repository.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnotationService {

    private final AnnotationRepository annotationRepository;

    @Autowired
    public AnnotationService(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    // 06 - CRUD Operations

    public List<Annotation> getAllAnnotations() {
        return annotationRepository.findAll();
    }

    public Optional<Annotation> getAnnotationById(Long id) {
        return annotationRepository.findById(id);
    }

    public Annotation createAnnotation(Annotation annotation) {
        return annotationRepository.save(annotation);
    }

    public Annotation updateAnnotation(Annotation annotation) {
        return annotationRepository.save(annotation);
    }

    public void deleteAnnotation(Long id) {
        annotationRepository.deleteById(id);
    }

    // Custom Queries

    public List<Annotation> getAnnotationsByUserApplication(UserApplication userApplication) {
        return annotationRepository.findByUserApplication(userApplication);
    }

    public List<Annotation> getAnnotationsByUserApplicationAndIoTDomain(UserApplication userApplication, IoTDomain ioTDomain) {
        return annotationRepository.findByUserApplicationAndIoTDomainsContainingOrderByLastUpdateDesc(userApplication, ioTDomain);
    }

    public List<Annotation> getAnnotationsByUserApplicationAndArchitecture(UserApplication userApplication, Architecture architecture) {
        return annotationRepository.findByUserApplicationAndArchitecturesContainingOrderByLastUpdateDesc(userApplication, architecture);
    }

    public List<Annotation> getAnnotationsByUserApplicationAndQualityRequirement(UserApplication userApplication, QualityRequirement qualityRequirement) {
        return annotationRepository.findByUserApplicationAndQualityRequirementsContainingOrderByLastUpdateDesc(userApplication, qualityRequirement);
    }

    public List<Annotation> getAnnotationsByUserApplicationAndTechnology(UserApplication userApplication, Technology technology) {
        return annotationRepository.findByUserApplicationAndTechnologiesContainingOrderByLastUpdateDesc(userApplication, technology);
    }

}
