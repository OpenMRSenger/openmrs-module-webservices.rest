# OpenMRS REST Module - Logging & Audit Beveiligingsanalyse (NEN 7510-2:2024)

## 1. Inleiding
Dit document bevat de volledige inventarisatie, risicoanalyse en het mitigatieplan voor de logging- en auditfunctionaliteiten van de `openmrs-module-webservices.rest` module. Dit rapport is opgesteld conform de richtlijnen van **NEN 7510-2:2024 Control A.8.15 (Logging)**, de Algemene Verordening Gegevensbescherming (AVG/GDPR) en de HIPAA Security Rule.

Het doel van deze analyse is het in kaart brengen van tekortkomingen in de huidige logging-architectuur en het aandragen van direct toepasbare mitigatiemaatregelen en code-oplossingen om onweerlegbaarheid (immutability), consistentie, dataminimalisatie en volledige event-dekking te waarborgen.

---

## 2. Attack Surface & Event Audit Matrix

Onderstaande matrix koppelt kritieke API-gebeurtenissen (events) aan het aanvalsoppervlak (attack surface) en specificeert de huidige logging-status, eventuele risico's op het lekken van gevoelige gegevens en de NEN 7510-compliance status.

| Event / Endpoint | Attack Surface Context | Gelogd? | Gevoelige data in log? | Compliant met NEN-7510 A.8.15? |
| :--- | :--- | :--- | :--- | :--- |
| **Authenticatiepoging**<br>(Basic Auth via `AuthorizationFilter`) | Externe toegangspoort. Risico op brute-force en credential stuffing. | **Gedeeltelijk**<br>(Alleen op DEBUG-niveau via log4j/slf4j. Geen formele audit logs). | **Nee** (Wachtwoord zelf niet gelogd; wel risico bij debuggen exception traces). | **Nee**<br>(Mislukte inlogpogingen en lockouts moeten onweerlegbaar gelogd worden). |
| **Sessie beëindigen**<br>(DELETE `/session`) | Sessiebeheer. Risico op session hijacking of openstaande sessies. | **Nee** | **Nee** | **Nee** |
| **Inzien patiëntdossier**<br>(GET `/patient/{uuid}`) | Toegang tot Medische Gegevens (PHI). Risico op ongeautoriseerde data-inzage. | **Nee**<br>(Kritiek gat: dossierinzage wordt niet geregistreerd). | **Nee** (Bij debugging kan URL/query param PHI lekken). | **Nee**<br>(NEN 7510 vereist registratie van wie welk patiëntdossier heeft ingezien). |
| **Wijzigen patiëntdossier**<br>(POST/PUT/DELETE `/patient`) | Integriteit van Medische Gegevens. Risico op ongeoorloofde wijzigingen. | **Gedeeltelijk**<br>(Via OpenMRS Core database audit, maar zonder REST/IP context). | **Nee** | **Nee**<br>(REST-sessie context en client IP ontbreken in de audit logs). |
| **Wachtwoord wijzigen**<br>(POST `/changepassword`) | Privilegebeheer. Risico op Privilege Escalation en accountovername. | **Nee** | **Nee** (Wel risico op logging van parameters bij fouten). | **Nee**<br>(Wijzigingen in authenticatiemiddelen moeten altijd gelogd worden). |
| **Diagnostische info opvragen**<br>(GET `/session/diag`) | Systeeminformatie-lek. Risico op verkenning door aanvallers (reconnaissance). | **Nee** | **Ja** (Lekt interne rollen en server-metadata in HTTP response). | **Nee** |
| **Database cache legen**<br>(POST `/cleardbcache`) | Systeembeheer / Denial of Service. Risico op performance-degradatie. | **Gedeeltelijk**<br>(Alleen op DEBUG-niveau: "Clearing DB cache"). | **Nee** | **Nee**<br>(Beheerdershandelingen moeten onweerlegbaar gelogd worden). |
| **IP Block / IP Allowlist Falen**<br>(Filter blokkeert IP) | Toegangscontrole. Risico op brute-force of bypass pogingen. | **Nee**<br>(Stuurt direct 403 Forbidden retour zonder server-log). | **Nee** (IP-adres zelf is metadata die gelogd moet worden). | **Nee**<br>(Beveiligingsincidenten en weigeringen moeten gelogd worden). |

