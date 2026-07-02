# Maintainability Strategy & Prioritization Strategy

This document establishes a structured approach for identifying, evaluating, and prioritizing code maintainability issues in the OpenMRS Web Services REST module.

---

## 1. Key Maintainability Metrics & Thresholds

We track 4 key metrics to determine code quality and maintainability health:

| Metric | Description | Target Threshold |
| :--- | :--- | :--- |
| **Cognitive Complexity** | Measures the logical difficulty and control flow nesting of a method. | **Max 10** per method. |
| **Unit Test Coverage** | Percentage of code lines and branches verified by unit tests. | **Min 80%** (100% preferred for core logic). |
| **Usage (Coupling) Count** | Number of references to a class or method across the codebase. | Indicates impact weight. High coupling = High impact. |
| **Method/Class Size** | Total lines of code (LOC). Long files are harder to read and test. | **Max 81 LOC** per method, **Max 500 LOC** per class. |

---

## 2. Prioritization Matrix (Impact vs. Effort)

Fixes are prioritized by plotting them on an **Impact vs. Effort** matrix to maximize return on refactoring:

| Effort \ Impact | Low Impact | High Impact |
| :--- | :--- | :--- |
| **Low Effort** | **Low Priority**<br>(Minor cleanups, dead code removal) | **Quick Wins (Priority 1)**<br>(Modularizing heavily used helper utilities with existing unit tests) |
| **High Effort** | **De-prioritized**<br>(Rewriting obscure legacy features) | **Major Initiatives (Priority 2)**<br>(Refactoring base resource parent classes, reducing deep inheritance) |

---

## 3. Justification for First Refactor: ConversionUtil.convert

Based on this strategy, `ConversionUtil.convert` was selected as the first refactoring target:

1. **Metrics Violations**:
   * **Cognitive Complexity**: **36** (Target: < 10). Violates threshold due to deeply nested date parsing loops and reflection catch blocks.
   * **Method Size**: **~120 LOC** (Target: < 81 LOC). Violates size threshold.
2. **Impact Assessment**:
   * **Usage**: **47 references** across the codebase (29 in source, 18 in tests). Highly central to request/response serialization. High Impact.
3. **Effort Assessment**:
   * **Risk**: **Low**. Guarded by 16 existing unit tests in `ConversionUtilTest.java`. Low Effort.

*Conclusion*: Fits **Quick Wins (Priority 1)** quadrant. Refactoring will reduce cognitive complexity from **36 to 3** and resolve deprecation warnings with minimal risk.
