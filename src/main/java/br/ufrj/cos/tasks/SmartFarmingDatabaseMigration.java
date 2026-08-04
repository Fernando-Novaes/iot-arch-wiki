package br.ufrj.cos.tasks;

import br.ufrj.cos.domain.ArchitectureSolution;
import br.ufrj.cos.domain.IoTDomain;
import br.ufrj.cos.domain.PaperReference;
import br.ufrj.cos.domain.Technology;
import br.ufrj.cos.repository.ArchitectureSolutionRepository;
import br.ufrj.cos.repository.IoTDomainRepository;
import br.ufrj.cos.repository.PaperReferenceRepository;
import br.ufrj.cos.repository.TechnologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Migration runner that updates all instances of 'Smart Farm' to 'Smart Farming'
 * across all database tables (IoTDomain, ArchitectureSolution, Technology, PaperReference)
 * on application startup.
 */
@Component
public class SmartFarmingDatabaseMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SmartFarmingDatabaseMigration.class);

    private final IoTDomainRepository ioTDomainRepository;
    private final ArchitectureSolutionRepository architectureSolutionRepository;
    private final TechnologyRepository technologyRepository;
    private final PaperReferenceRepository paperReferenceRepository;

    public SmartFarmingDatabaseMigration(
            IoTDomainRepository ioTDomainRepository,
            ArchitectureSolutionRepository architectureSolutionRepository,
            TechnologyRepository technologyRepository,
            PaperReferenceRepository paperReferenceRepository) {
        this.ioTDomainRepository = ioTDomainRepository;
        this.architectureSolutionRepository = architectureSolutionRepository;
        this.technologyRepository = technologyRepository;
        this.paperReferenceRepository = paperReferenceRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int totalUpdates = 0;

        // 1. Update IoTDomains
        List<IoTDomain> domains = ioTDomainRepository.findAll();
        for (IoTDomain domain : domains) {
            boolean changed = false;
            if (domain.getName() != null && domain.getName().contains("Smart Farm")) {
                String oldName = domain.getName();
                domain.setName(oldName.replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (domain.getDescription() != null && domain.getDescription().contains("Smart Farm")) {
                domain.setDescription(domain.getDescription().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (changed) {
                ioTDomainRepository.save(domain);
                totalUpdates++;
                logger.info("Updated IoTDomain ID {} to 'Smart Farming'", domain.getId());
            }
        }

        // 2. Update ArchitectureSolutions
        List<ArchitectureSolution> solutions = architectureSolutionRepository.findAll();
        for (ArchitectureSolution solution : solutions) {
            boolean changed = false;
            if (solution.getDescription() != null && solution.getDescription().contains("Smart Farm")) {
                solution.setDescription(solution.getDescription().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (changed) {
                architectureSolutionRepository.save(solution);
                totalUpdates++;
                logger.info("Updated ArchitectureSolution ID {} description with 'Smart Farming'", solution.getId());
            }
        }

        // 3. Update Technologies
        List<Technology> technologies = technologyRepository.findAll();
        for (Technology tech : technologies) {
            boolean changed = false;
            if (tech.getDescription() != null && tech.getDescription().contains("Smart Farm")) {
                tech.setDescription(tech.getDescription().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (tech.getNotes() != null && tech.getNotes().contains("Smart Farm")) {
                tech.setNotes(tech.getNotes().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (changed) {
                technologyRepository.save(tech);
                totalUpdates++;
                logger.info("Updated Technology ID {} with 'Smart Farming'", tech.getId());
            }
        }

        // 4. Update PaperReferences
        List<PaperReference> papers = paperReferenceRepository.findAll();
        for (PaperReference paper : papers) {
            boolean changed = false;
            if (paper.getTitle() != null && paper.getTitle().contains("Smart Farm")) {
                paper.setTitle(paper.getTitle().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (paper.getReference() != null && paper.getReference().contains("Smart Farm")) {
                paper.setReference(paper.getReference().replace("Smart Farm", "Smart Farming").replace("Smart Farminging", "Smart Farming"));
                changed = true;
            }
            if (changed) {
                paperReferenceRepository.save(paper);
                totalUpdates++;
                logger.info("Updated PaperReference ID {} with 'Smart Farming'", paper.getId());
            }
        }

        if (totalUpdates > 0) {
            logger.info("Database Migration Completed: Successfully updated {} records from 'Smart Farm' to 'Smart Farming'.", totalUpdates);
        } else {
            logger.info("Database Migration Check: All records are already up-to-date with 'Smart Farming'.");
        }
    }
}
