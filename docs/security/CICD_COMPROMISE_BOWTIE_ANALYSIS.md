# CI/CD Pipeline Compromise - Bowtie Analysis (Tabular Format)

## Vulnerability Overview

**Hazard**: Operation of a software build, test, and release pipeline (CI/CD) for a clinical healthcare application (OpenMRS REST Module) handling Protected Health Information (PHI) and clinical records.

**Top Event**: CI/CD Pipeline Compromise (Unauthorized access, control, or modification of the build, test, or deployment workflow/infrastructure).

| Top Event / Hazard | Inherent Likelihood | Inherent Impact | Overall Severity |
|---|---|---|---|
| **CI/CD Pipeline Compromise** | Medium | Extreme | **Critical** |

---

## Bowtie Analysis Table

| **CAUSES (Threats - Left)** | **PREVENTIONS (Left Barriers - CRITICAL)** | **TOP EVENT** | **CONSEQUENCES (Right Impact)** | **MITIGATIONS (Right Barriers - CRITICAL)** |
|---|---|---|---|---|
| **Compromised Secrets & Tokens**<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Low* | **[L3-SECRETS]** Implement short-lived OpenID Connect (OIDC) authentication for cloud providers<br>**[L3-SECRETS]** Mandatory automated secret scanning (e.g., GitGuardian) on all commits<br>**[L3-SECRETS]** Enforce secret masking and access restrictions in pipeline environments | **CI/CD Pipeline Compromise - Attackers gain write access, configuration control, or execution capability in build workflows** | **Deployment of backdoored software** - Malicious code/dependencies injected into production releases (SolarWinds style)<br>• *Impact (Inherent): Critical*<br>• *Impact (Residual): Low* | **[L1-SIGNING]** Cryptographic signing of artifacts (e.g., Sigstore/Cosign) during build<br>**[L1-SIGNING]** Mandatory signature verification in production/deployment environments before execution |
| **Over-Privileged Pipeline Permissions**<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L2-CONFIG]** Restrict default runner permissions (e.g., `contents: read` in GitHub workflows)<br>**[L2-CONFIG]** Disallow automatic execution of workflows for unapproved external forks<br>**[L2-CONFIG]** Linter validation of all CI/CD configuration files | | **Exfiltration of sensitive credentials** - Database passwords, TLS keys, and cloud API tokens leaked to external servers<br>• *Impact (Inherent): Critical*<br>• *Impact (Residual): Low* | **[L3-EGRESS]** Restrict build runner network access to whitelisted domains only<br>**[L2-MONITOR]** Log analysis and anomaly detection of outbound runner traffic<br>**[L4-INCIDENT]** Immediate credentials revocation and rotation procedure |
| **Dependency Confusion & Upstream Supply Chain**<br>• *Likelihood (Inherent): High*<br>• *Likelihood (Residual): Medium* | **[L3-SCAN]** Enforce strict dependency pinning (hashes/commit shas instead of version ranges)<br>**[L3-SCAN]** Software Composition Analysis (SCA) & vulnerability scanning on every build<br>**[L4-INFRA]** Private registry mirrors with strict package allow-listing | | **Unauthorized production deployment** - Attackers deploy rogue assets, cloud environments, or cryptominers<br>• *Impact (Inherent): Critical*<br>• *Impact (Residual): Low* | **[L4-INCIDENT]** Immediate teardown and isolation of affected environments<br>**[L2-MONITOR]** Alerts on unexpected deployment actions or unauthorized resource spawning |
| **Runner Infrastructure Compromise (Host Escape)**<br>• *Likelihood (Inherent): Low*<br>• *Likelihood (Residual): Low* | **[L4-INFRA]** Use isolated, short-lived, single-use ephemeral build runners<br>**[L4-INFRA]** Harden self-hosted runner operating systems and apply regular patches<br>**[L4-INFRA]** Disable root/administrator execution inside runner containers | | **Disruption of operations & build service** - Deletion of build environments, pipeline lockout, or ransomware<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L5-AUDIT]** Store pipeline log files in immutable, read-only remote logging locations<br>**[L4-INCIDENT]** Rapid rebuild capabilities from verified infrastructure-as-code templates |
| **Insider Threat or Hijacked Developer Account**<br>• *Likelihood (Inherent): Medium*<br>• *Likelihood (Residual): Low* | **[L5-REVIEW]** Strict branch protection rules requiring at least two independent code approvals<br>**[L5-REVIEW]** Enforce signed commits (PGP/SSH signatures) to ensure non-repudiation<br>**[L5-REVIEW]** Alerts on any workflow/CI configuration changes in pull requests | | **Compliance failure & regulatory penalties** - Violation of GDPR, HIPAA, and NEN 7510 rules regarding software integrity<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L5-AUDIT]** Automated Software Bill of Materials (SBOM) generation on every build<br>**[L4-INCIDENT]** Post-incident forensic analysis and reporting templates |
| **Unauthenticated Artifact Registry Access**<br>• *Likelihood (Inherent): Low*<br>• *Likelihood (Residual): Low* | **[L3-SECRETS]** Enforce strong authentication (MFA/IP restriction) on Maven Central / Nexus / npm registries<br>**[L4-INFRA]** Read-only registry endpoints for public packages and write-only via build system tokens | | **Reputation damage & loss of customer trust** - System compromised publicly via official updates<br>• *Impact (Inherent): High*<br>• *Impact (Residual): Low* | **[L4-NOTIFY]** Standardized communication templates for vendor/partner disclosure<br>**[L4-NOTIFY]** Quick release of clean, patched updates under emergency protocol |

