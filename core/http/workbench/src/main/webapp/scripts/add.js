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
    var add;
    (function (add) {
        function handleFormatSelection(selected) {
            if (selected == 'application/x-trig' || selected == 'application/trix'
                || selected == 'text/x-nquads') {
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
            var file = $('#file');
            file.prop('disabled', !isfile);
            var isurl = (selected == 'url');
            var url = $('#url');
            url.prop('disabled', !isurl);
            if (istext) {
                var turtle = contentType.find("option[value='application/x-turtle']");
                if (turtle.length == 0) {
                    turtle = contentType.find("option[value='text/turtle']");
                }
                if (turtle.length > 0) {
                    turtle.prop('selected', true);
                }
            }
            else {
                firstType.prop('selected', true);
                var baseURI = $('#baseURI');
                var checked = $('#useForContext').prop('checked');
                if (isfile) {
                    baseURI.val(file.val() == '' ? '' : encodeURI('file://'
                        + file.val().replace(/\\/g, '/')));
                    if (checked) {
                        setContextFromBaseURI();
                    }
                }
                else if (isurl) {
                    baseURI.val(url.val());
                    if (checked) {
                        setContextFromBaseURI();
                    }
                }
            }
        }
        add.enabledInput = enabledInput;
    })(add = workbench.add || (workbench.add = {}));
})(workbench || (workbench = {}));
//# sourceMappingURL=add.js.map