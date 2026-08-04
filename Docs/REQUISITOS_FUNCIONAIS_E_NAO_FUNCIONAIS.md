# Documento de Requisitos Funcionais e Não Funcionais
**Projeto:** IoT Architectural Design Assistant (IoT Arch Wiki)  
**Versão:** 1.1.0  
**Data:** 02 de Agosto de 2026  
**Autor:** Equipe de Arquitetura de Sistemas IoT  

---

## 1. Visão Geral do Sistema

O **IoT Architectural Design Assistant** é uma plataforma web para apoio ao design e avaliação de arquiteturas de sistemas da Internet das Coisas (IoT). O sistema combina um catálogo estruturado de padrões, tecnologias, domínios e requisitos de qualidade (ISO 25010) oriundos da literatura científica com um **Assistente Inteligente de RAG (Retrieval-Augmented Generation)** alimentado pelo Google Gemini API e LangChain4j, com suporte completo a **governança de contexto estrito/offline** e **gestão de normas e documentos de referência customizados**.

---

## 2. Requisitos Funcionais (RF)

| ID | Módulo | Descrição do Requisito Funcional | Prioridade |
| :--- | :--- | :--- | :--- |
| **RF01** | **Assistente IA (Chat)** | O sistema deve permitir que o usuário interaja em tempo real com o Assistente de IA para tirar dúvidas, listar domínios, padrões e requisitos de qualidade IoT. | **Alta** |
| **RF02** | **RAG & Streaming** | O sistema deve executar consultas de RAG vetorizado (LangChain4j + Google Gemini API 1.5 Flash) e transmitir respostas via streaming em tempo real (efeito *typewriter* via Reactive Flux/SSE). | **Alta** |
| **RF03** | **Canvas de Arquitetura & Fluxo Inter-Camadas** | O sistema deve disponibilizar um Construtor Visual de Arquitetura (Architecture Builder) permitindo selecionar Domínio IoT, Padrão Arquitetural, Tecnologias de Edge, Fog e Cloud, Requisitos de Qualidade ISO 25010 e definir conectores interativos de **Fluxo de Comunicação e Protocolos** (Protocolo de Transporte, Criptografia TLS/DTLS e Padrão de Mensageria) entre as camadas (Edge ➔ Fog e Fog ➔ Cloud). | **Alta** |
| **RF04** | **Avaliação por IA (7 Seções)** | O sistema deve executar diagnósticos arquiteturais detalhados em 7 seções estruturadas (Pontuação de Compatibilidade & Evidência RAG, Alocação de Camadas/Placement Check, Diagnóstico de Comunicação & Protocolos Inter-Camadas, Pontos Fortes, Lacunas & Riscos de Segurança/Latência, Recomendações de Adaptabilidade e Referências Acadêmicas APA 7). | **Alta** |
| **RF05** | **Exportação e Relatórios** | O sistema deve permitir exportar a arquitetura criada no Canvas para os formatos de diagrama **Mermaid.js**, **PlantUML** e relatórios executivos em **PDF**. | **Média** |
| **RF06** | **Catálogo & Grafo** | O sistema deve disponibilizar uma Matriz de Soluções com visualização em Grafo Interativo (Vis.js) conectando Domínios, Padrões, Tecnologias e Requisitos de Qualidade. | **Média** |
| **RF07** | **Data Manager (CRUD)** | O sistema deve fornecer uma interface de gerenciamento de dados (Knowledge Manager) para cadastrar, editar e excluir Domínios IoT, Artigos/Referências Acadêmicas (no padrão **APA 7**), Padrões Arquiteturais, Requisitos de Qualidade e Tecnologias. | **Alta** |
| **RF08** | **Indexação Master RAG** | O sistema deve gerar e vetorizar dinamicamente documentos de índice mestre (`MASTER_INDEX`, `TECHNOLOGIES`, `PAPER_REFERENCES`) para viabilizar consultas diretas e listagens completas. | **Alta** |
| **RF09** | **Configuração de API Key & Perfil** | O sistema deve permitir cadastrar e atualizar dinamicamente a chave de API do Google Gemini no menu *App Config*, mantendo a chave mascarada (`••••••••`) com os botões **New**, **Save Key** e **Cancel**. O acesso a este menu é compartilhado entre `ADMIN` e `USER` com visibilidade adaptativa. | **Alta** |
| **RF10** | **Governança de Contexto da IA** | O sistema deve permitir que Administradores e Usuários configurem o escopo de contexto da IA entre **Híbrido/Aberto** (uso prioritário dos dados internos + permissão para dados externos da internet) e **Estrito/Offline** (consultas estritamente restritas a todo o acervo interno da aplicação, englobando a Base de Conhecimento e os documentos de referência PDF/normas ISO cadastrados, proibindo qualquer consulta externa à internet). | **Alta** |
| **RF11** | **Gestão de Documentos de Referência & Normas (ISO)** | O sistema deve permitir que Administradores façam upload, versionamento/atualização (ex: ISO 25010:2011 para ISO 25010:2023) e exclusão de documentos de referência nos formatos **PDF**, **TXT** e **MD** de até **50MB**, com expurgo e re-indexação atômica dos vetores no repositório offline. | **Alta** |
| **RF12** | **Menu de Avatar Modernizado** | O sistema deve disponibilizar um menu de avatar suspenso moderno com card de perfil do usuário, pílulas de ícones coloridas e divisores por seção funcional (*My Account*, *Governance*, *Resources*, *Session*). | **Média** |
| **RF13** | **Atalhos e Usabilidade** | O campo de texto do chat deve enviar mensagens ao pressionar a combinação de teclas **`Ctrl + Enter`** (ou **`Cmd + Enter`**). | **Média** |
| **RF14** | **Anotações Pessoais** | O sistema deve disponibilizar uma aba *My Notes* para o usuário registrar anotações técnicas e notas de estudo privadas. | **Baixa** |
| **RF15** | **Tour Guiado** | O sistema deve oferecer um Tour Guiado Interativo (Onboarding Tour) explicando a navegação e o uso da plataforma. | **Baixa** |

