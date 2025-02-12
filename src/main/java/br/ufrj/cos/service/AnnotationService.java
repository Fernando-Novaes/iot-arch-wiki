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

    public Optional<Annotation> getAnnotationsByUserApplicationAndIoTDomain(UserApplication userApplication, IoTDomain ioTDomain) {
        List<Annotation> list = annotationRepository.findFirstByUserApplicationAndIoTDomainOrderByLastUpdateDesc(userApplication, ioTDomain);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Annotation> getAnnotationsByUserApplicationAndArchitecture(UserApplication userApplication, Architecture architecture) {
        List<Annotation> list = annotationRepository.findByUserApplicationAndArchitecturesContainingOrderByLastUpdateDesc(userApplication, architecture);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Annotation> getAnnotationsByUserApplicationAndQualityRequirement(UserApplication userApplication, QualityRequirement qualityRequirement) {
        List<Annotation> list = annotationRepository.findByUserApplicationAndQualityRequirementsContainingOrderByLastUpdateDesc(userApplication, qualityRequirement);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Annotation> getAnnotationsByUserApplicationAndTechnology(UserApplication userApplication, Technology technology) {
        List<Annotation> list = annotationRepository.findByUserApplicationAndTechnologiesContainingOrderByLastUpdateDesc(userApplication, technology);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

}
