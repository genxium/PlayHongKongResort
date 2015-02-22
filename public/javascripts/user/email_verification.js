var g_redirectCounter = null;
var g_redirectCountDown = 0;

function redirectTimer(name, email) {
	g_redirectCountDown = g_redirectCountDown - 1;
	if (g_redirectCountDown <= 0) {
		clearInterval(g_redirectCounter);
		window.location = "/";	
		return;
	}
	$("#content").html(MESSAGES["email_verification_success"].format(name, email, g_redirectCountDown));
};

function routeByHash() {
	var href = window.location.href;
	var bundle = extractTagAndParams(href);
	if (bundle == null) {
		return;
	}
	var tag = bundle["tag"];	
	var params = bundle["params"];

	if (tag == null || tag == "") {
		return;
	}
	if (tag == "success") {
		g_redirectCountDown = 5;
		g_redirectCounter = setInterval(redirectTimer, 1000, params[g_keyName], params[g_keyEmail]);
		return;
	}
	if (tag == "failure") {
		$("#content").html(MESSAGES["email_verification_failure"].format(params[g_keyName], params[g_keyEmail]));
		return;
	}
}

$(document).ready(function(){
	$(window).on("hashchange", function(evt) {
		routeByHash();
	});
	routeByHash();
});
