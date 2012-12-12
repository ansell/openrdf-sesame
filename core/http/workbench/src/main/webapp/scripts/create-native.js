// Prerequisite: template.js

function populateParameters() {
	var elements = getQueryStringElements();
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		if (pair[0] == 'id') {
			document.getElementById('id').value = value;
		}
		if (pair[0] == 'title') {
			document.getElementById('title').value = value;
		}
	}
}

addLoad(function() {
	populateParameters();
});