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
/// <reference path="yasqe.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts sub-folder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var yasqeHelper;
    (function (yasqeHelper) {
        function setupCompleters(namespaces) {
            var newPrefixCompleterName = "customPrefixCompleter";
            //take the current prefix completer as base, to present our own namespaces for prefix autocompletion
            YASQE.registerAutocompleter(newPrefixCompleterName, function (yasqe, name) {
                //also, auto-append prefixes if needed
                yasqe.on("change", function () {
                    YASQE.Autocompleters.prefixes.appendPrefixIfNeeded(yasqe, name);
                });
                return {
                    bulk: true,
                    async: false,
                    autoShow: true,
                    get: function () {
                        var completerArray = [];
                        for (var key in namespaces) {
                            completerArray.push(key + " <" + namespaces[key] + ">");
                        }
                        return completerArray;
                    },
                    isValidCompletionPosition: function () {
                        return YASQE.Autocompleters.prefixes.isValidCompletionPosition(yasqe);
                    },
                    preProcessToken: function (token) {
                        return YASQE.Autocompleters.prefixes.preprocessPrefixTokenForCompletion(yasqe, token);
                    }
                };
            });
            //i.e., disable the property/class autocompleters
            YASQE.defaults.autocompleters = [newPrefixCompleterName, "variables"];
        }
        yasqeHelper.setupCompleters = setupCompleters;
    })(yasqeHelper = workbench.yasqeHelper || (workbench.yasqeHelper = {}));
})(workbench || (workbench = {}));
//# sourceMappingURL=yasqeHelper.js.map