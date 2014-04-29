(function defineCookieInDOM() {
    // This is a workaround for Firefox not setting the cookie object in the
    // DOM when processing XSLT. See 
    // https://bugzilla.mozilla.org/show_bug.cgi?id=230214
    if (typeof (document.cookie) == 'undefined') {
	    var obj = document
            .createElementNS('http://www.w3.org/1999/xhtml', 'object');
        obj.width = 0;
        obj.height = 0;
        obj.type = 'text/html';
        obj.data = 'data:text/html;charset=utf-8,%3Cscript%3Eparent._htmlDom%3Ddocument%3C/script%3E';
        document.getElementsByTagName('body')[0].appendChild(obj);
        var _htmlDom;
        document.__defineGetter__('cookie', function getCookie() {
            return _htmlDom.cookie;
	    });
	    document.__defineSetter__('cookie', function setCookie(c) {
		    _htmlDom.cookie = c;
        });
    }
})();

var workbench = {

    // Note that the way this is currently constructed, functions added with
    // addLoad() will be executed in the order that they were added.
    //
    // @see http://onwebdevelopment.blogspot.com/2008/07/chaining-functions-in-javascript.html
    // @param fn
    //          function to add
    addLoad: function _addLoad(fn) {

        // The following is to allow composed XSLT style sheets to each add
        // functions to the window.onload event.
        function chain(args) {
            return function() {
                for ( var i = 0; i < args.length; i++) {
                    args[i]();
                }
            }
        };

        window.onload = typeof (window.onload) == 'function' ? chain([
            window.onload, fn ]) : fn;
    }
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
    var cookies = document.cookie.split(';');
    var rval = '';
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eq = cookie.indexOf('=');
        if (name == cookie.substr(0, eq).replace(/^\s+|\s+$/g, '')) {
            rval = decodeURIComponent(
                cookie.substr(eq + 1).replace(/\+/g, '%20'));
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
 * Utility method for assembling the query string for a request URL.
 * 
 * @param sb
 *            string buffer, actually an array of strings to be joined later
 * @param id
 *            name of parameter to add
 */
function addParam(sb, id) {
	sb[sb.length] = id + '=';
	var tag = document.getElementById(id);
	sb[sb.length] = (tag.type == 'checkbox') ? tag.checked : 
	    encodeURIComponent(tag.value);
	sb[sb.length] = '&';
}

/**
 * Code to run when the document loads: eliminate the 'noscript' warning
 * message, and display an unauthenticated user properly.
 */
workbench.addLoad(function() {
	var noscript = document.getElementById('noscript-message').style.display = 'none';
	var user = getCookie('server-user');
	if (user.length == 0 || user == '""') {
		user = '<span class="disabled">None</span>';
	}
	var selectedUser = document.getElementById('selected-user');
	selectedUser.innerHTML = user;
});
