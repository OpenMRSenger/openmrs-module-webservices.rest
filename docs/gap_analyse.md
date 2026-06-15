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

## Eis uit NEN 7510

Beveiligingsrelevante gebeurtenissen moeten worden geregistreerd zodat incidenten achteraf kunnen worden onderzocht.

## Huidige implementatie

OpenMRS registreert auditinformatie op objectniveau.

Voorbeeld:

```text
auditInfo
- creator
- dateCreated
- changedBy
- dateChanged
- voidedBy
```

Deze informatie wordt gekoppeld aan wijzigingen in gegevens.

## Bewijs

### AuditInfo-object

```text
auditInfo
- creator
- dateCreated
- changedBy
- dateChanged
- voidedBy
```

## Status

**Gedeeltelijk aanwezig**

## Gap

Geen aantoonbaar bewijs gevonden voor logging van:

- Succesvolle logins
- Mislukte logins
- REST API-calls
- Toegang tot patiëntgegevens
- Security incidents
- Privilege-escalaties

Ook ontbreekt bewijs voor:

- Centrale logverzameling
- SIEM-integratie
- Logretentiebeleid
- Bescherming tegen wijziging van logbestanden

## Benodigde maatregelen

1. Security logging toevoegen voor alle REST-calls.
2. Login success- en failure-events registreren.
3. Toegang tot patiëntgegevens loggen.
4. Logs integreren met SIEM/Syslog.
5. Logretentiebeleid vastleggen.
6. Monitoring en alerting implementeren.

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
