# Bijlage - SBOM & Dependency Update-advies (CVE/CVSS)

**Project:** OpenMRS `webservices.rest` (v3.2.0) | **Groep 3** | 19-06-2026

## 1. SBOM
- Geautomatiseerd gegenereerd via de **CycloneDX Maven-plugin** in GitHub Actions (`sbom.yaml`), bij elke push/merge naar `dev`/`main`.
- Output: machine-leesbare `bom.json` (CycloneDX) met per component groupId, artifactId, versie, licentie en `purl`. *(bijgevoegd)*
- CVE/CVSS getoetst tegen **NVD** en de **GitHub Advisory Database**; **Dependabot** bewaakt continu nieuwe advisories. CVSS = v3.1 base score.

## 2. Bevindingen & advies

| # | Component | Versie | Severity | CVE | CVSS | Advies | Prio |
|---|---|---|---|---|---|---|---|
| D-01 | tomcat `jasper` | 6.0.53 | **Kritiek** | CVE-2020-1938 (Ghostcat) | **9.8** | Upgrade naar 9.0.x+ / JSP-dep verwijderen | **P1** |
| D-02 | jackson-dataformat-yaml vs core | 2.22.0 / 2.18.7 | Medium | - (BOM-misalignment) | n.v.t. | Een `jackson-bom` afdwingen | **P2** |
| D-03 | swagger-core | 1.6.16 | Laag | geen directe CVE | n.v.t. | Bijhouden; endpoint afschermen | P3 |
| D-04 | joda-time | 2.14.2 | Info | CVE-2024-23080 (betwist) | 5.5 | Geen actie (zie 3) | P4 |
| D-05 | commons-codec | 1.22.0 | Laag | geen bekende CVE | n.v.t. | Bijhouden | P4 |
| D-06 | jackson-core/databind | 2.18.7 | Laag (gepatcht) | CVE-2025-52999 (fix >=2.18.6) | n.v.t. | Houden | P4 |

## 3. Toelichting toprisico's

**D-01 - Tomcat/Jasper 6.0.53 (P1).** De 6.0.x-tak is EOL (sinds 2016) en krijgt geen fixes meer.
Het concrete gevolg is **Ghostcat (CVE-2020-1938, 9.8)**: file-read via de AJP-connector, en bij
mogelijke upload zelfs RCE. Voor 7/8/9 gepatcht, **voor 6.x niet** -> niet-mitigeerbaar op
componentniveau. Context-verlagend: AJP (poort 8009) is intern en niet extern bereikbaar, maar
dat neemt het patch-loze defect niet weg. *Actie:* JSP-afhankelijkheid verwijderen indien mogelijk,
anders meeliften met de Tomcat 9.0.x+ van OpenMRS. Verifieer eerst scope + of AJP actief is.

**D-02 - Jackson-BOM-misalignment (P2).** `dataformat-yaml` (2.22.0) loopt voor op core/databind
(2.18.7); dit kan transitief een afwijkende `jackson-core` binnentrekken en bemoeilijkt
kwetsbaarheidsbeheer. *Actie:* `jackson-bom` in `dependencyManagement`, daarna integratietests.

**D-04 - joda-time CVE-2024-23080.** Door de maintainer **ongeldig verklaard** (NPE bij `null`-invoer
= normaal Java-gedrag, geen securityfout). Geen actie; documenteren zodat scanners die 'm flaggen
met onderbouwing als onterecht gemarkeerd kunnen worden.

**D-06 - Jackson 2.18.7.** Actueel en gepatcht (CVE-2025-52999 opgelost vanaf 2.18.6). De eerdere
"geaccepteerd Jackson-risico"-aanname in het hoofdrapport is hiermee achterhaald.

## 4. Prioritering (context-gewogen)
Netwerk-exploiteerbare findings wegen één niveau lager omdat de API alleen binnen het afgeschermde
intranet/VPN draait (zelfde lijn als geaccepteerde restrisico's SEC-10/SEC2-06). Findings **zonder**
patchpad (D-01) wegen juist zwaarder: acceptatie is daar permanent i.p.v. tijdelijk.

## 5. Procesborging
- CI laten falen op nieuwe `high`/`critical` advisories (Dependabot/SBOM-gate) -> operationaliseert NEN 7510 control 8.8.
- Roadmap: artifact-/release-ondertekening (Cosign) en hash-pinning voor supply-chain-integriteit.

## 6. Bronnen
NVD/Apache - CVE-2020-1938 (Ghostcat, 9.8; 6.x affected, geen patch) | Tomcat 6 EOL (tomcat.apache.org/security-6.html) | NVD - CVE-2025-52999 (fix >=2.18.6) | Joda-Time Security - CVE-2024-23080 ongeldig verklaard | CycloneDX `bom.json` (bijgevoegd) | NEN 7510-2:2024 control 8.8
