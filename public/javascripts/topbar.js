var g_topbar=null;

function initTopbar(){
	g_topbar=$("#topbar");
	var topbarTitle=$("<span>", {
		id: "topbar_title",
		style: "float: left",
		text: "Hong Kong Resort"
	}).appendTo(g_topbar);
	g_sectionLogin=$("<span>", {
		style: "float: right; margin-right: 10pt"
	}).appendTo(g_topbar);
        initLoginWidget();
}
