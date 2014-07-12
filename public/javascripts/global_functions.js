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
	var regex = /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html
	return regex.test(email);
}

function validatePassword(password) {
	var regex = /^([0-9a-zA-Z])+$/;
	return regex.test(password);	
}

function generateDataPicker(time) {
    
	var ret=$('<div>', {
		class: 'col-sm-6'    
	});

	var formGroup=$('<div>', {
		class: 'form-group'    
	}).appendTo(ret);

	var inputGroup=$("<div>", {
		class: 'input-group date'    
	}).appendTo(formGroup);

	var input=$('<input>', {
		type: 'text',
		value: time,
		class: 'form-control'    
	}).appendTo(inputGroup);

	var inputGroupAddon=$('<span>', {
		class: 'input-group-addon'    
	}).appendTo(inputGroup);

	var glyphiconCalendar=$('<span>', {
		class: 'glyphicon glyphicon-calendar'   
	}).appendTo(inputGroupAddon);

	inputGroup.datetimepicker({
		format: 'YYYY-MM-DD HH:mm',
		pickSeconds: false,
		pick12HourFormat: false  
	});
	return ret;
}

function getDateTime(picker){
	var ret = null;
	do{
		var formGroup = $(picker.children(".form-group")[0]);
		var inputGroup = $(formGroup.children(".input-group.date")[0]);
		var input = $(inputGroup.children(".form-control")[0]); 
		var dateStr = input.val();
		if (dateStr==null || dateStr=="" || dateStr.length==0) break;
		ret = dateStr+":00";
	} while(false);
	return ret;
}

function extractParams(url){
	var params=url.split("?")[1].split("&");	
	return params;
}
