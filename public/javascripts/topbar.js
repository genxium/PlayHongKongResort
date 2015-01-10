var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	g_topbar.empty();
	var topbarBanner = $("<div>", {
		id: "topbar-title",
	}).appendTo(g_topbar);
	var topbarTitle = $("<p>", {
		text: "Let's Date"
	}).appendTo(topbarBanner);

	g_sectionLogin = $("<div>", {
		id: "login-section"	
	}).appendTo(g_topbar);

	topbarTitle.click(function(evt){
		evt.preventDefault();
		window.location.href = "/";
	});
}
