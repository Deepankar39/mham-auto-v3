document.querySelectorAll('[data-confirm-clear]').forEach(button => {
  button.closest('form')?.addEventListener('submit', event => {
    if(!window.confirm('Clear all saved parts from your cart?')) event.preventDefault();
  });
});

document.querySelectorAll('.c5-thumbnail').forEach(holder => {
  const img = holder.querySelector('img');
  if(img) img.addEventListener('error', () => {
    const fallback=holder.getAttribute('data-fallback');
    if(fallback && !img.src.endsWith(fallback)) img.src=fallback;
  });
});
