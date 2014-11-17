var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	var topbarTitle = $("<span>", {
		id: "topbar-title",
		text: "Hong Kong Resort"
	}).appendTo(g_topbar);
	topbarTitle.click(function(evt){
		evt.preventDefault();
		requestHome();
	});	
	g_sectionLogin = $("<span>", {
		style: "position: absolute; width: 33%; height: 95%; left: 67%;"
	}).appendTo(g_topbar);
}
