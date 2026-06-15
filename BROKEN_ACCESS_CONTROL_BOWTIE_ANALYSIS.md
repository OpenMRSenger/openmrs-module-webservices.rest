# Broken Access Control - Bowtie Analysis (Tabular Format)

## Vulnerability Overview

**Top Event**: Broken Access Control

---

## Bowtie Analysis Table

| **CAUSES (Threats - Left)** | **PREVENTIONS (Left Barriers - CRITICAL)** | **TOP EVENT** | **CONSEQUENCES (Right Impact)** | **MITIGATIONS (Right Barriers - CRITICAL)** |
|---|---|---|---|---|
| **Missing Privilege Enforcement** | **[L1-CODE]** @RequiresPrivilege annotations on all resources<br>**[L2-REVIEW]** Mandatory security code reviews<br>**[L3-FRAMEWORK]** Framework-enforced authorization checks<br>**[L4-INFRA]** Fine-grained privilege database constraints | **Broken Access Control - Multiple authorization flaws allowing users to bypass privilege checks** | **Unauthorized data exposure** - Medical records accessed by non-authorized staff | **[L1-OUTPUT]** Role-based filtering on all responses<br>**[L2-MONITOR]** Access pattern monitoring & anomaly detection<br>**[L3-AUDIT]** Immutable audit logs of access<br>**[L4-INCIDENT]** Breach response & notification protocol |
| **No Ownership Validation** | **[L1-CODE]** Ownership checks in all CRUD operations<br>**[L2-REVIEW]** Unit tests verifying ownership enforcement<br>**[L3-FRAMEWORK]** @PreExecute hook validating resource owner | | **Privilege escalation to Admin** - Full system breach | **[L1-OUTPUT]** Response filtering by ownership<br>**[L3-AUDIT]** Log all ownership validation attempts<br>**[L4-INCIDENT]** Immediate access revocation procedures<br>**[L2-MONITOR]** Detect anomalous user role changes |
| **Unfiltered Search Results** | **[L1-CODE]** Privilege filter in base search repository<br>**[L2-REVIEW]** Code linting rules enforcing filters<br>**[L3-FRAMEWORK]** Query interceptors adding privilege predicates<br>**[L4-INFRA]** Database row-level security (RLS) constraints | | **Patient record tampering** - Allergies/meds modified → wrong treatment given | **[L1-OUTPUT]** Verify search results against user privileges<br>**[L3-AUDIT]** Track all search queries & accessed records<br>**[L2-MONITOR]** Detect bulk searches, unusual result set sizes |
| **No Audit Immutability** | **[L1-CODE]** Append-only audit table design<br>**[L4-INFRA]** Database constraints preventing DELETE on audit | | **Mass data exfiltration** - Entire patient database downloaded | **[L3-AUDIT]** Cryptographic signatures on audit entries<br>**[L2-MONITOR]** Alert on DELETE attempts against audit table<br>**[L1-OUTPUT]** Backup & recovery capability<br>**[L4-INCIDENT]** Data breach forensics & restoration |
| **Missing Sub-Resource Checks** | **[L1-CODE]** Parent resource ownership validation before child access<br>**[L3-FRAMEWORK]** @PreExecute hook for nested resource checks | | **Audit trail destruction** - Evidence erased → compliance violation | **[L3-AUDIT]** Write-once audit logs with signatures<br>**[L2-MONITOR]** Detect audit deletion attempts |
| **Unprotected Admin Ops** | **[L1-CODE]** Explicit @RequiresPrivilege(DELETE/PURGE) annotations<br>**[L3-FRAMEWORK]** Framework-level privilege enforcement<br>**[L4-INFRA]** Multi-approval workflow for destructive ops<br>**[L2-REVIEW]** Security review of all admin operations | | **Cross-patient data leaks** - One patient accesses another's records | **[L1-OUTPUT]** Verify admin operations are scoped correctly<br>**[L3-AUDIT]** Log all DELETE/PURGE operations with context<br>**[L2-MONITOR]** Alert on unusual admin activity<br>**[L4-NOTIFY]** Notify affected patients of access incidents |

---

## Prevention Layers

