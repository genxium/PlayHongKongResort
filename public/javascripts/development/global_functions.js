// cookie operations
function setCookie(key, val, path, days) {
	if (!path) var path = "/";
	var expires = null;
	if (!(!days)) {
		var date = new Date();
		date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
		expires = "; expires=" + date.toGMTString();	
	} else expires = ""; 
	document.cookie = key + "=" + val + expires + "; path=" + path;	
}

function getCookie(key) {
	if (document.cookie.length <= 0) return null;
	var start = document.cookie.indexOf(key + "=");
	if (start == -1)	return null;
	if (start - 1 >= 0 && (document.cookie[start - 1] != ' ' && document.cookie[start - 1] != ';')) return null;
	start = start + key.length + 1;
	end = document.cookie.indexOf(";", start);
	if (end == -1)	end = document.cookie.length;
	return unescape(document.cookie.substring(start, end));
}

function removeCookie(key) {
	setCookie(key, "", "/", -1);
}

// javascript inheritance sugar, reference http://www.crockford.com/javascript/inheritance.html
Function.prototype.method = function (name, func) {
    this.prototype[name] = func;
    return this;
};

Function.method('inherits', function (parent) {
    this.prototype = new parent();
    var d = {}, 
        p = this.prototype;
    this.prototype.constructor = parent; 
    this.method('uber', function uber(name) {
        if (!(name in d)) {
            d[name] = 0;
        }        
        var f, r, t = d[name], v = parent.prototype;
        if (t) {
            while (t) {
                v = v.constructor.prototype;
                t -= 1;
            }
            f = v[name];
        } else {
            f = p[name];
            if (f == this[name]) {
                f = v[name];
            }
        }
        d[name] += 1;
        r = f.apply(this, Array.prototype.slice.apply(arguments, [1]));
        d[name] -= 1;
        return r;
    });
    return this;
});

Function.method('swiss', function (parent) {
    for (var i = 1; i < arguments.length; i += 1) {
        var name = arguments[i];
        this.prototype[name] = parent.prototype[name];
    }
    return this;
});

// ends inheritance sugar

function callbackOnPageLoaded(windowHandle, callbackFunc){
        if (callbackFunc === null) return;
        var signaled=false;
        windowHandle.onload = windowHandle.onreadystatechange = function() {
            var completed = (this.readyState === null || this.readyState == 'complete');
            if(signaled || !completed) return;
            signaled = true;
            callbackFunc();
        };
}

function loadJavaScript(windowHandle, script, callbackFunc) {
        var doc = windowHandle.document;
        if(doc === null) return;
        var scriptNode = doc.createElement('script');
        var textNode = doc.createTextNode(script);
        scriptNode.appendChild(textNode);

        if(callbackFunc !== null) {
            var signaled = false;
            scriptNode.onload = scriptNode.onreadystatechange = function() {
                var completed = (this.readyState === null || this.readyState == 'complete');
                if(signaled || !completed) return;
                signaled = true;
                callbackFunc();
            };
		}

        var heads = doc.getElementsByTagName("head");
        if(heads === null) return;
        var head = heads[0];
        if(head === null) return;
        head.appendChild(scriptNode);
}

function loadScriptFile(windowHandle, path, filetype, callbackFunc) {

	var fileRef = null;
	if (filetype == "js") {
		//if path is a external JavaScript file
		fileRef = windowHandle.document.createElement('script');
		fileRef.setAttribute("type","text/javascript");
		fileRef.setAttribute("src", path);
	} else if (filetype == "css"){
		//if path is an external CSS file
		fileRef = windowHandle.document.createElement("link");
		fileRef.setAttribute("rel", "stylesheet");
		fileRef.setAttribute("type", "text/css");
		fileRef.setAttribute("href", path);
	} else;

	if (typeof fileRef == "undefined") return;

	if(callbackFunc !== null) {
		var signaled = false;
		scriptNode.onload = scriptNode.onreadystatechange = function() {
			var completed = (this.readyState === null || this.readyState == 'complete');
			if(signaled || !completed) return;
			signaled = true;
			callbackFunc();
		};
	}

	var heads = windowHandle.document.getElementsByTagName("head");
    if(heads === null) return;
    var head = heads[0];
    if(head === null) return;
    head.appendChild(fileRef);

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
	var regex = g_playernamePattern;
	return regex.test(name);	
}

