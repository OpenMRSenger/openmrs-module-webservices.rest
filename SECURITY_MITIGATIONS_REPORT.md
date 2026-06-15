# Security Mitigations & NEN 7510 Compliance Report

This report summarizes the programmatische changes implemented to address security vulnerabilities identified in the OpenMRS Webservices REST module. 

## Linked Reference Analyses
*   [BROKEN_ACCESS_CONTROL_BOWTIE_ANALYSIS.md](file:///C:/Users/gijsv/Downloads/BROKEN_ACCESS_CONTROL_BOWTIE_ANALYSIS.md)
*   [INJECTION_BOWTIE_ANALYSIS.md](file:///C:/Users/gijsv/Downloads/INJECTION_BOWTIE_ANALYSIS.md)
*   [NEN7510_COMPLIANCE_RAPPORT_NL.md](file:///C:/Users/gijsv/Downloads/NEN7510_COMPLIANCE_RAPPORT_NL.md)

---

## 1. Injection Vulnerabilities Resolved

### VULN-002: Reflected XSS in Swagger doc debugging
*   **Vulnerability**: Attacker could send javascript in `tag` parameter to execute in user browser session.
*   **Fix**: Added alphanumeric character checking whitelist (`^[a-zA-Z0-9_-]+$`) and added security response headers (`CSP`, `X-XSS-Protection`, `X-Content-Type-Options`).
*   **Modified File**: [SwaggerDocController.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/java/org/openmrs/module/webservices/rest/web/controller/SwaggerDocController.java)

### VULN-006: Stored XSS in help pages
*   **Vulnerability**: Resource properties and details loaded from modules were printed directly to HTML without escaping.
*   **Fix**: Replaced raw EL expressions `${...}` with XML-escaped `<c:out value="..." escapeXml="true" />` blocks.
*   **Modified Files**:
    *   [resources.jsp](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/webapp/resources.jsp)
    *   [searchResources.jsp](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/webapp/searchResources.jsp)

---

## 2. Broken Access Control & Authentication Vulnerabilities Resolved

### VULN-001: Unauthenticated Global Property Disclosure
*   **Vulnerability**: Anyone could search and read all global properties (names and values), exposing passwords and secrets.
*   **Fix**: Added `Context.requirePrivilege(RestConstants.PRIV_MANAGE_RESTWS)` checks to block unauthenticated access.
*   **Modified File**: [SettingsFormController.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/java/org/openmrs/module/webservices/rest/web/controller/SettingsFormController.java)

### VULN-003: Unauthorized Cache Eviction
*   **Vulnerability**: Any authenticated user could clear Hibernate database cache regions.
*   **Fix**: Added `Context.requirePrivilege(RestConstants.PRIV_MANAGE_RESTWS)` authorization check.
*   **Modified File**: [ClearDbCacheController2_0.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs2_0/ClearDbCacheController2_0.java)

### VULN-007: Weak Authentication (Cleartext Basic Auth)
*   **Vulnerability**: Basic authentication allowed over insecure cleartext HTTP.
*   **Fix**: Enforced HTTPS/SSL requirement checking (both direct secure check and reverse proxy `X-Forwarded-Proto`).
*   **Modified File**: [AuthorizationFilter.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java)

### VULN-008: Insecure IP Restriction Logic (Proxy Spoofing)
*   **Vulnerability**: Relied on `request.getRemoteAddr()`, easily spoofed or bypassed behind reverse proxies.
*   **Fix**: Implemented `X-Forwarded-For` header parsing to identify the correct remote client IP.
*   **Modified File**: [AuthorizationFilter.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java)

---

## 3. Configuration & Software Integrity Vulnerabilities Resolved

### VULN-004: Insecure Deserialization (XStream)
*   **Vulnerability**: XStream configured to autodetect annotations without restrictively limiting classes. Risk of Remote Code Execution (RCE).
*   **Fix**: Cleared default permissions using `NoTypePermission.NONE` and configured strict class whitelists allowing only module packages and basic JDK classes.
*   **Modified File**: [RestInit.java](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestInit.java)

### VULN-005: Stack Trace Exposure
*   **Vulnerability**: `enableStackTraceDetails` default was set to `true`, leaking system internal paths on error.
*   **Fix**: Changed property default value to `false`.
*   **Modified File**: [config.xml](file:///E:/School/Avans/Jaar%202_ICT/2.4/repos/openmrs-module-webservices.rest/omod/src/main/resources/config.xml)
