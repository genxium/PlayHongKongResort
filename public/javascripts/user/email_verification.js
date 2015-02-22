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
		$("#content").html(MESSAGES["email_verification_success"].format(params[g_keyName], params[g_keyEmail]), 5);
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
