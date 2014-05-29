function handleFormatSelection(selected) {
	if (selected == 'application/x-trig' || selected == 'application/trix'
			|| selected == 'text/x-nquads') {
		$('#useForContext').prop('checked', false);
		$('#context').val('').prop('readonly', false);
	}
}

function handleBaseURIUse() {
	var readOnly = $('#useForContext').prop('checked');
	if (readOnly) {
		setContextFromBaseURI();
	}
}

function setContextFromBaseURI(){
	var baseURI = $('#baseURI').val();
	$('#context').val(baseURI == '' ? '' : '<' + baseURI + '>');	
}

function enabledInput(selected) {
    istext = (selected == 'text');
	$('#text').prop('disabled', !istext);
	var contentType = document.getElementById('Content-Type');
	contentType.options[0].disabled = istext;
	contentType.options[0].selected = !istext;
	$('#source-' + selected).prop('checked', true);
	var isfile = (selected == 'file');
	$('#file').prop('disabled', !isfile);
	var isurl = (selected == 'url');
	$('#url').prop('disabled', !isurl);
	if (istext) {
	    for (i = 1; i < contentType.options.length; i++) {
		    var option = contentType.options[i];
		    if (option.value == 'application/x-turtle'
				|| option.value == 'text/turtle') {
			    option.selected = true;
			    break;
		    }
		}
	}
	else {
	    var baseURI = $('#baseURI');
	    var checked = $('#useForContext').prop('checked');
	    if (isfile) {
		    var file = $('#file');
		    baseURI.val(file.val() == '' ? '' : encodeURI('file://'
				+ file.val().replace(/\\/g, '/')));
		    if (checked) {
			    setContextFromBaseURI();
		    }
	    }
	    else if (isurl) {
		    baseURI.val($('#url').val());
		    if (checked) {
			    setContextFromBaseURI();
		    }
	    }
	}
}
