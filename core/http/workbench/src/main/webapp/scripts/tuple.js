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
/// <reference path="paging.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit
// the corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
workbench.addLoad(function () {
    var query = 'query';
    var suffix = '_' + query;
    var limitParam = workbench.paging.LIMIT + suffix;
    var limitElement = $(workbench.paging.LIM_ID + suffix);
    function setElement(num) {
        limitElement.val(String(parseInt(0 + num, 10)));
    }
    setElement(workbench.paging.hasQueryParameter(limitParam) ?
        workbench.paging.getQueryParameter(limitParam) :
        workbench.getCookie(limitParam));
    workbench.paging.correctButtons(query);
    var limit = workbench.paging.getLimit(query); // Number
    // Modify title to reflect total_result_count cookie
    if (limit > 0) {
        var h1 = document.getElementById('title_heading');
        var total_result_count = workbench.paging.getTotalResultCount();
        var have_total_count = (total_result_count > 0);
        var offset = workbench.paging.getOffset();
        var first = offset + 1;
        var last = offset + limit;
        last = have_total_count ? Math.min(total_result_count, last) : last;
        var newHTML = /^.*\(/.exec(h1.innerHTML)[0] + first + '-' + last;
        if (have_total_count) {
            newHTML = newHTML + ' of ' + total_result_count;
        }
        newHTML = newHTML + ')';
        h1.innerHTML = newHTML;
    }
    workbench.paging.setShowDataTypesCheckboxAndSetChangeEvent();
});
//# sourceMappingURL=tuple.js.map