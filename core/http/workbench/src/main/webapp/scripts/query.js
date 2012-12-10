// Prerequisite: template.js

/**
 * Populate the query text area with the value of the URL query parameter, if
 * and only if it is present.
 */
function populateParameters() {
	var href = document.location.href;
	var elements = href.substring(href.indexOf('?') + 1).substring(
			href.indexOf(';') + 1).split(decodeURIComponent('%26'));
	var param = 'query';
	var query = document.getElementById(param);
	var setFromHref = false;
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		if (pair[0] == param) {
			if (!query.value) {
				query.value = value;
				setFromHref = true;
			}
		}
	}

	if (!setFromHref) {
		var cookie = getCookie(param);
		if (cookie) {
			query.value = cookie;
		}
	}
}

/**
 * Return the text content of a given element, trimmed of any leading or
 * trailing whitespace.
 */
function textContent(element) {
	var text = element.innerText || element.textContent;

	// TODO It may be possible to just use JavaScript String.trim() here.
	return text.replace(/^\s*/, "").replace(/\s*$/, "");
}

/**
 * Global variable for holding the current query language.
 */
var currentQueryLn;

/**
 * Populate reasonable default name space declarations into the query text area.
 * The server has provided the declaration text in hidden elements.
 */
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

/**
 * After confirming with the user, clears the query text and loads the current
 * repository and query language name space declarations.
 */
function resetNamespaces() {
	if (confirm('Click OK to clear the current query text and replace it with the '
			+ document.getElementById('queryLn').value
			+ ' namespace declarations.')) {
		document.getElementById('query').value = '';
		loadNamespaces();
	}
}

/**
 * Add click handlers identifying the clicked element in a hidden 'action' form
 * field.
 */
function addClickHandlers() {
	addClickHandler('exec');
	addClickHandler('save');
}

/**
 * Add a click handler to the specified element, that will set the value on a
 * hidden 'action' form field.
 * 
 * @param id
 *            the id of the element to add the click handler to
 */
function addClickHandler(id) {
	document.getElementById(id).onclick = function() {
		document.getElementById('action').value = id;
	}
}

/**
 * Clear the save feedback field, and look at the contents of the query name
 * field. Disables the save button if the field doesn't satisfy a given regular
 * expression.
 */
function disableSaveIfNotValidName() {
	var name = document.getElementById('query-name');
	var save = document.getElementById('save');
	var valid = /^[- \w]{1,32}$/
	save.disabled = !valid.test(name.value);
	clearFeedback();
}

/**
 * Clear any contents of the save feedback field.
 */
function clearFeedback() {
	var feedback = document.getElementById('save-feedback');
	feedback.className = '';
	feedback.innerHTML = '';
}

/**
 * Calls another function with a delay of 200 msec, to give enough time after
 * the event for the document to have changed. (Workaround for annoying browser
 * behavior.)
 */
function handleNameChange() {
	setTimeout('disableSaveIfNotValidName()', 200);
}

/**
 * Add event handlers to the save name field to react to changes in it.
 */
function addSaveNameHandler() {
	var name = document.getElementById('query-name');
	name.onkeydown = handleNameChange;
	name.onpaste = handleNameChange;
	name.oncut = handleNameChange;
}

/**
 * Add event handlers to the query text area to react to changes in it.
 */
function addQueryChangeHandler() {
	var query = document.getElementById('query');
	query.onkeydown = clearFeedback;
	query.onpaste = clearFeedback;
	query.oncut = clearFeedback;
}

/**
 * Trim the query text area contents of any leading and/or trailing whitespace.
 */
function trimQuery() {
	var query = document.getElementById('query');
	query.value = query.value.trim();
}

/**
 * Detect if there is no current authenticated user, and if so, disable the
 * 'save privately' option.
 */
function disablePrivateSaveForAnonymous() {
	var user = document.getElementById('selected-user').getElementsByTagName(
			'span')[0].innerHTML;
	if (user == 'None') {
		var checkbox = document.getElementById('save-private');
		checkbox.setAttribute('value', false);
		checkbox.setAttribute('disabled', 'disabled');
	}
}

/**
 * Add code to be called when the document is loaded.
 */
addLoad(function() {
	populateParameters();
	loadNamespaces();
	trimQuery();
	addClickHandlers();
	addSaveNameHandler();
	addQueryChangeHandler();
	disablePrivateSaveForAnonymous();
});

/**
 * 
 * @param sb
 * @param name
 * @param id
 */
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

/**
 * Utility method to create an XMLHTTPRequest object.
 * 
 * @returns a new object for sending an HTTP request
 */
function createXMLHttpRequest() {
	try {
		return new XMLHttpRequest();
	} catch (e) {
	}
	try {
		return new ActiveXObject("Msxml2.XMLHTTP");
	} catch (e) {
	}
	alert("XMLHttpRequest not supported");
	return null;
}

/**
 * Send a background HTTP request, and handle the response asynchronously.
 * 
 * @param url
 *            the URL to send the request to
 */
function ajaxSave(url) {
	var request = createXMLHttpRequest();
	var requestTimer = setTimeout(
			function() {
				request.abort();
				feedback.className = 'error';
				feedback.innerHTML = 'Timed out waiting for response. Uncertain if save occured.';
			}, 5000);
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			clearTimeout(requestTimer);
			var feedback = document.getElementById('save-feedback');
			if (request.status == 200) {
				var response = JSON.parse(request.responseText);
				if (response.accessible) {
					if (response.written) {
						feedback.className = 'success';
						feedback.innerHTML = 'Query saved.';
					} else {
						if (response.existed) {
							if (confirm('Query name exists. Click OK to overwrite.')) {
								ajaxSave(url + 'overwrite=true&');
							} else {
								feedback.className = 'error';
								feedback.innerHTML = 'Cancelled overwriting existing query.';
							}
						}
					}
				} else {
					feedback.className = 'error';
					feedback.innerHTML = 'Repository was not accessible (check your permissions).';
				}
			} else {
				feedback.className = 'error';
				feedback.innerHTML = 'Failure: Response Status = '
						+ request.status;
			}
		}
	};
	request.open("post", url, true); // true => async handling
	request.send(); // noarg => all data in URL
}

/**
 * Handle form submission. Note: MSIE6 does not like XSLT w/ this query string,
 * so we use URL parameters.
 */
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
		var save = (document.getElementById('action').value == 'save');
		if (save) {
			addParam(url, 'query-name');
			addParam(url, 'save-private');
		}
		addParam(url, 'queryLn');
		addParam(url, 'query');
		addParam(url, 'limit');
		addParam(url, 'infer');
		var href = url.join('');
		if (save) {
			ajaxSave(href);
		} else {
			document.location.href = href;
		}
		return false;
	}
}