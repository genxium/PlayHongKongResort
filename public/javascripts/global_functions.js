function loadScriptFile(windowHandle, path, filetype, callbackFunc){
    do{
        var fileRef=null;
        if (filetype=="js"){ //if path is a external JavaScript file
            fileRef=windowHandle.document.createElement('script');
            fileRef.setAttribute("type","text/javascript");
            fileRef.setAttribute("src", path);
        } else if (filetype=="css"){ //if path is an external CSS file
            fileRef=windowHandle.document.createElement("link")
            fileRef.setAttribute("rel", "stylesheet")
            fileRef.setAttribute("type", "text/css")
            fileRef.setAttribute("href", path)
        } else;
        if (typeof fileRef=="undefined") break;
        var heads=windowHandle.document.getElementsByTagName("head");
        if(heads==null) break;
        var head=heads[0];
        if(head==null) break;
        head.appendChild(fileRef);
        
        if(callbackFunc==null) break;
        fileRef.onload=callbackFunc;
    }while(false);
}

function loadJquery(windowHandle, callbackFunc){
    do{
        loadScriptFile(windowHandle, "javascripts/jquery-2.0.3.min.js", "js", callbackFunc);
    }while(false);
}

function loadJqueryPlugins(windowHandle){
    do{
        // jquery-cookie
        loadScriptFile(windowHandle, "javascripts/jquery-cookie.js", "js", null);
        // jquery-ui
        loadScriptFile(windowHandle, "javascripts/jquery-ui.js", "js", null);
    }while(false);
}

function loadCommonScripts(windowHandle){
    do{
        // global_variables
        loadScriptFile(windowHandle, "javascripts/global_variables.js", "js", null);
        // global_functions
        loadScriptFile(windowHandle, "javascripts/global_functions.js", "js", null);
    }while(false);
}
