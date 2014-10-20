var g_topbar = null;

function initTopbar(par){
	g_topbar = par;
	var topbarTitle = $("<span>", {
		id: "topbar-title",
		style: "float: left; margin-left: 5pt; margin-top: 10pt",
		text: "Hong Kong Resort"
	}).appendTo(g_topbar);
	topbarTitle.click(function(){
		var homepagePath = "/";
		window.open(homepagePath);
	});	
	g_sectionLogin = $("<span>", {
		style: "height: 95%; float: right; margin-right: 10pt"
	}).appendTo(g_topbar);
	initLoginWidget();
}
