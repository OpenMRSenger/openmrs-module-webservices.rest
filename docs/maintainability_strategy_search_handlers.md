# Code Maintainability Strategy: Search Handlers Refactoring

This document outlines the maintainability strategy and metrics used to identify, prioritize, and refactor code quality issues within the search handlers of `openmrs-module-webservices.rest`.

---

## 1. Key Maintainability Metrics & Thresholds

To ensure long-term code quality, we track and enforce the following thresholds:

### 1.1 Cognitive Complexity
* **Definition:** A measure of how difficult the control flow of a method/class is to understand (nesting, switches, conditions, etc.).
* **Thresholds:** Max **15** per method, max **50** per class.

### 1.2 Code Duplication
* **Definition:** The percentage of lines that are identical or near-identical to lines elsewhere in the codebase.
* **Thresholds:** Max **5%** overall, **0%** on new/refactored code.

### 1.3 Class Size & Lines of Code (LOC)
* **Definition:** The total number of physical lines of code in a file or class.
* **Thresholds:** Max **100** lines per method, max **300** lines per class.

### 1.4 SOLID Principles Compliance
* **Single Responsibility Principle (SRP):** Classes should have only one reason to change. Version-specific handlers should only manage query creation, not duplicate input parsing and database object resolution.
* **Open/Closed Principle (OCP):** Base classes should be closed for modification but open for extension by new version-specific search handlers.

---

## 2. Prioritization Matrix (Impact vs. Effort)

Refactoring tasks are prioritized based on their impact on code health vs. developer effort.

| Impact / Effort | High Impact | Low Impact |
| :--- | :--- | :--- |
| **High Effort** | **Priority 2 (Strategic Refactoring)**<br>- God Classes (e.g., `SwaggerSpecificationCreator` - Issue 93) | **Priority 4 (De-prioritized / Backlog)**<br>- Complex legacy controller deprecations |
| **Low Effort** | **Priority 1 (Immediate Wins / Hotspots)**<br>- Duplication in Order Search Handlers (`OrderSearchHandler2_2`/`2_3` - Issue 96) | **Priority 3 (Routine Maintenance)**<br>- Minor logging standardization |

### Target Hotspot Selection: Order Search Handlers Duplication
* **Impact:** High. Query parsing, list splitting, and validation logic is duplicated across version boundaries. Changes to query parameters or exceptions would require parallel edits in multiple versions.
* **Effort:** Low/Medium. Extracting a shared base class reduces code duplication without affecting client-facing APIs.
