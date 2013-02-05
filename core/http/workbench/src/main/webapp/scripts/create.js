// Prerequisite: jquery-1.9.0.min.js
// Prerequisite: template.js

function populateParameters() {
	var elements = getQueryStringElements();
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		if (pair[0] == 'id') {
			$('#id').val(value);
		}
		if (pair[0] == 'title') {
			$('#title').val(value);
		}
	}
}

/**
 * Disables the create button if the id field doesn't have any text.
 */
function disableCreateIfEmptyId() {
	if (/.+/.test($('#id').val())) {
		$('input#create').removeAttr("disabled");
	} else {
		$('input#create').attr("disabled", "disabled");
	}
}

/**
 * Calls another function with a delay of 0 msec. (Workaround for annoying
 * browser behavior.)
 */
function handleNameChange() {
	setTimeout('disableCreateIfEmptyId()', 0);
}

addLoad(function() {
	populateParameters();
	disableCreateIfEmptyId();
	$('#id').on('keydown paste cut', handleNameChange);
});