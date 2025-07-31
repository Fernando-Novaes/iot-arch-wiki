package br.ufrj.cos.views.iotarch;

import br.ufrj.cos.components.treeview.TreeViewComponent;
import br.ufrj.cos.domain.*;
import br.ufrj.cos.views.record.ArchitectureSolutionRecord;
import br.ufrj.cos.views.record.IoTDomainRecord;
import br.ufrj.cos.views.record.QualityRequirementRecord;
import br.ufrj.cos.views.record.TechnologyRecord;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterDataTreeView {

    // --- UI Components ---
    private TreeViewComponent treeViewComponent;
    private MultiSelectComboBox<IoTDomainRecord> iotDomainCombo;
    private MultiSelectComboBox<ArchitectureSolutionRecord> architectureCombo;
    private MultiSelectComboBox<QualityRequirementRecord> qualityCombo;
    private MultiSelectComboBox<TechnologyRecord> technologiesCombo;

    // --- Master Data Sources (The Single Source of Truth) ---
    // This holds the original, complete, and unmodified data.
    private List<IoTDomain> fullDomainHierarchy;

    // This is a flattened list for efficient filtering.
    private List<ArchitectureSolution> fullArchitectureSolutions;

    FilterDataTreeView(TreeViewComponent treeViewComponent,
                       MultiSelectComboBox<IoTDomainRecord> iotDomainCombo,
                       MultiSelectComboBox<ArchitectureSolutionRecord> architectureCombo,
                       MultiSelectComboBox<QualityRequirementRecord> qualityCombo,
                       MultiSelectComboBox<TechnologyRecord> technologiesCombo,
                       List<ArchitectureSolution> fullArchitectureSolutions,
                       List<IoTDomain> fullDomainHierarchy) {
        this.treeViewComponent = treeViewComponent;
        this.iotDomainCombo = iotDomainCombo;
        this.architectureCombo = architectureCombo;
        this.qualityCombo = qualityCombo;
        this.technologiesCombo = technologiesCombo;
        this.fullArchitectureSolutions = fullArchitectureSolutions;
        this.fullDomainHierarchy = fullDomainHierarchy;
    }

    /**
     * A powerful, reusable helper method that filters the master list of solutions
     * based on the current selections in all ComboBoxes.
     *
     * @return A new list containing only the ArchitectureSolution objects that match all active filters.
     */
//    private List<ArchitectureSolution> getFilteredSolutions() {
//        // Start with the complete, flattened list of all solutions.
//        Stream<ArchitectureSolution> solutionStream = this.fullArchitectureSolutions.stream();
//
//        // Filter by IoT Domain if selected
//        if (iotDomainCombo.getValue() != null) {
//            // Obtenha o conjunto de domínios selecionados
//            Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
//
//            // Aplique o filtro SOMENTE se houver domínios selecionados.
//            // Se nenhum domínio for selecionado, o filtro não é aplicado e todas as soluções passam.
//            if (selectedDomains != null && !selectedDomains.isEmpty()) {
//                solutionStream = solutionStream.filter(solution ->
//                        // Garante que a solução tenha um domínio antes de verificar
//                        solution.getIoTDomain() != null &&
//                                // Verifica se o domínio da solução está no conjunto de domínios selecionados
//                                selectedDomains.contains(solution.getIoTDomain())
//                );
//            }
//
//            // Se o conjunto 'selectedDomains' for nulo ou vazio, o 'solutionStream' original
//            // é mantido, o que significa que o filtro de domínio é efetivamente ignorado.
//        }
//
//        // Filter by Architecture if selected
//        Set<ArchitectureSolutionRecord> selectedArchitectures = architectureCombo.getValue();
//        if (selectedArchitectures != null && !selectedArchitectures.isEmpty()) {
//            Set<String> selectedArchNames = selectedArchitectures.stream()
//                    .map(ArchitectureSolutionRecord::name)
//                    .collect(Collectors.toSet());
//
//            solutionStream = solutionStream.filter(solution ->
//                    solution.getArchitecture() != null &&
//                            selectedArchNames.contains(solution.getArchitecture().getName())
//            );
//        }
//
//        // Filter by Quality Requirement if selected
//        Set<QualityRequirementRecord> selectedQRs = qualityCombo.getValue();
//        if (selectedQRs != null && !selectedQRs.isEmpty()) {
//            solutionStream = solutionStream.filter(solution ->
//                    solution.getQualityRequirements() != null &&
//                            solution.getQualityRequirements().stream().anyMatch(selectedQRs::contains)
//            );
//        }
//
//        // Filter by Technology if selected
//        Set<TechnologyRecord> selectedTechs = technologiesCombo.getValue();
//        if (selectedTechs != null && !selectedTechs.isEmpty()) {
//            Set<String> selectedTechDescriptions = selectedTechs.stream()
//                    .map(TechnologyRecord::description)
//                    .collect(Collectors.toSet());
//
//            solutionStream = solutionStream.filter(solution ->
//                    solution.getTechnologies() != null &&
//                            solution.getTechnologies().stream()
//                                    .anyMatch(tech -> selectedTechDescriptions.contains(tech.getDescription()))
//            );
//        }
//
//
//        return solutionStream.collect(Collectors.toList());
//    }

    /**
     * This is the main entry point for filtering, replacing the old method.
     * It intelligently dispatches to the correct filtering logic based on the
     * type of data currently being displayed in the TreeView.
     */
    public void filterTreeViewDataSource(List<IoTDomain> data) {
        // Get the currently displayed data to check its type
        List<?> currentData = data;

        if (currentData != null) {
            if (currentData.isEmpty()) {
                // Nothing to filter, do nothing.
                return;
            }

            // Get the type of the items in the list
            //Object firstItem = currentData.getFirst();

            // --- DISPATCHER LOGIC ---
//            if (firstItem instanceof IoTDomain) {
//                filterDataOnHierarchyView();
//            } else if (firstItem instanceof ArchitectureSolution) {
//                filterForFlatSolutionView();
//            } else if (firstItem instanceof QualityRequirement) {
//                filterForFlatQualityView();
//            } else if (firstItem instanceof Technology) {
//                filterForFlatTechnologyView();
//            } else {
//                System.err.println("Warning: Unknown data type in TreeView. Cannot filter.");
//            }

            filterDataOnHierarchyView(data);
        }
    }


    /**
     * Filters the data when the TreeView is displaying the full Domain -> Solution hierarchy.
     */
    private void filterDataOnHierarchyView(List<IoTDomain> data) {
        // 1. Obtenha os dados originais e as seleções de todos os filtros.
        Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
        Set<ArchitectureSolutionRecord> selectedArchitectures = architectureCombo.getValue();
        Set<QualityRequirementRecord> selectedQRs = qualityCombo.getValue();
        Set<TechnologyRecord> selectedTechs = technologiesCombo.getValue();

        // 2. Crie Sets com os nomes/descrições das seleções para busca eficiente.
        final Set<String> selectedDomainNames = (selectedDomains != null && !selectedDomains.isEmpty()) ?
                selectedDomains.stream().map(IoTDomainRecord::name).collect(Collectors.toSet()) : Collections.emptySet();
        final Set<String> selectedArchitectureNames = (selectedArchitectures != null && !selectedArchitectures.isEmpty()) ?
                selectedArchitectures.stream().map(ArchitectureSolutionRecord::name).collect(Collectors.toSet()) : Collections.emptySet();
        final Set<String> selectedQrNames = (selectedQRs != null && !selectedQRs.isEmpty()) ?
                selectedQRs.stream().map(QualityRequirementRecord::name).collect(Collectors.toSet()) : Collections.emptySet();
        final Set<String> selectedTechDescriptions = (selectedTechs != null && !selectedTechs.isEmpty()) ?
                selectedTechs.stream().map(TechnologyRecord::description).collect(Collectors.toSet()) : Collections.emptySet();

        // 3. Inicie uma stream a partir da lista original de domínios.
        Stream<IoTDomain> domainStream = data.stream();

        // --- FASE 1: FILTRAGEM DE ALTO NÍVEL ---
        // Decide quais IoTDomains inteiros serão considerados para a próxima fase.

        // Filtro por Nome do Domínio IoT
        if (!selectedDomainNames.isEmpty()) {
            domainStream = domainStream.filter(domain -> selectedDomainNames.contains(domain.getName()));
        }

        // Filtro por Arquitetura
        if (!selectedArchitectureNames.isEmpty()) {
            domainStream = domainStream.filter(domain ->
                    domain.getArchitectureSolutions().stream()
                            .anyMatch(solution -> solution.getArchitecture() != null &&
                                    selectedArchitectureNames.contains(solution.getArchitecture().getName())));
        }

        // Filtro por Quality Requirement e Tecnologia
        if (!selectedQrNames.isEmpty() || !selectedTechDescriptions.isEmpty()) {
            domainStream = domainStream.filter(domain ->
                    domain.getArchitectureSolutions().stream()
                            .anyMatch(solution -> solution.getQualityRequirementTechnologies().stream()
                                    .anyMatch(qrt ->
                                            (selectedQrNames.isEmpty() || (qrt.getQualityRequirement() != null && selectedQrNames.contains(qrt.getQualityRequirement().getName()))) &&
                                                    (selectedTechDescriptions.isEmpty() || (qrt.getTechnology() != null && selectedTechDescriptions.contains(qrt.getTechnology().getDescription())))
                                    )
                            )
            );
        }

        // --- FASE 2: MAPEAMENTO E PODA INTERNA ---
        // Transforma os domínios que passaram pelo filtro, podando seu conteúdo interno.
        List<IoTDomain> finalFilteredDomains = domainStream.map(domain -> {
                    // Cria uma nova instância de IoTDomain para a view, para não modificar a original.
                    IoTDomain newDomain = new IoTDomain();
                    newDomain.setId(domain.getId());
                    newDomain.setName(domain.getName());
                    newDomain.setDescription(domain.getDescription());

                    // Processa as soluções internas para criar uma nova lista podada.
                    List<ArchitectureSolution> prunedSolutions = domain.getArchitectureSolutions().stream()
                            // 1. Filtra as soluções pelo nome da arquitetura.
                            .filter(solution -> selectedArchitectureNames.isEmpty() ||
                                    (solution.getArchitecture() != null && selectedArchitectureNames.contains(solution.getArchitecture().getName())))
                            // 2. Mapeia cada solução sobrevivente para uma nova instância com sua lista de QRTs podada.
                            .map(solution -> {
                                // Cria uma nova instância de ArchitectureSolution para a view.
                                ArchitectureSolution newSolution = new ArchitectureSolution();
                                newSolution.setId(solution.getId());
                                newSolution.setDescription(solution.getDescription());
                                newSolution.setArchitecture(solution.getArchitecture());
                                newSolution.setIoTDomain(solution.getIoTDomain()); // Mantém a referência, se necessário.
                                newSolution.setPaperReference(solution.getPaperReference());

                                // AQUI ESTÁ A LÓGICA CHAVE: Filtra a lista de QualityRequirementTechnology.
                                List<QualityRequirementTechnology> prunedQRTs = solution.getQualityRequirementTechnologies().stream()
                                        .filter(qrt ->
                                                (selectedQrNames.isEmpty() || (qrt.getQualityRequirement() != null && selectedQrNames.contains(qrt.getQualityRequirement().getName()))) &&
                                                        (selectedTechDescriptions.isEmpty() || (qrt.getTechnology() != null && selectedTechDescriptions.contains(qrt.getTechnology().getDescription())))
                                        )
                                        .collect(Collectors.toList());

                                // Define a lista podada na nova solução.
                                newSolution.setQualityRequirementTechnologies(prunedQRTs);
                                return newSolution;
                            })
                            // 3. Remove as soluções que ficaram vazias após a poda de seus QRTs.
                            .filter(solution -> !solution.getQualityRequirementTechnologies().isEmpty())
                            .collect(Collectors.toList());

                    // Define a lista de soluções podadas no novo domínio.
                    newDomain.setArchitectureSolutions(prunedSolutions);
                    return newDomain;

                })
                // 4. Remove os domínios que ficaram vazios após a poda de suas soluções.
                .filter(domain -> !domain.getArchitectureSolutions().isEmpty())
                .collect(Collectors.toList());

        // 5. Atualize a visualização na árvore com a nova estrutura de objetos.
        this.treeViewComponent.setTreeViewData(finalFilteredDomains);
    }

    /**
     * Filters the data when the TreeView is displaying a flat list of ArchitectureSolutions.
     */
//    private void filterForFlatSolutionView() {
//        // 1. Obtenha a lista original e os valores de todos os filtros de uma vez.
//        List<ArchitectureSolution> originalSolutions = (List<ArchitectureSolution>) this.treeViewComponent.getTreeViewData();
//        Set<IoTDomainRecord> selectedDomains = iotDomainCombo.getValue();
//        Set<ArchitectureSolutionRecord> selectedArchitectures = architectureCombo.getValue();
//        Set<QualityRequirementRecord> selectedQRs = qualityCombo.getValue();
//        Set<TechnologyRecord> selectedTechs = technologiesCombo.getValue();
//
//        // 2. Inicie um stream a partir da lista original de soluções.
//        Stream<ArchitectureSolution> solutionStream = originalSolutions.stream();
//
//        // 3. Aplique cada filtro sequencialmente à stream.
//
//        // --- Filtro por Domínio IoT ---
//        if (selectedDomains != null && !selectedDomains.isEmpty()) {
//            solutionStream = solutionStream.filter(solution ->
//                    solution.getIoTDomain() != null && selectedDomains.contains(solution.getIoTDomain())
//            );
//        }
//
//        // --- Filtro por Arquitetura ---
//        if (selectedArchitectures != null && !selectedArchitectures.isEmpty()) {
//            // Crie um Set com os nomes das arquiteturas para uma busca eficiente (O(1)).
//            Set<String> selectedArchNames = selectedArchitectures.stream()
//                    .map(ArchitectureSolutionRecord::name)
//                    .collect(Collectors.toSet());
//
//            solutionStream = solutionStream.filter(solution ->
//                    solution.getArchitecture() != null && selectedArchNames.contains(solution.getArchitecture().getName())
//            );
//        }
//
//        // --- Filtro por Requisito de Qualidade ---
//        if (selectedQRs != null && !selectedQRs.isEmpty()) {
//            solutionStream = solutionStream
//                    // Passo 1 (FILTRAR): Mantenha apenas as soluções que têm PELO MENOS UM dos QRs selecionados.
//                    // Collections.disjoint é uma forma eficiente de verificar a interseção.
//                    .filter(solution -> solution.getQualityRequirements() != null && !Collections.disjoint(solution.getQualityRequirements(), selectedQRs))
//                    // Passo 2 (MAPEAR/MODIFICAR): Para as soluções que passaram, crie uma cópia
//                    // e filtre sua lista interna de detalhes para mostrar apenas os QRs selecionados.
//                    .map(solution -> {
//                        ArchitectureSolution filteredSolution = new ArchitectureSolution();
//                        BeanUtils.copyProperties(solution, filteredSolution);
//
//                        List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies().stream()
//                                .filter(qrt -> qrt.getQualityRequirement() != null && selectedQRs.contains(qrt.getQualityRequirement()))
//                                .collect(Collectors.toList());
//
//                        filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
//                        return filteredSolution;
//                    });
//        }
//
//        // --- Filtro por Tecnologia ---
//        if (selectedTechs != null && !selectedTechs.isEmpty()) {
//            solutionStream = solutionStream
//                    // Passo 1 (FILTRAR): Mantenha apenas soluções que usam PELO MENOS UMA das tecnologias selecionadas.
//                    .filter(solution -> solution.getTechnologies() != null && !Collections.disjoint(solution.getTechnologies(), selectedTechs))
//                    // Passo 2 (MAPEAR/MODIFICAR): Crie uma cópia e filtre os detalhes internos.
//                    .map(solution -> {
//                        ArchitectureSolution filteredSolution = new ArchitectureSolution();
//                        BeanUtils.copyProperties(solution, filteredSolution);
//
//                        List<QualityRequirementTechnology> filteredQRTs = solution.getQualityRequirementTechnologies().stream()
//                                .filter(qrt -> qrt.getTechnology() != null && selectedTechs.contains(qrt.getTechnology()))
//                                .collect(Collectors.toList());
//
//                        filteredSolution.setQualityRequirementTechnologies(filteredQRTs);
//                        return filteredSolution;
//                    });
//        }
//
//        // 4. Colete os resultados finais da stream em uma lista.
//        List<ArchitectureSolution> finalFilteredSolutions = solutionStream.collect(Collectors.toList());
//
//        // 5. Atualize a visualização na árvore.
//        this.treeViewComponent.setTreeViewData(finalFilteredSolutions);
//    }

    /**
     * Filters the data when the TreeView is displaying a flat list of QualityRequirements.
     */
//    private void filterForFlatQualityView() {
//        // 1. Get all solutions that match the filters.
//        List<ArchitectureSolution> matchingSolutions = getFilteredSolutions();
//
//        // 2. From those solutions, extract all unique Quality Requirements.
//        List<QualityRequirement> filteredQualities = matchingSolutions.stream()
//                .flatMap(solution -> solution.getQualityRequirements().stream())
//                .distinct() // Avoid duplicates
//                .collect(Collectors.toList());
//
//        this.treeViewComponent.setTreeViewData(filteredQualities);
//    }

    /**
     * Filters the data when the TreeView is displaying a flat list of Technologies.
     */
//    private void filterForFlatTechnologyView() {
//        // 1. Get all solutions that match the filters.
//        List<ArchitectureSolution> matchingSolutions = getFilteredSolutions();
//
//        // 2. From those solutions, extract all unique Technologies.
//        List<Technology> filteredTechnologies = matchingSolutions.stream()
//                .flatMap(solution -> solution.getTechnologies().stream())
//                .distinct() // Avoid duplicates
//                .collect(Collectors.toList());
//
//        this.treeViewComponent.setTreeViewData(filteredTechnologies);
//    }

}
