/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var create;
    (function (create) {
        create.id = $('#id');
    })(create = workbench.create || (workbench.create = {}));
})(workbench || (workbench = {}));
/**
 * Invoked by the "Create" button on the form for all but
 * create-federate.xsl. Checks with the InfoServlet for the user-provided id
 * for the existence of the id already, giving a chance to back out if it
 * does. Depends on the current behavior of getting a failure response (500
 * Internal Server Error at present), when the ID does not exist.
 */
function checkOverwrite() {
    var submit = false;
    $.ajax({
        url: '../' + workbench.create.id.val() + '/info',
        async: false,
        success: function () {
            submit = confirm('WARNING: You are about to overwrite the ' +
                'configuration of an existing repository!');
        },
        statusCode: {
            500: function () {
                submit = true;
            }
        }
    });
    if (submit) {
        $("form[action='create']").submit();
    }
}
workbench.addLoad(function createPageLoaded() {
    /**
     * Disables the create button if the id field doesn't have any text.
     */
    function disableCreateIfEmptyId() {
        $('input#create').prop('disabled', !(/.+/.test($('#id').val())));
    }
    // Populate parameters
    var elements = workbench.getQueryStringElements();
    for (var i = 0; elements.length - i; i++) {
        var pair = elements[i].split('=');
        var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
        if (pair[0] == 'id') {
            workbench.create.id.val(value);
        }
        if (pair[0] == 'title') {
            $('#title').val(value);
        }
    }
    disableCreateIfEmptyId();
    // Calls another function with a delay of 0 msec. (Workaround for 
    // annoying browser behavior.)
    $('#id').on('keydown paste cut', function () {
        setTimeout(disableCreateIfEmptyId, 0);
    });
});
//# sourceMappingURL=create.js.map