function callbackOnPageLoaded(windowHandle, callbackFunc){
    do{ 
        if(callbackFunc==null) break;
        var signaled=false;
        windowHandle.onload=windowHandle.onreadystatechange=function(){
            if(!signaled && (this.readyState==null || this.readyState=='complete')){
                signaled=true;
                callbackFunc();
            }
        };
    }while(false);
}

function loadJavaScript(windowHandle, script, callbackFunc){
    do{
        var doc=windowHandle.document;
        if(doc==null) break;
        var scriptNode=doc.createElement('script');
        var textNode=doc.createTextNode(script);
        scriptNode.appendChild(textNode);
        
        do{ 
            if(callbackFunc==null) break;
            var signaled=false;
            scriptNode.onload=scriptNode.onreadystatechange=function(){
                if(!signaled && (this.readyState==null || this.readyState=='complete')){
                    signaled=true;
                    callbackFunc();
                }
            };
        }while(false);

        var heads=doc.getElementsByTagName("head");
        if(heads==null) break;
        var head=heads[0];
        if(head==null) break;
        head.appendChild(scriptNode);
    }while(false);
}

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
        
        do{
            if(callbackFunc==null) break;
            var signaled=false;
            fileRef.onload=fileRef.onreadystatechange=function(){
                if(!signaled && (this.readyState==null || this.readyState=='complete')){
                    signaled=true;
                    callbackFunc();
                }
            };
        }while(false);

        var heads=windowHandle.document.getElementsByTagName("head");
        if(heads==null) break;
        var head=heads[0];
        if(head==null) break;
        head.appendChild(fileRef);

    }while(false);
}

function loadJquery(windowHandle, callbackFunc){
    do{
        loadScriptFile(windowHandle, "assets/javascripts/jquery-2.0.3.min.js", "js", callbackFunc);
    }while(false);
}

function loadJqueryPlugins(windowHandle){
    do{
        // jquery-cookie
        loadScriptFile(windowHandle, "assets/javascripts/jquery-cookie.js", "js", null);
        // jquery-ui
        loadScriptFile(windowHandle, "assets/javascripts/jquery-ui.js", "js", null);
    }while(false);
}

function loadCommonScripts(windowHandle){
    do{
        // global_variables
        loadScriptFile(windowHandle, "assets/javascripts/global_variables.js", "js", null);
        // global_functions
        loadScriptFile(windowHandle, "assets/javascripts/global_functions.js", "js", null);
    }while(false);
}

function validateEmail(email) { 
        var regex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return regex.test(email);
}
