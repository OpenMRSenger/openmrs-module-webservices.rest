# Attack Surface Overzicht

## Doel

Het identificeren van alle ingangen (entry points) van de OpenMRS REST Web Services Module en het vaststellen van de belangrijkste risico's.

H = High Risk |
M/L = Medium/Low Risk

## Attack Surface

| Entry Point                            | Beschrijving                            | Impliciet Vertrouwen                    | Risico                             | Risk |
| -------------------------------------- | --------------------------------------- | --------------------------------------- | ---------------------------------- | --------- |
| REST API endpoints (/ws/rest/v1/\*)    | Externe toegang tot resources           | OpenMRS Core verzorgt autorisatie       | Broken Access Control, API abuse   | H         |
| HTTP Basic Authentication              | Authenticatie van gebruikers            | Credentials zijn geldig                 | Credential stuffing, brute force   | H         |
| Session endpoint (/ws/rest/v1/session) | Sessiebeheer                            | Sessies worden correct beheerd          | Session hijacking                  | H         |
| CRUD-operaties op patiëntgegevens      | Aanmaken, wijzigen en opvragen van data | Gebruiker heeft juiste rechten          | Datalekken                         | H         |
| User management endpoints              | Beheer van rollen en gebruikers         | Alleen admins hebben toegang            | Privilege escalation               | H         |
| Query parameters                       | Zoek- en filterfunctionaliteit          | Input is correct gevalideerd            | Injection, enumeration             | M/L       |
| JSON serialisatie/deserialisatie       | Omzetten tussen JSON en objecten        | Input is valide                         | Malformed input                    | M/L       |
| HTTP responses en foutmeldingen        | Terugkoppeling naar clients             | Exceptions bevatten geen gevoelige data | Information disclosure             | M/L       |
| Configuratieparameters                 | Module-instellingen                     | Configuratie is correct ingesteld       | Misconfiguratie                    | M/L       |
| Databaseconnectie                      | Opslag van patiëntgegevens              | Database is beveiligd                   | Datalekken                         | H         |
| Logging subsystem                      | Logging en auditing                     | Logs zijn beschikbaar en integer        | Onvoldoende detectie van aanvallen | M/L       |

## High Risk Entry Points

### REST API Endpoints

**Risico's**

- Broken Access Control
- API abuse
- Enumeration
- Data exposure

### HTTP Basic Authentication

**Risico's**

- Credential stuffing
- Brute-force aanvallen
- Password reuse

### Session Endpoint

**Risico's**

- Session hijacking
- Session fixation

### Patient CRUD Resources

**Risico's**

- Datalekken
- Privacy-inbreuken
- Ongeautoriseerde wijzigingen

### User Management Endpoints

**Risico's**

- Privilege escalation
- Misbruik van administratorrechten

## Vertrouwensrelaties (Trust)

De REST-module vertrouwt impliciet op:

| Component         | Vertrouwen                                    |
| ----------------- | --------------------------------------------- |
| OpenMRS Core      | Correcte authenticatie en autorisatie         |
| Database          | Integriteit en vertrouwelijkheid van gegevens |
| Logging subsystem | Beschikbaarheid en integriteit van logs       |
| Externe clients   | Correct geformatteerde requests               |
| Sessiebeheer      | Veilige opslag van sessies                    |

## Conclusie

De grootste attack surface bevindt zich in de publieke REST API, authenticatie, sessiebeheer en toegang tot patiëntgegevens. De module vertrouwt sterk op OpenMRS Core voor authenticatie en autorisatie. Hierdoor kunnen fouten of misconfiguraties in OpenMRS Core directe gevolgen hebben voor de beveiliging van de REST-module.