| Layer | Threat Prevention | Combined Approach |
|---|---|---|
| **Input** | Validate principals/roles in all authorization decisions | All authorization decisions must reference privilege database |
| **Processing** | Apply decorators/annotations to enforce authorization before business logic | Systematic permission checking at framework level |
| **Output** | Filter response data by user's privileges | Only return data user is authorized to access |
| **Code Quality** | Mandatory security reviews of authorization logic | Security-first code review for all access control |
| **Infrastructure** | Database constraints enforce privilege boundaries | Multi-layer data protection |

---

## Consequences Analysis

| Consequence | Risk Level | HIPAA Impact | GDPR Impact |
|---|---|---|---|
| Unauthorized data exposure | **Critical** | HIPAA Breach | GDPR Right to Privacy Violation |
| Privilege escalation to Admin | **Critical** | Complete Compliance Failure | Personal Data Processing Breach |
| Patient record tampering | **Critical** | Care Delivery Risk + Breach | Data Integrity Violation |
| Mass data exfiltration | **Critical** | Large-Scale Breach | Article 33 Notification Required |
| Audit trail destruction | **Critical** | Non-Repudiation Failure | Accountability Violation |
| Cross-patient data leaks | **Critical** | Multi-Patient Breach | Unlawful Processing |

---

## Defense-in-Depth Control Framework

This section maps each defense layer with specific identifiers. Defense in depth requires multiple independent controls at different layers to prevent and detect broken access control attacks.

### PREVENTION LAYERS (Left Side - Stop Attack Before Execution)

| Layer ID | Layer Name | Control Type | Applicable Threats | Specific Controls | Effectiveness |
|---|---|---|---|---|---|
| **L1-CODE** | Code-Level Authorization Controls | Preventive | All (CRITICAL) | @RequiresPrivilege annotations, ownership checks in code, authorization decorators | High - Stops unauthorized access at source |
| **L2-REVIEW** | Design & Code Review | Preventive | Missing Privilege, No Ownership, Unfiltered Search, Unprotected Admin (CRITICAL) | Mandatory security reviews of authorization logic, test coverage for permission checks, threat modeling | High - Catches authorization flaws before deployment |
| **L2-TRAINING** | Developer Training | Preventive | All (RECOMMENDED) | Secure coding for authorization, OWASP broken access control, framework best practices | Medium - Reduces authorization logic errors |
| **L3-FRAMEWORK** | Framework-Level Controls | Preventive | Missing Privilege, No Ownership, Unfiltered Search, Unprotected Admin (CRITICAL); Sub-Resource Checks (RECOMMENDED) | @PreExecute hooks, query interceptors, centralized permission validation, decorators | High - Systematic authorization enforcement |
| **L4-INFRA** | Infrastructure Protection | Preventive | Unfiltered Search, Unprotected Admin (CRITICAL); Audit Immutability (CRITICAL); Missing Privilege (RECOMMENDED) | Database row-level security (RLS), privilege table constraints, network segmentation, firewall rules, DB triggers | Medium - Defense perimeter protection |

### MITIGATION LAYERS (Right Side - Detect & Contain Attacks)

| Layer ID | Layer Name | Control Type | Applicable Consequences | Specific Controls | Detection Time |
|---|---|---|---|---|---|
| **L1-OUTPUT** | Response Data Filtering | Corrective | Unauthorized Access, Data Leaks | Filter response data by user privileges, verify ownership before returning data | Immediate (prevents data leak) |
| **L2-MONITOR** | Monitoring & Logging | Detective | All consequences | Log all authorization attempts/denials, SIEM integration, anomaly detection for unusual access patterns | Seconds to minutes |
| **L3-AUDIT** | Audit Logging & Immutability | Detective | All (especially Audit Destruction) | Write-once audit logs, track all access/modifications, cryptographic signatures, tamper detection | Immediate (immutable record) |
| **L4-INCIDENT** | Incident Response | Corrective | All consequences | Detect privilege escalation, contain unauthorized access, revoke compromised credentials | Minutes to hours |
| **L4-NOTIFY** | User Communication | Corrective | Cross-patient leaks, Reputation damage | Breach notification, affected patient alerts, regulatory reporting | Hours to days |

### Defense-in-Depth Strategy by Threat

