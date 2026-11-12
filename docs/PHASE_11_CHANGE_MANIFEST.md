# Phase 11 change manifest

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026


Phase 11 is a documentation, test/release and deployment packaging layer. It does **not** change existing Java business logic, JSP views, CSS/JS assets, preceding migration SQL or Maven settings.

New root/project files:
- `README.md`
- `database/15_phase11_read_only_validation.sql` (READ ONLY)
- `scripts/Test-Phase11-Offline.ps1`
- `scripts/Test-Phase11-Database.ps1`
- `scripts/Test-Phase11-Live.ps1`
- `scripts/Collect-Phase11-Evidence.ps1`
- `scripts/phase11_static_audit.py`
- `docs/PHASE_11_RELEASE_GUIDE.md`
- `docs/PHASE_11_ARCHITECTURE.md`, `docs/PHASE_11_ERD.mmd`
- `docs/PHASE_11_DATABASE.md`, `docs/PHASE_11_SCHEMA_DICTIONARY.csv`
- `docs/PHASE_11_SYLLABUS_MAPPING.md`
- `docs/PHASE_11_TEST_MATRIX.csv`
- `docs/PHASE_11_EVIDENCE_CHECKLIST.md`
- `docs/PHASE_11_SECURITY_AND_LIMITATIONS.md`
- `docs/PHASE_11_VERIFICATION_RESULTS.md`
- `docs/PHASE_11_CHANGE_MANIFEST.md`
- Project report DOCX/PDF generated next.
- Empty evidence folder placeholders.

The Phase 11 *overlay ZIP* contains only these additions; the *full source release ZIP* merges every previous stage in chronological order. The source is a release candidate until your actual full Windows build and browser verification are successful.
