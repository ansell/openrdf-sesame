/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
var workbench;
(function (workbench) {
    (function (namespaces) {
        function updatePrefix() {
            var select = $('#prefix-select');
            $('#prefix').val(select.find('option:selected').text());
            $('#namespace').val(select.val());
        }
        namespaces.updatePrefix = updatePrefix;
    })(workbench.namespaces || (workbench.namespaces = {}));
    var namespaces = workbench.namespaces;
})(workbench || (workbench = {}));
//# sourceMappingURL=namespaces.js.map
