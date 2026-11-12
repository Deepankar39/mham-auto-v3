# Security review, known boundaries and production exclusions

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026


## Existing controls (subject to local integration verification)
- PBKDF2 password hashing and per-user role/active-state recheck on protected routes.
- HttpSession rotation after login, idle expiration and explicit invalidation on logout.
- CSRF tokens on writes; Phase 10 Origin check on unsafe HTTP methods; ADMIN role and owner checks.
- Prepared SQL and bounded inputs, escaped JSP text output and strict Content Security Policy.
- Default headers: X-Frame-Options, X-Content-Type-Options, Permissions-Policy, Referrer-Policy.
- MySQL constraints for account/variant uniqueness and demo order snapshots.

## Security, data and business limitations still open
1. **Not production certified.** Real penetration testing, vulnerability scan, load test and legal/privacy review have NOT been performed here.
2. Default JDBC URL has `sslMode=DISABLED` and allows public key retrieval: acceptable only for trusted localhost development; configure TLS in controlled deployment.
3. Local site is HTTP. Secure cookies, HTTPS redirection and HSTS require an HTTPS production endpoint.
4. Login throttling is process-local; clustered/production implementation requires shared durable anti-abuse limits, monitoring and account recovery.
5. No MFA, transactional emails, verified identity/business documents or production support workflow.
6. Retail prices imported from Shopify CSV are **source-currency unverified**. Never publish or charge customers based on these unknown values.
7. External CDN image URLs depend on hosting and rights outside the Java application; not guaranteed to stay reachable.
8. Vehicle fitment text is descriptive and cannot be treated as certified compatibility data.
9. Stock quantity is source/demo data; no true reservation, inventory reconciliation, dispatch or real payment.
10. Business `APPROVED` means only academic review status; it does not grant special commerce permissions.
11. Scalability to 1,000,000+ catalogue items is a design goal, NOT a measured result. Existing 500 synthetic rows do not benchmark 1M.
12. Prior migration scripts and routes must be verified against your current database before release acceptance.

## Release decision
`RC1 - prepared` is the only valid status until local tests and academic evidence are completed. Do not use this system to solicit real payments, accept live orders or expose a public merchant checkout.
