package br.ufrj.cos.service;

import br.ufrj.cos.components.chart.data.ArchitectureSolutionChartRecord;
import br.ufrj.cos.components.chart.data.IoTDomainChartRecord;
import br.ufrj.cos.components.chart.data.QualityRequirementChartRecord;
import br.ufrj.cos.domain.*; // Assuming all your domain classes are here
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
// No iText imports needed for this specific RAG string generation method

@Service
public class RAGService {

    // Assuming these are your actual domain classes.
    // You'll need getters in these classes (e.g., getName(), getDescription(), getLink(), etc.)
    // For brevity, I'm not redefining them here, but they are implied by the code.

    private final IoTDomainService ioTDomainService; // Use one consistent name
    private final ArchitectureService architectureService;
    private final ArchitectureSolutionService architectureSolutionService;
    private final QualityRequirementService qualityRequirementService;
    private final TechnologyService technologyService; // Added, assuming it's needed for setQRandTech if tech details come from there
    private final PaperReferenceService paperReferenceService; // Added, assuming it's needed
    private static final DecimalFormat df = new DecimalFormat("0.0"); // For formatting percentage

    // Constructor with corrected/clarified service injections
    public RAGService(IoTDomainService ioTDomainService, // Corrected to use one instance
                      ArchitectureService architectureService,
                      ArchitectureSolutionService architectureSolutionService,
                      QualityRequirementService qualityRequirementService,
                      TechnologyService technologyService,
                      PaperReferenceService paperReferenceService) {
        this.ioTDomainService = ioTDomainService;
        this.architectureService = architectureService;
        this.architectureSolutionService = architectureSolutionService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;
        this.paperReferenceService = paperReferenceService;
    }

    public double calculatePercentage(double part, double total) {
        if (total == 0) {
            return 0.0; // Or throw an exception, but for display, 0% might be acceptable
        }
        return (part / total) * 100.0;
    }

