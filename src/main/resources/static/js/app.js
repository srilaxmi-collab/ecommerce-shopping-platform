// Small enhancement: disable submit buttons after the first click to prevent double orders.
document.addEventListener('submit', function (e) {
  var btn = e.target.querySelector('button[data-once]');
  if (btn) { btn.disabled = true; btn.textContent = 'Please wait…'; }
});
