// Prerequisite: template.js

function populateParameters() {
	var href = document.location.href;
	var elements = href.substring(href.indexOf('?') + 1).substring(
			href.indexOf(';') + 1).split(decodeURIComponent('%26'));
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		var q = document.getElementById('query');
		if (pair[0] == 'query')
			if (!q.value) {
				q.value = value;
			}
	}
}

function textContent(element) {
	var text = element.innerText || element.textContent;
	return text.replace(/^\s*/, "").replace(/\s*$/, "");
}

var currentQueryLn;

function loadNamespaces() {
	var query = document.getElementById('query');
	var queryLn = document.getElementById('queryLn').value;
	var namespaces = document.getElementById(queryLn + '-namespaces');
	var last = document.getElementById(currentQueryLn + '-namespaces');
	if (namespaces) {
		if (!query.value) {
			query.value = namespaces.innerText || namespaces.textContent;
			currentQueryLn = queryLn;
		}

		if (last) {
			var text = last.innerText || last.textContent;
			if (query.value == text) {
				query.value = namespaces.innerText || namespaces.textContent;
				currentQueryLn = queryLn;
			}
		}
	}
}

function addClickHandlers() {
	addClickHandler('exec');
	addClickHandler('save');
}

function addClickHandler(id) {
	document.getElementById(id).onclick = function() {
		document.getElementById('action').value = id;
	}
}

function disableSaveIfNotValidName() {
	var name = document.getElementById('query-name');
	var save = document.getElementById('save');
	var valid = /^[- \w]{1,32}$/
	save.disabled = !valid.test(name.value);
}

function handleNameChange(){
    setTimeout('disableSaveIfNotValidName()', 200);	
}

function addSaveNameHandler() {
	var name = document.getElementById('query-name');
	name.onkeydown = handleNameChange;
	name.onpaste = handleNameChange;
	name.oncut = handleNameChange;
} 

addLoad(function() {
	populateParameters();
	loadNamespaces();
	addClickHandlers();
	addSaveNameHandler();
});

function addParam(sb, name, id) {
	if (!id) {
		id = name;
	}

	var tag = document.getElementById(id);
	sb[sb.length] = name;
	sb[sb.length] = '=';
	if (tag.type == "checkbox") {
		if (tag.checked) {
			sb[sb.length] = 'true';
		} else {
			sb[sb.length] = 'false';
		}
	} else {
		sb[sb.length] = encodeURIComponent(tag.value);
	}

	sb[sb.length] = '&';
}

/* MSIE6 does not like XSLT w/ this query string, so we use URL parameters. */
function doSubmit() {
	if (document.getElementById('query').value.length >= 1000) {
		// some functionality will not work as expected on result pages
		return true;
	} else { // safe to use in request-URI
		var url = [];
		url[url.length] = 'query';
		if (document.all) {
			url[url.length] = ';';
		} else {
			url[url.length] = '?';
		}

		addParam(url, 'action');
		if (document.getElementById('action').value == 'save') {
			addParam(url, 'query-name');
			addParam(url, 'save-private');
		}
		addParam(url, 'queryLn');
		addParam(url, 'query');
		addParam(url, 'limit');
		addParam(url, 'infer');
		document.location.href = url.join('');
		return false;
	}
}