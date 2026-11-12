'use strict';
document.querySelectorAll('[data-toggle-password]').forEach(button=>{
  button.addEventListener('click',()=>{
    const input=document.getElementById(button.getAttribute('data-toggle-password'));
    if(!input) return;
    const show=input.type==='password';input.type=show?'text':'password';
    button.textContent=show?'Hide':'Show';button.setAttribute('aria-label',show?'Hide password':'Show password');
  });
});
const form=document.querySelector('form[data-register]');
if(form){
  const password=form.querySelector('[name=password]');
  const confirm=form.querySelector('[name=confirm]');
  const message=form.querySelector('[data-password-message]');
  function validate(){const matching=!confirm.value||password.value===confirm.value;
    confirm.setCustomValidity(matching?'':'Passwords do not match');
    if(message) message.textContent=matching?'Password values must match. Passwords are never stored as plaintext.':'The passwords do not match.';
  }
  password.addEventListener('input',validate);confirm.addEventListener('input',validate);
}
