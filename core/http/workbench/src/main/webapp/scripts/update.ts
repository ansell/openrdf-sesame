/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />

module workbench {
  
  export module update {

	export function doSubmit() {
		if ($('#update').text().length >= 1000) {
			// Too long to put in URL for a GET request. Instead, POST.
			// Browser back-button may not work as expected.
			return true;
		} else { // safe to use in request-URI
			var url = [];
			url[url.length] = 'update';
			if (document.all) {
				url[url.length] = ';';
			} else {
				url[url.length] = '?';
			}
			workbench.addParam(url, 'queryLn');
			workbench.addParam(url, 'update');
			workbench.addParam(url, 'limit');
			workbench.addParam(url, 'infer');
			url[url.length - 1] = '';
			document.location.href = url.join('');
			return false;
		}
	}
  }
}

workbench.addLoad(function updatePageLoaded() {
	// Populate parameters
	var elements = workbench.getQueryStringElements();
	var update = $('#update');
	for ( var i = 0; elements.length - i; i++) {
		var pair = elements[i].split('=');
		var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
		if (pair[0] == 'update') {
			if (!update.text()) {
				update.text(value);
			}
		}
	}

	// Load URI namespace->prefix mappings into text area (could do this at
	// XSLT-processing time, but that would break the logic of the above code,
	// which looks to see if the text area is empty before populating it with
	// the contents of the update parameter.
	var namespaces = $('#SPARQL-namespaces');
	if (!update.text()) {
		update.text(namespaces.text());
	}
});
