package br.ufrj.cos.service;

import br.ufrj.cos.domain.ArchitectureSolution;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RAGService {

    private ArchitectureSolutionService architectureSolutionService;
    private ArchitectureService architectureService;
    private QualityRequirementService qualityRequirementService;
    private TechnologyService technologyService;

    public RAGService(ArchitectureSolutionService architectureSolutionService, ArchitectureService architectureService, QualityRequirementService qualityRequirementService, TechnologyService technologyService) throws IOException {
        this.architectureSolutionService = architectureSolutionService;
        this.architectureService = architectureService;
        this.qualityRequirementService = qualityRequirementService;
        this.technologyService = technologyService;
        this.generateData();
    }

    public void generateData() throws IOException {
        String dest = "iot_design_assistant.pdf";
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Define fonts
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
        PdfFont italicFont = PdfFontFactory.createFont("Helvetica-Oblique"); // Added italic font


        // Content
        Paragraph title1 = new Paragraph("WHAT IS THE IoT DESIGN DECISION ASSISTANT?")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title1);

        Paragraph content1 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        content1.add("Comprehensive Knowledge Base: Access a base of knowledge of IoT domains, architectural solutions, quality requirements, and technologies, all curated from peer-reviewed research and industry best practices.\n");
        content1.add("Intelligent Decision Support: Leverage our advanced technology to match your project requirements with optimal architectural solutions and technologies.\n");
        content1.add("Quality-Driven Approach: Ensure your designs meet the highest standards by aligning them with established quality attributes and requirements specific to IoT systems.\n");
        content1.add("Stay Current: Benefit from regularly updated content from the literature, reflecting the latest advancements in IoT technology and design methodologies.\n");
        document.add(content1);

        Paragraph keyFeaturesTitle = new Paragraph("Key Features:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(keyFeaturesTitle);

        List keyFeaturesList = new List()
                .setFont(normalFont)
                .setFontSize(12)
                .setListSymbol("\u2022"); // Bullet point

        keyFeaturesList.add(new ListItem("Interactive Design Explorer: Visually navigate through IoT domains, solutions, and technologies."));
        keyFeaturesList.add(new ListItem("Requirements Analyzer: Define and prioritize your project's quality requirements with ease."));
        keyFeaturesList.add(new ListItem("Solution Recommender: Receive tailored architectural recommendations based on your specific needs."));
        keyFeaturesList.add(new ListItem("Technology Evaluator: Compare and assess various IoT technologies to find the perfect fit for your project."));
        keyFeaturesList.add(new ListItem("Knowledge Contribution: Submit your own experiences and solutions to enrich the community's collective wisdom."));
        document.add(keyFeaturesList);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page


        Paragraph title2 = new Paragraph("IoT Architecture Solution Knowledge Base")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title2);

        Paragraph content2 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        content2.add("Comprehensive Knowledge Base: Access a base of knowledge of IoT domains, architectural solutions, quality requirements, and technologies, all curated from peer-reviewed research and industry best practices.\n");
        content2.add("Intelligent Decision Support: Leverage our advanced technology to match your project requirements with optimal architectural solutions and technologies.\n");
        content2.add("Quality-Driven Approach: Ensure your designs meet the highest standards by aligning them with established quality attributes and requirements specific to IoT systems.\n");
        content2.add("Stay Current: Benefit from regularly updated content from the literature, reflecting the latest advancements in IoT technology and design methodologies.\n");
        document.add(content2);

        Paragraph keyFeaturesTitle2 = new Paragraph("Key Features:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(keyFeaturesTitle2);

        List keyFeaturesList2 = new List()
                .setFont(normalFont)
                .setFontSize(12)
                .setListSymbol("\u2022"); // Bullet point

        keyFeaturesList2.add(new ListItem("Interactive Design Explorer: Visually navigate through IoT domains, solutions, and technologies."));
        keyFeaturesList2.add(new ListItem("Requirements Analyzer: Define and prioritize your project's quality requirements with ease."));
        keyFeaturesList2.add(new ListItem("Solution Recommender: Receive tailored architectural recommendations based on your specific needs."));
        keyFeaturesList2.add(new ListItem("Technology Evaluator: Compare and assess various IoT technologies to find the perfect fit for your project."));
        keyFeaturesList2.add(new ListItem("Knowledge Contribution: Submit your own experiences and solutions to enrich the community's collective wisdom."));
        document.add(keyFeaturesList2);


        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page

        Paragraph title3 = new Paragraph("WHAT IS QUALITY REQUIREMENT OR NOM-FUNCTIONAL REQURIMENTS?")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title3);

        Paragraph content3 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        content3.add("Based on ISO 25010:2023, quality requirements are defined by eight main quality characteristics for software products and systems. \n\n");
        content3.add("Here's the complete list with definitions for all quality characteristics and sub-characteristics from ISO 25010:2023:\n\n");
        document.add(content3);


        Paragraph qualityCharacteristicsTitle = new Paragraph("1. Functional Suitability:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle);

        Paragraph qualityCharacteristicsContent1 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent1.add("capability of a product to provide functions that meet stated and implied needs of intended users when it is used under specified conditions. Functional suitability is concerned with whether the functions meet not only stated and implied needs, but also the functional specification.\n");
        qualityCharacteristicsContent1.add("- Functional completeness: Degree to which functions cover all specified tasks/objectives\n");
        qualityCharacteristicsContent1.add("- Functional correctness: Degree to which functions provide correct results with needed precision\n");
        qualityCharacteristicsContent1.add("- Functional appropriateness: Degree to which functions facilitate task accomplishment\n\n");
        document.add(qualityCharacteristicsContent1);

        Paragraph qualityCharacteristicsTitle2 = new Paragraph("2. Performance Efficiency:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle2);

        Paragraph qualityCharacteristicsContent2 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent2.add("capability of a product to perform its functions within specified time and throughput parameters and be efficient in the use of resources under specified conditions. Resources can be CPU, memory, storage, and network devices. It also can include other software products, the software and hardware configuration of the system, energy, and materials (e.g. print paper, storage media).\n");
        qualityCharacteristicsContent2.add("- Time behavior: Response and processing times and throughput rates\n");
        qualityCharacteristicsContent2.add("- Resource utilization: Resources used (materials, CPU, memory, I/O devices)\n");
        qualityCharacteristicsContent2.add("- Capacity: Maximum limits (data storage, users, bandwidth) that meet requirements\n\n");
        document.add(qualityCharacteristicsContent2);

        Paragraph qualityCharacteristicsTitle3 = new Paragraph("3. Compatibility:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle3);

        Paragraph qualityCharacteristicsContent3 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent3.add("capability of a product to exchange information with other products, and/or to perform its required functions while sharing the same common environment and resources.\n");
        qualityCharacteristicsContent3.add("- Co-existence: Efficient performance while sharing environment with other products\n");
        qualityCharacteristicsContent3.add("- Interoperability: Exchange information and use information that has been exchanged\n\n");
        document.add(qualityCharacteristicsContent3);

        Paragraph qualityCharacteristicsTitle4 = new Paragraph("4. Interaction capability:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle4);

        Paragraph qualityCharacteristicsContent4 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent4.add("capability of a product to be interacted with by specified users to exchange information between a user and a system via the user interface to complete the intended task.\n");
        qualityCharacteristicsContent4.add("- Appropriateness recognizability: Users recognize if product meets their needs\n");
        qualityCharacteristicsContent4.add("- Learnability: Product can be used for specified learning goals\n");
        qualityCharacteristicsContent4.add("- Operability: Ease of operation and control\n");
        qualityCharacteristicsContent4.add("- User error protection: System protects against user errors\n");
        qualityCharacteristicsContent4.add("- User interface aesthetics: Pleasant and satisfying interaction\n");
        qualityCharacteristicsContent4.add("- Accessibility: Usable by people with widest range of characteristics\n");
        qualityCharacteristicsContent4.add("- Technical accessibility: Meets technical accessibility standards/guidelines\n\n");
        document.add(qualityCharacteristicsContent4);

        Paragraph qualityCharacteristicsTitle5 = new Paragraph("5. Reliability:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle5);

        Paragraph qualityCharacteristicsContent5 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent5.add("capability of a product to perform specified functions under specified conditions for a specified period of time without interruptions and failures.\n");
        qualityCharacteristicsContent5.add("- Maturity: Meets reliability needs under normal operation\n");
        qualityCharacteristicsContent5.add("- Availability: Operational and accessible when required\n");
        qualityCharacteristicsContent5.add("- Fault tolerance: Operates despite hardware/software faults\n");
        qualityCharacteristicsContent5.add("- Recoverability: Recover data and restore state after interruption\n\n");
        document.add(qualityCharacteristicsContent5);

        Paragraph qualityCharacteristicsTitle6 = new Paragraph("6. Security:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle6);

        Paragraph qualityCharacteristicsContent6 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent6.add("capability of a product to protect information and data so that persons or other products have the degree of data access appropriate to their types and levels of authorization, and to defend against attack patterns by malicious actors.\n");
        qualityCharacteristicsContent6.add("- Confidentiality: Data accessible only to authorized access\n");
        qualityCharacteristicsContent6.add("- Integrity: Prevents unauthorized data modification\n");
        qualityCharacteristicsContent6.add("- Non-repudiation: Actions can be proven to have occurred\n");
        qualityCharacteristicsContent6.add("- Accountability: Actions can be traced to specific entity\n");
        qualityCharacteristicsContent6.add("- Authenticity: Identity of subject/resource can be proven\n");
        qualityCharacteristicsContent6.add("- Auditability: Records can be analyzed to investigate events\n");
        qualityCharacteristicsContent6.add("- Prevention: Prevents unauthorized access, attacks, data breaches\n");
        qualityCharacteristicsContent6.add("- Privacy: Personal/private information protected per regulations\n\n");
        document.add(qualityCharacteristicsContent6);

        Paragraph qualityCharacteristicsTitle7 = new Paragraph("7. Maintainability:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle7);

        Paragraph qualityCharacteristicsContent7 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent7.add("capability of a product to be modified by the intended maintainers with effectiveness and efficiency.\n");
        qualityCharacteristicsContent7.add("- Modularity: Component changes have minimal impact on others\n");
        qualityCharacteristicsContent7.add("- Reusability: Assets can be used in multiple systems\n");
        qualityCharacteristicsContent7.add("- Analysability: Impact assessment effectiveness for modification\n");
        qualityCharacteristicsContent7.add("- Modifiability: Can be modified without quality degradation\n");
        qualityCharacteristicsContent7.add("- Testability: Test criteria can be established and tests performed\n\n");
        document.add(qualityCharacteristicsContent7);

        Paragraph qualityCharacteristicsTitle8 = new Paragraph("8. Flexibility:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle8);

        Paragraph qualityCharacteristicsContent8 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent8.add("capability of a product to be adapted to changes in its requirements, contexts of use, or system environment.\n");
        qualityCharacteristicsContent8.add("- Adaptability: Can adapt to different environments\n");
        qualityCharacteristicsContent8.add("- Installability: Can be installed/uninstalled effectively\n");
        qualityCharacteristicsContent8.add("- Replaceability: Can replace other specified software\n");
        qualityCharacteristicsContent8.add("- Scalability: capability of a product to handle growing or shrinking workloads or to adapt its capacity to handle variability\n\n");
        document.add(qualityCharacteristicsContent8);

        Paragraph qualityCharacteristicsTitle9 = new Paragraph("9. Safety:")
                .setFont(boldFont)
                .setFontSize(14);
        document.add(qualityCharacteristicsTitle9);

        Paragraph qualityCharacteristicsContent9 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        qualityCharacteristicsContent9.add("capability of a product under defined conditions to avoid a state in which human life, health, property, or the environment is endangered.\n");
        qualityCharacteristicsContent9.add("- Operational Constraint: capability of a product to constrain its operation to within safe parameters or states when encountering operational hazard\n");
        qualityCharacteristicsContent9.add("- Risk Identification: capability of a product to identify a course of events or operations that can expose life, property or environment to unacceptable risk\n");
        qualityCharacteristicsContent9.add("- Fail Safe: capability of a product to automatically place itself in a safe operating mode, or to revert to a safe condition in the event of a failure\n");
        qualityCharacteristicsContent9.add("- Hazard Warning: capability of a product to provide warnings of unacceptable risks to operations or internal controls so that they can react in sufficient time to sustain safe operations\n");
        qualityCharacteristicsContent9.add("- Safe Integration: capability of a product to maintain safety (3.9) during and after integration with one or more components\n\n");
        document.add(qualityCharacteristicsContent9);

        Paragraph content4 = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);
        content4.add("Each characteristic should be specified with measurable criteria and acceptable thresholds when used as quality requirements.\n\n");
        content4.add("For more details, visit - ");
        content4.add(new Text("https://www.iso.org/obp/ui/en/#iso:std:iso-iec:25010:ed-2:v1:en").setFont(italicFont).setFontSize(12));
        content4.add("\n\n");
        document.add(content4);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page


        Paragraph title4 = new Paragraph("SOLUTIONS FOUND BY THIS RESEARCH")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title4);

        Paragraph separator = new Paragraph("----------------------------------------------------------------------------------------------------------------------------------------------")
                .setFont(normalFont)
                .setFontSize(10);

        document.add(separator);

        Paragraph solutionTemplate = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);

        solutionTemplate.add("The ${SOLUTION} solution is composed by the architecture named as ${ARCHITECTURE}, in the paper ${PAPER}, {PUBLISH_YEAR}, with DOI ${DOI}, found in ${PAPER_LINK}. This solution addresses the following Quality Requirements: ${QR_LIST}. @LOOP[The quality requirement ${QR} is satisfied using the technology/feature ${TECH}, by: ${QR_TECH_DESCRIPTION}.]@END_LOOP The IoT domain of this solution is ${IoTDOMAIN}. You can find all information about the ${SOLUTION} in Knowledge base, filtering by ${IoTDOMAIN} and ${ARCHITECTURE}.\n");
        solutionTemplate.add("${SOLUTION}: ${DESCRIPTION}\n\n");
        solutionTemplate.add("${ARCHITECTURE}: ${DESCRIPTION}\n\n");
        solutionTemplate.add("@LOOP[\n${QR}\n]\n\n");
        solutionTemplate.add("@LOOP[\n${TECH}\n]\n\n");
        solutionTemplate.add("Reference: ${PAPER_TITLE},${PUBLISH_YEAR}\n");

        document.add(solutionTemplate);

        document.add(separator);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page


        Paragraph title5 = new Paragraph("ALL SOLUTIONS")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title5);

        Paragraph solutionsTemplate = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);

        //Total of Architecture Solution found
        solutionsTemplate.add(String.format("Total of %s solutions found.\n", this.architectureSolutionService.findAll().size()));

        //Architecture solution description
        this.architectureSolutionService.findAllOrderedByName().forEach(solution -> {
            solutionsTemplate.add(String.format("\n    %s: %s\n", solution.getArchitecture().getName(), solution.getDescription()));
            document.add(solutionsTemplate);
            document.add(separator);
        });

        document.add(separator);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page


        Paragraph title6 = new Paragraph("ALL ARCHITECTURES")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title6);

        Paragraph architecturesTemplate = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);

        //Total of Architecture found
        solutionsTemplate.add(String.format("Total of %s architectures found.\n", this.architectureSolutionService.findAll().size()));

        //Architecture description
        this.architectureSolutionService.findAllOrderedByName().forEach(arch -> {
            architecturesTemplate.add(String.format("\n    %s: %s\n]\n", arch.getArchitecture().getName(), arch.getDescription()));
            document.add(architecturesTemplate);
            document.add(separator);
        });

        document.add(separator);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page


        Paragraph title7 = new Paragraph("ALL QUALITY REQUIREMENTS")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title7);

        Paragraph qualityRequirementsTemplate = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);

        qualityRequirementsTemplate.add(String.format("Total of %s quality requirements.\n", this.qualityRequirementService.findAllQualityRequirementGroupedByName().size()));
        this.qualityRequirementService.findAllOrderedByName().forEach(qr -> {
            qualityRequirementsTemplate.add(String.format("\n    %s: %s\n]\n", qr.getName(), qr.getDescription()));
            document.add(qualityRequirementsTemplate);
            document.add(separator);
        });

        document.add(separator);

        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // New page

        Paragraph title8 = new Paragraph("ALL TECHNOLOGIES/FEATURES")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title8);

        Paragraph technologiesTemplate = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12);

        technologiesTemplate.add(String.format("Total of %s\n", this.technologyService.findAll().size()));
        this.technologyService.findAllOrderedByDescription().forEach(tech -> {
            technologiesTemplate.add(String.format("\n    %s: %s\n]\n", tech.getDescription(), tech.getNotes()));
            document.add(technologiesTemplate);
            document.add(separator);
        });

        document.add(separator);

        document.close();
        System.out.println("PDF generated successfully!");
    }
}