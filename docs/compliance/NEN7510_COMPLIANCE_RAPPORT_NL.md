# NEN 7510-2 Compliance Formuleringsrapport
**Project Audit & Beveiligingsarchitectuur Beheersmaatregelen**
**Doelframework:** NEN 7510-2 (Informatiebeveiliging in de zorg)
**Status:** Gereed voor Compliance Review

---

## 1. Managementsamenvatting
Dit document legt de officiële technische vertaling vast van de **NEN 7510-2** normeisen voor onze codebase en infrastructuurarchitectuur. Door gebruik te maken van de gestructureerde risicopatronen die zijn ontwikkeld tijdens onze interne beveiligingsanalyse—specifiek gericht op **Broken Access Control** en **Injection Vulnerabilities** koppelen we high-level compliance-doelstellingen in de zorg rechtstreeks aan programmatische barrières, geautomatiseerde verificatiecontroles en infrastructurele maatregelen.

De implementatie van de hieronder beschreven beheersmaatregelen garandeert dat ons platform de beschikbaarheid, integriteit en vertrouwelijkheid (BIV-classificatie) waarborgt die wettelijk verplicht is voor het verwerken van persoonlijke gezondheidsinformatie (patiëntgegevens) binnen de Nederlandse gezondheidszorg.

---

## 2. NEN 7510-2 Beheersmaatregelen & Project Mapping

### Control 1: NEN 7510-2 Beheersmaatregel 9.4.1 - Informatielogging (Auditing)
> **Normatieve Eis:** Logboeken over gebeurtenissen waarin gebruikersactiviteiten, uitzonderingen en informatiebeveiligingsgebeurtenissen worden vastgelegd, worden geproduceerd, bewaard en regelmatig beoordeeld. Bij het verwerken van persoonlijke gezondheidsinformatie moet de logging herleidbaar zijn tot een natuurlijk persoon en inzicht geven in welke patiëntgegevens zijn ingezien of gewijzigd (NEN 7513 specificatie).

#### 2.1.1 Risicocontext & Systeembedreigingen
Op basis van de Risico Matrix van ons project wordt de applicatie geconfronteerd met kritieke dreigingen, o.a. **Unauthorized Data Exposure** en **Audit Trail Destruction**. In een zorgomgeving vormt een kwaadwillende actor die toegangspatronen probeert te verhullen een catastrofale inbreuk op de onweerlegbaarheid (non-repudiation). Zonder gegarandeerde log-integriteit is forensische reconstructie van een incident onmogelijk, wat leidt tot non-compliance bij de Autoriteit Persoonsgegevens (AP).

#### 2.1.2 Codebase & Architectural Implementatie
Om absolute compliance te bereiken, implementeert ons project een rigide, gelaagd framework voor beveiligde logging:
* **Append-Only Databaseschema (`[L1-CODE]`, `[L4-INFRA]`):** De database-engine host een geïsoleerde audit log-tabel die strikt als 'append-only' is gestructureerd. Triggers op de storage-engine en database-privilegitematrices weigeren expliciet `DELETE`-, `UPDATE`- of `DROP`-rechten aan de runtime-principal van de applicatie.
* **Cryptografische Handtekeningen (`[L3-AUDIT]`):** Elke afzonderlijke logregel (die vastlegt *Wie* toegang had, *Wanneer*, *Welk Patiënt-ID* en *Welke Operatie*) wordt cryptografisch gehasht en gekoppeld of ondertekend op de applicatielaag. Elke ongeautoriseerde wijziging in een historisch logboek verbreekt de verificatieketen, wat direct beveiligingsexcepties activeert.
* **Geautomatiseerde Log-exfiltratie & SIEM-alerting (`[L2-MONITOR]`):** De logging-middleware van de applicatie verzendt telemetrie out-of-band naar een gecentraliseerde log-harvester. Actieve alert-regels monitoren op afwijkende toegangsindicatoren (zoals bulk-zoekopdrachten of queries die audit-tabellen structureel proberen te wijzigen) en sturen real-time meldingen naar de incident response-wachtrij.