---

## Threats & Likelihood Analysis (Left Side)

This table analyzes the likelihood of the threats that can trigger a CI/CD compromise, showing both inherent risk (without barriers) and residual risk (with prevention barriers active).

| Threat (Cause) | Inherent Likelihood | Residual Likelihood | Prevention Effectiveness | Key Prevention Barrier |
|---|---|---|---|---|
| **Compromised Secrets & Tokens** | High | Low | High | Short-lived OIDC keys & automated secret scanning |
| **Over-Privileged Pipeline Permissions** | Medium | Low | High | Default-deny token scopes & Yaml linters |
| **Dependency Confusion & Upstream Supply Chain** | High | Medium | Medium | SCA scanning, strict dependency hash pinning |
| **Runner Infrastructure Compromise (Host Escape)** | Low | Low | High | Ephemeral short-lived single-use runner VMs |
| **Insider Threat or Hijacked Developer Account** | Medium | Low | High | Mandatory branch protection & signed commits |
| **Unauthenticated Artifact Registry Access** | Low | Low | High | MFA, IP restrictions & write-only tokens |

---

## Prevention Layers

| Layer | Threat Prevention | Combined Approach |
|---|---|---|
| **Secrets Management** | Protect and restrict access to pipeline credentials | Move to zero long-lived secrets using short-lived OIDC federation |
| **Workflow Configuration** | Design workflows with least-privilege permissions | Strict default-deny rules, restricting write permissions globally |
| **Dependency Security** | Prevent malicious packages and dependency hijacking | Require lockfiles, hash pinning, and local package mirrors |
| **Infrastructure Hardening** | Isolate and protect build hosts and runners | Ephemeral runner architecture with clean environments per build |
| **Governance & Approval** | Prevent unauthorized modifications to codebase and configurations | Multi-party reviews and signed commits required for code progression |

---

## Consequences Analysis (Right Side)

This table analyzes the impact of the consequences resulting from a CI/CD compromise, showing both inherent impact (without barriers) and residual impact (with mitigation barriers active).

