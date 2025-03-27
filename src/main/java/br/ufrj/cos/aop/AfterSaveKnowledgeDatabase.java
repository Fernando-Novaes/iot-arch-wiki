package br.ufrj.cos.aop;

import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.repository.AppConfigRepository;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
public class AfterSaveKnowledgeDatabase {
    /***
     * This AOP class executes after save in knowledge database
     */
    private static final Logger logger = LoggerFactory.getLogger(AfterSaveKnowledgeDatabase.class);

    @Autowired
    private AppConfigRepository appConfigRepository;

    @After("execution(* *.save*(..)) && " +
            "target(br.ufrj.cos.repository.ArchitectureRepository) || " +
            "target(br.ufrj.cos.repository.ArchitectureSolutionRepository) || " +
            "target(br.ufrj.cos.repository.IoTDomainRepository) || " +
            "target(br.ufrj.cos.repository.PaperReferenceRepository) || " +
            "target(br.ufrj.cos.repository.QualityRequirementRepository) || " +
            "target(br.ufrj.cos.repository.QualityRequirementTechnologyRepository) ||" +
            "target(br.ufrj.cos.repository.TechnologyRepository)"
            )
    public void updateKnowledgeDatabaseModificationDate() {
        AppConfig appConfig = appConfigRepository.findAll().getFirst();

        if (appConfig != null) {
            appConfig.setKnowledgeDatabaseLastUpdate(Instant.now());
            appConfigRepository.save(appConfig);
        }

        logger.info("Update knowledge database modification date");
    }

}