    /**
     * Helper method to format a single Quality Requirement and its associated Technology/Feature
     * as a Markdown list item.
     */
    private String formatQRAndTechForMarkdown(String qrName, String techNameOrDescription, String techNotes, String solutionSpecificNotes) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("*   **%s:** Addressed by **%s**.", qrName, techNameOrDescription));
        if (techNotes != null && !techNotes.isBlank()) {
            sb.append(String.format(" (Technology Details: %s)", techNotes));
        }
        if (solutionSpecificNotes != null && !solutionSpecificNotes.isBlank()) {
            sb.append(String.format(" (Implementation Notes for this solution: %s)", solutionSpecificNotes));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String loadDataStatistics() {
        double total = 0.0;
        StringBuilder sb = new StringBuilder();
        String chunkSeparator = "\n###CHUNK###\n"; // If this method's output is part of a larger multi-chunk string

        // --- Overall Statistics Introduction ---
        sb.append("## Knowledge Base Statistics Overview\n\n");
        sb.append("This section provides a statistical overview of the concepts covered within this IoT System Architecture Knowledge Base. Percentages indicate the prevalence of each item relative to the total number of architectural solutions analyzed or relevant items cataloged.\n\n");

        // --- IoT Domain Statistics ---
        sb.append("### IoT Domain Statistics\n\n");
        List<IoTDomainChartRecord> recordDataDomain = this.ioTDomainService.countIoTDomainByArchitectureSolution();
        total = this.architectureSolutionService.findAll().size();
        if (recordDataDomain == null || recordDataDomain.isEmpty()) {
            sb.append("*   No IoT domain statistics available at this time.\n");
        } else {
            sb.append("The following IoT domains are represented in the architectural solutions:\n");
            double finalTotal = total;
            recordDataDomain.forEach(domain -> {
                double percentage = calculatePercentage(domain.qtd(), finalTotal);
                sb.append(String.format("*   **%s:** Represents **%s%%** of the analyzed solutions (Count: %s out of %s).\n",
                        domain.description(),
                        df.format(percentage), // Format to 2 decimal places
                        domain.qtd(),
                        finalTotal));
            });
        }
        sb.append("\n"); // Add a newline for spacing before the next section or separator

        // --- Architecture Statistics ---
        sb.append("### Architectural Pattern Statistics\n\n");
        List<ArchitectureSolutionChartRecord> recordDataArch = this.architectureSolutionService.geArchitectureSolutionCountGroupedByName();
        if (recordDataArch == null || recordDataArch.isEmpty()) {
            sb.append("*   No architectural pattern statistics available at this time.\n");
        } else {
            sb.append("The following architectural patterns are utilized in the solutions:\n");
            recordDataArch.forEach(arch -> {
                double percentage = calculatePercentage(arch.qtd(), arch.total());
                sb.append(String.format("*   **%s:** Utilized in **%s%%** of the analyzed solutions (Count: %d out of %d).\n",
                        arch.description(),
                        df.format(percentage),
                        arch.qtd(),
                        arch.total()));
            });
        }
        sb.append("\n");

        // --- Quality Requirement Statistics ---
        sb.append("### Quality Requirement Statistics\n\n");
        List<QualityRequirementChartRecord> recordDataQR = this.qualityRequirementService.getQualityRequirementCountGroupedByName();
        if (recordDataQR == null || recordDataQR.isEmpty()) {
            sb.append("*   No quality requirement statistics available at this time.\n");
        } else {
            sb.append("The following quality requirements are addressed by the solutions:\n");
            double finalTotal1 = total;
            recordDataQR.forEach(qr -> {
                double percentage = calculatePercentage(qr.qtd(), finalTotal1);
                sb.append(String.format("*   **%s:** Addressed in **%s%%** of the analyzed solutions (Count: %d out of %s).\n",
                        qr.description(),
                        df.format(percentage),
                        qr.qtd(),
                        finalTotal1));
            });
        }
        // No final chunk separator here if this is the entire content of ONE chunk.
        // If this method's output is appended to other content and then split,
        // the calling method should handle the final separator.

        return sb.toString();
    }

    /**
     * Generates a single string containing all knowledge base data, formatted into
     * RAG-friendly Markdown chunks, separated by "###CHUNK###".
     */
    public String generateStringData() {
        StringBuilder allContentBuilder = new StringBuilder();
        String chunkSeparator = "\n###CHUNK###\n"; // Define separator clearly

        // Metrics of Domain, Qr and Architecture
        allContentBuilder.append(chunkSeparator);
        allContentBuilder.append(this.loadDataStatistics());
        allContentBuilder.append(chunkSeparator);

        // --- Chunk 1: Introduction to the Knowledge Base ---
        // This chunk can be static or dynamically generated if you have meta-info
        allContentBuilder.append("## IoT System Architecture Knowledge Base Overview\n\n");
        allContentBuilder.append("This knowledge base contains information on IoT domains, architectural patterns, quality requirements (based on ISO 25010:2023), and specific architectural solutions derived from scientific literature and best practices. Use this information to guide the design of robust and effective IoT systems.\n");
        allContentBuilder.append(chunkSeparator);


        // --- Chunk 2: All IoT Domains ---
        allContentBuilder.append("## IoT Domains Covered in this Knowledge Base\n\n");
        List<IoTDomain> domains = this.ioTDomainService.findAll(); // Assuming findAllOrderByName() if you want sorted
        if (domains == null || domains.isEmpty()) {
            allContentBuilder.append("No IoT domains are currently defined in the knowledge base.\n");
        } else {
            domains.forEach(domain -> {
                allContentBuilder.append(String.format("*   **%s**", domain.getName()));
                if (domain.getDescription() != null && !domain.getDescription().isBlank()) {
                    allContentBuilder.append(String.format(": %s\n", domain.getDescription()));
                } else {
                    allContentBuilder.append("\n");
                }
            });
        }
        allContentBuilder.append(chunkSeparator);

        // --- Chunk 3: All Quality Requirements (ISO 25010:2023 Overview) ---
        allContentBuilder.append("## Quality Requirements Considered (Based on ISO 25010:2023)\n\n");
        List<QualityRequirement> qualityRequirements = this.qualityRequirementService.findAll(); // Assuming findAllOrderedByName()
        if (qualityRequirements == null || qualityRequirements.isEmpty()) {
            allContentBuilder.append("No quality requirements are currently defined in the knowledge base.\n");
        } else {
            qualityRequirements.forEach(qr -> {
                allContentBuilder.append(String.format("*   **%s**", qr.getName()));
                if (qr.getDescription() != null && !qr.getDescription().isBlank()) {
                    allContentBuilder.append(String.format(": %s\n", qr.getDescription()));
                } else {
                    allContentBuilder.append("\n");
                }
            });
        }
        allContentBuilder.append(chunkSeparator);

        // --- Chunk 4: All Architectural Patterns/Names ---
        allContentBuilder.append("## Architectural Patterns/Names Cataloged\n\n");
        List<Architecture> architectures = this.architectureService.findAll(); // Assuming findAllOrderedByName()
        if (architectures == null || architectures.isEmpty()) {
            allContentBuilder.append("No architectural patterns are currently defined in the knowledge base.\n");
        } else {
            architectures.forEach(arch -> {
                allContentBuilder.append(String.format("*   **%s**", arch.getName()));
                if (arch.getName() != null && !arch.getName().isBlank()) {
                    allContentBuilder.append(String.format(": %s\n", arch.getName()));
                } else {
                    allContentBuilder.append("\n");
                }
            });
        }
        allContentBuilder.append(chunkSeparator);


        // --- Subsequent Chunks: Individual Architecture Solutions ---
        List<ArchitectureSolution> solutions = this.architectureSolutionService.findAll(); // Assuming findAllOrderedByName()
        if (solutions != null && !solutions.isEmpty()) {
            solutions.forEach(architectureSolution -> {
                // Null checks for safety, though ideally your data layer ensures these are present
                PaperReference paper = architectureSolution.getPaperReference();
                Architecture archEntity = architectureSolution.getArchitecture();
                IoTDomain domainEntity = architectureSolution.getIoTDomain();

                String paperTitle = (paper != null && paper.getTitle() != null) ? paper.getTitle() : "N/A";
                String paperLink = (paper != null && paper.getLink() != null) ? paper.getLink() : "#";
                String paperDOI = (paper != null && paper.getDoi() != null) ? paper.getDoi() : "N/A";
                String paperRef = (paper != null && paper.getReference() != null) ? paper.getReference() : "N/A";
                String paperYear = (paper != null && paper.getPublishYear() != null) ? paper.getPublishYear().toString() : "N/A";

                String archName = (archEntity != null && archEntity.getName() != null) ? archEntity.getName() : "N/A";
                String solutionDesc = (architectureSolution.getDescription() != null) ? architectureSolution.getDescription() : "No specific solution description provided."; // Solution's own description

                String iotDomainName = (domainEntity != null && domainEntity.getName() != null) ? domainEntity.getName() : "N/A";

                StringBuilder qrBlockBuilder = new StringBuilder();
                List<QualityRequirementTechnology> qrTechs = architectureSolution.getQualityRequirementTechnologies();
                if (qrTechs == null || qrTechs.isEmpty()) {
                    qrBlockBuilder.append("No specific quality requirement technologies detailed for this solution.\n");
                } else {
                    qrTechs.forEach(qrTech -> {
                        QualityRequirement qr = qrTech.getQualityRequirement();
                        Technology tech = qrTech.getTechnology();
                        if (qr != null && tech != null) {
                            qrBlockBuilder.append(
                                    this.formatQRAndTechForMarkdown(
                                            qr.getName() != null ? qr.getName() : "N/A",
                                            tech.getDescription() != null ? tech.getDescription() : tech.getDescription(), // Prefer description, fallback to name
                                            tech.getNotes(),
                                            qrTech.getNotes() // These are notes specific to how this solution uses the tech for the QR
                                    )
                            );
                        }
                    });
                }
                allContentBuilder.append(chunkSeparator);

                List<Technology> techs = this.technologyService.findAllOrderedByDescription();
                StringBuilder techsBlockBuilder = new StringBuilder();
                techs.forEach(tech -> {
                    techsBlockBuilder.append(String.format("Technology/Feature: %s - Definition/Notes: %s\n\n", tech.getDescription(), tech.getNotes() != null ? tech.getNotes() : "N/A"));
                });


                String solutionChunk = String.format("""
                        ## Architectural Solution: %s for %s

                        ### Source Publication
                        *   **Title:** %s
                        *   **Year:** %s
                        *   **Link:** [%s](%s)
                        *   **DOI:** [https://doi.org/%s](https://doi.org/%s)
                        *   **Full Reference:** %s

                        ### Proposed Architectural Pattern
                        *   **Name:** %s

                        ### Solution Description
                        %s

                        ### Target IoT Domain
                        *   %s

                        ### Quality Requirements Addressed & Technologies/Features
                        %s
                        
                        ### All technologies and features concepts
                        %s
                        """,
                        archName, iotDomainName,
                        paperTitle, paperYear,
                        paperLink.equals("#") ? paperTitle : paperLink, paperLink, // Use title if link is placeholder
                        paperDOI.equals("N/A") ? paperDOI : paperDOI, paperDOI.equals("N/A") ? "#" : "https://doi.org/" + paperDOI, // Handle N/A DOI for link
                        paperRef,
                        archName,
                        solutionDesc.replace("\n", "\n                        "), // Indent multiline description
                        iotDomainName,
                        qrBlockBuilder.toString().isBlank() ? "Details not specified." : qrBlockBuilder.toString(),
                        techsBlockBuilder.toString()
                );
                allContentBuilder.append(solutionChunk);
                allContentBuilder.append(chunkSeparator);
            });
        }


        // Remove the last separator if it was added
        String finalText = allContentBuilder.toString();
        if (finalText.endsWith(chunkSeparator)) {
            finalText = finalText.substring(0, finalText.length() - chunkSeparator.length());
        }
        return finalText;
    }

    // The generateStringData(ArchitectureSolution architectureSolution) method
    // can be similarly refactored to use the Markdown format and the
    // formatQRAndTechForMarkdown helper if it's intended for RAG.
    // The current one has `\\n` which might not render as expected in all Markdown parsers
    // and its formatting is different from the main one.
    // For consistency, I'd recommend refactoring it too if it feeds into the RAG.

    // The setQRandTech method is now replaced by formatQRAndTechForMarkdown
    // and its old implementation is no longer needed if you adopt the Markdown list style.
}