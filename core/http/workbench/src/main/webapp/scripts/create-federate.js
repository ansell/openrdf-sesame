// Prerequisite: jquery-1.9.0.min.js

function respondToFormState() {
	var enoughMembers = $('input.memberID').filter(':checked').length >= 2;
	if (enoughMembers) {
		$('#create-feedback').hide();
	} else {
		$('#create-feedback').show();
	}
	var validID = /.+/.test($('#id').val());
	var disable = !(validID && enoughMembers);
	$('input#create').prop('disabled', disable);
	var sparql = $("input[name='type']:checked").val() == 'sparql';
	var readonly = $("input[name='readonly']");
	if (sparql) {
		readonly.prop('checked', true);
	}
	readonly.prop('disabled', sparql);
}

/**
 * Calls another function with a delay of 0 msec. (Workaround for annoying
 * browser behavior.)
 */
function timeoutRespond() {
	setTimeout('respondToFormState()', 0);
}

addLoad(function() {
	respondToFormState();
	$('input.memberID').on('change', respondToFormState);
	$("input[name='type']").on('change', respondToFormState);
	$('#id').off().on('keydown paste cut', timeoutRespond);
});