'use strict';
document.addEventListener('DOMContentLoaded', async ()=>{
  const grid=document.getElementById('pro-featured-grid');if(!grid)return;
  const fallback='<div class="pro-featured-fallback"><strong>Explore our complete parts catalogue</strong><p>Product highlights are available when the catalogue database is connected.</p><a href="'+(grid.dataset.catalogueUrl||'products')+'">Browse all products</a></div>';
  try{
    const controller=new AbortController();const timeout=setTimeout(()=>controller.abort(),7500);
    const response=await fetch(grid.dataset.url,{credentials:'same-origin',signal:controller.signal,headers:{'Accept':'text/html'}});
    clearTimeout(timeout);
    if(!response.ok)throw new Error('Highlights unavailable');
    const html=await response.text(); if(!html.trim())throw new Error('Empty highlights');
    grid.innerHTML=html;
  }catch(error){grid.innerHTML=fallback}
});
