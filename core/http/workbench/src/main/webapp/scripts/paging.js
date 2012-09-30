// The following bit of script is a workaround for Firefox not setting the 
// cookie object in the DOM when processing XSLT.
// See https://bugzilla.mozilla.org/show_bug.cgi?id=230214

var _htmlDom;

if (typeof(document.cookie) == 'undefined') {
	var obj = document.createElementNS('http://www.w3.org/1999/xhtml', 'object');
	obj.width = 0;
	obj.height = 0;
	obj.type = 'text/html';
	obj.data = 'data:text/html;charset=utf-8,%3Cscript%3Eparent._htmlDom%3Ddocument%3C/script%3E';
	document.getElementsByTagName('body')[0].appendChild(obj);
	document.__defineGetter__('cookie', function() { return _htmlDom.cookie; });
	document.__defineSetter__('cookie', function(c) { _htmlDom.cookie = c; });
}

/**
 * First, adds the given parameter to the URL query string. Second, adds a 
 * 'know_total' parameter if its current value is 'false' or non-existent.
 * Third, simplifies the URL. Fourth, sends the browser to the modified URL. 
 * 
 * @param {String} name The name of the query parameter.
 * @param {number or string} value The value of the query parameter.
 */
function addParam(name, value) {
	var url = document.location.href;
	var hasParams = (url.indexOf('?') + 1 || url.indexOf(';') + 1);
	var amp = decodeURIComponent('%26');
	var sep =  hasParams ? amp : ';';
	url = url + sep + name + '=' + value;
	var know_total = getParameter('know_total');
	if ('false' == know_total || know_total.length == 0) {
		url = url + amp + 'know_total=' + getTotalResultCount();
	} 
	
	document.location.href = simplifyParameters(url);
}

/**
 * Retrieves the value of the cookie with the given name.
 * @param {String} name The name of the cookie to retrieve.
 * @returns {String} The value of the given cookie, or an empty string if it
 * doesn't exist.
 */
function getCookie(name) {
	var cookies = document.cookie.split(";");
	var rval = "";
	var i,cookie,eq,temp;
	for (i=0; i < cookies.length; i++) {
	    cookie = cookies[i];
	    eq = cookie.indexOf("=");
		temp = cookie.substr(0, eq).replace(/^\s+|\s+$/g,"");
		if (name == temp) {
		    rval = unescape(cookie.substr(eq + 1));
		    break;
		}
	}
  				
	return rval;
}

/**
 * Changes the limit query parameter, and navigates to the new URL.
 */
function addLimit() {
	addParam('limit', document.getElementById('limit').value);
}
			
/**
 * Increments the offset query parameter, and navigates to the new URL.
 */
function nextOffset() {
    var offset = getOffset() + getLimit();
    addParam('offset', offset);
}

/**
 * Decrements the offset query parameter, and navigates to the new URL.
 */
function previousOffset() {
    var offset = Math.max(0, getOffset() - getLimit());
    addParam('offset', offset);
}

/**
 * @returns {number} The value of the offset query parameter.
 */
function getOffset() {
    var offset = getParameter('offset');
    return ('' == offset) ? 0 : parseInt(offset, 10);
}

/**
 * @returns {number} The value of the limit query parameter.
 */
function getLimit() {
	var limit = document.getElementById('limit').value;
	return parseInt(limit, 10);
}

/**
 * Retrieves the query parameter with the given name.
 * 
 * @param {String} name The name of the parameter to retrieve.
 * @returns {String} The value of the given parameter, or an empty string if
 * it doesn't exist.
 */
function getParameter(name) {
	var rval = '';
	var href = document.location.href;
	var elements = tailAfter(tailAfter(href, '?'), ';');
	elements = elements.split(decodeURIComponent('%26'));
	for (var i=0;elements.length-i;i++) {
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
 * @param {String} value The string to get the tail of.
 * @param split character to give tail after
 * @returns The substring after the 'split' character, or the original string
 * if 'split' is not found.
 */
function tailAfter(value, split) {
	return value.substring(value.indexOf(split) + 1);
}

/**
 * Scans the given URI for duplicate query parameter names, and removes all
 * but the last occurence for any duplicate case.
 * @param {Strng} href The URI to simplify.
 * @returns {String} The URI with only the last occurrence of any given 
 * parameter name remaining.
 */
function simplifyParameters(href) {
	var params = new Object();
	var rval = '';
	var elements = tailAfter(tailAfter(href, '?'), ';');
	var start = href.substring(0, href.indexOf(elements));
	elements = elements.split(decodeURIComponent('%26'));
	for (var i=0;elements.length-i;i++) {
		var pair = elements[i].split('=');
		params[pair[0]] = pair[1];
		
		// Keep looping. We are interested in the last value.
	}
	
	var amp = decodeURIComponent('%26');
	for (var name in params) {
	    // use hasOwnProperty to filter out keys from the Object.prototype
	    if (params.hasOwnProperty(name)) {
	    	rval = rval + name + '=' + params[name] + amp;
	    }
	}
	
	rval = start + rval.substring(0, rval.length - 1);
				
	return rval;
}

/**
 * Using the value of the 'limit' query parameter, correct the text of the
 * Next and Previous buttons.
 */
function correctButtons() {
    var limit = getLimit();
    var nextButton = document.getElementById('nextX');
    var previousButton = document.getElementById('previousX');
   
    // Using RegExp to preserve any localization.
	var buttonWordPattern = /^[A-z]+\s+/
    var buttonNumberPattern = /\d+$/
    var oldNext = nextButton.value;
    var count = parseInt(buttonNumberPattern.exec(oldNext), 10);
    nextButton.value = buttonWordPattern.exec(oldNext) + limit;
    previousButton.value = 
        buttonWordPattern.exec(previousButton.value) + limit;
    if (getOffset() <= 0 || limit <= 0) {
        previousButton.disabled = true;
    }

    if (count < limit || limit <= 0) {
    	nextButton.disabled = true;
    }
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
		s_trc = getCookie('total_result_count');		
	}

	if (s_trc.length > 0) {
		total_result_count = parseInt(s_trc, 10);
	}
	
	return total_result_count;
}