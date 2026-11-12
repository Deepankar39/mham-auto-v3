"""MHAM AUTO Pro release: non-live source/template/asset regression checks."""
from pathlib import Path
import re, subprocess, shutil, sys
root=Path(__file__).resolve().parents[1]
web=root/'src/main/webapp'
views=list((web/'WEB-INF/views').glob('*.jsp'))+[web/'index.jsp']
errors=[]; checked=0
for jsp in views:
    text=jsp.read_text(encoding='utf-8');checked+=1
    if 'premium.css' not in text and jsp.name!='featured-products.jsp':errors.append(f'{jsp.name}: missing premium.css')
    if jsp.name!='featured-products.jsp' and 'charset=UTF-8' not in text and 'document-basics.jspf' not in text:errors.append(f'{jsp.name}: missing charset')
    if '<c:' in text and 'jakarta.tags.core' not in text:errors.append(f'{jsp.name}: JSTL core taglib not declared')
    for include in re.findall(r'<%@\s*include\s+file="([^"]+)"',text):
        if not (web/include.lstrip('/')).exists():errors.append(f'{jsp.name}: missing JSP include {include}')
    for asset in re.findall(r'\$\{pageContext\.request\.contextPath\}(/assets/[^"\s<>]+)',text):
        # JSP expressions in dynamic src should not be interpreted as literal files.
        if '${' not in asset and '<' not in asset and not (web/asset.lstrip('/')).exists():errors.append(f'{jsp.name}: missing asset {asset}')
    if 'href="#"' in text:errors.append(f'{jsp.name}: dead anchor href')
    if 'onclick=' in text:errors.append(f'{jsp.name}: inline onclick violates CSP')
assert (web/'assets/images/mham-logo-3x.png').exists()
assert (web/'assets/images/mham-official-logo.png').exists()
assert (web/'assets/images/parts-placeholder.svg').exists()
assert (root/'src/main/java/com/mhamauto/servlet/FeaturedProductsServlet.java').exists()
assert (root/'src/main/java/com/mhamauto/servlet/WishlistServlet.java').exists()
for path in sorted((web/'assets/js').glob('*.js')):
    if shutil.which('node'):
        result=subprocess.run(['node','--check',str(path)],capture_output=True,text=True)
        if result.returncode:errors.append(f'JS syntax {path.name}: {result.stderr}')
print(f'Checked {checked} JSP pages, {len(list((web/"assets/js").glob("*.js")))} JS files, asset/include links.')
if errors:
    for e in errors: print('FAIL',e)
    sys.exit(1)
print('PASS: source asset/fragment references, JSP taglibs, JS syntax, CSP-safe event wiring')
print('NOTE: This is NOT a JSP compiler, Maven/JUnit or MySQL/Tomcat integration test.')
