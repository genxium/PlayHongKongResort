function callbackOnPageLoaded(windowHandle, callbackFunc){
        if(callbackFunc==null) return;
        var signaled=false;
        windowHandle.onload=windowHandle.onreadystatechange=function(){
	    var completed = (this.readyState==null || this.readyState=='complete');
            if(signaled || !completed) return;
	    signaled=true;
	    callbackFunc();
        };
}

function loadJavaScript(windowHandle, script, callbackFunc){
        var doc = windowHandle.document;
        if(doc == null) return;
        var scriptNode = doc.createElement('script');
        var textNode = doc.createTextNode(script);
        scriptNode.appendChild(textNode);
        
	if(callbackFunc != null) {
            var signaled = false;
            scriptNode.onload = scriptNode.onreadystatechange = function(){
		var completed = (this.readyState == null || this.readyState == 'complete');
                if(signaled || !completed) return;
		signaled = true;
		callbackFunc();
            };
	}

        var heads=doc.getElementsByTagName("head");
        if(heads == null) return;
        var head = heads[0];
        if(head == null) return;
        head.appendChild(scriptNode);
}

function loadScriptFile(windowHandle, path, filetype, callbackFunc){

	var fileRef = null;
	if (filetype == "js"){ //if path is a external JavaScript file
		fileRef = windowHandle.document.createElement('script');
		fileRef.setAttribute("type","text/javascript");
		fileRef.setAttribute("src", path);
	} else if (filetype == "css"){ //if path is an external CSS file
		fileRef = windowHandle.document.createElement("link");
		fileRef.setAttribute("rel", "stylesheet");
		fileRef.setAttribute("type", "text/css");
		fileRef.setAttribute("href", path);
	} else;

	if (typeof fileRef == "undefined") return;

	if(callbackFunc != null) {
		var signaled = false;
		scriptNode.onload = scriptNode.onreadystatechange = function(){
			var completed = (this.readyState == null || this.readyState == 'complete');
			if(signaled || !completed) return;
			signaled = true;
			callbackFunc();
		};
	}

	var heads = windowHandle.document.getElementsByTagName("head");
	if(heads == null) return;
	var head = heads[0];
	if(head == null) return;
	head.appendChild(fileRef);

}

function loadJquery(windowHandle, callbackFunc){
        loadScriptFile(windowHandle, "assets/javascripts/jquery-2.0.3.min.js", "js", callbackFunc);
}

function loadJqueryPlugins(windowHandle){
	// jquery-cookie
	loadScriptFile(windowHandle, "assets/javascripts/jquery-cookie.js", "js", null);
	// jquery-ui
	loadScriptFile(windowHandle, "assets/javascripts/jquery-ui.js", "js", null);
}

function loadCommonScripts(windowHandle){
        // global_variables
        loadScriptFile(windowHandle, "assets/javascripts/global_variables.js", "js", null);
        // global_functions
        loadScriptFile(windowHandle, "assets/javascripts/global_functions.js", "js", null);
}

function validateEmail(email) { 
	var regex = /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html
	return regex.test(email);
}

function validatePassword(password) {
	var regex = /^[0-9a-zA-Z]{6,20}$/;
	return regex.test(password);	
}

function validateName(name) {
	var regex = /^[0-9a-zA-Z]{6,20}$/;
	return regex.test(name);	
}


function extractParams(url){
	var params=url.split("?")[1].split("&");	
	return params;
}

function firstChild(obj, selector){
	return $(obj.children(selector)[0]);
}

function truncateMillisec (dateStr) {
	try {
		var parts = dateStr.split(".");
		if (parts.length <= 1) throw "oops!";
		return parts[0];
	} catch (err) {
		return dateStr;
	}
}

function getCurrentYmdhisDate() {
	var date = new Date();
	var anterior =  date.getFullYear().toString() + "-" + (date.getMonth() + 1).toString() + "-" + date.getDate().toString(); 
	var posterior = date.getHours().toString() + ":" + date.getMinutes().toString() + ":" + date.getSeconds().toString(); 
	return anterior + " " + posterior;
}

function compareYmdhisDate(foo, bar) {
	var first = new Array();	
	var second = new Array();
	{
		var parts = foo.split(" ");
		var ymd = parts[0].split("-"); 
		var his = parts[1].split(":");
		for(var n in ymd) first.push(parseInt(ymd[n]));
		for(var n in his) first.push(parseInt(his[n]));
	}
	{
		var parts = bar.split(" ");
		var ymd = parts[0].split("-"); 
		var his = parts[1].split(":");
		for(var n in ymd) second.push(parseInt(ymd[n]));
		for(var n in his) second.push(parseInt(his[n]));
	}
	for(var i = 0; i <  first.length; i++) {
		if(first[i] < second[i]) return -1;
		if(first[i] > second[i]) return +1;
	}
	return 0;
}

function disableField(field) {
	field.prop('disabled', true);
}

function enableField(field) {
	field.prop('disabled', false);
}
