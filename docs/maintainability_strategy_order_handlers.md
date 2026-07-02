# Code Maintainability Strategy

This document establishes a structured, metrics-driven approach to evaluating and improving the maintainability of the `openmrs-module-webservices.rest` codebase. It outlines key metrics, thresholds, and a prioritization framework to guide refactoring efforts and ensure long-term code quality.

---

## 1. Key Maintainability Metrics & Thresholds

To systematically monitor and improve the codebase, we track four primary maintainability metrics:

### 1.1 Cognitive Complexity
* **Definition:** A measure of how difficult the control flow of a method/class is to understand, following the SonarSource specification (e.g., nesting, switch cases, boolean operations, recursion).
* **Justification:** High cognitive complexity increases cognitive load, making code harder to review, debug, and modify. In a dynamic REST framework, clear flow control is critical.
* **Thresholds:**
  * **Method Level:** Maximum Cognitive Complexity of **15**.
  * **Class Level:** Maximum Cognitive Complexity of **50**.

### 1.2 Code Duplication
* **Definition:** The percentage of lines that are identical or near-identical to lines elsewhere in the codebase.
* **Justification:** Duplicated code (code smells) leads to high maintenance effort, as bug fixes or changes must be applied in multiple locations. The rubric specifically penalizes unaddressed duplication.
* **Thresholds:**
  * **Global/Module Level:** Less than **5%** overall duplication.
  * **New/Refactored Code:** **0%** duplication.

### 1.3 Class Size & Lines of Code (LOC)
* **Definition:** The total number of physical lines of code in a file or class.
* **Justification:** Excessively large classes (God Classes) violate the Single Responsibility Principle (SRP), are hard to navigate, and make parallel development difficult.
* **Thresholds:**
  * **Method Level:** Maximum **100** lines (excluding comments).
  * **Class Level:** Maximum **500** lines (excluding comments).

### 1.4 Coupling & Design Consistency (Dependency Quality)
* **Definition:** The degree of interdependence between classes, specifically tracking the use of static mutable states, mixed logging frameworks, and anti-patterns combining static singletons with Spring Dependency Injection (DI).
* **Justification:** Tight coupling and inconsistent dependency injection make components untestable in isolation (blocking unit testing).
* **Thresholds:**
  * **Zero** static singleton + Spring DI hybrid anti-patterns.
  * **Zero** mixed logging frameworks (standardize on SLF4J).

---

## 2. Prioritization Matrix (Impact vs. Effort)

Refactoring tasks are prioritized using an **Impact vs. Effort** matrix. This ensures that developer effort is directed where it yields the highest return on maintainability.

| Metric / Aspect | High Impact | Low Impact |
| :--- | :--- | :--- |
| **High Effort** | **Priority 2 (Strategic Refactoring)**<br>- God Classes (e.g., `SwaggerSpecificationCreator` - Issue 93)<br>- Versioned Search Handlers (e.g., 21 search handlers over 8 folders - Issue 96) | **Priority 4 (De-prioritized / Backlog)**<br>- Complex deprecated class refactoring (e.g., `UserAndPassword1_8` - Issue 89) |
| **Low Effort** | **Priority 1 (Immediate Wins / Hotspots)**<br>- Duplication in Order Handlers (`DrugOrder`/`TestOrder` - Issue 136)<br>- Static Singleton + Spring DI (`OpenmrsClassScanner` - Issue 98) | **Priority 3 (Routine Maintenance)**<br>- Clean up inconsistent logging (SLF4J vs. Commons - Issue 95)<br>- Configurable thread pools (Issue 83) |

### Prioritization Criteria
* **Impact Evaluation:**
  * How frequently is this class modified or extended?
  * Does this block automated testing (unit tests)?
  * Does this affect core system behavior (e.g., authentication, order entry)?
* **Effort Evaluation:**
  * Does the refactoring require modifying public APIs?
  * What is the risk of breaking regression?
  * How many class dependencies need to be updated?

---

## 3. Selected Target Hotspot for LU2 Refactoring

Based on the rubric feedback and the prioritization matrix, the **Order Handler Duplication** (`DrugOrderSubclassHandler1_10` & `TestOrderSubclassHandler1_10`) is selected as the primary hotspot:
1. **High Impact:** The ordering system is a critical, frequently used component of OpenMRS. The duplication in the `getActiveOrders` method hampered readability and could lead to inconsistencies.
2. **Low Effort / Safe Refactoring:** Refactoring using delegation is clean, has minimal side effects, and can be thoroughly validated with targeted unit and integration tests to ensure no regression.
3. **Rubric Alignment:** Directly addresses the rubric requirement to resolve code duplication through design-led improvements (not just inheritance) and demonstrate proof of validation.