| Consequence | Inherent Impact | Residual Impact | Risk Level | HIPAA Impact | GDPR Impact | NEN 7510:2024 Impact |
|---|---|---|---|---|---|---|
| **Deployment of backdoored software** | Critical | Low | **Critical** | HIPAA Breach (clinical integrity compromised) | GDPR Right to Privacy Violation | Code and System Integrity (A.12.2.1 / A.12.5.1) |
| **Exfiltration of sensitive credentials** | Critical | Low | **Critical** | Secrets exposure leading to medical record breach | Personal Data Processing Compromise | Cryptographic Controls & Key Protection (A.10.1.1) |
| **Unauthorized production deployment** | Critical | Low | **Critical** | Clinical availability risk / Data compromise | Unlawful processing and profiling | Operations Security & System Audits (A.12.1.1) |
| **Disruption of operations & build service** | High | Low | **High** | Operational outage, care delivery delayed | Service availability issues | Information Security Continuity (A.17.1.1) |
| **Compliance failure & regulatory penalties** | High | Low | **High** | Loss of HIPAA certification | Administrative fines up to €20M / 4% global revenue | Compliance with Legal & Security Requirements (A.18.2.1) |
| **Reputation damage & loss of trust** | High | Low | **High** | Patient trust violation | Loss of corporate credibility | Relationship Security & Communication (A.13.2.1) |

---

## Defense-in-Depth Control Framework

This section maps each defense layer with specific identifiers. Defense in depth requires multiple independent controls at different layers to prevent and detect CI/CD pipeline compromise attacks.

### PREVENTION LAYERS (Left Side - Stop Attack Before Execution)

| Layer ID | Layer Name | Control Type | Applicable Threats | Specific Controls | Effectiveness |
|---|---|---|---|---|---|
| **L1-REVIEW** | PR & Commit Governance | Preventive | Insider Threat, Hijacked Accounts (CRITICAL) | Branch protection, mandatory two-person review, signed commits, alert on YAML edits | High - Stops malicious commits entering the main codebase |
| **L2-CONFIG** | Workflow Hardening | Preventive | Over-privileged permissions, Runner Host Escape (CRITICAL) | Default-deny token permissions (`contents: read`), pull request check constraints | High - Limits blast radius of a compromised runner |
| **L3-SECRETS** | Secrets & Access Management | Preventive | Compromised Secrets, Registry Access (CRITICAL) | short-lived OIDC authentication, secret masking, automated secret scanning | High - Eliminates long-lived static API keys / credentials |
| **L3-SCAN** | Dependency & Vulnerability Scan | Preventive | Dependency Confusion, Supply Chain (CRITICAL) | SCA dependency scanning, hash pinning, local Maven registry mirroring | Medium - Identifies vulnerable or malicious packages |
| **L4-INFRA** | Runner & Host Hardening | Preventive | Runner Escape, Registry Access (CRITICAL) | Ephemeral build runners, rootless container executions, OS security updates | High - Prevents persistent runner infection and host escape |

### MITIGATION LAYERS (Right Side - Detect & Contain Attacks)

| Layer ID | Layer Name | Control Type | Applicable Consequences | Specific Controls | Detection Time |
|---|---|---|---|---|---|
| **L1-SIGNING** | Artifact Signing & Verification | Corrective | Backdoored software deployment | PGP/Sigstore signing of built artifacts, admission controllers verifying signatures | Immediate (prevents execution of unsigned builds) |
| **L2-MONITOR** | Runner & Traffic Monitoring | Detective | Secret exfiltration, Unauthorized deployments | SIEM runner behavior logs, outbound egress alerts, unexpected deployment warnings | Seconds to minutes |
| **L3-EGRESS** | Egress Network Filtering | Corrective | Secret exfiltration | Firewall rules restricting runner egress to whitelisted package registries and APIs | Immediate (blocks untrusted connections) |
| **L4-INCIDENT** | Containment & Recovery | Corrective | Disruption of services, compromised runners | Automated isolation of compromised runners, single-button rollback of deployments | Minutes to hours |
| **L4-NOTIFY** | Security Communication | Corrective | Reputation damage, regulatory failure | Customer disclosures, vendor notifications, regulatory notification templates | Hours to days |
| **L5-AUDIT** | Immutable Log Auditing | Detective | Disruption of service, compliance checks | Immutable log exports, SBOM generation on every release, build audit trails | Minutes |

