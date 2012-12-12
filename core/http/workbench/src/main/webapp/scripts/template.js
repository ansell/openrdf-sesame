// The following bit of script is a workaround for Firefox not setting the 
// cookie object in the DOM when processing XSLT.
// See https://bugzilla.mozilla.org/show_bug.cgi?id=230214

var _htmlDom;

if (typeof (document.cookie) == 'undefined') {
	var obj = document
			.createElementNS('http://www.w3.org/1999/xhtml', 'object');
	obj.width = 0;
	obj.height = 0;
	obj.type = 'text/html';
	obj.data = 'data:text/html;charset=utf-8,%3Cscript%3Eparent._htmlDom%3Ddocument%3C/script%3E';
	document.getElementsByTagName('body')[0].appendChild(obj);
	document.__defineGetter__('cookie', function() {
		return _htmlDom.cookie;
	});
	document.__defineSetter__('cookie', function(c) {
		_htmlDom.cookie = c;
	});
}

// The following is to allow composed XSLT style sheets to each add functions to
// the window.onload event.

chain = function(args) {
	return function() {
		for ( var i = 0; i < args.length; i++) {
			args[i]();
		}
	}
};

/**
 * Note that the way this is currently constructed, functions added with
 * addLoad() will be executed in the order that they were added.
 * 
 * @see http://onwebdevelopment.blogspot.com/2008/07/chaining-functions-in-javascript.html
 * @param fn
 *            function to add
 */
function addLoad(fn) {
	window.onload = typeof (window.onload) == 'function' ? chain([
			window.onload, fn ]) : fn;
}

/**
 * Retrieves the value of the cookie with the given name.
 * 
 * @param {String}
 *            name The name of the cookie to retrieve.
 * @returns {String} The value of the given cookie, or an empty string if it
 *          doesn't exist.
 */
function getCookie(name) {
	var cookies = document.cookie.split(";");
	var rval = "";
	var i, cookie, eq, temp;
	for (i = 0; i < cookies.length; i++) {
		cookie = cookies[i];
		eq = cookie.indexOf("=");
		temp = cookie.substr(0, eq).replace(/^\s+|\s+$/g, "");
		if (name == temp) {
			rval = decodeURIComponent(cookie.substr(eq + 1))
					.replace(/\+/g, ' ');
			break;
		}
	}

	return rval;
}

/**
 * Parses workbench URL query strings into processable arrays.
 * 
 * @returns an array of the 'name=value' substrings of the URL query string
 */
function getQueryStringElements() {
	var href = document.location.href;
	return href.substring(href.indexOf('?') + 1).split(
			decodeURIComponent('%26'));
}

/**
 * Code to run when the document loads: eliminate the 'noscript' warning
 * message, and display an unauthenticated user properly.
 */
addLoad(function() {
	var noscript = document.getElementById('noscript-message').style.display = 'none';
	var user = getCookie("server-user");
	if (user.length == 0 || user == '""') {
		user = '<span class="disabled">None</span>';
	}
	var selectedUser = document.getElementById('selected-user');
	selectedUser.innerHTML = user;
});