function handleFormatSelection(selected) {
	if (selected == 'application/x-trig' || selected == 'application/trix'
			|| selected == 'text/x-nquads') {
		var checkbox = document.getElementById('useForContext');
		var context = document.getElementById('context');

		checkbox.checked = false;
		context.readOnly = false;
		context.value = '';
	}
}

function handleBaseURIUse() {
	var baseURI = document.getElementById('baseURI');
	var checkbox = document.getElementById('useForContext');
	var context = document.getElementById('context');
	if (checkbox.checked) {
		context.readOnly = true;
		if (baseURI.value != '') {
			context.value = "<" + baseURI.value + ">";
		} else {
			context.value = '';
		}
	} else {
		context.readOnly = false;
	}
}

function enabledInput(selected) {
	document.getElementById('source-' + selected).checked = true;
	document.getElementById('file').disabled = selected != 'file';
	document.getElementById('url').disabled = selected != 'url';
	document.getElementById('text').disabled = selected != 'text';

	var checkbox = document.getElementById('useForContext');
	var context = document.getElementById('context');
	var contentType = document.getElementById('Content-Type');
	if (selected == 'text') {
		contentType.options[0].disabled = true;
		contentType.options[0].selected = false;
		for (i = 1; i < contentType.options.length; i++) {
			var option = contentType.options[i];
			if (option.value == 'application/x-turtle'
					|| option.value == 'text/turtle') {
				option.selected = true;
				break;
			}
		}
	} else {
		contentType.options[0].disabled = false;
		contentType.options[0].selected = true;
	}

	var baseURI = document.getElementById('baseURI');
	var file = document.getElementById('file');
	var url = document.getElementById('url');
	if (selected == 'file') {
		if (file.value != '') {
			baseURI.value = encodeURI('file://'
					+ file.value.replace(/\\/g, '/'));
		} else {
			baseURI.value = '';
		}
		if (checkbox.checked) {
			if (baseURI.value != '') {
				context.value = "<" + baseURI.value + ">";
			} else {
				context.value = '';
			}
		}
	}
	if (selected == 'url') {
		baseURI.value = url.value;
		if (checkbox.checked) {
			if (baseURI.value != '') {
				context.value = "<" + baseURI.value + ">";
			} else {
				context.value = '';
			}
		}
	}
}