'use strict';
/* Progressive enhancement; navigation, forms and product views work without this file. */
document.addEventListener('DOMContentLoaded', () => {
  // Only an actual current path receives aria-current; do not mark 'Explore parts' on every page.
  const path = location.pathname.replace(/\/$/, '') || '/';
  for (const nav of document.querySelectorAll('.mh-nav, .a8-nav')) {
    const links = Array.from(nav.querySelectorAll('a[href]'));
    for (const a of links) {
      a.classList.remove('mh-nav-active');
      a.removeAttribute('aria-current');
      const url = new URL(a.href, location.href);
      if (url.origin === location.origin && url.pathname.replace(/\/$/, '') === path && !url.hash) {
        a.classList.add('mh-nav-active');
        a.setAttribute('aria-current', 'page');
      }
    }
  }
  // Announce any existing server-rendered error without taking focus from keyboard users.
  for (const element of document.querySelectorAll('.a4-error, .p7-error, .a8-error, .t9-error')) {
    if (!element.getAttribute('role')) element.setAttribute('role', 'alert');
  }
});