#### Threat 1: Missing Privilege Enforcement

**Prevention Chain (Left):**
1. **L1-CODE:** Mandatory @RequiresPrivilege annotations on all API resources *(CRITICAL)*
2. **L2-REVIEW:** Security code review verifying privilege checks on every method *(CRITICAL)*
3. **L3-FRAMEWORK:** Framework decorator enforcement of @RequiresPrivilege before method execution *(CRITICAL)*
4. **L4-INFRA:** Database privilege table constraints backing up framework checks *(CRITICAL)*

**Mitigation Chain (Right):**
1. **L1-OUTPUT:** Filter response objects by user's granted privileges *(CRITICAL)*
2. **L2-MONITOR:** Log all authorization denials and detect repeated failed attempts *(CRITICAL)*
3. **L3-AUDIT:** Write to immutable audit log of all access attempts *(CRITICAL)*
4. **L4-INCIDENT:** Alert on repeated privilege check failures; trigger access review *(CRITICAL)*

#### Threat 2: No Ownership Validation

**Prevention Chain (Left):**
1. **L1-CODE:** Ownership validation in all retrieve/update/delete operations *(CRITICAL)*
2. **L2-REVIEW:** Unit test requirements verifying ownership enforcement *(CRITICAL)*
3. **L3-FRAMEWORK:** @PreExecute hook validating resource owner before CRUD op *(CRITICAL)*
4. **L4-INFRA:** Database constraints ensuring ownership relationship exists *(RECOMMENDED)*

**Mitigation Chain (Right):**
1. **L1-OUTPUT:** Response validation ensuring only owner sees data *(CRITICAL)*
2. **L3-AUDIT:** Track all ownership validation attempts (pass/fail) *(CRITICAL)*
3. **L2-MONITOR:** Alert on ownership validation failures (privilege escalation attempts) *(CRITICAL)*
4. **L4-INCIDENT:** Immediate investigation and access revocation *(CRITICAL)*

#### Threat 3: Unfiltered Search Results

**Prevention Chain (Left):**
1. **L1-CODE:** Privilege predicate added in base search repository class *(CRITICAL)*
2. **L2-REVIEW:** Code linting rules enforcing privilege filters on all searches *(CRITICAL)*
3. **L3-FRAMEWORK:** Query interceptors automatically adding privilege WHERE clauses *(CRITICAL)*
4. **L4-INFRA:** Database row-level security (RLS) policies limiting query result set *(CRITICAL)*

**Mitigation Chain (Right):**
1. **L1-OUTPUT:** Response filtering verifying search results respect privileges *(CRITICAL)*
2. **L2-TEST:** Regular penetration testing attempting privilege bypass on search *(RECOMMENDED)*
3. **L3-AUDIT:** Track all searches and records accessed *(CRITICAL)*
4. **L2-MONITOR:** Detect anomalies (bulk searches, unusual result set sizes) *(CRITICAL)*

#### Threat 4: No Audit Immutability

**Prevention Chain (Left) - Integrity & Non-Repudiation:**
1. **L1-CODE:** Append-only audit table schema (no UPDATE/DELETE operations allowed) *(CRITICAL)*
2. **L4-INFRA:** Database trigger preventing DELETE operations on audit table + backup audit database *(CRITICAL)*
3. **L3-FRAMEWORK:** Separate audit system writing to isolated DB; no app-level DELETE capability *(RECOMMENDED)*
4. **L2-REVIEW:** Security review of audit system design *(RECOMMENDED)*

**Mitigation Chain (Right) - Detection & Recovery:**
1. **L3-AUDIT:** Cryptographic signatures on audit entries (detect tampering) *(CRITICAL)*
2. **L2-MONITOR:** Alert on DELETE attempts against audit table *(CRITICAL)*
3. **L1-OUTPUT:** Maintain backup databases of all audit records *(CRITICAL)*
4. **L4-INCIDENT:** Restore audit from backup; forensic analysis of tampering *(CRITICAL)*

#### Threat 5: Missing Sub-Resource Checks

