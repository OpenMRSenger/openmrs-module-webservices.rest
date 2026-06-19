# Module-keuze: OpenMRS webservices.rest

**Datum:** 03-06-2026
**Groep:** A3

## 1. Module Gegevens
* **Naam:** webservices.rest
* **Huidige Versie:** 3.2.0
* **Broncode / Repository:** [\[Link naar jullie GitHub repository\]](https://github.com/OpenMRSenger/openmrs-module-webservices.rest)
* **Grootte:** 63.740 regels code (Complexity: 3.878)

## 2. Motivatie voor deze module
Voor dit project hebben wij bewust gekozen voor de `webservices.rest` module. Deze module vormt de brug tussen de OpenMRS-kern en externe applicaties. Deze positie in de architectuur maakt het een uitstekende kandidaat voor zowel het verbeteronderzoek naar onderhoudbaarheid als de security audits. Onze keuze is gebaseerd op de volgende drie pijlers:

### A. Complexiteit en Refactoring (Onderhoudbaarheid)
Met 63.000 regels code en een hoge cyclomatische complexiteit biedt deze module meer dan voldoende mogelijkheid voor verbetering. 
* **Design Patterns:** REST API's groeien vaak organisch, wat leidt tot overbelaste controllers. Dit biedt ons de kans om ontwerppatronen zoals het *Adapter*-patroon (voor datamapping) of het *Strategy*-patroon (voor API-versiebeheer) succesvol toe te passen.
* **Regressietesten:** Omdat de in- en output van een REST API strak is gedefinieerd (JSON/XML), is het goed mogelijk om betrouwbare regressietests op te zetten om aan te tonen dat onze refactoring de bestaande functionaliteit niet heeft aangetast.

### B. Aanvalsoppervlak (Security & Penetration Testing)
Webservices en API's zijn momenteel aantrekkelijke doelwitten (zie de OWASP API Top 10).
* **Kritieke Kwetsbaarheden:** Deze module verwerkt direct externe input, wat het een ideale plek maakt om te zoeken naar kwetsbaarheden zoals *Broken Object Level Authorization* (BOLA/IDOR), ontbrekende authenticatie en injecties.
* **Impact in de Zorg:** Als we via penetratietesten kunnen aantonen dat patiëntgegevens ongeautoriseerd via de API kunnen worden opgevraagd, hebben we direct een kritiek risico te pakken dat naadloos aansluit bij onze NEN-7510 en CRA gap-analyse.

### C. Dependencies en SBOM (Compliance)
De module leunt zwaar op externe 3rd-party libraries voor het parsen en serialiseren van data (zoals JSON parsers). Dergelijke libraries zijn historisch gezien gevoelig voor kwetsbaarheden (zoals *deserialization* CVE's). Dit garandeert dat we een waardevolle Software Bill of Materials (SBOM) kunnen genereren en een relevant update-advies kunnen schrijven.

## 3. Conclusie
De `webservices.rest` module is niet te simpel, maar ook niet zo massaal als de legacy UI. Het biedt de perfecte balans en scope om alle facetten van de projectopdracht – van code refactoring tot API hacking en NEN-7510 compliance – succesvol en aantoonbaar uit te voeren.
