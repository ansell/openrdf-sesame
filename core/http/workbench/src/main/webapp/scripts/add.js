/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
var workbench;
(function (workbench) {
    (function (add) {
        function handleFormatSelection(selected) {
            if (selected == 'application/x-trig' || selected == 'application/trix' || selected == 'text/x-nquads') {
                $('#useForContext').prop('checked', false);
                $('#context').val('').prop('readonly', false);
            }
        }
        add.handleFormatSelection = handleFormatSelection;

        function setContextFromBaseURI() {
            var baseURI = $('#baseURI').val();
            $('#context').val(baseURI == '' ? '' : '<' + baseURI + '>');
        }

        function handleBaseURIUse() {
            if ($('#useForContext').prop('checked')) {
                setContextFromBaseURI();
            }
        }
        add.handleBaseURIUse = handleBaseURIUse;

        function enabledInput(selected) {
            var istext = (selected == 'text');
            $('#text').prop('disabled', !istext);
            var contentType = $('#Content-Type');
            var firstType = contentType.find('option:first');
            firstType.prop('disabled', true);
            $('#source-' + selected).prop('checked', true);
            var isfile = (selected == 'file');
            $('#file').prop('disabled', !isfile);
            var isurl = (selected == 'url');
            $('#url').prop('disabled', !isurl);
            if (istext) {
                var turtle = contentType.find("option[value='application/x-turtle']");
                if (turtle.length == 0) {
                    turtle = contentType.find("option[value='text/turtle']");
                }
                if (turtle.length > 0) {
                    turtle.prop('selected', true);
                }
            } else {
                firstType.prop('selected', true);
                var baseURI = $('#baseURI');
                var checked = $('#useForContext').prop('checked');
                if (isfile) {
                    var file = $('#file');
                    baseURI.val(file.val() == '' ? '' : encodeURI('file://' + file.val().replace(/\\/g, '/')));
                    if (checked) {
                        setContextFromBaseURI();
                    }
                } else if (isurl) {
                    baseURI.val($('#url').val());
                    if (checked) {
                        setContextFromBaseURI();
                    }
                }
            }
        }
        add.enabledInput = enabledInput;
    })(workbench.add || (workbench.add = {}));
    var add = workbench.add;
})(workbench || (workbench = {}));
//# sourceMappingURL=add.js.map
