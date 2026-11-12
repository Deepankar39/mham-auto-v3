/** Same-origin, read-only count. No cookies or account identifiers saved in localStorage. */
(() => {
  const spots = [...document.querySelectorAll('[data-cart-count]')];
  if (!spots.length) return;
  const link = spots[0].closest('a');
  const base = link ? new URL(link.href).pathname.replace(/\/cart$/, '') : '';
  fetch(`${base}/api/cart/count`, {credentials:'same-origin', cache:'no-store'})
    .then(r => {if(!r.ok) throw new Error('badge unavailable'); return r.json();})
    .then(data => {
      const count = Number(data.count);
      if(!Number.isSafeInteger(count) || count<1) return;
      spots.forEach(spot => {spot.textContent=String(count);spot.hidden=false;spot.setAttribute('aria-label', `${count} cart items`);});
    }).catch(()=>{});
})();
