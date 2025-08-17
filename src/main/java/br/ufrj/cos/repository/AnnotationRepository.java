package br.ufrj.cos.repository;

import br.ufrj.cos.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    // Finds most recent annotation ONLY based on UserApplication and IoTDomain.
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "ORDER BY a.lastUpdate DESC")
    Optional<List<Annotation>> findMostRecentByUserApplication(
            @Param("userApplication") UserApplication userApplication
    );

    // Finds most recent annotation ONLY based on UserApplication and IoTDomain.
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "AND ad.ioTDomain = :iotDomain " +
            "AND ad.architecture IS NULL " +
            "AND ad.qualityRequirement IS NULL " +
            "AND ad.technology IS NULL " +
            "ORDER BY a.lastUpdate DESC")
    Optional<Annotation> findMostRecentByUserApplicationAndIoTDomainWhereOnlyTheseArePresent(
            @Param("userApplication") UserApplication userApplication,
            @Param("iotDomain") IoTDomain iotDomain
    );


    // Finds most recent annotation based on UserApplication and IoTDomain
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "AND ad.ioTDomain = :iotDomain " +
            "ORDER BY a.lastUpdate DESC")
    Optional<Annotation> findMostRecentByUserApplicationAndIoTDomain(
            @Param("userApplication") UserApplication userApplication,
            @Param("iotDomain") IoTDomain iotDomain
    );

    // Finds most recent annotation by UserApplication, IoTDomain, and Architecture
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "AND ad.ioTDomain = :iotDomain " +
            "AND ad.architecture = :architecture " +
            "ORDER BY a.lastUpdate DESC")
    Optional<Annotation> findMostRecentByUserApplicationAndIoTDomainAndArchitecture(
            @Param("userApplication") UserApplication userApplication,
            @Param("iotDomain") IoTDomain iotDomain,
            @Param("architecture") Architecture architecture
    );

    // Finds most recent annotation by UserApplication, IoTDomain, Architecture, and QualityRequirement
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "AND ad.ioTDomain = :iotDomain " +
            "AND ad.architecture = :architecture " +
            "AND ad.qualityRequirement = :qualityRequirement " +
            "ORDER BY a.lastUpdate DESC")
    Optional<Annotation> findMostRecentByUserApplicationAndIoTDomainAndArchitectureAndQualityRequirement(
            @Param("userApplication") UserApplication userApplication,
            @Param("iotDomain") IoTDomain iotDomain,
            @Param("architecture") Architecture architecture,
            @Param("qualityRequirement") QualityRequirement qualityRequirement
    );

    // Finds most recent annotation by UserApplication, IoTDomain, Architecture, QualityRequirement, and Technology
    @Query("SELECT a FROM Annotation a " +
            "JOIN a.annotationDomains ad " +
            "WHERE a.userApplication = :userApplication " +
            "AND ad.ioTDomain = :iotDomain " +
            "AND ad.architecture = :architecture " +
            "AND ad.qualityRequirement = :qualityRequirement " +
            "AND ad.technology = :technology " +
            "ORDER BY a.lastUpdate DESC")
    Optional<Annotation> findMostRecentByUserApplicationAndIoTDomainAndArchitectureAndQualityRequirementAndTechnology(
            @Param("userApplication") UserApplication userApplication,
            @Param("iotDomain") IoTDomain iotDomain,
            @Param("architecture") Architecture architecture,
            @Param("qualityRequirement") QualityRequirement qualityRequirement,
            @Param("technology") Technology technology
    );
}