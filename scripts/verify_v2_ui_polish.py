#!/usr/bin/env python3
"""MHAM AUTO V2 UI-only, database-free release checks. Python stdlib only.

Does not replace a Maven, JSP compiler, browser-on-Tomcat, or MySQL integration test.
"""
from pathlib import Path
import re, subprocess, shutil, sys, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
web=root/'src/main/webapp'
views=[web/'index.jsp',*(web/'WEB-INF/views').glob('*.jsp')]
frags=web/'WEB-INF/views/fragments'
issues=[]

def check(expr,msg):
    if not expr: issues.append(msg)
check((web/'assets/css/ui-polish.css').is_file(),'Missing shared polish CSS')
check((web/'assets/js/ui-polish.js').is_file(),'Missing shared polish JavaScript')
check((frags/'ui-icons.jspf').is_file(),'Missing local SVG icon sprite')
sprite=(frags/'ui-icons.jspf').read_text(encoding='utf-8')
sprite=sprite.split('--%>',1)[-1] if '--%>' in sprite else sprite
try:
    ET.fromstring(sprite)
except ET.ParseError as e:
    issues.append(f'Unparseable SVG icon sprite: {e}')
icon_defs=set(re.findall(r'id="(mh-icon-[a-z-]+)"',sprite))
used=set()
for page in views:
    s=page.read_text(encoding='utf-8')
    if page.name!='featured-products.jsp':
        check('assets/css/ui-polish.css' in s, f'{page.name}: missing polish stylesheet')
        check('assets/js/ui-polish.js' in s, f'{page.name}: missing polish script')
    check('\ufffd' not in s, f'{page.name}: Unicode replacement character')
    check('cheeku' not in s.lower(), f'{page.name}: hardcoded display name')
    for include in re.findall(r'<%@\s*include\s+file="([^"]+)"',s):
        check((web/include.lstrip('/')).is_file(),f'{page.name}: broken include: {include}')
    for asset in re.findall(r'\$\{pageContext\.request\.contextPath\}(/assets/[^"\s<>]+)',s):
        if '${' not in asset and '<' not in asset:
            check((web/asset.lstrip('/')).is_file(),f'{page.name}: broken asset: {asset}')
    used.update(re.findall(r'href="#(mh-icon-[a-z-]+)"',s))
for file in frags.glob('*.jspf'):
    s=file.read_text(encoding='utf-8')
    check('\ufffd' not in s,f'{file.name}: Unicode replacement character')
    used.update(re.findall(r'href="#(mh-icon-[a-z-]+)"',s))
for file in (web/'assets/js').glob('*.js'):
    used.update(re.findall(r"#(mh-icon-[a-z-]+)",file.read_text(encoding='utf-8')))
check(used<=icon_defs, 'Undefined SVG icons: '+', '.join(sorted(used-icon_defs)))
header=(frags/'catalog-header.jspf').read_text(encoding='utf-8')
check('class="mh-header-search"' in header,'Missing shared header search')
check('width="20" height="20"' in header,'Search SVG missing fixed 20 x 20 sizing')
check('fill="none"' in header,'Search SVG missing transparent fill')
check('ui-icons.jspf' in header,'Storefront header missing icon sprite')
admin=(frags/'admin-shell-start.jspf').read_text(encoding='utf-8')
for label in ['Overview','Products','Categories','Demo orders','Customers','Trade applications']:
    check('>'+label+'</span>' in admin, 'Missing admin nav item: '+label)
check('ui-icons.jspf' in admin,'Admin shell missing icon sprite')
check('aria-current=' in admin,'Admin navigation missing current-page semantics')
check('sessionScope.authUser.displayName' in admin,'Admin identity missing authenticated session value')
greeting=(web/'WEB-INF/views/admin-dashboard.jsp').read_text(encoding='utf-8')
check('sessionScope.authUser.displayName' in greeting,'Dashboard greeting not based on session principal')
check('method="post"' in (web/'WEB-INF/views/login.jsp').read_text(),'Login form method unexpectedly changed')
for file in (web/'assets/css').glob('*.css'):
    check('\ufffd' not in file.read_text(encoding='utf-8'),f'{file.name}: replacement chars')
jsfiles=sorted((web/'assets/js').glob('*.js'))
if shutil.which('node'):
    for file in jsfiles:
        r=subprocess.run(['node','--check',str(file)],capture_output=True,text=True)
        check(r.returncode==0,f'{file.name}: JS syntax: {r.stderr.strip()}')
if issues:
    for issue in issues: print('FAIL:',issue)
    sys.exit(1)
print(f'PASS: {len(views)} JSPs, {len(jsfiles)} JavaScript assets, SVG sprite ({len(icon_defs)} symbols), links, UTF-8, greeting source and search sizing')
print('NOTE: UI static checks only; Maven/JSP compilation and live MySQL/Tomcat tests must be run separately.')
