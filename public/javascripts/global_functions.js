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
		class: 'form-control',
		style: 'width: auto'    
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
	var formGroup = $(picker.children(".form-group")[0]);
	var inputGroup = $(formGroup.children(".input-group.date")[0]);
	var input = $(inputGroup.children(".form-control")[0]); 
	var dateStr = input.val();
	if (dateStr==null || dateStr=="" || dateStr.length==0) return null;
	ret = dateStr+":00";
	return ret;
}

function extractParams(url){
	var params=url.split("?")[1].split("&");	
	return params;
}

function firstChild(obj, selector){
	return $(obj.children(selector)[0]);
}

function queryActivities(refIndex, numItems, order, direction, userId, relation, status, onSuccess, onError){
	var params = {};
	if(refIndex != null) params[g_keyRefIndex] = refIndex.toString();
	if(numItems != null) params[g_keyNumItems] = numItems;
	if(order != null) params[g_keyOrder] = parseInt(order);
	if(direction != null) params[g_keyDirection] = parseInt(direction);
	if(userId != null) params[g_keyUserId] = userId;
	if(relation != null) params[g_keyRelation] = relation;
	if(status != null) params[g_keyStatus] = status;

	var token = $.cookie(g_keyToken);
	if(token != null)	params[g_keyToken] = token;

	try{
		$.ajax({
			type: "GET",
			url: "/activity/query",
			data: params,
			success: onSuccess,
			error: onError 
		});
	} catch(err){

	}
}

function compareYmdhisDate(foo, bar) {
	var first = new Array();	
	var second = new Array();
	{
		var parts = foo.split(" ");
		var ymd = parts[0].split("-"); 
		var his = parts[1].split(":");
		for(var n in ymd) first.push(parseInt(n));
		for(var n in his) first.push(parseInt(n));
	}
	{
		var parts = bar.split(" ");
		var ymd = parts[0].split("-"); 
		var his = parts[1].split(":");
		for(var n in ymd) second.push(parseInt(n));
		for(var n in his) second.push(parseInt(n));
	}
	for(var i = 0; i <  first.length; i++) {
		if(first[i] < second[i]) return -1;
		if(first[i] > second[i]) return +1;
	}
	return 0;
}
