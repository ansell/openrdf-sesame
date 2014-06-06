/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />

module workbench {

    export module savedQueries {

        export function deleteQuery(savedBy: string, name: string, urn: string) {
            var currentUser = workbench.getCookie("server-user");
            if ((!savedBy || currentUser == savedBy)) {
                if (confirm("'"
                    + name
                    + "' will no longer be accessible, even using your browser's history. "
                    + "Do you really wish to delete it?")) {
                    (<HTMLFormElement>document.forms.namedItem(urn)).submit();
                }
            } else {
                alert("'" + name + "' was saved by user '" + savedBy + "'.\nUser '"
                    + currentUser + "' is not allowed do delete it.");
            }
        }

        function toggleElement(urn: string, suffix: string) {
            var htmlElement = document.getElementById(urn + suffix)
	    htmlElement.style.display = (htmlElement.style.display == 'none') ?
            '' : 'none';
        }

        export function toggle(urn: string) {
            toggleElement(urn, '-metadata');
            toggleElement(urn, '-text');
            var toggle = document.getElementById(urn + '-toggle');
            var attr = 'value';
            var show = 'Show';
            var text = toggle.getAttribute(attr) == show ? 'Hide' : show;
            toggle.setAttribute(attr, text);
        }
    }
}

workbench
    .addLoad(function() {
        // not using jQuery.html(...) for this since it doesn't do the 
        // whitespace correctly
        var queries = document.getElementsByTagName('pre');
        for (var i = 0; i < queries.length; i++) {
            queries[i].innerHTML = queries[i].innerHTML.trim();
        }

        $('[name="edit-query"]').find('[name="query"]').each(function() {
            $(this).attr('value', $(this).attr('value').trim());
        });
    });
