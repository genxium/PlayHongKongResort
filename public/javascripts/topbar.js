var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	g_topbar.empty();
	var topbarTitle = $("<div>", {
		id: "topbar-title",
		text: "Let's Date"
	}).appendTo(g_topbar);

	g_sectionLogin = $("<div>", {
		id: "login-section"	
	}).appendTo(g_topbar);

	topbarTitle.click(function(evt){
		evt.preventDefault();
		window.location.href = "/";
	});
}
