# MHAM AUTO V2 UI Polish — Windows Deployment and Rollback

This is a **UI-only overlay**. It requires **no schema migration** and must not delete existing MySQL records. Use the complete source ZIP as the primary development artifact; the provided WAR is a convenience deployment candidate assembled from the earlier compiled WAR.

## 1. Back up the current working installation

Back up the entire existing project directory, your currently deployed `mham-auto.war`, the exploded Tomcat `webapps/mham-auto` folder, and export the existing `mham_auto` MySQL database. Keep the backups outside the Tomcat deployment directory. Do not rerun database creation, seed, or synthetic CSV import commands against the working database.

## 2. Extract the source into a separate directory

For example `D:\Java Project\MHAM_AUTO_V2_UI_Final`. Do not overwrite your original project until this build has passed your local tests.

## 3. Build and test (Java 21, Maven, MySQL 8.x, Tomcat 10.1)

```powershell
cd "D:\Java Project\MHAM_AUTO_V2_UI_Final"
java -version
mvn -version
python .\scripts\verify_v2_ui_polish.py
mvn clean test package
```

A successful Maven build produces `target\mham-auto.war`. If the build fails, fix the reported error before deploying; do not replace your working application with an unverified WAR. The compiled WAR bundled with the release has not passed Maven/JSP compilation in this execution environment.

## 4. Configure the same existing database connection

Use your already functioning environment variables/credentials. Example, in the same shell that will launch Tomcat:

```powershell
$env:MHAM_DB_USER = "YOUR_MYSQL_USER"
$env:MHAM_DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:MHAM_DB_URL = "jdbc:mysql://127.0.0.1:3306/mham_auto?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=UTC"
```

The project's existing DB configuration logic is unchanged. Never publish credentials or store them in source code.

## 5. Stop Tomcat, deploy, restart

Stop Tomcat using the existing `shutdown.bat` or service manager, then after backing up the old deployment, replace the WAR and **remove the old exploded folder only after creating a verified backup** to prevent stale JSP/CSS:

```powershell
$tomcat = "D:\Java Tools\apache-tomcat-10.1.61"
# Stop Tomcat using your normal method before these steps.
Copy-Item ".\target\mham-auto.war" "$tomcat\webapps\mham-auto.war" -Force
# If the old exploded folder remains, rename it to a backup OUTSIDE webapps.
& "$tomcat\bin\startup.bat"
```

Browse `http://localhost:8080/mham-auto/`. Perform a hard refresh (`Ctrl+Shift+R`) because CSS and JavaScript filenames remain stable and cached assets could hide the update.

## 6. Regression checklist

- Home, global search, nav, footer at 390px/768px/1024px/1440px.
- Login/register, password visibility, session/logout and input validation.
- Product search, filter, sort, listing, images, detail/variants, saved parts and recently viewed.
- Cart add/update/remove; checkout address selection and **demo** order confirmation.
- Account/profile, saved addresses, order history/detail and trade application/status.
- Admin overview, product edit/archive, category operations, demo orders, customer directory and trade review; keyboard and mobile drawer.
- Check browser console, Tomcat logs, live MySQL reads/writes and permissions on non-admin routes.

## 7. Roll back safely

Stop Tomcat. Restore the saved previous WAR and exploded directory using your original backup, then restart Tomcat. **No database schema change was made by this UI patch**, so schema rollback is not required. Avoid restoring an older database backup over new customer/order activity unless separately justified and authorized.