---

### Defense-in-Depth Strategy by Threat

#### Threat 1: Compromised Secrets & Tokens

**Prevention Chain (Left):**
1. **L3-SECRETS:** Use short-lived, dynamic OIDC tokens instead of static credentials *(CRITICAL)*
2. **L3-SECRETS:** Enforce automated secrets scanning to block commits containing passwords or keys *(CRITICAL)*
3. **L3-SECRETS:** Enable security-masked runner variables to hide secrets from log outputs *(CRITICAL)*
4. **L2-CONFIG:** Restrict access scope of pipeline secrets to production branches only *(RECOMMENDED)*

**Mitigation Chain (Right):**
1. **L3-EGRESS:** Block runner outbound traffic to unknown IP addresses to prevent secret exfiltration *(CRITICAL)*
2. **L2-MONITOR:** Alert on unusual credential access patterns or API calls from build scripts *(CRITICAL)*
3. **L4-INCIDENT:** Automatically revoke and rotate any credentials detected in logs or compromised environments *(CRITICAL)*
4. **L5-AUDIT:** Maintain immutable logs of secret configuration access *(CRITICAL)*

#### Threat 2: Over-Privileged Pipeline Permissions

**Prevention Chain (Left):**
1. **L2-CONFIG:** Enforce default-deny settings for workflow execution environments (`contents: read` only) *(CRITICAL)*
2. **L2-CONFIG:** Enforce workflow linting to detect unsafe shell command injections inside workflow files *(CRITICAL)*
3. **L1-REVIEW:** Require senior reviewer approval for any changes to files in `.github/workflows` or `bamboo-specs` *(CRITICAL)*
4. **L4-INFRA:** Run build scripts in non-privileged containers to prevent host control *(RECOMMENDED)*

**Mitigation Chain (Right):**
1. **L1-SIGNING:** Refuse deployment of build outputs that cannot prove they were generated by authorized release pipelines *(CRITICAL)*
2. **L2-MONITOR:** Alert on unexpected code execution or parameter modification in CI runs *(CRITICAL)*
3. **L4-INCIDENT:** Halt pipeline executions instantly upon threat identification *(CRITICAL)*
4. **L5-AUDIT:** Retain all pipeline configuration changes in an immutable log server *(CRITICAL)*

#### Threat 3: Dependency Confusion & Upstream Supply Chain

**Prevention Chain (Left):**
1. **L3-SCAN:** Pin dependencies to precise cryptographic hashes/commit SHAs *(CRITICAL)*
2. **L3-SCAN:** Run SCA checks (e.g., OWASP Dependency-Check) on every commit to flag vulnerable packages *(CRITICAL)*
3. **L4-INFRA:** Configure private Maven/Nexus package proxy repositories containing strict allow-lists *(CRITICAL)*
4. **L1-REVIEW:** Require approval for pull requests introducing new external libraries *(RECOMMENDED)*

**Mitigation Chain (Right):**
1. **L5-AUDIT:** Generate a Software Bill of Materials (SBOM) for every release to inventory all active libraries *(CRITICAL)*
2. **L2-MONITOR:** Scan the production build environment for unexpected third-party components *(CRITICAL)*
3. **L4-INCIDENT:** Maintain immediate rollback capability to previous clean, verified package configurations *(CRITICAL)*
4. **L4-NOTIFY:** Maintain communication playbooks if a dependency is found compromised downstream *(RECOMMENDED)*

#### Threat 4: Runner Infrastructure Compromise (Host Escape)

**Prevention Chain (Left):**
1. **L4-INFRA:** Ensure all build runners are short-lived, disposable, and destroyed immediately after a single job completes *(CRITICAL)*
2. **L4-INFRA:** Apply OS security updates and patch container runtimes on runner host machines regularly *(CRITICAL)*
3. **L4-INFRA:** Enforce rootless container execution for all build and test jobs *(CRITICAL)*
4. **L2-CONFIG:** Enforce network isolation between public runners and internal servers *(RECOMMENDED)*

