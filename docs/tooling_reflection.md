# Tooling Reflection: AI & Code Quality Assistants

This document contains a critical reflection on how artificial intelligence (AI) assistants, code linters, and development tools were utilized during the LU2 Maintainability refactoring.

---

## 1. Description of Used Tooling

Three main categories of tools were used during this project:

1. **Google Antigravity (AI Coding Assistant):**
   * **Role:** Used for codebase analysis, drafting documentation (maintainability strategy, redesign doc, and validation report), and assisting in writing unit/integration tests.
   * **Context:** Leveraged the LLM's understanding of design patterns (Delegation, Strategy, Template Method) and structural dependencies to analyze alternatives.
2. **SonarCloud / SonarQube:**
   * **Role:** Identified code duplication hotspots (the `getActiveOrders` methods) and quantified code smell density.
   * **Context:** Established the base metrics (duplication percentages and cognitive complexity limits) that dictated the refactoring strategy.
3. **Maven & Java Development Kits (JDK):**
   * **Role:** The execution and validation environment. Used to compile, run tests, and format Java code to verify the safety of changes.

---

## 2. Usage & Workflow Integration

* **Analysis & Code Exploration:** The AI assistant was used to locate declarations of `DrugOrderSubclassHandler1_10` and `TestOrderSubclassHandler1_10` and trace their inheritance hierarchy. This allowed us to quickly understand the constraints of the OpenMRS framework.
* **Design & Alternatives Discussion:** We prompted the AI to evaluate alternatives like the Template Method and Strategy patterns against the existing class structure. The AI helped formulate the tradeoffs regarding single inheritance constraints in Java.
* **Test Generation:** The AI assisted in writing targeted unit tests to verify the `getOrders` helper method in `OrderResource1_10`, ensuring edge cases (sorting by date activated, handling inactive statuses) were covered.

---

## 3. Critical Reflection

### 3.1 Benefits
* **Accelerated Documentation:** Drafting architecture diagrams (Mermaid UML) and write-ups was significantly faster with AI, leaving more time for verification.
* **Objective Code Quality Gate:** SonarCloud provided an objective, tool-agnostic baseline. This prevented "developer bias" in choosing what to refactor.
* **Context Retrieval:** The AI's ability to search and read file structures minimized manual navigation within a large, legacy enterprise codebase.

### 3.2 Limitations & Risks
* **Hallucination of API Methods:** During test drafting, the AI occasionally suggested methods from modern JUnit or Mockito versions that were not in the project's dependency scope (e.g., using JUnit 5 assertions in a JUnit 4 codebase). Developer verification was required to correct these imports.
* **Over-Engineering Bias:** LLMs tend to recommend complex design patterns (like Strategy or Abstract Factories) when a simpler solution (like simple method extraction or delegation) is more maintainable. Developer oversight was necessary to apply the YAGNI principle.
* **Lack of Core Domain Semantics:** The AI does not inherently understand the medical workflow semantics of OpenMRS (e.g., the difference between `DrugOrder` and `TestOrder` in a clinical setting). It treats them as purely technical types, which could lead to semantic errors if not guided.
