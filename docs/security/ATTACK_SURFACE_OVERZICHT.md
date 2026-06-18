# Attack Surface Overzicht

## Doel

Het identificeren van alle ingangen (entry points) van de OpenMRS REST Web Services Module en het vaststellen van de belangrijkste risico's.

## Attack Surface

| Entry Point                            | Beschrijving                            | Impliciet Vertrouwen                    | Risico                             | High Risk |
| -------------------------------------- | --------------------------------------- | --------------------------------------- | ---------------------------------- | --------- |
| REST API endpoints (/ws/rest/v1/\*)    | Externe toegang tot resources           | OpenMRS Core verzorgt autorisatie       | Broken Access Control, API abuse   | ✅        |
| HTTP Basic Authentication              | Authenticatie van gebruikers            | Credentials zijn geldig                 | Credential stuffing, brute force   | ✅        |
| Session endpoint (/ws/rest/v1/session) | Sessiebeheer                            | Sessies worden correct beheerd          | Session hijacking                  | ✅        |
| CRUD-operaties op patiëntgegevens      | Aanmaken, wijzigen en opvragen van data | Gebruiker heeft juiste rechten          | Datalekken                         | ✅        |
| User management endpoints              | Beheer van rollen en gebruikers         | Alleen admins hebben toegang            | Privilege escalation               | ✅        |
| Query parameters                       | Zoek- en filterfunctionaliteit          | Input is correct gevalideerd            | Injection, enumeration             | ⚠️        |
| JSON serialisatie/deserialisatie       | Omzetten tussen JSON en objecten        | Input is valide                         | Malformed input                    | ⚠️        |
| HTTP responses en foutmeldingen        | Terugkoppeling naar clients             | Exceptions bevatten geen gevoelige data | Information disclosure             | ⚠️        |
| Configuratieparameters                 | Module-instellingen                     | Configuratie is correct ingesteld       | Misconfiguratie                    | ⚠️        |
| Databaseconnectie                      | Opslag van patiëntgegevens              | Database is beveiligd                   | Datalekken                         | ✅        |
| Logging subsystem                      | Logging en auditing                     | Logs zijn beschikbaar en integer        | Onvoldoende detectie van aanvallen | ⚠️        |

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
