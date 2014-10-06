/*
 * Datetime Picker
 */
function generateDatePicker(time) {
    
	var ret=$('<div>', {
		class: 'col-sm-6'    
	});

	var formGroup=$('<div>', {
		class: 'form-group'    
	}).appendTo(ret);

	var inputGroup=$("<div>", {
		class: 'input-group date'    
	}).appendTo(formGroup);

	var input=$('<input>', {
		type: 'text',
		value: time,
		disabled: true,
		class: 'form-control',
		style: 'color: black; background-color: white; cursor: default; width: auto'    
	}).appendTo(inputGroup);

	var inputGroupAddon=$('<span>', {
		class: 'input-group-addon'    
	}).appendTo(inputGroup);

	var glyphiconCalendar=$('<span>', {
		class: 'glyphicon glyphicon-calendar'   
	}).appendTo(inputGroupAddon);

	inputGroup.datetimepicker({
		format: 'YYYY-MM-DD HH:mm',
		pickSeconds: false,
		pick12HourFormat: false  
	});
	return ret;
}

function getDateTime(picker){
	var ret = null;
	var formGroup = $(picker.children(".form-group")[0]);
	var inputGroup = $(formGroup.children(".input-group.date")[0]);
	var input = $(inputGroup.children(".form-control")[0]); 
	var dateStr = input.val();
	if (dateStr==null || dateStr=="" || dateStr.length==0) return null;
	ret = dateStr+":00";
	return ret;
}

/*
 * Pager Widgets
 */

function PagerContainer(screen, bar, orderKey, orientation, numItemsPerPage, url, paramsGenerator) {
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

    this.url = url;

    // prototype: paramsGenerator(page)
    this.paramsGenerator = paramsGenerator;
}

function PagerButton(container, page) {
    // the pager button is determined to trigger only "GET" ajax
	this.container = container;
	this.page = page;
}

function createPagerBar(container, oldSt, oldEd, onSuccess, onError) {

    // prototypes: onSuccess(data), onError()
	var page  = container.page;
	var orientation = container.orientation; 
	var newSt = container.st; 
	var newEd = container.ed;
	if(orientation == +1 && newSt > oldEd) ++page;
	if(orientation == +1 && newEd < oldSt) --page;
	if(orientation == -1 && newSt < oldEd) ++page;
	if(orientation == -1 && newEd > oldSt) --page; 
	container.page = page;

	// display pager bar 
	container.bar.empty();

	var previous = new PagerButton(container, page - 1);
	var btnPrevious = $("<button>", {
		text: "Prev",
		style: "margin-right: 2px"
	}).appendTo(container.bar);
	btnPrevious.click(previous, function(evt) {
	    if (container.url == null) return;
        var button = evt.data;
        var params = container.paramsGenerator(container, button.page);
        if (params == null) return;
        disableField(btnPrevious);
        $.ajax({
            type: "GET",
            url: container.url,
            data: params,
            success: function(data, status, xhr) {
                enableField(btnPrevious);
                onSuccess(data);
            },
            error: function(xhr, status, err) {
                enableField(btnPrevious);
                onError();
            }
        });
	});
	
	var currentPageIndicator = $("<text>", {
		text: container.page.toString(),
		style: "font-size: 14pt; margin-left: 10px; margin-right: 10px;"
	}).appendTo(container.bar);

	var next = new PagerButton(container, page + 1);
    var btnNext = $("<button>", {
        text: "Next",
        style: "margin-left: 2px"
    }).appendTo(container.bar);
    btnNext.click(next, function(evt) {
        if (container.url == null) return;
        var button = evt.data;
        var params = container.paramsGenerator(container, button.page);
        if (params == null) return;
        disableField(btnNext);
        $.ajax({
            type: "GET",
            url: container.url,
            data: params,
            success: function(data, status, xhr) {
                enableField(btnNext);
                onSuccess(data);
            },
            error: function(xhr, status, err) {
                enableField(btnNext);
                onError();
            }
        });
    });
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
