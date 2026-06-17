# Security Audit: Risk Matrix

This document outlines the vulnerabilities identified during the security audit of the OpenMRS Webservices REST module.

## Summary Table

| ID | Vulnerability | Severity | Likelihood | Impact | Category | Location |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **VULN-001** | Unauthenticated Global Property Disclosure | **Critical** | High | Extreme | Broken Access Control | `SettingsFormController.java` |
| **VULN-002** | Reflected Cross-Site Scripting (XSS) | **High** | High | High | Injection | `SwaggerDocController.java` |
| **VULN-003** | Unauthorized Cache Eviction | **High** | Medium | High | Broken Access Control | `ClearDbCacheController2_0.java` |
| **VULN-004** | Insecure Deserialization (XStream) | **High** | Low | Extreme | Software/Data Integrity | `RestInit.java` |
| **VULN-005** | Stack Trace Exposure | **Medium** | High | Low | Insecure Design | `config.xml` |
| **VULN-006** | XSS in Help Pages | **Medium** | Medium | Medium | Injection | `searchResources.jsp` |
| **VULN-007** | Weak Authentication (Basic Auth) | **Medium** | Medium | High | Cryptographic Failures | `AuthorizationFilter.java` |
| **VULN-008** | Insecure IP Restriction Logic | **Low** | Low | Medium | Broken Access Control | `AuthorizationFilter.java` |

---

## Vulnerability Details

### VULN-001: Unauthenticated Global Property Disclosure
- **Description**: The `/module/webservices/rest/settings.form/search` endpoint lacks any authorization check. It enumerates and returns all global properties (names and values) matching a prefix.
- **Risk**: Any unauthenticated user can leak sensitive secrets like database passwords or API keys.

### VULN-002: Reflected Cross-Site Scripting (XSS)
- **Description**: The `tag` parameter in the `/apiDocs/debug` endpoint is reflected directly into the HTML response without sanitization.
- **Risk**: Attackers can execute arbitrary JavaScript in the victim's browser session.

### VULN-003: Unauthorized Cache Eviction
- **Description**: The `/cleardbcache` endpoint allows any authenticated user to clear Hibernate cache regions without specific administrative privileges.
- **Risk**: Potential Denial of Service (DoS) or performance degradation.

### VULN-004: Insecure Deserialization (XStream)
- **Description**: XStream is configured to autodetect annotations without an explicit class allow-list.
- **Risk**: Remote Code Execution (RCE) via malicious XML payloads if the environment is not otherwise hardened.

### VULN-005: Stack Trace Exposure
- **Description**: `enableStackTraceDetails` is set to `true` by default in `config.xml`.
- **Risk**: Leaks implementation details and internal server paths to users on error.

### VULN-006: XSS in Help Pages
- **Description**: Several JSP pages use EL expressions (e.g., `${resource.name}`) without escaping.
- **Risk**: Potential script injection if resource names or descriptions are user-controllable or sourced from malicious modules.

### VULN-007: Weak Authentication (Basic Auth over HTTP)
- **Description**: Relies on Basic Authentication which sends credentials in an easily reversible Base64 format.
- **Risk**: Credentials travel in cleartext if SSL/TLS is not strictly enforced.

### VULN-008: Insecure IP Restriction Logic
- **Description**: Uses `request.getRemoteAddr()` to enforce IP allow-lists.
- **Risk**: This can be easily spoofed or bypassed in environments with misconfigured reverse proxies.
