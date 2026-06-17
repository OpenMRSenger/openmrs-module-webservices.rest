# Injection Vulnerabilities - Bowtie Analysis (Tabular Format)

## Vulnerability Overview

**Hazard**: Processing and rendering user-controlled inputs/parameters in a healthcare application containing patient and administrative pages.

**Top Event**: Injection Attack Successfully Compromises Application (reflected/stored scripting or code injection).

| Top Event / Hazard | Inherent Likelihood | Inherent Impact | Overall Severity |
|---|---|---|---|
| **Injection Attack** | High | High | **High** |

| Vulnerability | Severity | Likelihood | Impact | Type |
|---|---|---|---|---|
| **VULN-002**: Reflected XSS | High | High | High | SwaggerDocController.java |
| **VULN-006**: XSS in Help Pages | Medium | Medium | Medium | searchResources.jsp |

---

## Bowtie Analysis Table

| **CAUSES (Threats)** | **PREVENTIONS (Left Barriers - Multiple Levels)** | **TOP EVENT** | **CONSEQUENCES (Right Impact)** | **MITIGATIONS (Right Barriers - Multiple Levels)** |
|---|---|---|---|---|
| **VULN-002**: Attacker crafts malicious input with JavaScript in `tag` parameter<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L1-CODE]** Input Validation: Whitelist acceptable characters and reject dangerous patterns<br>**[L3-FRAMEWORK]** Spring MVC parameter validation annotations<br>**[L4-INFRA]** WAF filtering rules for query string XSS signatures | **Injection Attack Successfully Compromises Application** | Arbitrary JavaScript execution in victim's browser context<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L1-OUTPUT]** Output Encoding: HTML-encode all data (`<` → `&lt;`, `"` → `&quot;`)<br>**[L3-CSP]** Content Security Policy: Restrict script execution to trusted sources only |
| **VULN-002**: User input accepted without validation in SwaggerDocController<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L2-REVIEW]** Security Code Review: Audit parameter handling in controllers<br>**[L2-TRAINING]** Developer Training: Secure coding standards for handling user input | | Session hijacking via cookie theft<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L3-COOKIE]** HTTP-Only Cookies: Set on all session cookies; Secure flag enabled<br>**[L2-MONITOR]** Log / SIEM detection of session hijacking anomalies |
| **VULN-002**: Input not sanitized before HTML rendering<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L3-FRAMEWORK]** Parameterized Templates: Use template engines with auto-escaping enabled | | Credential theft through phishing forms injected into page<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L3-COOKIE]** HTTP-Only Cookies: Set on all session cookies; Secure flag enabled |
| **VULN-002**: Dynamic content rendered without encoding<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L2-REVIEW]** Security Code Review: Mandatory audit of all parameter handling | | Malware distribution to other users accessing the application<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L3-CSP]** Content Security Policy: Restrict script execution to trusted sources only |
| **VULN-006**: Resource names/descriptions contain malicious scripts from modules<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L2-REVIEW]** Source Code Review: Find vulnerable EL expressions in JSP pages | | Data exfiltration of sensitive patient/medical information<br>• *Impact (Inherent): Critical*<br>• *Impact (Residual): Low* | **[L1-OUTPUT]** Context-Aware Encoding: HTML/JS/URL/CSS appropriate per context<br>**[L2-MONITOR]** Log / SIEM monitoring of large data read anomalies<br>**[L4-INCIDENT]** Data breach response protocol |
| **VULN-006**: EL expressions (`${resource.name}`) evaluated without escaping<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L1-CODE]** JSP Security Practices: Replace with JSTL `<c:out>` tags<br>**[L3-FRAMEWORK]** Configure JSP engine auto-escaping in server | | Account takeover and complete session compromise<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L4-INCIDENT]** Incident Response Plan: Detect, contain, and respond to attacks<br>**[L3-COOKIE]** Enforce HTTP-Only flags on admin and user session cookies |
| **VULN-006**: User-controllable content from modules not validated<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L1-CODE]** Module Registry: Input length and character sanitization for registered resources<br>**[L3-FRAMEWORK]** Use safe framework model objects and API schema validations | | Reputation damage & loss of user trust in application<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L4-NOTIFY]** Breach Notification Protocol: Fast user advisory and disclosure<br>**[L2-MONITOR]** Monitoring & Logging: Track suspicious pattern attempts and resource modifications |
| **VULN-006**: Trust in module-provided resource data without sanitization<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L4-INFRA]** Web Application Firewall (WAF): Detect and block XSS patterns | | Defacement of help pages and support documentation<br>• *Impact (Inherent): Medium*<br>• *Impact (Residual): Low* | **[L2-TEST]** Regular Security Testing: Penetration testing and vulnerability scanning |
| **No output encoding implemented**<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L2-TRAINING]** Developer Training: Secure coding and sanitization practices | | Cross-patient data leaks (help content accessible to unauthorized users)<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L4-NOTIFY]** User Education: Warn about clicking suspicious links |
| **Lack of contextual encoding awareness**<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L3-FRAMEWORK]** Framework DELETE/PURGE privilege validation in template engine | | Financial loss from security breaches or regulatory fines<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L4-NOTIFY]** Breach Notification Protocol: Alert affected users immediately |

