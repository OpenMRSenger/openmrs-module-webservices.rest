## Projectraming: NEN-7510 Audit & Refactoring

### 1. Uitgangspunten en Scope
Voor dit project werken we aan de OpenMRS module `webservices.rest`. Uit de initiële code-analyse (SCC) blijkt dat dit een zeer groot en complex systeem is:
* **Omvang:** 743 bestanden met ruim 63.000 regels code (waarvan 54.402 regels in Java).
* **Complexiteit:** Een cyclomatische score van 4.003, wat wijst op zwaar vertakte en complexe logica.
* **Systeemwaarde:** De geschatte initiële bouwkosten (COCOMO) bedragen ruim 2,1 miljoen dollar met een ontwikkeltijd van meer dan 18 maanden.

Vanwege de enorme omvang en de grote financiële en functionele risico's (regressie), is het volledig herschrijven van de code buiten de scope van dit project. Wij richten ons op een beveiligingsaudit volgens de NEN-7510 norm en het verlagen van de technische complexiteit via een gerichte Proof of Concept (PoC).

### 2. Resources (Benodigde middelen)
* **Projectteam:** 4 teamleden (verantwoordelijk voor projectmanagement, compliance analyse en software engineering).
* **Projectduur:** 4 weken.
* **Inzet:** 30 uur per persoon, per week (120 uur per week voor het gehele team).
* **Infrastructuur:** GitHub (omgeving en CI/CD pipeline), Docker (voor de gescheiden lokale OTAP-testomgeving) en SAST-tooling.

### 3. Tijd en Budget
Voor de onderstaande begroting hanteren we in totaal 480 projecturen (4 personen x 30 uur x 4 weken). We rekenen met een fictief intern uurtarief van 75 euro per uur.

| Fase | Uren | Omschrijving | Kosten |
| :--- | :--- | :--- | :--- |
| **1. Setup & Tooling** | 100 | Inrichten testomgeving (OTAP), CI/CD pipelines en code-scanners. | € 7.500 |
| **2. NEN-7510 Audit** | 100 | Uitvoeren van de gap-analyse en valideren van de kwetsbaarheden. | € 7.500 |
| **3. Refactoring (PoC)** | 120 | Ontwikkelen van de Proof of Concept tegen code duplicatie (MNT-01). | € 9.000 |
| **4. Security Fixes** | 100 | Oplossen van kritieke lekken (zoals de ongeldige sessie-timeouts). | € 7.500 |
| **5. Rapportage** | 60 | Opleveren van het adviesverslag, de SBOM en de verantwoording. | € 4.500 |
| **Totaal** | **480** | **Totale geplande projectdoorlooptijd: 4 weken** | **€ 36.000** |