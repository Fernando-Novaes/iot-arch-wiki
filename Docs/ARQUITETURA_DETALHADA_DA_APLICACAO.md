# Documento de Arquitetura Detalhada da Aplicação
**Projeto:** IoT Architectural Design Assistant (IoT Arch Wiki)  
**Versão:** 1.1.0  
**Data:** 02 de Agosto de 2026  
**Autor:** Equipe de Arquitetura de Sistemas IoT  

---

## 1. Visão Geral da Arquitetura

O **IoT Architectural Design Assistant** é uma aplicação Java web monolítica moderna, modular e orientada a componentes, desenvolvida sobre a plataforma **Spring Boot 3** e **Vaadin Flow 24**, integrada a um motor nativo de **RAG (Retrieval-Augmented Generation)** utilizando **LangChain4j** e a API do **Google Gemini (1.5 Flash)**.

A aplicação inclui um sistema completo de **Governança de Contexto da IA** (suportando os modos Híbrido/Aberto e Estrito/Offline) e um **Módulo de Carga e Gestão de Documentos de Referência & Normas (PDF/TXT/MD)**, permitindo o carregamento offline de normas como a ISO 25010 e artigos técnicos diretamente no banco de vetores local.

---

## 2. Tecnologias Utilizadas (Tech Stack)

```
+-------------------------------------------------------------------------+
|                         CAMADA DE APRESENTAÇÃO                          |
|    Vaadin Flow 24 (Server-side Java UI) + HTML5/Vanilla CSS + Vis.js    |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                          CAMADA DE NEGÓCIO                              |
|           Spring Boot 3.x (Spring MVC, Spring Security, Spring Data)    |
+-------------------------------------------------------------------------+
            |                                            |
            v                                            v
+-----------------------+                    +----------------------------+
|  MOTOR DE IA & RAG    |                    |   BANCO DE DADOS H2 / JPA  |
|  LangChain4j + PDFBox |                    |   Spring Data Repositories |
|  Google Gemini REST   |                    |   H2 Relational DB         |
|  InMemoryVectorStore  |                    +----------------------------+
+-----------------------+
```

- **Linguagem:** Java 21 (LTS)
- **Framework Web & UI:** Vaadin Flow 24 (Componentes Java dinâmicos executados no servidor com renderização reativa WebSocket/Push)
- **Framework Backend:** Spring Boot 3.4.x (Injeção de Dependências, Spring Security, Spring Data JPA)
- **Motor de IA / RAG:** LangChain4j + Google Rest Chat Service (Gemini API 1.5 Flash) + `InMemoryEmbeddingStore` + `HashingEmbeddingModel` + `Apache PDFBox`
- **Banco de Dados Relacional:** H2 Database (modo em memória / arquivo de persistência JPA)
- **Visualização de Grafo:** Vis.js (para matrizes e grafos de dependência arquitetural)
- **Automação de Build:** Apache Maven
- **Contêinerização:** Docker + Kubernetes

---

## 3. Arquitetura de Componentes e Camadas (C4 Model - Nível 3)

### 3.1. Camada de Apresentação (Views & UI Components)
- `MainLayout`: Layout principal da aplicação com cabeçalho, barra superior com ações, menu de avatar modernizado, tour guiado e navegação lateral.
- `ArchitectureBuilderView`: Canvas visual e construtor interativo para montagem de pilhas de arquitetura IoT (*Domain*, *Pattern*, *Edge Techs*, *Fog Techs*, *Cloud Techs*, *ISO 25010 Quality Requirements*) e definição de conectores interativos de **Fluxo de Comunicação e Protocolos Inter-Camadas** (*Protocolo de Transporte*, *Criptografia TLS/DTLS* e *Padrão de Mensageria*).
- `AiChatView` & `AIChatInputComponent`: Interface de bate-papo em tempo real com o assistente inteligente, apresentando suporte a *typewriter streaming*, atalho de teclado `Ctrl+Enter`, sugestões rápidas de prompt e histórico contextual.
- `DataManagerView` & `ArchitectureSolutionDataManager`: Painel completo de gestão de dados (Knowledge Manager) com abas em grade CRUD para cadastro de Artigos (**APA 7**), Domínios, Padrões, Requisitos de Qualidade, Tecnologias e a nova aba de **Standards & Reference Docs (PDF)**.
- `ReferenceDocumentDataManager`: Componente visual de gerenciamento de documentos de referência com grade, modal de upload de arquivos de até 50MB, modal de edição e atualização de versão, e modal de exclusão com expurgo de vetores.
- `AvatarComponent`: Menu de avatar moderno com card de perfil (*Profile Card*), pílulas de ícones coloridas (*Icon Pills*) e divisores por seção funcional.
- `AppConfigView`: Interface administrativa e de governança para configuração da chave de API do Gemini, agendador de tarefas e a nova opção de **Governança de Contexto da IA** (*Allow AI to consult general IoT knowledge beyond application context*).

