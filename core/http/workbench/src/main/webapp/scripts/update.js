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
/// <reference path="yasqeHelper.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var update;
    (function (update) {
        var yasqe = null;
        function initYasqe() {
            workbench.yasqeHelper.setupCompleters(namespaces);
            yasqe = YASQE.fromTextArea(document.getElementById('update'), {
                createShareLink: function () {
                    return { update: yasqe.getValue() };
                },
                consumeShareLink: function (yasqe, args) {
                    if (args.update)
                        yasqe.setValue(args.update);
                },
                // This way, we don't conflict with the YASQE editor of the
                // regular query interface, and we show the most recent
                // -update- query.
                persistent: "update"
            });
            // Some styling conflicts. Could add my own css file, but not a
            // lot of things need changing, so just do this programmatically.
            // First, set the font size (otherwise font is as small as menu,
            // which is too small). Second, set the width. YASQE normally
            // expands to 100%, but the use of a table requires us to set a
            // fixed width.
            $(yasqe.getWrapperElement()).css({
                "fontSize": "14px",
                "width": "900px"
            });
            // We made a change to the css wrapper element (and did so after
            // initialization). So, force a manual update of the yasqe
            // instance.
            yasqe.refresh();
            // If the text area we instantiated YASQE on has no query val,
            // then show a regular default update query.
            if (yasqe.getValue().trim().length == 0) {
                yasqe.setValue('INSERT DATA {\n\t<http://exampleSub> ' +
                    '<http://examplePred> <http://exampleObj> .\n}');
            }
        }
        update.initYasqe = initYasqe;
        /**
         * Invoked upon form submission.
         *
         * @returns {boolean} true, always
         */
        function doSubmit() {
            // Save yasqe content to text area.
            if (yasqe) {
                yasqe.save();
            }
            return true;
        }
        update.doSubmit = doSubmit;
    })(update = workbench.update || (workbench.update = {}));
})(workbench || (workbench = {}));
workbench.addLoad(function updatePageLoaded() {
    workbench.update.initYasqe();
});
//# sourceMappingURL=update.js.map