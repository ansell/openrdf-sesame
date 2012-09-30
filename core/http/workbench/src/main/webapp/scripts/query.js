function populateParameters() {
	var href = document.location.href;
	var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
	for (var i=0;elements.length-i;i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		var q = document.getElementById('query');
		if (pair[0] == 'query') if (!q.value) {
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

window.onload = function() {
	populateParameters();
	loadNamespaces();
}

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

/* MSIE6 does not like xslt w/ this querystring, so we use url parameters. */
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

		addParam(url, 'queryLn');
		addParam(url, 'query');
		addParam(url, 'limit');
		addParam(url, 'infer');
		document.location.href = url.join('');
		return false;
	}
}