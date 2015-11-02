var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	g_topbar.empty();
	var topbarBanner = $("<div>", {
		id: "topbar-title",
		"class": "left"
	}).appendTo(g_topbar);
	var btnHome = $("<div>", {
		"class": "glyphicon glyphicon-home"
	}).appendTo(topbarBanner).click(function(evt){
		evt.preventDefault();
		window.location.href = "/";
	});

	g_sectionLogin = $("<div>", {
		id: "login-section"
	}).appendTo(g_topbar);
}
