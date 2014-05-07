// Prerequisite: template.js
// Prerequisite: jquery

workbench.paging = {

	/**
	 * Invoked in graph.xsl and tuple.xsl for download functionality. Takes a
	 * document element by name, and creates a request with it as a parameter.
	 */
	addGraphParam : function _addGraphParam(name) {
		var value = document.getElementById(name).value;
		var url = document.location.href;
		if (url.indexOf('?') + 1 || url.indexOf(';') + 1) {
			document.location.href = url + decodeURIComponent('%26') + name
					+ '=' + encodeURIComponent(value);
		} else {
			document.location.href = url + ';' + name + '='
					+ encodeURIComponent(value);
		}
	},

	/**
	 * First, adds the given parameter to the URL query string. Second, adds a
	 * 'know_total' parameter if its current value is 'false' or non-existent.
	 * Third, simplifies the URL. Fourth, sends the browser to the modified URL.
	 * 
	 * @param {String}
	 *            name The name of the query parameter.
	 * @param {number
	 *            or string} value The value of the query parameter.
	 */
	addPagingParam : function _addPagingParam(name, value) {

		/**
		 * Scans the given URI for duplicate query parameter names, and removes
		 * all but the last occurrence for any duplicate case.
		 * 
		 * @param {String}
		 *            href The URI to simplify.
		 * @returns {String} The URI with only the last occurrence of any given
		 *          parameter name remaining.
		 */
		function simplifyParameters(href) {
			var params = new Object();
			var rval = '';
			var elements = tailAfter(tailAfter(href, '?'), ';');
			var start = href.substring(0, href.indexOf(elements));
			elements = elements.split(decodeURIComponent('%26'));
			for ( var i = 0; elements.length - i; i++) {
				var pair = elements[i].split('=');
				params[pair[0]] = pair[1];
				// Keep looping. We are interested in the last value.
			}

			var amp = decodeURIComponent('%26');
			for ( var name in params) {
				// use hasOwnProperty to filter out keys from the
				// Object.prototype
				if (params.hasOwnProperty(name)) {
					rval = rval + name + '=' + params[name] + amp;
				}
			}

			rval = start + rval.substring(0, rval.length - 1);
			return rval;
		}

		var url = document.location.href;
		var hasParams = (url.indexOf('?') + 1 || url.indexOf(';') + 1);
		var amp = decodeURIComponent('%26');
		var sep = hasParams ? amp : ';';
		url = url + sep + name + '=' + value;
		var know_total = getParameter('know_total');
		if ('false' == know_total || know_total.length == 0) {
			url = url + amp + 'know_total=' + getTotalResultCount();
		}

		document.location.href = simplifyParameters(url);
	},

	/**
	 * Invoked in tuple.xsl and explore.xsl. Changes the limit query parameter,
	 * and navigates to the new URL.
	 */
	addLimit : function _addLimit() {
		workbench.paging.addPagingParam('limit', $('#limit').val());
	},

	/**
	 * Increments the offset query parameter, and navigates to the new URL.
	 */
	nextOffset : function _nextOffset() {
		var offset = workbench.paging.getOffset() + workbench.paging.getLimit();
		workbench.paging.addPagingParam('offset', offset);
	},

	/**
	 * Decrements the offset query parameter, and navigates to the new URL.
	 */
	previousOffset : function _previousOffset() {
		var wbp = workbench.paging; // for easier reading
		var offset = Math.max(0, wbp.getOffset() - wbp.getLimit());
		wbp.addPagingParam('offset', offset);
	},

	/**
	 * @returns {number} The value of the offset query parameter.
	 */
	getOffset : function _getOffset() {
		var offset = getParameter('offset');
		return ('' == offset) ? 0 : parseInt(offset, 10);
	},

	/**
	 * @returns {number} The value of the limit query parameter.
	 */
	getLimit : function _getLimit() {
		return parseInt($('#limit').val(), 10);
	}
}

/**
 * Retrieves the query parameter with the given name.
 * 
 * @param {String}
 *            name The name of the parameter to retrieve.
 * @returns {String} The value of the given parameter, or an empty string if it
 *          doesn't exist.
 */
function getParameter(name) {
	var rval = '';
	var href = document.location.href;
	var elements = tailAfter(tailAfter(href, '?'), ';');
	elements = elements.split(decodeURIComponent('%26'));
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		if (name != pair[0]) {
			continue;
		}

		rval = pair[1];
		// Keep looping. We are interested in the last value.
	}

	return rval;
}

/**
 * Convenience function for returning the tail of a string after a given
 * character.
 * 
 * @param {String}
 *            value The string to get the tail of.
 * @param split
 *            character to give tail after
 * @returns The substring after the 'split' character, or the original string if
 *          'split' is not found.
 */
function tailAfter(value, split) {
	return value.substring(value.indexOf(split) + 1);
}

/**
 * Using the value of the 'limit' query parameter, correct the text of the Next
 * and Previous buttons. Makes use of RegExp to preserve any localization.
 */
function correctButtons() {
	var buttonWordPattern = /^[A-z]+\s+/
	var nextButton = $('#nextX');
	var oldNext = nextButton.val();
	var count = parseInt(/\d+$/.exec(oldNext), 10);
	var limit = workbench.paging.getLimit();
	nextButton.val(buttonWordPattern.exec(oldNext) + limit);
	var previousButton = $('#previousX');
	previousButton.val(buttonWordPattern.exec(previousButton.val()) + limit);
	var offset = workbench.paging.getOffset();
	previousButton.prop('disabled', (offset <= 0 || limit <= 0));
	nextButton
			.prop(
					'disabled',
					(count < limit || limit <= 0 || (offset + count) >= getTotalResultCount()));
}

/**
 * Gets the total result count, preferably from the 'know_total' query
 * parameter. If the parameter doesn't exist, get it from the
 * 'total_result_count' cookie.
 * 
 * @returns {Number} The given total result count, or zero if it isn't given.
 */
function getTotalResultCount() {
	var total_result_count = 0;
	var s_trc = getParameter('know_total');
	if (s_trc.length == 0) {
		s_trc = workbench.getCookie('total_result_count');
	}

	if (s_trc.length > 0) {
		total_result_count = parseInt(s_trc, 10);
	}

	return total_result_count;
}

function hideExternalLinksAndSetHoverEvent() {
	$('span.resource:has(span.resourceURL)').each(function(index) {
		var externalLink = $(this).find('span.resourceURL');
		externalLink.css('margin-left', '8px').css('visibility', 'hidden');
		$(this).hover(function() {
			externalLink.css('visibility', 'visible');
		}, function() {
			externalLink.css('visibility', 'hidden');
		});
	})
}

function setDataTypeVisibility(show) {
	function setCookie(c_name, value, exdays) {
		var exdate = new Date();
		exdate.setDate(exdate.getDate() + exdays);
		var c_value = escape(value)
				+ ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
		document.cookie = c_name + "=" + c_value;
	}

	setCookie('show-datatypes', show, 365);
	var data = show ? 'data-longform' : 'data-shortform';
	$('span.resource[' + data + ']').each(function(index) {
		var newform = decodeURIComponent($(this).attr(data));
		$(this).find('a:first').text(newform);
	});
}

function setShowDataTypesCheckboxAndSetChangeEvent() {
	var hideDataTypes = (workbench.getCookie('show-datatypes') == 'false');
	var showDTcb = $("input[name='show-datatypes']");
	if (hideDataTypes) {
		showDTcb.prop('checked', false);
		setDataTypeVisibility(false);
	}
	showDTcb.on('change', function() {
		setDataTypeVisibility(showDTcb.prop('checked'));
	});
}
