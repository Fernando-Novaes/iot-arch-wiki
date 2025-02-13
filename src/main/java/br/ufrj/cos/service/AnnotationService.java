package br.ufrj.cos.service;

import br.ufrj.cos.domain.*;
import br.ufrj.cos.repository.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnnotationService {

    private final AnnotationRepository annotationRepository;

    @Autowired
    public AnnotationService(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    //This method finds the most recent annotation associated with a given UserApplication and IoTDomain,
    // where the associated AnnotationDomain entry explicitly has NULL values for Architecture, QualityRequirement,
    // and Technology.
    public Optional<Annotation> findMostRecentOnlyUserAppAndDomain(UserApplication userApplication, IoTDomain iotDomain) {
        return annotationRepository.findMostRecentByUserApplicationAndIoTDomainWhereOnlyTheseArePresent(userApplication, iotDomain);
    }

    public Optional<Annotation> findMostRecentAnnotation(UserApplication userApplication, IoTDomain iotDomain, Architecture architecture) {
        // Existing logic to find most recent annotation based on UserApplication, IoTDomain, and Architecture
        // Will return NULL if no results or an Optional containing null value.
        return annotationRepository.findMostRecentByUserApplicationAndIoTDomainAndArchitecture(userApplication, iotDomain, architecture);
    }

    public Optional<Annotation> findMostRecentAnnotation(UserApplication userApplication, IoTDomain iotDomain, Architecture architecture, QualityRequirement qualityRequirement) {
        // Existing logic to find most recent annotation based on UserApplication, IoTDomain, Architecture and QualityRequirement
        // Will return NULL if no results or an Optional containing null value.
        return annotationRepository.findMostRecentByUserApplicationAndIoTDomainAndArchitectureAndQualityRequirement(userApplication, iotDomain, architecture, qualityRequirement);
    }

    public Optional<Annotation> findMostRecentAnnotation(UserApplication userApplication, IoTDomain iotDomain, Architecture architecture, QualityRequirement qualityRequirement, Technology technology) {
        // Existing logic to find most recent annotation based on UserApplication, IoTDomain, Architecture, QualityRequirement, and Technology
        // Will return NULL if no results or an Optional containing null value.
        return annotationRepository.findMostRecentByUserApplicationAndIoTDomainAndArchitectureAndQualityRequirementAndTechnology(userApplication, iotDomain, architecture, qualityRequirement, technology);
    }

    public Annotation saveAnnotation(Annotation annotation) {
        return annotationRepository.save(annotation);
    }

    public void deleteAnnotation(Long id) {
        annotationRepository.deleteById(id);
    }

    public Annotation getAnnotationById(Long id) {
        return annotationRepository.findById(id).orElse(null); // Handle the case where the annotation is not found
    }
}