---

## 3. Analyse van niet-gelogde gebeurtenissen (Kritieke Gaten)

Het ontbreken van logging bij de volgende gebeurtenissen vormt het grootste beveiligings- en compliancerisico:

1.  **Gebrek aan dossierinzage logging (Patient Read Access)**:
    *   *Risico*: Een medewerker of aanvaller kan duizenden patiëntdossiers downloaden via `/ws/rest/v1/patient` zonder dat dit ergens wordt geregistreerd. Dit is een directe schending van de NEN 7510 en AVG/GDPR wetgeving.
2.  **Ontbreken van IP-blokkering en Brute-force logging**:
    *   *Risico*: Systemen kunnen langdurig onderworpen worden aan brute-force aanvallen op de Basic Auth filter zonder dat het SOC (Security Operations Center) of de beheerder gealarmeerd wordt, omdat weigeringen niet gelogd worden.
3.  **Geen logging van kritieke account-wijzigingen**:
    *   *Risico*: Ongeautoriseerde wachtwoordwijzigingen (zoals misbruik van `SEC-03`) worden niet geregistreerd, waardoor forensisch onderzoek achteraf onmogelijk is.

---

## 4. Het gat tussen Huidig en Gewenst (Gap Analysis)

| Aspect | Huidige Situatie (Gat) | Gewenste Situatie (NEN 7510 Compliant) |
| :--- | :--- | :--- |
| **Immutability (Onweerlegbaarheid)** | Logs staan in-memory (`MemoryAppender`) en worden gewist bij JVM-reboot. Lokale logbestanden zijn onbeveiligd en kunnen door een beheerder/hacker worden aangepast of gewist. | Auditlogs worden direct en asynchroon doorgestuurd naar een gecentraliseerde, alleen-lezen logserver (WORM / SIEM). Lokale bestanden hebben strenge OS-rechten. |
| **Formaat & Consistentie** | Logs zijn platte tekst. De `/serverlog` parser gebruikt regex om kolommen te splitsen, wat faalt zodra de Log4j layout-configuratie wijzigt. | Structured logging (JSON format) voor alle audit events. Dit garandeert dat velden altijd consistent geïnterpreteerd kunnen worden. |
| **Dataminimalisatie (Privacy)** | Stack traces worden bij fouten volledig getoond aan API-gebruikers (indien enabled) en logs kunnen onbewust HTTP-headers en passwords bevatten. | Automatische maskering van wachtwoorden en tokens. Foutmeldingen aan cliënten bevatten alleen een unieke referentiecode; de details staan veilig in de server-logs. |
| **Event Dekking** | Alleen systeeminitialisatie en fouten worden gelogd. Geen registratie van patiëntdata-inzage (GET verzoeken) of beveiligingsincidenten. | 100% dekking van alle CRUD-acties op medische endpoints, mislukte authenticaties, en beheeracties (zoals cache wissen). |

---

## 5. Technische Uitwerking van Mitigaties (Voorgestelde Code)

### 5.1 Implementatie van de Audit Log Interceptor (GET/POST Dossierinzage)
Om te voldoen aan NEN 7510, moet een Spring Interceptor worden toegevoegd die specifiek de REST API endpoints monitort en dossierinzage registreert in een aparte, beveiligde audit log.

```java
package org.openmrs.module.webservices.rest.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class RestAuditLogInterceptor extends HandlerInterceptorAdapter {
    
    private static final Logger auditLog = LoggerFactory.getLogger("REST_AUDIT_LOGGER");
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = request.getRemoteAddr();
        String user = Context.isAuthenticated() ? Context.getAuthenticatedUser().getUsername() : "ANONYMOUS";
        
        // Controleer of het verzoek medische gegevens raakt
        if (path.contains("/rest/v1/patient") || path.contains("/rest/v1/encounter") || path.contains("/rest/v1/obs")) {
            // Log in gestructureerd JSON formaat voor SIEM-ingest
            auditLog.info("{{\"timestamp\":\"{}\", \"event\":\"MEDICAL_DATA_ACCESS\", \"user\":\"{}\", \"ip\":\"{}\", \"method\":\"{}\", \"uri\":\"{}\"}}",
                java.time.Instant.now(), user, ipAddress, method, path);
        }
        return true;
    }
}
```