---

## Threats & Likelihood Analysis (Left Side)

This table analyzes the likelihood of the threats that can trigger injection vulnerabilities, showing both inherent risk (without barriers) and residual risk (with prevention barriers active).

| Threat (Cause) | Inherent Likelihood | Residual Likelihood | Prevention Effectiveness | Key Prevention Barrier |
|---|---|---|---|---|
| **VULN-002: Malicious inputs in tag parameter** | High | Low | High | Input validation whitelists and WAF filtering rules |
| **VULN-002: Parameter handling in SwaggerDocController** | High | Low | High | Security code reviews and controller logic auditing |
| **VULN-002: Render without sanitization** | High | Low | High | Template engines with automatic HTML escaping enabled |
| **VULN-006: Malicious scripts in module resources** | Medium | Low | High | Source code reviews of expression languages (EL) in JSP files |
| **VULN-006: Escapeless evaluation of resource names** | Medium | Low | High | Replacing inline script calls with JSTL `<c:out>` |
| **No Output Encoding / Context Awareness** | High | Low | High | Developer training and context-aware encoding rules |

---

## Threat Mapping

### VULN-002 Threats (SwaggerDocController.java - Reflected XSS)
1. Attacker crafts malicious `tag` parameter with JavaScript
2. Parameter reflected directly into HTML without sanitization
3. Dynamic content rendered without encoding
4. No input validation or filtering

### VULN-006 Threats (searchResources.jsp - Stored XSS)
1. Malicious script in resource names/descriptions
2. EL expressions evaluated without escaping
3. Module-provided data trusted without validation
4. JSP uses `${resource.name}` instead of `<c:out>`

---

## Prevention Layers

| Layer | VULN-002 Prevention | VULN-006 Prevention | Combined Approach |
|---|---|---|---|
| **Input** | Whitelist characters in `tag` parameter | Validate resource names from modules | All user inputs must pass validation |
| **Processing** | Sanitize/remove dangerous characters | Escape EL expressions | Apply consistent sanitization logic |
| **Output** | HTML-encode reflected content | Use JSTL `<c:out>` with escapeXml | Context-aware encoding for all outputs |
| **Code Quality** | Security review of SwaggerDocController | Audit all JSP files for vulnerable EL | Mandatory security code reviews |
| **Infrastructure** | WAF rules for XSS patterns | Template engine auto-escaping | Multi-layer defense strategy |

---

## Consequences Analysis (Right Side)

This table analyzes the impact of the consequences resulting from injection attacks, showing both inherent impact (without barriers) and residual impact (with mitigation barriers active).