---

### Control 2: NEN 7510-2 Beheersmaatregel 9.2.3 - Beheer van Toegangsrechten (Privilege Enforcement)
> **Normatieve Eis:** Toegangsrechten van gebruikers tot systemen en netwerken moeten worden toegekend of ingetrokken in overeenstemming met het toegangsbeveiligingsbeleid. Autorisatiecontroles moeten systematisch en op elk niveau van de applicatie worden afgedwongen op basis van het 'need-to-know'-principe.

#### 2.2.1 Risicocontext & Systeembedreigingen
Onze architectuuranalyse markeert **Missing Privilege Enforcement**, **No Ownership Validation** en **Missing Sub-Resource Checks** als kritieke kwetsbaarheden. Als een gebruiker verzoekparameters wijzigt (bijvoorbeeld door het manipuleren van een REST-endpoint-URL of IDOR-payload van `/api/patient/101` naar `/api/patient/102`), resulteert een gebrek aan uitgebreide autorisatiecontroles in onrechtmatige blootstelling van patiëntgegevens, of in het ergste geval, in een systemische privilege-escalatie tot applicatiebeheerder.

#### 2.2.2 Codebase & Architectural Implementatie
We dwingen de 'need-to-know'-beperking af over meerdere onafhankelijke lagen van de applicatie-executie (*Defense-in-Depth*):
* **Autorisatie op Framework-niveau (`[L1-CODE]`, `[L3-FRAMEWORK]`):** Elke blootgestelde REST-controller of business service-methode vereist declaratieve beveiligingsvalidatie. We maken gebruik van structurele annotaties (`@RequiresPrivilege`) en gecentraliseerde interceptor-hooks (`@PreExecute`) die de inkomende executiecontext onderscheppen en de actieve sessierollen van de gebruiker valideren tegen de toegangsmatrix van het doelobject *voordat* de business logica wordt uitgevoerd.
* **Contextuele Eigendomsvalidatie (`[L1-CODE]`):** De database-repositorylaag valideert automatisch relatiebeperkingen tijdens lees-, update- en verwijdersequenties. Het framework dwingt een operationele grens af die vereist dat het token van de geauthenticeerde zorgverlener of instelling expliciet gekoppeld is aan het dossier van de doelpatiënt, waardoor datalekken bij genestelde sub-resources (bijv. onbevoegde opvraging van een allergielijst) worden voorkomen.
* **Filtering van Response-output (`[L1-OUTPUT]`):** Als expliciete vangnetgrens onderscheppen uitgaande serialisatie-pipelines de responses om rollengebaseerde opschoning toe te passen. Als een entiteit uitgebreide patiëntkenmerken bevat die buiten de huidige workflow van de client vallen, worden deze sleutels programmatisch uit de JSON-output verwijderd.

---

### Control 3: NEN 7510-2 Beheersmaatregel 14.2.5 - Beveiligde Ontwikkelingsprincipes (Secure Coding)
> **Normatieve Eis:** Er moeten principes voor het technisch ontwerpen en realiseren van systemen worden geformuleerd, gedocumenteerd en toegepast op de implementatie van systemen voor informatieverwerking. Input- en outputvalidatie moeten systematisch worden toegepast om injectie-aanvallen en runtime manipulatie te voorkomen.

#### 2.3.1 Risicocontext & Systeembedreigingen
Security auditing identificeerde legacy-parameters (specifiek `VULN-002` binnen `SwaggerDocController.java` and `VULN-006` binnen `searchResources.jsp`) als kwetsbaar voor **Gereflecteerde en Opgeslagen Cross-Site Scripting (XSS)** aanvallen. Zonder mitigatie stellen deze injectievectoren kwaadwillende clients in staat om willekeurige JavaScript-payloads uit te voeren binnen de browsersandbox van een zorgverlener. Deze blootstelling faciliteert rechtstreeks sessie-hijacking via cookie-extractie, ongeoorloofde manipulatie van patiëntendossiers of de inzet van interne phishing-formulieren.

