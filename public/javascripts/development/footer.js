var g_footer = null;
var g_announcement = null;

function initFooter(par){
	g_footer = par;
	g_announcement = new Announcement();
	g_announcement.appendTo($("#content"));

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
		text: TITLES.about_us
	}).appendTo(link1);

	var link2 = $("<li>").appendTo(links);
		
	about.click(function(evt) {
		evt.preventDefault();
		if (!(!g_announcement)) g_announcement.hide();
		g_announcement.refresh(MESSAGES.about_us);
		g_announcement.show();
	});

	var privacy = $("<a>", {
		text: TITLES.privacy_policy
	}).appendTo(link2);

	privacy.click(function(evt) {
		evt.preventDefault();
		if (!(!g_announcement)) g_announcement.hide();
		g_announcement.refresh(MESSAGES.privacy_policy);
		g_announcement.show();
	});
	
	var footerRight = $("<div>", {
		id: "footer-copy",
		"class": "right",
		html: TITLES.copyright
	}).appendTo(g_footer);
}
