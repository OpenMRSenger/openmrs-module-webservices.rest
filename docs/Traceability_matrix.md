# Traceability Matrix

## Doel

De traceability matrix koppelt NEN 7510:2024-controls aan concrete artefacten uit de OpenMRS REST Web Services Module. Hierdoor is aantoonbaar op basis van welk bewijs een control als aanwezig, gedeeltelijk aanwezig of afwezig is beoordeeld.

| NEN 7510 Control          | Omschrijving                                             | Artefact                                     | Bewijs                                                                               | Status                |
| ------------------------- | -------------------------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------------ | --------------------- |
| A.8.3 Toegangsbeveiliging | Toegang mag alleen plaatsvinden op basis van autorisatie | OpenMRS Core RBAC + REST Module configuratie | Rollen en privileges in OpenMRS, configuratieparameter `webservices.rest.allowedips` | Gedeeltelijk aanwezig |
| A.8.5 Authenticatie       | Gebruikers moeten veilig worden geauthenticeerd          | REST API documentatie                        | HTTP Basic Authentication en `/ws/rest/v1/session` endpoint                          | Gedeeltelijk aanwezig |
| A.8.15 Logging            | Beveiligingsgebeurtenissen moeten worden vastgelegd      | `auditInfo` object in OpenMRS resources      | Velden `creator`, `dateCreated`, `changedBy`, `dateChanged`, `voidedBy`              | Gedeeltelijk aanwezig |

---

## Detailniveau per Control

### A.8.3 Toegangsbeveiliging

| Artefact Type | Artefact          | Traceerbaar bewijs                                       |
| ------------- | ----------------- | -------------------------------------------------------- |
| Configuratie  | REST Module       | `webservices.rest.allowedips`                            |
| Applicatie    | OpenMRS Core      | Rollen en privileges (RBAC)                              |
| Documentatie  | OpenMRS Wiki      | User Management & Access Control                         |
| Threat Model  | Resource Handlers | threat-027 – Ongeautoriseerde inzage van patiëntgegevens |

### A.8.5 Authenticatie

| Artefact Type    | Artefact                | Traceerbaar bewijs               |
| ---------------- | ----------------------- | -------------------------------- |
| API Documentatie | REST API                | HTTP Basic Authentication        |
| Endpoint         | `/ws/rest/v1/session`   | Sessiebeheer                     |
| Threat Model     | Authenticatie & Filters | threat-025 – Credential Stuffing |
| Threat Model     | Client                  | threat-026 – Session Hijacking   |
| Gap Analyse      | Control A.8.5           | Geen MFA aanwezig                |

### A.8.15 Logging

| Artefact Type   | Artefact                | Traceerbaar bewijs                                         |
| --------------- | ----------------------- | ---------------------------------------------------------- |
| Broncode        | auditInfo object        | creator, dateCreated, changedBy, dateChanged               |
| Logging analyse | Event matrix            | Ontbrekende login- en API-logging                          |
| Threat Model    | HTTP Request            | threat-029 – Geen audittrail voor patiëntinzage            |
| Threat Model    | Authenticatie & Filters | threat-028 – Ontbrekende logging van authenticatiepogingen |
| Gap Analyse     | Control A.8.15          | Onvoldoende security logging                               |

---

## Samenvatting

| Control                   | Belangrijkste Artefact                  | Status                |
| ------------------------- | --------------------------------------- | --------------------- |
| A.8.3 Toegangsbeveiliging | RBAC + `webservices.rest.allowedips`    | Gedeeltelijk aanwezig |
| A.8.5 Authenticatie       | Basic Authentication + Session endpoint | Gedeeltelijk aanwezig |
| A.8.15 Logging            | auditInfo object                        | Gedeeltelijk aanwezig |

## Conclusie

Voor alle drie onderzochte NEN 7510:2024-controls is bewijs aanwezig in de vorm van broncode, configuratie, documentatie, logging-analyse en threat-model artefacten. Geen van de controls is volledig geïmplementeerd; alle drie worden daarom beoordeeld als gedeeltelijk aanwezig.