| Consequence | Inherent Impact | Residual Impact | Risk Level | HIPAA Impact | GDPR Impact |
|---|---|---|---|---|---|
| **Code execution in browser** | High | Low | **High** | Potential HIPAA breach (clinical integrity risk) | GDPR Right to Privacy Violation |
| **Session hijacking** | High | Low | **High** | Session token theft leading to breach | Stolen session identifiers |
| **Credential theft** | High | Low | **High** | Clinical account compromise | Unlawful processing potential |
| **Malware distribution** | High | Low | **High** | Integrity of healthcare workstation compromised | Unlawful processing |
| **Data exfiltration** | Critical | Low | **Critical** | Large-scale HIPAA PHI breach | Article 33 Notification Required |
| **Account takeover** | High | Low | **High** | System administration hijack | GDPR accountability violation |
| **Reputation damage** | High | Low | **High** | Loss of clinical/patient trust | Breach of brand credibility |
| **Defacement of help pages** | Medium | Low | **Medium** | Information accuracy compromise | Information availability issues |
| **Cross-patient data leaks** | High | Low | **High** | Multiple patient records exposed | GDPR data protection breach |
| **Financial loss / Regulatory fines** | High | Low | **High** | Non-compliance penalty risk | GDPR Administrative fines |

---

## Defense-in-Depth Control Framework

This section maps each defense layer with specific identifiers used in the Bowtie table above. Defense in depth requires multiple independent controls at different layers to prevent and detect attacks.

### PREVENTION LAYERS (Left Side - Stop Attack Before Execution)

| Layer ID | Layer Name | Control Type | Applicable Vulnerabilities | Specific Controls | Effectiveness |
|---|---|---|---|---|---|
| **L1-CODE** | Code-Level Input Controls | Preventive | VULN-002, VULN-006 | Whitelist validation, JSTL `<c:out>` tags, input sanitization | High - Stops attack at source |
| **L2-REVIEW** | Design & Code Review | Preventive | VULN-002, VULN-006 | Mandatory security code reviews, static analysis, threat modeling | High - Catches design flaws early |
| **L2-TRAINING** | Developer Training | Preventive | VULN-002, VULN-006 | Secure coding practices, OWASP top 10, framework best practices | Medium - Reduces human error |
| **L3-FRAMEWORK** | Framework-Level Controls | Preventive | VULN-002, VULN-006 | Auto-escaping templates, framework security configs, privilege validation | High - Systematic protection |
| **L4-INFRA** | Infrastructure Protection | Preventive | VULN-002, VULN-006 | WAF rules, network segmentation, secure configuration | Medium - Defense perimeter |

### MITIGATION LAYERS (Right Side - Detect & Contain Attacks)

| Layer ID | Layer Name | Control Type | Applicable Consequences | Specific Controls | Detection Time |
|---|---|---|---|---|---|
| **L1-OUTPUT** | Output Encoding | Corrective | JavaScript execution, data exfiltration | HTML/JS/URL/CSS context-aware encoding | Immediate (prevents rendering) |
| **L2-HEADERS** | Security Headers | Corrective | Code execution, session hijacking | CSP, X-XSS-Protection, X-Content-Type-Options | Immediate (browser enforced) |
| **L2-MONITOR** | Monitoring & Logging | Detective | All consequences | Log analysis, SIEM integration, anomaly detection | Seconds to minutes |
| **L2-TEST** | Security Testing | Detective | All threats & consequences | Penetration testing, automated scanners, XSS payload testing | Pre-deployment |
| **L3-COOKIE** | Session Security | Corrective | Session hijacking, credential theft | HTTP-Only flag, Secure flag, SameSite attribute | Immediate (prevents access) |
| **L3-CSP** | Content Security Policy | Corrective | Code execution, malware distribution | CSP headers restrict script origins, inline script blocking | Immediate (browser enforced) |
| **L4-ENCODING** | Advanced Encoding | Corrective | Data exfiltration, cross-patient leaks | Context-specific encoding (HTML/JS/URL/CSS), templating escapes | Immediate (prevents rendering) |
| **L4-INCIDENT** | Incident Response | Corrective | Account takeover, reputation damage | Detection procedures, containment steps, recovery playbooks | Minutes to hours |
| **L4-NOTIFY** | User Communication | Corrective | Financial loss, reputation damage | Breach notification, user education, security advisories | Hours to days |

