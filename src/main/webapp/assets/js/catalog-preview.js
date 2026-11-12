'use strict';
// Progressive enhancement: navigation remains visible without JavaScript.
document.documentElement.classList.add('has-js');
document.addEventListener('DOMContentLoaded', () => {
    const button = document.querySelector('.mh-menu-button');
    const nav = document.getElementById('mh-nav');
    if (!button || !nav) return;
    button.addEventListener('click', () => {
        const open = button.getAttribute('aria-expanded') !== 'true';
        button.setAttribute('aria-expanded', String(open));
        nav.classList.toggle('is-open', open);
    });
    nav.querySelectorAll('a').forEach(link => link.addEventListener('click', () => {
        nav.classList.remove('is-open');
        button.setAttribute('aria-expanded', 'false');
    }));
    document.addEventListener('pointerdown', (event) => {
        if (nav.classList.contains('is-open') && !nav.contains(event.target) && !button.contains(event.target)) {
            nav.classList.remove('is-open');
            button.setAttribute('aria-expanded', 'false');
        }
    });
    window.addEventListener('resize', () => {
        if (window.innerWidth > 760 && nav.classList.contains('is-open')) {
            nav.classList.remove('is-open');
            button.setAttribute('aria-expanded', 'false');
        }
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && nav.classList.contains('is-open')) {
            nav.classList.remove('is-open');
            button.setAttribute('aria-expanded', 'false');
            button.focus();
        }
    });
});
