var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	g_topbar.empty();
	g_topbar.addClass("clearfix");
	var topbarBanner = $("<div>", {
		id: "topbar-title",
		"class": "left"
	}).appendTo(g_topbar);
	var topbarTitle = $("<p>", {
		text: TITLES.logo_alias 
	}).appendTo(topbarBanner).click(function(evt){
		evt.preventDefault();
		window.location.href = "/";
	});

	g_sectionLogin = $("<div>", {
		id: "login-section",
		"class": "patch-block-beta"	
	}).appendTo(g_topbar);
}