**Prevention Chain (Left):**
1. **L1-CODE:** Parent resource ownership validation before child resource access *(CRITICAL)*
2. **L3-FRAMEWORK:** @PreExecute hook verifying parent->child privilege path *(CRITICAL)*
3. **L2-REVIEW:** Security audit of all sub-resource endpoints (e.g., /patient/{id}/allergies) *(RECOMMENDED)*
4. **L4-INFRA:** Not directly applicable (code-level concern) *(N/A)*

**Mitigation Chain (Right):**
1. **L3-AUDIT:** Track parent-child authorization decisions *(CRITICAL)*
2. **L2-MONITOR:** Detect attempts to access sub-resources via URL manipulation *(CRITICAL)*
3. **L4-INCIDENT:** Log and alert on sub-resource authorization failures *(RECOMMENDED)*

#### Threat 6: Unprotected Admin Operations

**Prevention Chain (Left):**
1. **L1-CODE:** Explicit @RequiresPrivilege(ADMIN) on DELETE/PURGE operations *(CRITICAL)*
2. **L3-FRAMEWORK:** Framework enforces administrative privilege check before destructive ops *(CRITICAL)*
3. **L4-INFRA:** Multi-approval workflow for DELETE/PURGE operations *(CRITICAL)*
4. **L2-REVIEW:** Security review of all DELETE/PURGE implementations *(CRITICAL)*

**Mitigation Chain (Right):**
1. **L1-OUTPUT:** Verify admin operations are scoped to correct resource *(CRITICAL)*
2. **L3-AUDIT:** Log all DELETE/PURGE with context (who, what, when, why) *(CRITICAL)*
3. **L2-MONITOR:** Alert on unusual DELETE/PURGE patterns *(CRITICAL)*
4. **L4-NOTIFY:** Trace DELETE/PURGE impact; notify affected patients *(CRITICAL)*

### Cross-Layer Attack Scenario: Privilege Escalation Attempt

An attacker attempts to escalate from regular user to admin to access protected patient records:

```
Attack Path:
┌─ L1-CODE (Fails: @RequiresPrivilege(ADMIN) blocks access)
├─ L2-REVIEW (Fails: code review caught missing admin check)
├─ L3-FRAMEWORK (Fails: @PreExecute decorator enforces permission)
└─ L4-INFRA (Fails: database privilege constraints prevent escalation)

IF ALL PREVENTION LAYERS FAIL:
   ↓
   L1-OUTPUT (Fails: response filtering removes unauthorized fields)
   ├─ L3-AUDIT (Detects: escalation attempt logged with context)
   ├─ L2-MONITOR (Alerts: repeated failed privilege checks trigger alarm)
   ├─ L3-AUDIT (Tracks: complete attack timeline in audit log)
   └─ L4-INCIDENT (Responds: access revoked, credentials invalidated)
   
RESULT: Attack blocked and contained; incident response activated
```

---

## Quick Remediation Checklist

### Phase 1: Critical Controls (Immediate)
- [ ] Audit all REST resources for missing @RequiresPrivilege annotations
- [ ] Add ownership validation to all retrieve/update/delete operations
- [ ] Implement @PreExecute hook for authorization checking
- [ ] Add database constraints preventing DELETE on audit table
- [ ] Implement privilege filter in base search repository

### Phase 2: Prevention Layers (Week 1-2)
- [ ] Mandatory security code review process for all authorization logic
- [ ] Unit test requirements verifying ownership/privilege enforcement
- [ ] Deploy framework-level query interceptors adding privilege predicates
- [ ] Implement database row-level security (RLS) policies
- [ ] Setup automated code linting rules for privilege checks

### Phase 3: Detection & Response (Week 2-3)
- [ ] Deploy monitoring for repeated authorization failures
- [ ] Implement audit log immutability with cryptographic signatures
- [ ] Setup SIEM integration for authorization anomaly detection
- [ ] Create incident response procedures for privilege escalation
- [ ] Develop breach notification workflow for affected patients

### Phase 4: Infrastructure & Compliance (Week 3-4)
- [ ] Encrypt audit logs at rest and in transit
- [ ] Setup backup audit database for disaster recovery
- [ ] Implement multi-approval workflow for DELETE/PURGE operations
- [ ] Train developers on secure authorization patterns
- [ ] Conduct penetration testing on authorization logic
