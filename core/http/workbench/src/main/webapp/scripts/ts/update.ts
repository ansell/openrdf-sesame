/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="yasqeHelper.ts" />

// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.

module workbench {

    export module update {
        //need to declar YASQE library for typescript compilation
        declare var YASQE: any;
        declare var namespaces: {string:string};
        var yasqe: any = null;
        
        export function initYasqe() {
            workbench.yasqeHelper.setupCompleters(namespaces);
            yasqe = YASQE.fromTextArea(document.getElementById('update'), {
                createShareLink: function(){return {update: yasqe.getValue()};},
                consumeShareLink: function(yasqe: any, args: any){
                    if (args.update) yasqe.setValue(args.update)
                },
                persistent: "update"//this way, we don't conflict with the YASQE editor of the regular query interface, and we show the most recent -update- query
                
            });
            //some styling conflicts. Could add my own css file, but not a lot of things need changing, so just do this programmatically
            //first, set the font size (otherwise font is as small as menu, which is too small)
            //second, set the width. YASQE normally expands to 100%, but the use of a table requires us to set a fixed width
            $(yasqe.getWrapperElement()).css({"fontSize": "14px", "width": "900px"});
            //we made a change to the css wrapper element (and did so after initialization). So, force a manual update of the yasqe instance
            yasqe.refresh();
            
            //if the text area we instantiated YASQE on has no query val, then show a regular default update query
            if (yasqe.getValue().trim().length == 0) yasqe.setValue("INSERT DATA {<http://exampleSub> <http://examplePred> <http://exampleObj>}");
        }
        export function doSubmit() {
            //save yasqe content to textarea
            if (yasqe) yasqe.save();
            if ($('#update').text().length >= 1000) {
                // Too long to put in URL for a GET request. Instead, POST.
                // Browser back-button may not work as expected.
                return true;
            } else { // safe to use in request-URI
                var url: string[] = [];
                url[url.length] = 'update';
                if (document.all) {
                    url[url.length] = ';';
                } else {
                    url[url.length] = '?';
                }
                workbench.addParam(url, 'queryLn');
                workbench.addParam(url, 'update');
                workbench.addParam(url, 'limit');
                workbench.addParam(url, 'infer');
                url[url.length - 1] = '';
                document.location.href = url.join('');
                return false;
            }
        }
    }
}

workbench.addLoad(function updatePageLoaded() {
    workbench.update.initYasqe();
});
