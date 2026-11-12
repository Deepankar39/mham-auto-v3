'use strict';
/** Navigation enhancement only. All server-side authorization and actions are unchanged. */
document.addEventListener('DOMContentLoaded', () => {
  const trigger = document.querySelector('.pro-admin-mobile-toggle');
  const sidebar = document.getElementById('admin-sidebar');
  if (!trigger || !sidebar) return;
  const mobile = window.matchMedia('(max-width: 760px)');
  const close = (returnFocus = false) => {
    trigger.setAttribute('aria-expanded', 'false');
    trigger.setAttribute('aria-label', 'Open administrator menu');
    sidebar.classList.remove('is-open');
    document.body.classList.remove('admin-nav-open');
    sidebar.inert = mobile.matches;
    if (returnFocus && mobile.matches) trigger.focus();
  };
  const open = () => {
    sidebar.inert = false;
    sidebar.classList.add('is-open');
    trigger.setAttribute('aria-expanded', 'true');
    trigger.setAttribute('aria-label', 'Close administrator menu');
    document.body.classList.add('admin-nav-open');
    sidebar.querySelector('nav a')?.focus();
  };
  trigger.addEventListener('click', () => trigger.getAttribute('aria-expanded') === 'true' ? close(true) : open());
  document.addEventListener('keydown', event => { if (event.key === 'Escape' && sidebar.classList.contains('is-open')) close(true); });
  document.addEventListener('pointerdown', event => {
    if (sidebar.classList.contains('is-open') && !sidebar.contains(event.target) && !trigger.contains(event.target)) close();
  });
  sidebar.querySelectorAll('a').forEach(link => link.addEventListener('click', () => close()));
  mobile.addEventListener('change', () => close());
  close();
  const params = new URLSearchParams(location.search);
  if (params.get('saved') === '1') {
    const toast = document.createElement('div');
    toast.className = 'pro-toast';
    toast.setAttribute('role', 'status');
    toast.textContent = 'Changes saved successfully';
    document.body.appendChild(toast);
    window.setTimeout(() => toast.remove(), 4000);
  }
});