### 5.2 Aanpassen van RestUtil.wrapErrorResponse (Voorkomen van Datalekken)
Klasse-namen en regelnummers worden momenteel blootgesteld aan de client. Dit moet worden verborgen in productie.

```java
// Wijziging in RestUtil.java:
public static SimpleObject wrapErrorResponse(Exception ex, String reason) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    
    if (reason != null && !reason.isEmpty()) {
        map.put("message", reason);
    } else {
        map.put("message", "An unexpected error occurred.");
    }
    
    // Verwijder regelnummers en interne klassen uit API response voor productie
    map.put("code", "INTERNAL_ERROR"); 
    map.put("detail", "Details are recorded in server logs.");
    map.put("rawMessage", ""); // Leegmaken om query lekken te voorkomen
    
    return new SimpleObject().add("error", map);
}
```

---

## 6. Gedetailleerd Implementatie-Stappenplan

### Fase 1: Directe Acties & Hotfixes (Week 1)
- [ ] **Data Redactie in API Responses**: Pas `RestUtil.wrapErrorResponse` aan om blootstelling van klassennamen en stack traces direct te blokkeren voor alle REST API gebruikers.
- [ ] **DEBUG Log Opschoning**: Schakel het loggen van uitzonderingen (exceptions) in `AuthorizationFilter` uit voor productie, of filter exception-traces zodat credential-strings niet in plaintext logbestanden terechtkomen.
- [ ] **Beveilig Diagnostisch Endpoint**: Plaats `/session/diag` achter een expliciete beheerdersrolcheck.

### Fase 2: Audit Event Logging Implementatie (Week 1-2)
- [ ] **Implementeer `RestAuditLogInterceptor`**: Integreer de Spring interceptor (zie sectie 5.1) in de Spring Web-configuratie van de REST-module.
- [ ] **Dossierinzage Registratie**: Zorg ervoor dat elk GET-verzoek naar `/patient` en `/encounter` succesvol wordt vastgelegd inclusief de ID's van de opgevraagde resources.
- [ ] **IP Block & Security Alerts**: Voeg een waarschuwingslog (WARN) toe in `AuthorizationFilter` wanneer een IP-adres wordt geblokkeerd of wanneer Basic Auth decodering faalt.

### Fase 3: Infrastructuur & Onweerlegbaarheid (Week 2-3)
- [ ] **JSON Log Format**: Configureer Logback/Log4j om auditlogs weg te schrijven in JSON-formaat voor eenvoudige SIEM-parsing.
- [ ] **Log Centralisatie**: Configureer een `SyslogAppender` of forwarder (bijv. Filebeat) die logs realtime verstuurt naar een centrale log-accumulator (WORM of SIEM).
- [ ] **OS-Level Machtigingen**: Stel de directoryrechten van `/var/log/openmrs/` zo in dat alleen de openmrs-systeemservice schrijfrechten heeft en bewerkingsrechten voor administrators zijn uitgeschakeld (append-only).

### Fase 4: Validatie & Compliance Audit (Week 3-4)
- [ ] **Validatietests**: Voer integratietests uit die controleren of mislukte inlogs en dossierinzagen daadwerkelijk in de JSON-logs verschijnen.
- [ ] **Penetration Testing**: Test of het manipuleren van headers (zoals `X-Forwarded-For`) geen valse IP-informatie in de audit logs injecteert.
- [ ] **NEN 7510 Compliance Review**: Doorloop de auditmatrix met de compliance officer om formele goedkeuring te verkrijgen voor NEN 7510 Control A.8.15.
