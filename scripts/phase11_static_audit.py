#!/usr/bin/env python3
"""MHAM AUTO source/release preflight. Pure stdlib; does NOT prove Maven, MySQL or Tomcat work."""
import csv, sys, re, xml.etree.ElementTree as ET
from pathlib import Path

root=Path(__file__).resolve().parents[1]
required=['pom.xml','src/main/webapp/WEB-INF/web.xml','database/01_catalog_schema.sql','database/03_identity_schema.sql','database/05_cart_schema.sql','database/07_checkout_schema.sql','database/10_admin_schema.sql','database/12_trade_application_schema.sql','samples/product data sample.csv','samples/SYNTHETIC_TEST_500.csv']
missing=[f for f in required if not (root/f).is_file()]
if missing: raise SystemExit('FAIL missing: '+', '.join(missing))
ET.parse(root/'pom.xml');ET.parse(root/'src/main/webapp/WEB-INF/web.xml')
java=list((root/'src/main/java').rglob('*.java'))
assert len(java)>=75, f'Unexpected Java source count {len(java)}'
with (root/'samples/product data sample.csv').open(encoding='utf-8-sig',newline='') as f:
 r=csv.DictReader(f);headers=r.fieldnames or [];sample=list(r)
assert len(headers)==74, f'Expected 74 Shopify columns, got {len(headers)}'
assert len(sample)==4, f'Expected 4 sample rows, got {len(sample)}'
for h in ['Handle','Title','Variant SKU','Variant Price','Image Src']:
 assert h in headers, 'Missing source header '+h
with (root/'samples/SYNTHETIC_TEST_500.csv').open(encoding='utf-8-sig',newline='') as f:
 data=list(csv.DictReader(f))
assert len(data)==500, f'Expected 500 synthetic rows, got {len(data)}'
assert len({r['Handle'] for r in data})==500, 'Synthetic handles duplicate'
assert len({r['Variant SKU'] for r in data})==500, 'Synthetic SKU duplicates'
script_text='\n'.join((root/'database'/x).read_text() for x in ['01_catalog_schema.sql','03_identity_schema.sql','05_cart_schema.sql','07_checkout_schema.sql','10_admin_schema.sql','12_trade_application_schema.sql'])
tables=re.findall(r'CREATE TABLE IF NOT EXISTS\s+(\w+)',script_text,re.I)
assert len(tables)==26 and len(set(tables))==26, f'Expected 26 unique application tables, got {len(tables)}'
webpages=list((root/'src/main/webapp').rglob('*.jsp'))
assert len(webpages)>=20, f'Unexpected JSP count {len(webpages)}'
for p in (root/'src/main/webapp').rglob('*'):
 if p.is_file() and p.suffix=='.jsp':
  if '\u0000' in p.read_text(encoding='utf-8'): raise AssertionError('NUL in JSP '+str(p))
urls={}
for p in java:
 text=p.read_text(encoding='utf-8')
 for blob in re.findall(r'@WebServlet\s*\((.*?)\)',text,re.S):
  m=re.search(r'urlPatterns\s*=\s*\{([^}]+)\}',blob)
  vals=re.findall(r'"(/[^" ]+)"',m.group(1)) if m else re.findall(r'"(/[^" ]+)"',blob)
  for u in vals: urls.setdefault(u,[]).append(p.name)
dup={k:v for k,v in urls.items() if len(v)>1}
assert not dup, f'Duplicate servlet URL patterns: {dup}'
print('PASS: XML, CSV contract, 500-row synthetic fixture, 26 schema tables, '+str(len(java))+' Java sources, '+str(len(webpages))+' JSPs, URL pattern uniqueness')
print('NOTE: This static audit does not compile JSP, contact MySQL or run a servlet container.')
