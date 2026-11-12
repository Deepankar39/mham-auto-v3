'use strict';
/* Local wishlist/recently-viewed data is deliberately browser-only; never claims an account sync. */
(()=>{
const WISH='mham_wishlist_v2', RECENT='mham_recent_v2';
const read=(key)=>{try{const v=JSON.parse(localStorage.getItem(key)||'[]');return Array.isArray(v)?v:[]}catch(_){return []}};
const write=(key,items)=>{try{localStorage.setItem(key,JSON.stringify(items.slice(0,40)))}catch(_){}};
const safeUrl=(raw)=>{try{const u=new URL(raw,location.href);return u.origin===location.origin&&u.pathname.endsWith('/product')?u.pathname+u.search:null}catch(_){return null}};
const safeImage=(raw)=>{try{const u=new URL(raw,location.href);if((u.origin===location.origin&&u.pathname.includes('/assets/'))||(u.protocol==='https:'&&u.hostname==='cdn.shopify.com'))return u.href}catch(_){ }return ''};
const findProduct=(card)=>{
 const anchor=card.querySelector('h3 a')||card.querySelector('.p3-card-image')||card.querySelector('.pro-product-picture');
 if(!anchor)return null;const url=safeUrl(anchor.href);if(!url)return null;
 const title=card.querySelector('h3')?.textContent?.trim()?.slice(0,160)||'Automotive part';
 const photo=safeImage(card.querySelector('img')?.src||'');
 return {url,title,image:photo};
};
const isSaved=(url)=>read(WISH).some(p=>p.url===url);
function paintHeart(button, saved, withLabel=false){
 const svg=document.createElementNS('http://www.w3.org/2000/svg','svg');
 svg.setAttribute('class','mh-icon');svg.setAttribute('aria-hidden','true');svg.setAttribute('focusable','false');
 const use=document.createElementNS('http://www.w3.org/2000/svg','use');use.setAttribute('href','#mh-icon-heart');svg.append(use);
 if(saved)svg.style.fill='currentColor';
 button.replaceChildren(svg);
 if(withLabel){const label=document.createElement('span');label.textContent=saved?'Saved locally':'Save part';button.append(label)}
 button.setAttribute('aria-pressed',String(saved));
}

function toggle(product){const list=read(WISH).filter(p=>p&&typeof p.url==='string'&&safeUrl(p.url));const found=list.some(p=>p.url===product.url);write(WISH,found?list.filter(p=>p.url!==product.url):[product,...list]);document.querySelectorAll('.pro-wishlist-toggle').forEach(btn=>{const on=isSaved(btn.dataset.url);paintHeart(btn,on);btn.setAttribute('aria-label',on?'Remove from saved parts':'Save part to this browser')});}
function installCards(root=document){root.querySelectorAll('.p3-card,.pro-product-card').forEach(card=>{
 if(card.querySelector('.pro-wishlist-toggle'))return;
 const product=findProduct(card);if(!product)return;
 const button=document.createElement('button');button.type='button';button.className='pro-wishlist-toggle';button.dataset.url=product.url;
 const active=isSaved(product.url);paintHeart(button,active);button.setAttribute('aria-label',active?'Remove from saved parts':'Save part to this browser');button.title='Save in this browser (not synced to account)';
 button.addEventListener('click',()=>toggle(product));const wrapper=card.querySelector('.p3-card-image,.pro-product-picture');wrapper?.parentNode?.insertBefore(button,wrapper.nextSibling);
 });}
function installGallery(){
 const gallery=document.querySelector('.p3-gallery');if(!gallery)return;
 const images=[...gallery.querySelectorAll('.p3-gallery-frame img')].filter(img=>img.src);
 if(!images.length)return;
 const main=images[0];gallery.classList.add('pro-gallery-enhanced');
 if(images.length>1){const thumbs=document.createElement('div');thumbs.className='pro-gallery-thumbs';thumbs.setAttribute('role','group');thumbs.setAttribute('aria-label','Product image thumbnails');images.forEach((img,i)=>{
  const btn=document.createElement('button');btn.type='button';btn.className='pro-gallery-thumb';btn.setAttribute('aria-label','View image '+(i+1));btn.setAttribute('aria-pressed',String(i===0));
  const preview=document.createElement('img');preview.src=img.src;preview.alt='';preview.loading='lazy';btn.append(preview);
  btn.addEventListener('click',()=>{main.src=img.src;main.alt=img.alt;thumbs.querySelectorAll('button').forEach(b=>b.setAttribute('aria-pressed',String(b===btn)))});thumbs.append(btn)
 });gallery.append(thumbs)}
 const title=document.querySelector('.p3-product-info h1')?.textContent?.trim();
 const handle=new URLSearchParams(location.search).get('handle');
 if(title&&handle){const url=safeUrl(location.href);if(url){const item={url,title:title.slice(0,160),image:safeImage(main.src)};write(RECENT,[item,...read(RECENT).filter(p=>p&&p.url!==url)])}}
}
function installDetailSave(){const h1=document.querySelector('.p3-product-info h1');if(!h1)return;const url=safeUrl(location.href);if(!url)return;
 const item={url,title:h1.textContent.trim().slice(0,160),image:safeImage(document.querySelector('.p3-gallery img')?.src||'')};
 const button=document.createElement('button');button.type='button';button.className='pro-detail-save';paintHeart(button,isSaved(url),true);button.setAttribute('aria-label','Save part locally on this device');
 button.addEventListener('click',()=>{toggle(item);const saved=isSaved(url);paintHeart(button,saved,true)});h1.after(button)
}
function renderWishlist(){const destination=document.querySelector('[data-local-wishlist]');if(!destination)return;
 const items=read(WISH).filter(p=>p&&typeof p.title==='string'&&safeUrl(p.url));destination.replaceChildren();
 const counter=document.querySelector('[data-wishlist-count]');if(counter)counter.textContent=String(items.length);
 if(!items.length){const empty=document.createElement('div');empty.className='pro-local-empty';const h=document.createElement('h2');h.textContent='No saved parts yet';const p=document.createElement('p');p.textContent='Browse the catalogue and use the heart icon to save a product in this browser.';const a=document.createElement('a');a.href=destination.dataset.catalogue||'products';a.textContent='Explore parts';empty.append(h,p,a);destination.append(empty);return}
 for(const item of items){const row=document.createElement('article');row.className='pro-saved-item';if(safeImage(item.image)){const img=document.createElement('img');img.src=safeImage(item.image);img.alt='';img.loading='lazy';row.append(img)}const wrap=document.createElement('div');const a=document.createElement('a');a.href=safeUrl(item.url);a.textContent=item.title;const label=document.createElement('small');label.textContent='Saved locally on this device';wrap.append(a,label);const btn=document.createElement('button');btn.type='button';btn.textContent='Remove';btn.addEventListener('click',()=>{write(WISH,read(WISH).filter(p=>p.url!==item.url));renderWishlist()});row.append(wrap,btn);destination.append(row)}
}
document.addEventListener('DOMContentLoaded',()=>{
 installCards();installGallery();installDetailSave();renderWishlist();
 const grid=document.getElementById('pro-featured-grid');if(grid){const observer=new MutationObserver(()=>installCards(grid));observer.observe(grid,{childList:true})}
 document.querySelectorAll('form[data-confirm]').forEach(form=>form.addEventListener('submit',e=>{if(!window.confirm(form.dataset.confirm||'Continue?'))e.preventDefault()}));
 const recentBox=document.querySelector('[data-recent-items]');if(recentBox){for(const item of read(RECENT).slice(0,4)){const url=safeUrl(item.url);if(!url)continue;const a=document.createElement('a');a.href=url;a.textContent=item.title;recentBox.append(a)}}
});
})();