function validatePlayerAge(age) {
	var regex = g_playerAgePattern;
	return regex.test(age);
}

function validatePlayerGender(gender) {
	var regex = g_playerGenderPattern;
	return regex.test(gender);
}

function validatePlayerMood(mood) {
	var regex = g_playerMoodPattern;
	return regex.test(mood);
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
	var urlRegex = /https?:\/\/(.+)#(default|profile|detail|home|search|notifications|success|failure|access_token=[\w\d]+)\??\&?(.*)/i;
	var matchUrl = urlRegex.exec(url);

	if (matchUrl === null) return null;

	var ret = {};
	var tag = matchUrl[2];
	var tagRegex = /(\w+)=([@\.\w]+)/g;
	var matchTag = tagRegex.exec(tag);
	if (matchTag !== null) {
		tag = {};
		tag[matchTag[1]] = matchTag[2];	
	}
	
	ret[g_keyTag] = tag;
	ret[g_keyParams] = {};
	
	// TODO: this imposes an assumption on `params` that none of its component values contains character '='
	var params = decodeURIComponent(matchUrl[3]);
	var paramRegex = /(\w+)=([:,\[\]{}"@\.\w]+)/g; 
	var matchParams = paramRegex.exec(params);
	while (matchParams !== null) {
		ret[g_keyParams][matchParams[1]] = matchParams[2];
		matchParams = paramRegex.exec(params);
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
	var first = [];	
	var second = [];
	{
		var parts1 = foo.split(" ");
		var ymd1 = parts1[0].split("-"); 
		var his1 = parts1[1].split(":");
		for(var n1 in ymd1) first.push(parseInt(ymd1[n1]));
		for(var n2 in his1) first.push(parseInt(his1[n2]));
	}
	{
		var parts2 = bar.split(" ");
		var ymd2 = parts2[0].split("-"); 
		var his2 = parts2[1].split(":");
		for(var n3 in ymd2) second.push(parseInt(ymd2[n]));
		for(var n4 in his2) second.push(parseInt(his2[n]));
	}
	for(var i = 0; i <  first.length; i++) {
		if(first[i] < second[i]) return -1;
		if(first[i] > second[i]) return +1;
	}
	return 0;
}

function getTarget(evt) {
	return $(evt.srcElement ? evt.srcElement : evt.target);
}

function isChecked(checkbox) {
	return (checkbox !== null && checkbox.is(":checked"));
}

function isHidden(field) {
	return (field !== null && field.is(":hidden"));
}

function checkField(field) {
	if (field === null) return;
	field.prop("checked", true);
}

function uncheckField(field) {
	if (field === null) return;
	field.prop("checked", false);
}

function disableField(field) {
	if (field === null) return;
	field.prop('disabled', true);
}

function enableField(field) {
	if (field === null) return;
	field.prop('disabled', false);
}

function setMargin(field, left, top, right, bottom) {
	if (field === null) return;
	if (left !== null)	field.css("margin-left", left);
	if (top !== null)	field.css("margin-top", top);
	if (right !== null)	field.css("margin-right", right);
	if (bottom !== null)	field.css("margin-bottom", bottom);
}

function setBackgroundImageDefault(field, url) {
	if (field === null) return;
	setBackgroundImage(field, url, "contain", "no-repeat", "center");
}

function setBackgroundImage(field, url, size, repeat, position) {
	if (field === null) return;
	
	field.css("background-image", "url(" + url + ")");	

	field.css("background-size", size);
	field.css("-o-background-size", size);	
	field.css("-moz-background-size", size);
	field.css("-webkit-background-size", size);
	
	field.css("background-repeat", repeat);
	
	field.css("background-position", position);
}

function setDimensions(field, width, height) {
	if (field === null) return;
	if (width !== null)	field.css("width", width);
	else field.css("width", "auto");

	if (height !== null)	field.css("height", height);
	else field.css("height", "auto");
}

function getDimensions(field) {
	if (field === null) return {
		width: "0px",
		height: "0px"
	};
	return {
		width: field.css("width"),
		height: field.css("height")
	};
}

function setOffset(field, left, top) {
	if (field === null) return;

	if (left !== null)	field.css("left", left);
	else field.css("left", "auto");
	
	if (top !== null)	field.css("top", top);
	else field.css("top", "auto"); 
}

function getOffset(field) {
	if (field === null) return {
		left: "0px",
		top: "0px"
	};
	return {
		left: parseFloat(field.css("left")),
		top: parseFloat(field.css("top"))
	};
}

function stencilize(field) {
	if (field === null) return;
	field.css("background-color", "DimGray");
	field.css("color", "Gainsboro");
}

function isFileValid(file){
	if (file === null) return false;
	var fileSizeLimit = (1 << 21);// 2 mega bytes
	if (file.size > fileSizeLimit) return false;
	return true;
}

function validateImage(file){
	if (!file) return false;
	if (!isFileValid(file))	return false;
	var filetype = file.type;
	var ext = filetype.split('/').pop().toLowerCase();
	if (!ext) return false;
	if ($.inArray(ext, ['png','jpg','jpeg']) == -1) {
		alert(ALERTS.invalid_image_type);
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
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret === 0;
}

function isStandardFailure(data) {
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == 1;
}

function isCaptchaNotMatched(data) {
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errCaptcha;
}

function isTokenExpired(data) {
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errNotLoggedIn;
}

function isPlayerNotFound(data) {
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errPlayerNotFound;
}

function isPswErr(data) {
	if (data === null) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errPswErr;
}

function isDuplicated(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errDuplicated;
}

function isApplicantLimitExceeded(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errActivityAppliedLimit;
}

function isSelectedLimitExceeded(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errActivitySelectedLimit;
}

function isCreationLimitExceeded(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errActivityCreationLimit;
}

function isForeignPartyRegistrationRequired(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errForeignPartyRegistrationRequired;
}

function isTempForeignPartyRecordNotFound(data) {
	if (!data) return false;
	if (!data.hasOwnProperty(g_keyRet)) return false;
	var ret = parseInt(data[g_keyRet]);
	return ret == g_errTempForeignPartyRecordNotFound;
}

function addWarningStyle(field) {
	field.removeClass('patch-block-sigma');
	field.addClass('warning');
}

function removeWarningStyle(field) {
	field.removeClass('warning');
	field.addClass('patch-block-sigma');
}

function getToken() {
        return getCookie(g_keyToken);
}

function saveToken(token) {
	setCookie(g_keyToken, token);
}

function clearToken() {
        removeCookie(g_keyToken);
}

function getAccessToken() {
        return getCookie(g_keyAccessToken);
}

function getParty() {
        return getCookie(g_keyParty);
}

function saveAccessTokenAndParty(accessToken, party) {
        setCookie(g_keyParty, party);
        setCookie(g_keyAccessToken, accessToken);
}

function clearAccessTokenAndParty() {
	removeCookie(g_keyAccessToken);
	removeCookie(g_keyParty);
}

function queryCDNDomainSync() {
	var ret = null;
        var token = getToken();
        var url = apiCDNDomain(g_cdnQiniu);
        var params = {};
        params[g_keyToken] = token;
	$.ajax({
			type: "POST",
			url: url,
			data: params,
			async: false,
			success: function(data, status, xhr){
				if (isTokenExpired(data))       return null;
				if (isPlayerNotFound(data))     return null;
				if (!data.domain) return null;
				ret = data.domain;
			},
			error: function(xhr, status, err){
			        return null;
			}
	});
	return ret;
}


