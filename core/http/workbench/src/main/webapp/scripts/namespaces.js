// Prerequisite: jquery
// Prerequisite: template.js

workbench.namespaces = {
    updatePrefix : function _updatePrefix() {
        var select = $('#prefix-select');
        $('#prefix').val(select.find('option:selected').text());
        $('#namespace').val(select.val());
    }
}
