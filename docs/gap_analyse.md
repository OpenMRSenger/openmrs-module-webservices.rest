# Wat is een gap-analyse en waarom maak je deze?

Een **gap-analyse** is een vergelijking tussen:

1. De **huidige situatie (as-is)** – hoe een systeem, proces of organisatie momenteel werkt.
2. De **gewenste situatie (to-be)** – hoe het volgens een norm, wet of eis zou moeten werken.

De "gap" (kloof) is het verschil tussen deze twee situaties.

## Voorbeeld met OpenMRS en NEN 7510

De norm **NEN 7510-2:2024** stelt onder andere eisen aan:

- Toegangsbeveiliging (A.8.3)
- Authenticatie (A.8.5)
- Logging (A.8.15)

Vervolgens wordt onderzocht:

| Vraag                | Voorbeeld                   |
| -------------------- | --------------------------- |
| Wat eist de norm?    | MFA of sterke authenticatie |
| Wat doet OpenMRS nu? | Basic Authentication        |
| Is dat voldoende?    | Nee, slechts gedeeltelijk   |
| Wat ontbreekt?       | MFA                         |
| Wat moet gebeuren?   | MFA implementeren           |

Het ontbrekende deel wordt de **gap** genoemd.

---

# Waarom maak je een gap-analyse?

Een gap-analyse helpt om:

## 1. Compliance vast te stellen

Je kunt aantonen of een systeem voldoet aan normen en regelgeving zoals:

- NEN 7510
- ISO 27001
- AVG/GDPR
- NIS2

## 2. Risico's te identificeren

Ontbrekende beveiligingsmaatregelen kunnen leiden tot:

- Datalekken
- Onbevoegde toegang
- Onvoldoende controle op beveiligingsincidenten

## 3. Verbeteringen te plannen

Een gap-analyse levert concrete actiepunten op.

Voorbeeld:

| Control             | Huidige situatie   | Gewenste situatie          | Actie                 |
| ------------------- | ------------------ | -------------------------- | --------------------- |
| A.8.5 Authenticatie | Basic Auth         | MFA                        | MFA implementeren     |
| A.8.15 Logging      | Alleen auditvelden | Volledige security logging | API-logging toevoegen |

## 4. Prioriteiten te bepalen

Niet elke tekortkoming is even belangrijk.

Voor OpenMRS zouden mogelijke prioriteiten zijn:

1. MFA invoeren
2. Security logging uitbreiden
3. Rollen en privileges herzien

---

# Opbouw van een gap-analyse

Een gap-analyse bestaat meestal uit vier onderdelen:

| Onderdeel             | Beschrijving               |
| --------------------- | -------------------------- |
| Eis uit de norm       | Wat verlangt NEN 7510?     |
| Huidige implementatie | Wat zit er in OpenMRS?     |
| Gap                   | Wat ontbreekt?             |
| Maatregel             | Wat moet worden aangepast? |

### Voorbeeld

| Control             | Huidige situatie              | Gap                        | Maatregel                  |
| ------------------- | ----------------------------- | -------------------------- | -------------------------- |
| A.8.5 Authenticatie | Basic Authentication aanwezig | Geen MFA                   | MFA implementeren          |
| A.8.15 Logging      | AuditInfo aanwezig            | Geen logging van API-calls | Security logging toevoegen |

---

# Definitie voor in het verslag

> Een gap-analyse is een methode waarbij de huidige situatie van een systeem wordt vergeleken met de eisen uit een norm. Het doel is om vast te stellen welke onderdelen al voldoen, welke gedeeltelijk voldoen en welke ontbreken, zodat verbetermaatregelen kunnen worden vastgesteld om compliance te bereiken.
