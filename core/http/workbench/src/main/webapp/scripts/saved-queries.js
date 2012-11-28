// Prerequisite: template.js

var editForms = document.getElementsByTagName('form');

addLoad(function() {
	var queries = document.getElementsByTagName('pre');
	for (i = 0; i < queries.length; i++) {
		queries[i].innerHTML = queries[i].innerHTML.trim();
	}
	
	for ( var i = 0; i < editForms.length; i++) {
		var form = editForms[i];
		var queryText = form.getElementsByTagName('input')[2];
		queryText.setAttribute('value', queryText.getAttribute('value').trim());
	}
});