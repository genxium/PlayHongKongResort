var g_topbar=null;

function initTopbar(){
	g_topbar=$("#topbar");
	var topbarTitle=$("<span>", {
		id: "topbar_title",
		style: "float: left; margin-left: 5pt; margin-top: 10pt",
		text: "Hong Kong Resort"
	}).appendTo(g_topbar);
	topbarTitle.on("click", function(){
		var homepagePath="/";
		window.open(homepagePath);
	});	
	g_sectionLogin=$("<span>", {
		style: "height: 95%; float: right; margin-right: 10pt"
	}).appendTo(g_topbar);
        initLoginWidget();
}