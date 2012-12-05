// Prerequisite: template.js

function deleteQuery(savedBy, name, urn) {
	var currentUser = getCookie("server-user");
	if ((!savedBy || currentUser == savedBy)) {
		if (confirm("Do you really wish to delete '" + name + "'?")) {
			document.forms[urn].submit();
		}
	} else {
		alert("'" + name + "' was saved by user '" + savedBy + "'.\nUser '"
				+ currentUser + "' is not allowed do delete it.");
	}
}

function toggle(urn) {
	var rows = document.getElementsByTagName('tr');
	var clazz = urn + '-data';
	for (var i = 0; i < rows.length; i++) {
		row = rows[i];
		if (row.getAttribute('class') == clazz) {
			row.style.display = (row.style.display == 'none') ? '' : 'none';
		}
	}
	
	var toggle = document.getElementById(urn + '-toggle');
	var attr = 'value';
	var show = 'Show';
	var text = toggle.getAttribute(attr) == show ? 'Hide' : show;
	toggle.setAttribute(attr, text);
}

addLoad(function() {
	var queries = document.getElementsByTagName('pre');
	for (i = 0; i < queries.length; i++) {
		queries[i].innerHTML = queries[i].innerHTML.trim();
	}

	var editForms = document.getElementsByName('edit-query');
	for ( var i = 0; i < editForms.length; i++) {
		var form = editForms[i];
		var queryText = form.getElementsByTagName('input')[2];
		queryText.setAttribute('value', queryText.getAttribute('value').trim());
	}
});