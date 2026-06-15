# Gap-analyse NEN 7510-2:2024 – OpenMRS REST Module

## Projectinformatie

**Project:** OpenMRS REST Web Services Module

**Repository:** https://github.com/openmrs/openmrs-module-webservices.rest

## Samenvatting

| NEN 7510 Control          | Status                | Beoordeling                                                                                                                              |
| ------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| A.8.3 Toegangsbeveiliging | Gedeeltelijk aanwezig | OpenMRS gebruikt RBAC (rollen en privileges), maar de REST-module vertrouwt grotendeels op OpenMRS Core voor autorisatie.                |
| A.8.5 Authenticatie       | Gedeeltelijk aanwezig | Basic Authentication en sessietokens aanwezig, maar geen MFA of sterke authenticatie.                                                    |
| A.8.15 Logging            | Gedeeltelijk aanwezig | Auditinformatie op objectniveau aanwezig, maar geen volledige security logging van API-toegang, authenticatiepogingen en gebeurtenissen. |

---

# Control A.8.3 – Toegangsbeveiliging

## Eis uit NEN 7510

Gebruikers mogen uitsluitend toegang krijgen tot gegevens en functies waarvoor zij geautoriseerd zijn volgens het principe van minimale rechten (least privilege).

## Huidige implementatie

OpenMRS gebruikt een Role Based Access Control (RBAC) model waarbij gebruikers rechten krijgen via rollen en privileges.

Daarnaast vereist de REST API authenticatie voor vrijwel alle endpoints.

Voorbeeld van aanvullende beveiliging:

```text
webservices.rest.allowedips
```

Deze configuratie maakt het mogelijk om toegang tot de REST API te beperken op basis van IP-adressen.

## Bewijs

### OpenMRS Wiki

- User Management & Access Control
- REST Module documentatie
- REST API authenticatievereisten

### Voorbeeld configuratie

```text
webservices.rest.allowedips
```

## Status

**Gedeeltelijk aanwezig**

## Gap

De REST-module implementeert geen aanvullende toegangscontroles bovenop OpenMRS Core.

Daarnaast ontbreekt aantoonbaar:

- Periodieke review van gebruikersrechten
- Functiescheiding
- Vastgelegde least-privilege configuraties
- Logging van toegang tot patiëntgegevens

## Benodigde maatregelen

1. Documenteren welke REST-endpoints welke privileges vereisen.
2. Periodieke autorisatiereviews uitvoeren.
3. API-specifieke rollen definiëren volgens het least-privilege principe.
4. Logging van toegang tot patiëntgegevens toevoegen.

---

# Control A.8.5 – Authenticatie

## Eis uit NEN 7510

Gebruikers moeten op een veilige manier worden geauthenticeerd voordat toegang wordt verleend tot gegevens of functionaliteiten.

## Huidige implementatie

OpenMRS REST ondersteunt:

- Basic Authentication
- Sessietokens

Voorbeeld:

```http
Authorization: Basic base64(username:password)
```

Sessie-informatie kan worden opgevraagd via:

```http
GET /ws/rest/v1/session
```

## Bewijs

### REST API documentatie

Authenticatie via HTTP Basic Authentication:

```http
Authorization: Basic base64(username:password)
```

Sessie-endpoint:

```http
GET /ws/rest/v1/session
```

## Status

**Gedeeltelijk aanwezig**

## Gap

Geen bewijs gevonden voor:

- Multi-Factor Authentication (MFA)
- Account lockout na meerdere mislukte logins
- Adaptieve authenticatie
- OAuth2/OpenID Connect
- Logging van authenticatiegebeurtenissen

## Benodigde maatregelen

1. MFA implementeren.
2. Account lockout configureren.
3. Sterk wachtwoordbeleid afdwingen.
4. OAuth2/OpenID Connect implementeren.
5. Authenticatie-events loggen.

---

# Control A.8.15 – Logging

---

## Scope

Deze analyse richt zich specifiek op de logging-functionaliteit van de OpenMRS REST Web Services Module en beoordeelt deze ten opzichte van de eisen uit NEN 7510-2:2024 Control A.8.15 (Logging).

