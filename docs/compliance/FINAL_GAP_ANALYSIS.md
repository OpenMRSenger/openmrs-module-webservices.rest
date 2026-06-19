 ### A.8.3 Toegangsbeveiliging (Access Control)

  Status: Compliant.

  • Settings search secure: SettingsFormController.java requires  RestConstants.PRIV_MANAGE_RESTWS . Exposes no secrets.
  • Cache eviction secure: ClearDbCacheController2_0.java requires  RestConstants.PRIV_MANAGE_RESTWS .                  
  • Diagnostics endpoint secure:  /session/diag  in SessionController1_9.java requires  RestConstants.PRIV_MANAGE_RESTW
.
  • Password escalation fixed: ChangePasswordController1_8.java requires  PrivilegeConstants.EDIT_USER_PASSWORDS  for
other accounts.
  • IP bypass fixed: AuthorizationFilter.java checks  X-Forwarded-For  header. Prevents proxy IP spoofing.

  ### A.8.5 Authenticatie (Authentication)

  Status: NOT fully compliant (Gaps exist).

  • Basic Auth over HTTPS only: Enforced in AuthorizationFilter.java. Plain HTTP rejected.
  • Brute force lockout: Returns  HTTP 429 Too Many Requests  +  Retry-After: 300  when locked.
  • GAP (Unsafe) — Expired session bypass (SEC-01): AuthorizationFilter.java sends  401 Unauthorized  but has no  return
.
  Executing chain continues.
  • GAP (Unsafe) — Session Fixation (SEC2-02): Login does not refresh/regenerate  JSESSIONID  cookie.

  ### A.8.15 Logging (Auditing)

  Status: Compliant.

  • Medical data access: RestAuditLogInterceptor.java logs GET/POST/PUT/DELETE to  /patient ,  /encounter ,  /obs ,
/visit , etc.
  logs in JSON.
  • Security logging: Logs IP deny, failed login, account lockout, logout, password change, cache eviction.
  • Stack trace masked:  enableStackTraceDetails  in config.xml default  false . RestUtil.java masks exceptions
  in API response.

  Next step: add  return;  statement to expired session check in AuthorizationFilter.java.