### 3.2. Camada de Serviços de Negócio (Service Layer)
- `AiRagService`: Serviço central de IA responsável por orquestrar as consultas de RAG vetorizado, seleção de prompts com *few-shot learning*, verificação de modo de contexto estrito (`isExternalContextAllowed`), controle de histórico por ID de conversa, e método de re-indexação forçada (`forceReindexDocuments`).
- `DocumentIngestionService`: Serviço de ingestão de documentos de referência (PDF via Apache PDFBox, TXT, MD) responsável por ler arquivos, gerar chunks semânticos via LangChain4j, salvar cópias em `database/uploaded_docs/`, atualizar versões e executar o expurgo e reconstrução atômica do banco vetorial.
- `UploadedDocumentService`: Serviço de persistência JPA para a entidade `UploadedDocument`.
- `AsyncRagQueryService`: Serviço assíncrono reativo que expõe o fluxo de resposta da IA em *streaming* (`Flux<String>`) via Project Reactor.
- `RAGService`: Serviço encarregado da geração e estruturação dos documentos do sistema base para vetorização.
- `AppConfigService`: Serviço responsável pela leitura, atualização e persistência da configuração global do sistema, incluindo o campo `allowExternalContext`.

---

## 4. Fluxo de Carga e Ingestão de Documentos Offline (Sequence Diagram)

O diagrama a seguir detalha o processo de ingestão de um PDF de norma (ex: `ISO-25010.pdf`) e sua disponibilização offline para a IA:

```
[ Usuário (Admin UI) ]
         |
         | 1. Upload do PDF (ex: ISO_25010_2023.pdf) [max 50MB]
         v
[ ReferenceDocumentDataManager ]
         |
         | 2. Invoca documentIngestionService.processAndSaveDocument(...)
         v
[ DocumentIngestionService ]
         |
         | 3. Extrai texto via Apache PDFBox / UTF-8
         | 4. Salva o registro em UploadedDocument (JPA/H2)
         | 5. Salva o arquivo bruto em database/uploaded_docs/<id>_<filename>
         | 6. Executa rebuildVectorStoreWithCustomDocs()
         v
[ AiRagService / InMemoryEmbeddingStore ]
         |
         | 7. Gera embeddings e persiste em database/rag_vector_store.json
         v
[ IA Assistant em Modo Estrito ] ──► Responde dúvidas offline citando a ISO 25010 cadastrada!
```

---

## 5. Estrutura do Banco de Dados Relacional (Modelo de Entidades)

```
+-------------------+       1:N       +--------------------------+
|  PaperReference   |---------------->|   ArchitectureSolution   |
| (DOI, APA7, Year) |                 | (Description, Notes)     |
+-------------------+                 +--------------------------+
                                                   |
                                                   | N:1
                                                   v
                                      +--------------------------+
                                      |        IoTDomain         |
                                      | (Smart Farming, Ind 4.0) |
                                      +--------------------------+
                                                   |
                                                   | N:M (via QualityRequirementTechnology)
                                                   v
                                      +--------------------------+
                                      | QualityRequirementTech  |
                                      | (QR + Tech Allocation)   |
                                      +--------------------------+
                                        /                      \
                                  N:1  /                        \ N:1
                                      v                          v
                     +------------------------+      +-----------------------+
                     |   QualityRequirement   |      |      Technology       |
                     |  (ISO 25010 Pure QRs)  |      | (Edge/Fog/Cloud Tech) |
                     +------------------------+      +-----------------------+

+----------------------------------------------------------------+
|                        UploadedDocument                        |
| (documentTitle, fileName, fileType, fileSize, version, notes)   |
+----------------------------------------------------------------+
```

---

## 6. Implantação e Infraestrutura (DevOps)

### 6.1. Contêinerização com Docker
A aplicação utiliza um `Dockerfile` multi-etapa (*multi-stage build*) otimizado para produção:
- **Build Stage:** Utiliza a imagem `maven:3.9-eclipse-temurin-21` para compilar o código Java e executar o build de produção do Vaadin (`mvn clean package -Pproduction`).
- **Runtime Stage:** Utiliza a imagem leve `eclipse-temurin:21-jre-alpine` para executar o arquivo JAR empacotado.
