# Code Coverage Strategie: OpenMRS webservices.rest

**Datum:** 15-06-2026
**Groep:** A3
**Onderdeel:** Verbeteronderzoek Onderhoudbaarheid – Code Coverage

## 1. Doel

Geautomatiseerde code coverage-meting (JaCoCo + GitHub Actions) opzetten voor `webservices.rest`, met een realistisch coverage-doel voor deze legacy codebase.

## 2. Waarom niet 100%

Voor een omvangrijke legacy codebase (~63.700 regels, complexiteit ~3.878) is 100% coverage geen reëel doel:

- **Afnemende meeropbrengsten (Pareto):** de eerste 60-70% dekt de kernlogica; daarna kost elk extra procent exponentieel meer tijd (randgevallen, getters/setters, deprecated code, defensieve catch-blocks).
- **Coverage ≠ kwaliteit:** 100% regel-coverage zegt alleen dat code is *uitgevoerd*, niet dat ze correct getest is (Goodhart's Law)  jagen op het getal levert vooral nutteloze testjes op.
- **Legacy code is duur om te testen:** tight coupling en OpenMRS `Context`-singletons (Feathers, *Working Effectively with Legacy Code*) maken volledige testbaarheid afhankelijk van grootschalige refactoring, met regressierisico in een actief project.
- **Niet alle code is even kritisch:** resource-classes die patiëntdata serialiseren wegen zwaarder dan config- of exception-classes  risicogebaseerd testen levert meer op dan een uniform percentage.
- **Industriestandaard:** SonarQube/Google hanteren doorgaans 70-80% als "goed", niet 100%.

## 3. Gekozen doel: baseline + new code

| Scope | Doel |
|---|---|
| Overall baseline (bestaande code) | **≥ 60%** |
| Nieuwe/gewijzigde code (per PR) | **≥ 80%** |
| Security-gevoelige packages (auth, resource-controllers met patiëntdata) | **≥ 80-90%** |

Dit is de "Clean as You Code"-aanpak (vergelijkbaar met SonarQube Quality Gates):

- **Balans kwaliteit/doorontwikkeling:** ontwikkeling gaat door, terwijl elke wijziging de coverage van de codebase geleidelijk verbetert geen apart "test-sprint" nodig.
- **Haalbaar én ambitieus:** 60% baseline is een concreet, meetbaar tussendoel binnen de projectperiode (startpunt is nu ~0%, geen JaCoCo-rapportage aanwezig).
- **Geen perverse prikkels:** focus op nieuwe code voorkomt tijdverlies aan het testen van binnenkort te verwijderen legacy-code.

## 4. Vervolgstappen

- JaCoCo-rapport als build-artifact via GitHub Actions (`.github/workflows/code-coverage.yml`).
- Later eventueel: JaCoCo `check`-goal met `<rules>` om build te laten falen onder 60%, en/of SonarCloud-integratie voor de "new code" coverage-poort.