---

## 3. Requisitos Não Funcionais (RNF) - Padrão ISO/IEC 25010

### 3.1. Eficiência de Desempenho (Performance Efficiency)
- **RNF01 (Tempo de Resposta RAG):** O primeiro fragmento (*chunk*) de streaming do Assistente de IA deve iniciar em até **1.5 segundos** após o envio da pergunta.
- **RNF02 (Busca Vetorial):** A busca por similaridade de cosseno no banco vetorial local (`InMemoryEmbeddingStore`) deve levar menos de **50ms** para recuperar os 8 trechos mais relevantes.
- **RNF03 (Upload de Documentos Grandes):** O sistema deve processar, realizar a extração de texto (Apache PDFBox) e vetorizar documentos PDF/TXT de até **50MB** em menos de **3 segundos**.

### 3.2. Segurança (Security)
- **RNF04 (Proteção de Credenciais):** A chave de API do Google Gemini nunca deve ser exposta no HTML do cliente ou enviada em texto claro nas respostas. Quando configurada, deve aparecer estritamente mascarada na interface (`••••••••••••••••••••••••••••••••••••••••••••••••••••`).
- **RNF05 (Controle de Acesso Baseado em Papéis - RBAC):** As telas da aplicação e botões de ação devem respeitar as anotações `@RolesAllowed({"ADMIN", "USER"})`, onde funções administrativas críticas (como cadastro de documentos de referência e chave de API) ficam restritas ao perfil `ADMIN`.

### 3.3. Confiabilidade e Resiliência (Reliability)
- **RNF06 (Tratamento de Exceções de IA):** Erros de cota excedida (HTTP 429) ou indisponibilidade temporária do serviço Gemini (HTTP 503) devem ser capturados suavemente e apresentados com instruções amigáveis ao usuário.
- **RNF07 (Persistência Vetorial Atômica):** O estado dos vetores de conhecimento deve ser persistido em disco (`rag_vector_store.json`) e sincronizado com os arquivos em `database/uploaded_docs/`, garantindo resiliência contra reinicializações e operações de *Clean and Update*.

### 3.4. Manutenibilidade (Maintainability)
- **RNF08 (Arquitetura em Camadas):** O código-fonte deve seguir rigorosamente a separação de responsabilidades em camadas (`domain`, `repository`, `service`, `views`, `components`).
- **RNF09 (Modularidade do Front-End):** A interface desenvolvida com Vaadin Flow deve utilizar componentes reutilizáveis e desacoplados (`AIChatInputComponent`, `ArchitectureSolutionDataManager`, `ReferenceDocumentDataManager`, `AvatarComponent`).

### 3.5. Usabilidade (Usability)
- **RNF10 (Design Responsivo e Estética Premium):** A interface deve adotar uma paleta de cores moderna com suporte a temas escuro/claro, tipografia limpa, gradientes sutis e cartões informativos com micro-animações.
- **RNF11 (Internacionalização dos Relatórios):** Os relatórios de diagnóstico arquitetural gerados pela IA devem ser entregues em inglês técnico formal, enquanto consultas informativas diretas adaptam-se ao idioma da pergunta do usuário.

### 3.6. Portabilidade e Implantação (Portability)
- **RNF12 (Contêinerização):** A aplicação deve ser totalmente empacotável em contêiner **Docker** (com suporte a Java 21) e pronta para implantação em clusters **Kubernetes** através dos arquivos de manifesto incluídos no projeto (`Dockerfile` e `kubernetes.yaml`).
