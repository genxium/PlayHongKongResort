var g_footer = null;

function initFooter(par){
	g_footer = par;

	// please note that the nomenclature is :
	// 1. if this is a global variable use g_aBC
	// 2. if this is a local variable use aBC
	var footerLeft = $("<div>", {
		id: "footer-links",
	}).appendTo(g_footer);

	var links = $("<ul>").appendTo(footerLeft);

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
		id: "footer-copy"
	}).appendTo(g_footer);
	
	var footerRightPrefix = $("<span>", {
		text: "Copyright"
	}).appendTo(footerRight);
	
	var footerRightSymbol = $("<span>", {
		html: " &copy; ",
		class: "footer-copy-text" // modify the the styling parameters in footer-copy-text in BROWSER console to view the change
	}).appendTo(footerRight);

	var footerRightSuffix = $("<span>", {
		text: "All rights reserved."
	}).appendTo(footerRight);
}
