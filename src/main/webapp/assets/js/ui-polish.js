'use strict';
/** UI-only progressive enhancement; no form processing or data writes. */
document.addEventListener('DOMContentLoaded', () => {
  const statusTypes = [
    [/\b(approved|active|published|completed|confirmed|delivered)\b/i, 'is-success'],
    [/\b(pending|review|processing|open|awaiting|new)\b/i, 'is-alert'],
    [/\b(reject|cancel|fail|denied|suspend)\b/i, 'is-danger'],
    [/\b(archived|draft|inactive|closed)\b/i, 'is-neutral']
  ];
  document.querySelectorAll('.pro-status-pill').forEach(pill => {
    const value = pill.textContent.trim();
    const pair = statusTypes.find(([regex]) => regex.test(value));
    if (pair) pill.classList.add(pair[1]);
  });
  document.querySelectorAll('.a8-scroll').forEach(scroller => {
    if (!scroller.hasAttribute('tabindex')) scroller.tabIndex = 0;
    if (!scroller.hasAttribute('role')) scroller.setAttribute('role', 'region');
    if (!scroller.hasAttribute('aria-label')) scroller.setAttribute('aria-label', 'Scrollable data table');
  });
  // Header links are enhanced by phase10.js; sync the compact-nav label with state.
  const mobileNav = document.querySelector('.mh-menu-button');
  if (mobileNav) {
    const syncLabel = () => mobileNav.setAttribute('aria-label', mobileNav.getAttribute('aria-expanded') === 'true' ? 'Close navigation' : 'Open navigation');
    mobileNav.addEventListener('click', () => queueMicrotask(syncLabel));
    document.addEventListener('keydown', event => { if (event.key === 'Escape') queueMicrotask(syncLabel); });
    syncLabel();
  }
});
