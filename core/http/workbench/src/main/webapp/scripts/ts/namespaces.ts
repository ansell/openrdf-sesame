/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />

module workbench {

    export module namespaces {

        export function updatePrefix() {
            var select = $('#prefix-select');
            $('#prefix').val(select.find('option:selected').text());
            $('#namespace').val(select.val());
        }
    }
}