**Mitigation Chain (Right):**
1. **L3-EGRESS:** Enforce network policies restricting runner hosts to package repos and source code repository endpoints *(CRITICAL)*
2. **L2-MONITOR:** Integrate runtime detection agents (e.g., Falco) on runner nodes to flag container breakouts *(CRITICAL)*
3. **L4-INCIDENT:** Automatically isolate, snapshot, and destroy runner instances exhibiting anomalous behaviors *(CRITICAL)*
4. **L5-AUDIT:** Ship runner system audit logs to an offsite immutable log accumulator *(CRITICAL)*

---

### Cross-Layer Attack Scenario: Malicious Script Injection in Pull Request

An attacker attempts to inject a malicious script via a pull request to steal AWS credentials and publish a backdoored release:

```
Attack Path:
┌─ L1-REVIEW (Fails: branch protections prevent merging without review, attacker targets PR pipeline instead)
├─ L2-CONFIG (Fails: default GITHUB_TOKEN set to read-only, script cannot write/modify repository files)
├─ L3-SECRETS (Fails: short-lived OIDC role rejects credentials extraction; secrets masked in logs)
└─ L4-INFRA (Fails: runner runs in isolated rootless container, blocking access to runner host network)

IF ALL PREVENTION LAYERS FAIL:
   ↓
   L3-EGRESS (Fails: runner tries to exfiltrate secrets to attacker server; connection is blocked by egress firewall rules)
   ├─ L2-MONITOR (Detects: anomalous network traffic from runner triggers instant SIEM warning)
   ├─ L4-INCIDENT (Responds: runner environment is immediately terminated and credential scope disabled)
   ├─ L5-AUDIT (Logs: full event trace and execution logs preserved in immutable store)
   └─ L1-SIGNING (Blocks: the build process halts, and no cryptographically signed release is created)
   
RESULT: Attack blocked and contained; malicious artifact prevented from reaching registry
```

---

## Quick Remediation Checklist

### Phase 1: Critical Controls (Immediate)
- [ ] Set default pipeline token permissions to read-only (`contents: read`) across all GitHub Workflows and Bamboo configurations.
- [ ] Migrate credential configuration from static keys to OpenID Connect (OIDC) federation for runner-cloud authentication.
- [ ] Require branch protection rules requiring a minimum of two approved reviewers for all code merges to main/release branches.
- [ ] Configure automatic secret scanning (e.g., GitHub Advanced Security or GitGuardian) on every commit.

### Phase 2: Prevention Layers (Week 1-2)
- [ ] Implement strict dependency pinning (pinning by hash/SHA instead of version tag) for all dependencies.
- [ ] Deploy Software Composition Analysis (SCA) scanners to automatically block builds containing high-risk CVEs.
- [ ] Transition all self-hosted build runners to single-use, isolated, ephemeral runner configurations.
- [ ] Restrict outbound network access (egress filtering) from build runners using host firewalls or network policies.

### Phase 3: Detection & Response (Week 2-3)
- [ ] Enable centralized, immutable remote logging for all CI/CD build runs, workflow edits, and configuration changes.
- [ ] Configure automated Software Bill of Materials (SBOM) generation as part of release build actions.
- [ ] Set up SIEM alerts for anomalies like workflow modifications, secrets access outside pipelines, or unusual runner outbound traffic.
- [ ] Create a supply chain incident response plan detailing credential revocation, build container isolation, and rollback procedures.

### Phase 4: Infrastructure & Compliance (Week 3-4)
- [ ] Implement artifact signing (e.g., via Sigstore/Cosign) in build steps and enforce verification rules on production Kubernetes/Docker hosts.
- [ ] Enforce commit signature verification (require PGP/SSH-signed commits) to protect developer credentials.
- [ ] Perform a full security audit of permissions assigned to third-party pipeline apps, actions, and integrations.
- [ ] Conduct simulated pipeline escape and dependency confusion tabletop exercises.
