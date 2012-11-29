// Prerequisite: template.js

function requestDelete(urn) {
    method = 'delete';
    var form = document.createElement('form');
    form.setAttribute('method', 'delete');
    form.setAttribute('action', 'saved-queries?query=' + urn);
    form.submit();
}

function deleteQuery(user, name, urn) {
	var currentUser = getCookie("server-user");
	if ((!user || currentUser == user)) {
		if (confirm("Do you really wish to delete '" + name + "'?")) {
			requestDelete(urn);
		}
	}
}

addLoad(function() {
	var queries = document.getElementsByTagName('pre');
	for (i = 0; i < queries.length; i++) {
		queries[i].innerHTML = queries[i].innerHTML.trim();
	}
	
	var editForms = document.getElementsByTagName('form');
	for ( var i = 0; i < editForms.length; i++) {
		var form = editForms[i];
		var queryText = form.getElementsByTagName('input')[2];
		queryText.setAttribute('value', queryText.getAttribute('value').trim());
	}
});