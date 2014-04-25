// Prerequisite: template.js

/**
 * Global variable for holding the current query language.
 */
var currentQueryLn;

/**
 * Populate reasonable default name space declarations into the query text area.
 * The server has provided the declaration text in hidden elements.
 */
function loadNamespaces() {
	var query = $('#query');
	var queryLn = $('#queryLn').val();
	var namespaces = $('#' + queryLn + '-namespaces');
	var last = $('#' + currentQueryLn + '-namespaces');
	
	function toggleNamespaces() {
	    query.val(namespaces.text());
	    currentQueryLn = queryLn;
    }
	
	if (namespaces.length) {
		if (!query.val()) {;
		    toggleNamespaces();
		}

		if (last.length && (query.val() == last.text())) {
            toggleNamespaces();
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
 * Clear any contents of the save feedback field.
 */
function clearFeedback() {
	var feedback = document.getElementById('save-feedback');
	feedback.className = '';
	feedback.innerHTML = '';
}

/**
 * Clear the save feedback field, and look at the contents of the query name
 * field. Disables the save button if the field doesn't satisfy a given regular
 * expression. With a delay of 200 msec, to give enough time after
 * the event for the document to have changed. (Workaround for annoying browser
 * behavior.)
 */
function handleNameChange() {
	setTimeout( function disableSaveIfNotValidName() {
	    var name = document.getElementById('query-name');
	    var save = document.getElementById('save');
	    var valid = /^[- \w]{1,32}$/
	    save.disabled = !valid.test(name.value);
	    clearFeedback();
    }, 200);
}

addLoad( function queryPageLoaded() {
    /**
     * Gets a parameter from the URL or the cookies, preferentially in that 
     * order.
     * 
     * @param param
     *            the name of the parameter
     * @returns the value of the given parameter, or something that evaluates
                  as false, if the parameter was not found
     */
    function getParameterFromUrlOrCookie(param) {
	    var href = document.location.href;
	    var elements = href.substring(href.indexOf('?') + 1).substring(
			href.indexOf(';') + 1).split(decodeURIComponent('%26'));
	    var result = false;
	    for ( var i = 0; elements.length - i; i++) {
		    var pair = elements[i].split('=');
		    var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		    if (pair[0] == param) {
                result = value;
		    }
	    }
	    if (!result) {
		    result = getCookie(param);
	    }
	    return result;
    }

    function getQueryTextFromServer(queryParam, refParam) {
	    $.getJSON('query', {
		    action : "get",
		    query : queryParam,
		    ref : refParam
	    }, function(response) {
		    if (response.queryText) {
			    $('#query').val(response.queryText);
		    }
	    });
    }

    // Populate the query text area with the value of the URL query parameter,
    // only if it is present. If it is not present in the URL query, then 
    // looks for the 'query' cookie, and sets it from that. (The cookie
    // enables re-populating the text field with the previous query when the
    // user returns via the browser back button.)
	var query = getParameterFromUrlOrCookie('query');
	if (query) {
		var ref = getParameterFromUrlOrCookie('ref');
		if (ref == 'id' || ref == 'hash') {
			getQueryTextFromServer(query, ref);
		} else {
			$('#query').val(query);
		}
	}

    loadNamespaces();
    
    // Trim the query text area contents of any leading and/or trailing 
    // whitespace.
	var query = $('#query');
	query.val($.trim(query.val()));
	
	// Add click handlers identifying the clicked element in a hidden 'action' 
	// form field.
    addHandler = function(id) {
	    $('#' + id).click( function setAction() { $('#action').val(id); });
    }
    
	addHandler('exec');
	addHandler('save');

    // Add event handlers to the save name field to react to changes in it.
	$('#query-name').bind('keydown cut paste', handleNameChange);

    // Add event handlers to the query text area to react to changes in it.
    query.bind('keydown cut paste', clearFeedback);

    // Detect if there is no current authenticated user, and if so, disable
    // the 'save privately' option.
    if ($('#selected-user>span').is('.disabled')) {
		$('#save-private').prop('checked', false).prop('disabled', true);
	}
});

function doSubmit() {

    /**
     * Send a background HTTP request to save the query, and handle the
     * response asynchronously.
     * 
     * @param overwrite
     *            if true, add a URL parameter that tells the server we wish
     *            to overwrite any already saved query
     */
    function ajaxSave(overwrite) {
	    var feedback = $('#save-feedback');
	    var url = [];
	    url[url.length] = 'query';
	    if (overwrite) {
		    url[url.length] = document.all ? ';' : '?';
		    url[url.length] = 'overwrite=true&'
	    }
	    var href = url.join('');
	    var form = $('form[action="query"]');
	    $.ajax({
		    url : href,
		    type : 'POST',
		    dataType : 'json',
		    data : form.serialize(),
		    timeout : 5000,
		    error : function(jqXHR, textStatus, errorThrown) {
		        feedback.removeClass().addClass('error');
		        if (textStatus == 'timeout') {
			        feedback
					    .text('Timed out waiting for response. Uncertain if save occured.');
		        } else {
			        feedback.text('Save Request Failed: Error Type = ' + textStatus
					    + ', HTTP Status Text = "' + errorThrown + '"');
		        }
	        },
		    success : function(response) {
		        if (response.accessible) {
			        if (response.written) {
				        feedback.removeClass().addClass('success');
				        feedback.text('Query saved.');
			        } else {
				        if (response.existed) {
					        if (confirm('Query name exists. Click OK to overwrite.')) {
						        ajaxSave(true);
					        } else {
						        feedback.removeClass().addClass('error');
						        feedback.text('Cancelled overwriting existing query.');
					        }
				        }
			        }
		        } else {
			        feedback.removeClass().addClass('error');
			        feedback
					    .text('Repository was not accessible (check your permissions).');
		        }
	        }
	    });
    }

	var allowPageToSubmitForm = false;
	var save = ($('#action').val() == 'save');
	if (save) {
		ajaxSave(false);
	} else {
		var url = [];
		url[url.length] = 'query';
		if (document.all) {
			url[url.length] = ';';
		} else {
			url[url.length] = '?';
		}
		addParam(url, 'action');
		addParam(url, 'queryLn');
		addParam(url, 'query');
		addParam(url, 'limit');
		addParam(url, 'infer');
		var href = url.join('');
		var loc = document.location;
		var currentBaseLength = loc.href.length - loc.pathname.length
				- loc.search.length;
		var pathLength = href.length;
		var urlLength = pathLength + currentBaseLength;

		// Published Internet Explorer restrictions on URL length, which are the
		// most restrictive of the major browsers.
		if (pathLength > 2048 || urlLength > 2083) {
			alert("Due to its length, your query will be posted in the request body. "
					+ "It won't be possible to use a bookmark for the results page.");
			allowPageToSubmitForm = true;
		} else {
			// GET using the constructed URL, method exits here
			document.location.href = href;
		}
	}

	// Value returned to form submit event. If not true, prevents normal form
	// submission.
	return allowPageToSubmitForm;
}
