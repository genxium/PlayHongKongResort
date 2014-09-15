/*
	Pager Widgets
*/

function PagerContainer(screen, bar, orderKey, orientation, numItemsPerPage) {
	this.screen = screen; // screen of the container
	this.bar = bar; // control bar of the container
	this.orderKey = orderKey;
	this.orientation = orientation;
	this.nItems = numItemsPerPage; // number of items per page

	this.page = 0; // current page
	this.total = 0; // initial number of total pages should always be 0

	// starting & ending indices of the current page
	// the indices are set to -infinity & infinity respectively by default to facilitate initialization after the first query of items
	// they could be either integers or strings	
	this.st = -g_inf; 
	this.ed = g_inf; 

	// extra fields for filters
	this.relation = null;
	this.status = null;

}

function PagerButton(container, page) {
	this.container = container;
	this.page = page;
}

/*
 * Return a modal handle
 * */
function createModal(par, message, widthRatioPercentage, heightRatioPercentage){
	var container=$("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='' aria-hidden='true'>", {
		style: "width: " + widthRatioPercentage + "%; height: " + heightRatioPercentage + "%"
	}).appendTo(par);	
	var dialog=$("<div class='modal-dialog modal-lg'>").appendTo(container);
	var content=$("<div class='modal-content'>", {
		text: message
	}).appendTo(dialog);
	return container;
}

function showModal(container){
	container.modal({
                show: true    
        });
}

function hideModal(container){
	container.modal("hide");
}

function removeModal(container){
	container.empty();
	container.remove();
}

function createBinarySwitch(par, disabled, initVal, disabledText, positiveText, negativeText, inputId){
	var container=$("<div class='onoffswitch'>").appendTo(par);
	var input=$("<input type='checkbox' class='onoffswitch-checkbox' id='"+inputId+"'>").appendTo(container);
	var label=$("<label class='onoffswitch-label' for='"+inputId+"'>").appendTo(container);
	var inner=$("<span class='onoffswitch-inner'>").appendTo(label);
	var sw=$("<span class='onoffswitch-switch'>").appendTo(label);

	inner.attr("left-text", positiveText);
	inner.attr("right-text", negativeText);
	
	if(disabled) {
		disableBinarySwitch(container);
		inner.attr("right-text", disabledText);
		sw.hide();
	} else {
		sw.show();			
	}	
	setBinarySwitch(container, initVal);
	return container;
} 

function setBinarySwitch(container, value){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	input.prop("checked", value);
}

function setBinarySwitchOnClick(container, func){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	if(input.is(":disabled")) return;
	container.off("click").on("click", func);	
}

function getBinarySwitchState(container){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	return input.is(":checked");
}

function disableBinarySwitch(container){
	container.off("click");
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	input.prop("checked", false);
	input.prop("disabled", true);
}
