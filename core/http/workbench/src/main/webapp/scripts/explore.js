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
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
workbench.addLoad(function () {
    function removeDuplicates(self) {
        function textContent(element) {
            return $.trim(element.innerText || element.textContent);
        }
        var lists = document.getElementsByTagName('ul');
        for (var i = lists.length - 1; i + 1; i--) {
            var items = lists[i].getElementsByTagName('li');
            for (var j = items.length - 1; j; j--) {
                var text = textContent(items[j]);
                if (items[j].innerHTML == items[j - 1].innerHTML || text == self) {
                    items[j].parentNode.removeChild(items[j]);
                }
            }
            text = textContent(items[0]);
            if (text == self) {
                items[0].parentNode.removeChild(items[0]);
            }
            if (items.length == 0) {
                lists[i].parentNode.parentNode.removeChild(lists[i].parentNode);
            }
        }
    }
    // Populate parameters
    var elements = workbench.getQueryStringElements();
    var resource = $('#resource');
    var suffix = '_explore';
    var limit_param = workbench.paging.LIMIT + suffix;
    var limit_id = workbench.paging.LIM_ID + suffix;
    var limit_param_found = false;
    for (var i = 0; elements.length - i; i++) {
        var pair = elements[i].split('=');
        var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
        if ('resource' == pair[0]) {
            resource.val(value);
        }
        else if (limit_param == pair[0]) {
            $(limit_id).val(value);
            limit_param_found = true;
        }
    }
    if (!limit_param_found) {
        var limit_cookie = workbench.getCookie(limit_param);
        if (limit_cookie) {
            $(limit_id).val(limit_cookie);
        }
    }
    var explore = 'explore';
    workbench.paging.correctButtons(explore);
    var content = document.getElementById('content');
    var h1 = content.getElementsByTagName('h1')[0];
    var rvalue = resource.val();
    if (rvalue) {
        h1.appendChild(document.createTextNode(' (' + rvalue + ')'));
        removeDuplicates(rvalue);
        var limit = workbench.paging.getLimit(explore);
        // Modify title to reflect total_result_count cookie
        var total_result_count = workbench.paging.getTotalResultCount();
        var have_total_count = (total_result_count > 0);
        var offset = limit == 0 ? 0 : workbench.paging.getOffset();
        var first = offset + 1;
        var last = limit == 0 ? total_result_count : offset + limit;
        // Truncate range if close to end.
        last = have_total_count ? Math.min(total_result_count, last) : last;
        var newHTML = '(' + first + '-' + last;
        if (have_total_count) {
            newHTML = newHTML + ' of ' + total_result_count;
        }
        h1.appendChild(document.createTextNode(newHTML + ')'));
    }
    workbench.paging.setShowDataTypesCheckboxAndSetChangeEvent();
});
//# sourceMappingURL=explore.js.map