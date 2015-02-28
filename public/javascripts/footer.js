var g_footer = null;
var g_announcementContainer = null;

function initFooter(par){
	g_footer = par;

	// please note that the nomenclature is :
	// 1. if this is a global variable use g_aBC
	// 2. if this is a local variable use aBC
	var footerLeft = $("<div>", {
		id: "footer-links",
		"class": "left"
	}).appendTo(g_footer);

	var links = $("<ul>", {
		"class": "clearfix"
	}).appendTo(footerLeft);

	var link1 = $("<li>").appendTo(links);

	var about = $("<a>", {
		text: TITLES["about_us"]
	}).appendTo(link1);

	var link2 = $("<li>").appendTo(links);
		
	about.click(function(evt) {
		evt.preventDefault();
		if (g_announcementContainer != null) removeModal(g_announcementContainer);
		g_announcementContainer = createModal($("#content"), MESSAGES["about_us"], "80", "90");
		showModal(g_announcementContainer);
	});

	var privacy = $("<a>", {
		text: TITLES["privacy_policy"]
	}).appendTo(link2);

	privacy.click(function(evt) {
		evt.preventDefault();
		if (g_announcementContainer != null) removeModal(g_announcementContainer);
		g_announcementContainer = createModal($("#content"), MESSAGES["privacy_policy"], "80", "90");
		showModal(g_announcementContainer);
	});
	
	var footerRight = $("<div>", {
		id: "footer-copy",
		"class": "right",
		html: "Copyright &copy; All rights reserved."
	}).appendTo(g_footer);
}
