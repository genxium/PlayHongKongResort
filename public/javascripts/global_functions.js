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
	var regex = g_emailPattern;
	return regex.test(email);
}

function validatePassword(password) {
	var regex = g_passwordPattern;
	return regex.test(password);	
}

function validateName(name) {
	var regex = g_usernamePattern;
	return regex.test(name);	
}

function validateActivityTitle(title) {
	var regex = g_activityTitlePattern;  
	return regex.test(title);			
}

function validateActivityAddress(address) {
	var regex = g_activityAddressPattern;
	return regex.test(address);
}

function validateActivityContent(content) {
	var regex = g_activityContentPattern;
	return regex.test(content);
}

function validateCommentContent(content) {
	var regex = g_commentContentPattern;
	return regex.test(content);
}

function validateAssessmentContent(content) {
	var regex = g_assessmentContentPattern;
	return regex.test(content);
}

function extractTagAndParams(url) {
	var urlRegex = /https?:\/\/(.+)#(default|profile|detail|home|search|notifications|success|failure)\??(.*)/i;
	var matchUrl = urlRegex.exec(url);

	if (matchUrl == null) return null;

	var ret = {};
	ret["tag"] = matchUrl[2];
	ret["params"] = {};
	var params = matchUrl[3];

	var paramRegex = /(\w+)=(\w+)/g; // get all matches
	matchParams = paramRegex.exec(params);
	while (matchParams != null) {
		var key = matchParams[1];
		var val = matchParams[2];
		ret["params"][key] = val;
		matchParams = paramRegex.exec(params)
	}
	return ret;
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

function isChecked(checkbox) {
	return (checkbox != null && checkbox.is(":checked"));
}

function isHidden(field) {
	return (field != null && field.is(":hidden"));
}

function checkField(field) {
	if (field == null) return;
	field.prop("checked", true);
}

function uncheckField(field) {
	if (field == null) return;
	field.prop("checked", false);
}

function disableField(field) {
	if (field == null) return;
	field.prop('disabled', true);
}

function enableField(field) {
	if (field == null) return;
	field.prop('disabled', false);
}

function setMargin(field, left, top, right, bottom) {
	if (field == null) return;
	if (left != null)	field.css("margin-left", left);
	if (top != null)	field.css("margin-top", top);
	if (right != null)	field.css("margin-right", right);
	if (bottom != null)	field.css("margin-bottom", bottom);
}

function setBackgroundImageDefault(field, url) {
	if (field == null) return;
	setBackgroundImage(field, url, "contain", "no-repeat", "center");
}

function setBackgroundImage(field, url, size, repeat, position) {
	if (field == null) return;
	
	field.css("background-image", "url(" + url + ")");	

	field.css("background-size", size);
	field.css("-o-background-size", size);	
	field.css("-moz-background-size", size);
	field.css("-webkit-background-size", size);
	
	field.css("background-repeat", repeat);
	
	field.css("background-position", position);
}

function setDimensions(field, width, height) {
	if (field == null) return;
	if (width != null)	field.css("width", width);
	else field.css("width", "auto");

	if (height != null)	field.css("height", height);
	else field.css("height", "auto");
}

function getDimensions(field) {
	if (field == null) return {
		width: "0px",
		height: "0px"
	};
	return {
		width: field.css("width"),
		height: field.css("height")
	};
}

function setOffset(field, left, top) {
	if (field == null) return;

	if (left != null)	field.css("left", left);
	else field.css("left", "auto");
	
	if (top != null)	field.css("top", top);
	else field.css("top", "auto"); 
}

function getOffset(field) {
	if (field == null) return {
		left: "0px",
		top: "0px"
	};
	return {
		left: parseFloat(field.css("left")),
		top: parseFloat(field.css("top"))
	};
}

function stencilize(field) {
	if (field == null) return;
	field.css("background-color", "DimGray");
	field.css("color", "Gainsboro");
}

function isFileValid(file){
	if (file == null) return false;
	var fileSizeLimit = (1 << 21)// 2 mega bytes
	if (file.size > fileSizeLimit) return false;
	return true;
}

function validateImage(file){
	if (file == null) return false;
	if (!isFileValid(file)) {
		alert("Please choose an image that is less the 2MB(2048KB) in size");
		return false;
	}
	var fileName = file.value == null ? file.name : file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if ($.inArray(ext, ['png','jpg','jpeg']) == -1) {
		alert("Invalid image type!");
		return false;
	}
	return true;
}

function currentMillis() {
    var date = new Date();
    return 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
}

function gmtMiilisecToLocalYmdhis(millis) {
    var date = new Date();
    return moment(millis).zone(date.getTimezoneOffset()).format("YYYY-MM-DD HH:mm:ss"); 
}

function gmtMiilisecToLocalYmdhi(millis) {
    var date = new Date();
    return moment(millis).zone(date.getTimezoneOffset()).format("YYYY-MM-DD HH:mm"); 
}

function localYmdhisToGmtMillisec(dateStr) {
    var date = new Date();
    return 1000 * moment(dateStr, "YYYY-MM-DD HH:mm:ss").zone(date.getTimezoneOffset()).unix(); 
}

function generateUuid() {
	function s4() {
		return Math.floor((1 + Math.random()) * 0x10000)
			.toString(16)
			.substring(1);
	}

	return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
		s4() + '-' + s4() + s4() + s4();
}

function isStandardSuccess(data) {
	if (data == null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 0;
}

function isStandardFailure(data) {
	if (data == null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 1;
}

function isTokenExpired(data) {
	if (data == null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 1001;
}

function isApplicantLimitExceeded(data) {
	if (data == null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 3008;
}

function isSelectedLimitExceeded(data) {
	if (data == null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 3009;
}