#### 2.3.2 Codebase & Architectural Implementatie
Onze ontwikkelpipeline introduceert systemische input- en output-sanitatieprotocollen om executie-kwetsbaarheden te elimineren:
* **Whitelist Inputvalidatie (`[L1-CODE]`, `[L3-FRAMEWORK]`):** De applicatie past strikte structurele whitelists toe via parametervalidators op de controllers (bijv. Spring MVC-annotaties). Verzoeken die de gedefinieerde tekengrenzen, lengteparameters of patroontemplates overschrijden, worden onmiddellijk aan de netwerkgrens geweigerd.
* **Contextbewuste Output Encoding (`[L1-CODE]`, `[L4-ENCODING]`):** Oude vormen van expressie-evaluatie (`${resource.name}`) worden volledig uitgefaseerd en vervangen door context-veilige renderingpatronen, specifiek door het gebruik van JSTL `<c:out value="..." escapeXml="true"/>` logica. Dit zorgt ervoor dat elke invoerstring die uitvoerbare markdown- of script-syntaxis bevat, veilig wordt omgezet naar niet-uitvoerbare HTML-entiteiten (`<` wordt `&lt;`) vóór interpretatie door de client-browser.
* **Browserbeveiligingsrichtlijnen (`[L2-HEADERS]`, `[L3-COOKIE]`, `[L3-CSP]`):** Onze deployment-manifesten injecteren expliciete defensieve infrastructuurrichtlijnen. Alle sessiegerelateerde cookies zijn hardcoded voorzien van de vlaggen `HttpOnly`, `Secure` en `SameSite=Strict` om ervoor te zorgen dat ze volledig ontoegankelijk zijn voor de JavaScript DOM-engine. Daarnaast wordt er een strikte Content Security Policy (CSP) header geserveerd over de gehele applicatie, die script-executie strikt beperkt tot gewhiteliste, cryptografisch gegenereerde nonces, waardoor geïnjecteerde scripts volledig onschadelijk worden gemaakt.

---

## 3. Compliance Implementatiematrix

De volgende operationele checklist toont onze huidige mijlpalen op de roadmap om volledige NEN 7510-compliance te realiseren binnen onze deploymentcycli.

| Fase | Actiepunt | Doelmaatregel | Technische Laag | Verificatiestatus |
| :--- | :--- | :--- | :--- | :--- |
| **Fase 1** | Controleer alle controller-methoden op ontbrekende `@RequiresPrivilege` annotaties en implementeer input-whitelists. | Beheersmaatregel 9.2.3 / 14.2.5 | `L1-CODE` / `L3-FRAMEWORK` | [x] Geverifieerd (SEC-03 opgelost) |
| **Fase 1** | Implementeer database-triggers die `DELETE`-statements op audit-tabelschema's blokkeren. | Beheersmaatregel 9.4.1 | `L4-INFRA` | [ ] Geïmplementeerd |
| **Fase 2** | Refactor alle presentatielaag-blokken naar JSTL `<c:out>` output escaping. | Beheersmaatregel 14.2.5 | `L1-CODE` | [ ] In Uitvoering |
| **Fase 2** | Implementeer Spring Interceptor voor REST API-toegangslogging. | Beheersmaatregel 9.4.1 | `L1-CODE` / `L3-FRAMEWORK` | [x] Geverifieerd (RestAuditLogInterceptor actief) |
| **Fase 2** | Verplicht security-gerichte code reviews voor alle commits die betrekking hebben op autorisatielogica. | Beheersmaatregel 9.2.3 | `L2-REVIEW` | [ ] Operationeel |
| **Fase 3** | Integreer cryptografische ketens/handtekeningen op actieve audit trail-logs. | Beheersmaatregel 9.4.1 | `L3-AUDIT` | [ ] In Afwachting van Review |
| **Fase 3** | Roll-out van een systemische, applicatiebrede Content Security Policy (CSP) configuratie. | Beheersmaatregel 14.2.5 | `L3-CSP` / `L2-HEADERS` | [ ] Geïmplementeerd |