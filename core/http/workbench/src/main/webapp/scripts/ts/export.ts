/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="paging.ts" />

// WARNING: Do not edit the *.js version of this file. Instead, always edit
// the corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.

workbench.addLoad(function () {
    var suffix = '_explore';
    var limitParam = workbench.paging.LIMIT + suffix;
    if (workbench.paging.hasQueryParameter(limitParam)) {
        var limit = parseInt(0 +
            workbench.paging.getQueryParameter(limitParam), 10);
        $(workbench.paging.LIM_ID + suffix).val(String(limit));
    }
});