---

## Attack Surface Overzicht

De belangrijkste attack surface van de REST-module bestaat uit:

| Onderdeel                 | Risico                                     |
| ------------------------- | ------------------------------------------ |
| REST API endpoints        | Ongeautoriseerde toegang                   |
| Login/authenticatie       | Credential stuffing, brute force aanvallen |
| Patient CRUD-operaties    | Datalekken en ongeautoriseerde wijzigingen |
| User management endpoints | Privilege escalation                       |
| Session management        | Session hijacking                          |
| API requests              | Misbruik van accounts                      |
| Foutafhandeling           | Informatie-lekkage                         |

---

## Logging Inventarisatie

| Event                        | Gelogd?          | Gevoelige data | Compliant met NEN 7510 A.8.15? |
| ---------------------------- | ---------------- | -------------- | ------------------------------ |
| Gebruiker succesvol ingelogd | Gedeeltelijk     | Ja             | Nee                            |
| Mislukte loginpoging         | Niet aantoonbaar | Ja             | Nee                            |
| REST API request uitgevoerd  | Niet standaard   | Ja             | Nee                            |
| Opvragen patiëntgegevens     | Niet aantoonbaar | Ja             | Nee                            |
| Wijzigen patiëntgegevens     | Ja (auditInfo)   | Ja             | Gedeeltelijk                   |
| Verwijderen patiëntgegevens  | Ja (auditInfo)   | Ja             | Gedeeltelijk                   |
| Aanmaken patiëntgegevens     | Ja (auditInfo)   | Ja             | Gedeeltelijk                   |
| Rollen wijzigen              | Niet aantoonbaar | Ja             | Nee                            |
| Privileges wijzigen          | Niet aantoonbaar | Ja             | Nee                            |
| Authenticatiefouten          | Niet aantoonbaar | Ja             | Nee                            |
| Toegang geweigerd (403)      | Niet aantoonbaar | Ja             | Nee                            |
| Configuratiewijzigingen      | Niet aantoonbaar | Nee            | Nee                            |
| IP-adres van gebruiker       | Niet standaard   | Ja             | Nee                            |
| Sessiestart                  | Beperkt          | Ja             | Gedeeltelijk                   |
| Sessiebeëindiging            | Niet aantoonbaar | Ja             | Nee                            |

---

## Aangetroffen Logging

Binnen OpenMRS wordt auditinformatie bijgehouden via het `auditInfo` object:

```text
auditInfo
 ├── creator
 ├── dateCreated
 ├── changedBy
 ├── dateChanged
 ├── voidedBy
 └── dateVoided
```

Hiermee kan worden vastgesteld:

- Wie een record heeft aangemaakt
- Wie een record heeft gewijzigd
- Wanneer een wijziging heeft plaatsgevonden

Dit levert een audittrail op objectniveau, maar geen volledige security logging.

---

## Niet-Gelogde Beveiligingsgebeurtenissen

De volgende gebeurtenissen zijn relevant voor NEN 7510 maar worden niet aantoonbaar geregistreerd:

### Authenticatie

- Succesvolle loginpogingen
- Mislukte loginpogingen
- Brute-force aanvallen
- Account lockouts

### Autorisatie

- Toegang geweigerd (403)
- Wijzigingen in rollen
- Wijzigingen in privileges
- Pogingen tot privilege-escalatie

### REST API Gebruik

- Welke endpoint is benaderd
- Welke gebruiker de actie uitvoerde
- Bron-IP adres
- HTTP responsecode

### Patiëntgegevens

- Welke gebruiker een dossier heeft ingezien
- Welke patiënt betrokken was
- Tijdstip van inzage

Voor een zorginformatiesysteem is juist deze logging essentieel voor forensisch onderzoek en compliance.

---

## Gewenste Situatie volgens NEN 7510

Voor beveiligingsrelevante gebeurtenissen zou minimaal het volgende moeten worden vastgelegd:

| Logging Element             | Vereist |
| --------------------------- | ------- |
| Tijdstip                    | Ja      |
| Gebruiker                   | Ja      |
| Bron-IP                     | Ja      |
| Uitgevoerde actie           | Ja      |
| Resultaat van actie         | Ja      |
| Doelobject                  | Ja      |
| Bescherming tegen wijziging | Ja      |
| Bewaartermijn               | Ja      |

---

## Huidige Situatie

| Logging Element     | Huidige Implementatie |
| ------------------- | --------------------- |
| Tijdstip            | Gedeeltelijk          |
| Gebruiker           | Gedeeltelijk          |
| Bron-IP             | Nee                   |
| Uitgevoerde actie   | Gedeeltelijk          |
| Resultaat van actie | Nee                   |
| Doelobject          | Gedeeltelijk          |
| Bescherming logs    | Niet aantoonbaar      |
| Bewaartermijn       | Niet aantoonbaar      |

---

## Specifieke Gaps

| Gap                                | Risico                                    |
| ---------------------------------- | ----------------------------------------- |
| Geen logging van mislukte logins   | Brute-force aanvallen blijven onopgemerkt |
| Geen logging van API-calls         | Misbruik niet traceerbaar                 |
| Geen logging van dossierinzage     | Niet voldoen aan zorgaudit-eisen          |
| Geen logging van autorisatiefouten | Aanvallen moeilijk detecteerbaar          |
| Geen logging van bron-IP adressen  | Herkomst van aanvallen onbekend           |
| Geen centrale logverzameling       | Moeilijke incidentanalyse                 |
| Geen monitoring en alerting        | Incidenten worden laat ontdekt            |

---

## Aanbevolen Verbetermaatregelen

### Prioriteit 1 – Authenticatie Logging

Log minimaal:

- Login success
- Login failure
- Logout
- HTTP 401 responses
- HTTP 403 responses

### Prioriteit 2 – API Logging

Registreer per API-call:

```text
timestamp
user
source IP
endpoint
HTTP method
response code
```

### Prioriteit 3 – Patiëntdossier Logging

Registreer:

```text
user
patient UUID
actie
timestamp
```

### Prioriteit 4 – Centrale Monitoring

Integreer logging met een SIEM-oplossing zoals:

- Elastic Stack
- Splunk
- Microsoft Sentinel

### Prioriteit 5 – Logbeheer

- Logretentiebeleid definiëren
- Logs beschermen tegen wijziging
- Regelmatige controle van logbestanden

---

## Deelconclusie Logging

De OpenMRS REST-module beschikt over een beperkte audittrail via `auditInfo`, maar registreert onvoldoende beveiligingsrelevante gebeurtenissen. Vooral authenticatie-, autorisatie- en API-gerelateerde gebeurtenissen ontbreken. Hierdoor voldoet de module momenteel slechts gedeeltelijk aan de eisen van NEN 7510-2:2024 Control A.8.15 (Logging).

---

# Conclusie

| Control                   | Status                | Belangrijkste tekortkoming                                            |
| ------------------------- | --------------------- | --------------------------------------------------------------------- |
| A.8.3 Toegangsbeveiliging | Gedeeltelijk aanwezig | Onvoldoende aantoonbare least-privilege inrichting en toegangsreviews |
| A.8.5 Authenticatie       | Gedeeltelijk aanwezig | Geen MFA of moderne authenticatiemechanismen                          |
| A.8.15 Logging            | Gedeeltelijk aanwezig | Onvoldoende security logging en monitoring                            |

## Eindconclusie

De OpenMRS REST-module biedt een basis voor toegangscontrole, authenticatie en auditing via OpenMRS Core. Voor volledige naleving van NEN 7510:2024 zijn echter aanvullende maatregelen nodig.

De belangrijkste tekortkomingen bevinden zich op het gebied van:

- Sterke authenticatie (MFA)
- Uitgebreide security logging
- Monitoring en auditing van toegang tot patiëntgegevens

Op basis van deze analyse worden de drie onderzochte controls beoordeeld als **gedeeltelijk aanwezig**.
