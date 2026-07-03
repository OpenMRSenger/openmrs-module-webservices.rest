# Onderhoudbaarheidsstrategie & Prioritisering

Dit document beschrijft een gestructureerde aanpak voor het identificeren, evalueren en prioriteren van code-onderhoudbaarheidsproblemen in de OpenMRS Web Services REST-module.

---

## 1. Belangrijkste Onderhoudbaarheidsstatistieken & Drempelwaarden

We meten 4 belangrijke statistieken om de kwaliteit van de code en de onderhoudbaarheid te bepalen:

| Statistiek | Beschrijving | Grenswaarde |
| :--- | :--- | :--- |
| **Cognitieve Complexiteit** | Meet de logische moeilijkheid en nestingsdiepte van een methode. | **Max 10** per methode. |
| **Unit Test Coverage** | Percentage van de coderegels en vertakkingen gedekt door unit-tests. | **Min 80%** (100% aanbevolen voor kernlogica). |
| **Usage (Coupling) Count** | Aantal koppelingen/verwijzingen naar een klasse of methode in de codebase. | Geeft impact aan. Hoge koppeling = Hoge impact. |
| **Methode/Klasse Grootte** | Totaal aantal regels code (LOC). Grote bestanden zijn lastiger te lezen en te testen. | **Max 81 LOC** per methode, **Max 500 LOC** per klasse. |

---

## 2. Prioritiseringsmatrix (Impact vs. Moeite)

Knelpunten worden geprioriteerd door ze op een **Impact vs. Moeite** matrix te plaatsen om het rendement van refactoring te maximaliseren:

| Moeite \ Impact | Lage Impact | Hoge Impact |
| :--- | :--- | :--- |
| **Weinig Moeite** | **Lage Prioriteit**<br>(Kleine opschoningen, dode code verwijderen) | **Quick Wins (Prioriteit 1)**<br>(Modulariseren van veelgebruikte helpers met bestaande unit-tests) |
| **Veel Moeite** | **Gede-prioriteerd**<br>(Herschrijven van zelden gebruikte legacy-functionaliteiten) | **Grote Initiatieven (Prioriteit 2)**<br>(Refactoren van basis resource klassen, verminderen diepe overerving) |

---

## 3. Rechtvaardiging voor Eerste Refactor: ConversionUtil.convert

Op basis van deze strategie is `ConversionUtil.convert` geselecteerd als het eerste refactordoel:

1. **Schending Statistieken**:
   * **Cognitieve Complexiteit**: **36** (Grenswaarde: < 10). Overschrijdt de limiet door diep geneste datumparsings-lussen en reflectie catch-blokken.
   * **Methode Grootte**: **~120 LOC** (Grenswaarde: < 81 LOC). Overschrijdt de regellimiet.
2. **Impactanalyse**:
   * **Gebruik**: **47 verwijzingen** in de codebase (29 in broncode, 18 in tests). Cruciaal voor REST-serialisatie. Hoge Impact.
3. **Moeiteanalyse**:
   * **Risico**: **Laag**. Gedekt door 16 bestaande unit-tests in `ConversionUtilTest.java`. Weinig Moeite.

*Conclusie*: Valt in het **Quick Wins (Prioriteit 1)** kwadrant. De refactor verlaagt de cognitieve complexiteit van **36 naar 3** en lost deprecation-waarschuwingen op met minimaal risico.
