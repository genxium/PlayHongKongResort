var g_footer = null;

function initFooter(par){
	g_footer = par;

	// please note that the nomenclature is :
	// 1. if this is a global variable use g_aBC
	// 2. if this is a local variable use aBC
	var footerLeft = $("<div>", {
		id: "footer-links",
		class: "left"
	}).appendTo(g_footer);

	var links = $("<ul>", {
		class: "clearfix"
	}).appendTo(footerLeft);

	var link1 = $("<li>").appendTo(links);

	var about = $("<a>", {
		href: "/about",
		text: "About Us"
	}).appendTo(link1);

	var link2 = $("<li>").appendTo(links);

	var privacy = $("<a>", {
		href: "/privacy",
		text: "Privacy Policy"
	}).appendTo(link2);
	
	var footerRight = $("<div>", {
		id: "footer-copy",
		class: "right",
		html: "Copyright &copy; All rights reserved."
	}).appendTo(g_footer);
}
