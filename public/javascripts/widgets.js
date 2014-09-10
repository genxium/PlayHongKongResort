/*
	Pager Widgets
*/

function PagerScreen(container, currentPage, orderKey, startingIndex, endingIndex, numItemsPerPage) {
	this.container = container;
	this.currentPage = currentPage;
	this.orderKey = orderKey;
	this.startingIndex = startingIndex;
	this.endingIndex = endingIndex;
	this.numItemsPerPage = numItemsPerPage;
}

function PagerCoordinator(screen, totalPages, onClick) {
	this.screen = screen;
	this.totalPages = totalPages;
	this.onClick = onClick;
}

var g_btnPreviousPage=null;
var g_btnNextPage=null;

function initWidgets(callbackPreviousPage, callbackNextPage){
	g_btnPreviousPage=$("#btn-previous-page");
	g_btnNextPage=$("#btn-next-page");
		
	g_btnPreviousPage.on("click", callbackPreviousPage);
	g_btnNextPage.on("click", callbackNextPage);
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
