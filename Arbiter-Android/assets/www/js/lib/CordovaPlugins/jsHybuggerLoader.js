if (!/content:\/\/jsHybugger.org/.test(window.location)) {
  console.log('Redirecting to jsHybugger loader');
  window.location = 'content://jsHybugger.org/' + window.location;
}