/* Phase 3: local fallback for broken external CDN images, no server-side image downloads. */
document.addEventListener('DOMContentLoaded', () => {
  const fallback = document.body.dataset.productFallback ||
    `${document.querySelector('a.mh-brand')?.getAttribute('href') || '/mham-auto/'}assets/images/phase13/mark.svg`;
  document.querySelectorAll('img.p3-media').forEach((img) => {
    img.addEventListener('error', () => {
      if (img.dataset.fallbackApplied) return;
      img.dataset.fallbackApplied = 'true';
      img.src = fallback;
      img.alt = 'Product image unavailable';
    }, { once: true });
  });
});