---

### Defense-in-Depth Strategy by Vulnerability

#### VULN-002: SwaggerDocController Reflected XSS

**Prevention Chain (Left):**
1. **L1-CODE:** Whitelist validation on `tag` parameter input
2. **L2-REVIEW:** Security code review of SwaggerDocController
3. **L3-FRAMEWORK:** Use parameterized response builders
4. **L4-INFRA:** WAF rules for XSS patterns

**Mitigation Chain (Right):**
1. **L1-OUTPUT:** HTML-encode all output before rendering
2. **L2-HEADERS:** Apply X-XSS-Protection & CSP headers
3. **L3-CSP:** Restrict inline script execution
4. **L2-MONITOR:** Log and alert on suspicious tag parameters

#### VULN-006: searchResources.jsp Stored XSS

**Prevention Chain (Left):**
1. **L1-CODE:** Replace `${resource.name}` with `<c:out value="${resource.name}" escapeXml="true"/>`
2. **L2-REVIEW:** Audit all JSP files for vulnerable EL expressions
3. **L3-FRAMEWORK:** Enable auto-escaping in JSP configuration
4. **L2-TRAINING:** Train developers on JSTL security practices

**Mitigation Chain (Right):**
1. **L4-ENCODING:** Context-aware encoding for all resource fields
2. **L2-MONITOR:** Monitor access patterns to help pages
3. **L2-TEST:** Regular penetration testing on JSP endpoints
4. **L4-NOTIFY:** Document findings for help page updates

---

### Cross-Layer Attack Scenario: Stored XSS via Resource Help Page

An attacker attempts to inject XSS through the help page resource names:

```
Attack Path:
┌─ L1-CODE (Fails: <c:out> escapes malicious input)
├─ L3-FRAMEWORK (Fails: auto-escaping blocks script)
├─ L2-REVIEW (Fails: code review caught vulnerability before deployment)
└─ L4-INFRA (Fails: WAF blocks XSS signature)

IF ALL PREVENTION LAYERS FAIL:
   ↓
   L1-OUTPUT (Fails: HTML encoding prevents script execution)
   ├─ L2-HEADERS (Fails: X-XSS-Protection blocks script)
   ├─ L3-CSP (Fails: CSP prevents inline script execution)
   └─ L2-MONITOR (Detects & alerts: Suspicious pattern logged)
   
RESULT: Attack contained; incident response triggered
```

---

## Quick Remediation Checklist

### Phase 1: Immediate (VULN-002 - High Priority)
- [ ] Implement HTML output encoding for `tag` parameter in SwaggerDocController
- [ ] Add input validation whitelist for SwaggerDocController
- [ ] Apply security headers: `X-XSS-Protection`, `CSP`, `X-Content-Type-Options`
- [ ] Conduct urgent code review of SwaggerDocController

### Phase 2: High Priority (VULN-006 - Medium Priority)
- [ ] Replace all `${resource.name}` with `<c:out value="${resource.name}" escapeXml="true"/>` in searchResources.jsp
- [ ] Audit all EL expressions in searchResources.jsp
- [ ] Enable auto-escaping in JSP template engine
- [ ] Add unit tests for XSS prevention in resource rendering

### Phase 3: Ongoing (Both)
- [ ] Deploy WAF rules for XSS detection
- [ ] Implement monitoring for suspicious input patterns
- [ ] Conduct penetration testing with XSS payloads
- [ ] Train developers on secure coding practices
- [ ] Regular security testing for injection vulnerabilities
