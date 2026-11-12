# MHAM AUTO — Academic V1 / Phase 1.3

**Scope:** First Java Servlet -> Java service/model -> JSP page. Local asset bundle. This is not the database catalogue yet.

## 1. What to create manually

**No folders and no source files.** Extract the ZIP directly into `D:\Java Project\MHAM AUTO` so that the root of the ZIP's `src`, `scripts`, `tests`, and `docs` directories merge with the existing project. Do **not** extract into an additional nested directory. This overlay intentionally contains **no** `pom.xml`, `index.jsp`, `style.css`, or existing paths. It preserves the homepage built in Step 1.2.

## 2. Build and deploy from VS Code PowerShell

```powershell
cd "D:\Java Project\MHAM AUTO"
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\Test-Phase1_3_Offline.ps1"
mvn clean package
Test-Path ".\target\mham-auto.war"
Copy-Item ".\target\mham-auto.war" "D:\Java Tools\apache-tomcat-10.1.61\webapps\mham-auto.war" -Force
Start-Sleep -Seconds 8
Start-Process "http://localhost:8080/mham-auto/catalog"
```

If Tomcat is set up with `autoDeploy=false` or `unpackWARs=false`, adjust deployment/restart accordingly. For a default Tomcat 10.1 installation, auto-deploy is enabled. If `/catalog` is not yet available, wait a few seconds and refresh; use `logs` for diagnostics.

## 3. Live smoke test (Tomcat must be running)

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\Test-Phase1_3.ps1"
```

Browse: `/mham-auto/catalog` and `/mham-auto/catalog?audience=trade`. Invalid audience values should produce HTTP 400. Verify existing `/mham-auto/` remains working. Resize browser for mobile.

## 4. What's happening under the hood?

1. Browser requests `/mham-auto/catalog`.
2. `CatalogPreviewServlet` receives HTTP GET (the `@WebServlet` annotation registers the route).
3. `CatalogPreviewService` supplies **six sample** category objects and selects retail/trade presentation.
4. The Servlet attaches safe, server-controlled attributes to the request.
5. It forwards to the JSP in `WEB-INF/views`, which Tomcat renders into HTML.
6. The browser fetches local CSS, JavaScript and SVG drawings from `/assets/...`.

The JSP is under `WEB-INF` so visitors cannot load it directly. Java controls the route. No passwords, orders, payment, verified fitment or MySQL query exists in this phase. Sample categories are not evidence of real catalogue availability.

## 5. Security and quality notes

- Query value `audience` accepts only `retail` or `trade`, defaulting to `retail`.
- Unsupported values return HTTP 400; raw query strings are never interpolated into JSP output.
- The category data is developer-controlled in memory; when we later load database/user-controlled text into JSP, HTML escaping must be added (e.g. JSTL `c:out`) before production use.
- No new dependency is necessary. Existing `pom.xml` and homepage remain unchanged.
- This phase uses only system fonts and locally bundled graphics/scripts/styles; there are no external browser assets or font requests.
- The number of explicit category slots is limited to this introduction stage. A loop-based component will replace it during the database-backed catalogue stage.

## 6. Acceptance checklist

- Offline Java test passes.
- Maven WAR build succeeds.
- `/mham-auto/catalog` renders six sample category cards with illustrations.
- Retail/trade links change the description server-side.
- Local CSS, JavaScript and SVG resources load.
- Existing homepage still loads.
- Invalid `audience` is rejected.
- PowerShell HTTP smoke test passes.

**Stop after testing this phase and send the results.** Next phase will be chosen only after confirmation.
