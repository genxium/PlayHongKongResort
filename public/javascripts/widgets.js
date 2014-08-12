var g_btnPreviousPage=null;
var g_btnNextPage=null;

function initWidgets(callbackPreviousPage, callbackNextPage){
	g_btnPreviousPage=$("#btn_previous_page");
	g_btnNextPage=$("#btn_next_page");
		
	g_btnPreviousPage.on("click", callbackPreviousPage);
	g_btnNextPage.on("click", callbackNextPage);
}

/*
 * Return a modal handle
 * */
function createModal(par, message){
	var container=$("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='' aria-hidden='true'>", {
		style: "width: 80%; height: 80%"
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

	container.data("positive", positiveText);
	container.data("negative", negativeText);
	container.data("disabled", disabledText);
	
	if(disabled == false) {
		setBinarySwitch(container, initVal);
	} else {
		disableBinarySwitch(container);
	}
	return container;
} 

function setBinarySwitch(container, value){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	input.val(value);
	var label = firstChild(container, "label.onoffswitch-label");	
	var inner = firstChild(label, "span.onoffswitch-inner");
	var content =  "";
	if(value == true) content = container.data("positive"); 
	else content = container.data("negative");
	inner.attr("content", content);
}

function setBinarySwitchOnClick(container, func){
	container.off("click").on("click", func);	
}

function getBinarySwitchState(container){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	return input.val();
}

function disableBinarySwitch(container){
	var input = firstChild(container, "input.onoffswitch-checkbox");			
	input.prop("disabled", true);
	var label = firstChild(container, "label.onoffswitch-label");	
	var inner = firstChild(label, "span.onoffswitch-inner");
	inner.attr("content", container.data("disabled"));	
}
