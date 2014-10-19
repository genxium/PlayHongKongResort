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

function createSelector(par, titles, values, width, height, left, top) {
	if (titles.length != values.length) return;
	var length = titles.length;

	var ret = $("<select>").appendTo(par);
	setMargin(ret, "3pt", null, "3pt", null);
	setDimensions(ret, width, height); 
	setOffset(ret, left, top);
	
	for (var i = 0; i < length; ++i) {
		var title = titles[i];
		var val = values[i];
		$("<option>", {
			text: title,
			value: val
		}).appendTo(ret);
	}	

	return ret;
}

function PagerFilter(key, selector) {
	this.key = key;
	this.selector = selector;
}

function PagerCache(size) {
	this.size = size;
	this.map = {};
	this.first = 0;
	this.last = 0;
	this.prependPage = function(content) {

		var oldSize = Object.keys(this.map).length;
		if (this.map.hasOwnProperty(this.first))	{
			delete this.map[this.first];
			--this.first;
		}
		if (oldSize >= this.size) {
			delete this.map[this.last];
			--this.last;
		}
		this.map[this.first] = content;
		
	};

	this.appendPage = function(content) {

		var oldSize = Object.keys(this.map).length;
		if (this.map.hasOwnProperty(this.first))	{
			delete this.map[this.first];
			++this.first;
		}
		if (oldSize >= this.size) {
			delete this.map[this.last];
			++this.last;
		}
		this.map[this.last] = content;

	};

	this.putPage = function(page, content) {
		if (page > this.last) this.appendPage(content);
		else if (page < this.first) this.prependPage(content);
		else this.map[page] = content;
	}
}

function PagerButton(pager, page) {
	// the pager button is determined to trigger only "GET" ajax
	this.pager = pager;
	this.page = page;
}

function Pager(screen, bar, numItemsPerPage, url, paramsGenerator, pagerCache, filters, onQuerySuccess, onQueryError) {
	this.screen = screen; // screen of the pager
	this.nItems = numItemsPerPage; // number of items per page

	this.page = 0; // current page
	this.total = 0; // initial number of total pages should always be 0

	// starting & ending indices of the current page
	// the indices are set to -infinity & infinity respectively by default to facilitate initialization after the first query of items
	// they could be either integers or strings	
	this.st = -g_inf; 
	this.ed = g_inf; 
	this.url = url;

	// prototype: paramsGenerator(Pager, page)
	this.paramsGenerator = paramsGenerator;

	// prototypes: onQuerySuccess(data), onQueryError()
	this.onQuerySuccess = onQuerySuccess;
	this.onQueryError = onQueryError;
		
	// pager cache
	this.cache = pagerCache;

	// pager bar
	this.bar = bar; // control bar of the pager
	if (bar == null) return;
	this.refreshBar = function(page) {
		var pager = this;
		var pagerCache = pager.cache;
		// display pager bar 
		pager.bar.empty();
		var length = Object.keys(pagerCache.map).length;
		for(var key in pagerCache.map) {

			var index = parseInt(key) + 1;
			var indicator = $("<button>", {
				text: index,
				style: "font-size: 14pt; margin-left: 10px; margin-right: 10px;"
			}).appendTo(pager.bar);
		
			var pagerButton = new PagerButton(pager, index);
			indicator.click(pagerButton, function(evt) {
				if (pager.url == null) return;
				var button = evt.data;
				var params = pager.paramsGenerator(pager, button.page);
				if (params == null) return;
				var indicator = $(this);
				disableField(indicator);
				$.ajax({
				    type: "GET",
				    url: pager.url,
				    data: params,
				    success: function(data, status, xhr) {
					enableField(indicator);
					pager.onQuerySuccess(data);
				    },
				    error: function(xhr, status, err) {
					enableField(indicator);
					pager.onQueryError();
				    }
				});
			});
			
			if (index != page) continue;
			indicator.css("font-weight", "bold");
				
		}	
	};	

	// multi-level filters
	this.filters = filters;
	
	if (filters == null) return;
	var pager = this;
	for (var i = 0; i < filters.length; ++i) {
		var filter = filters[i];
		filter.selector.change(pager, function(evt){
			var pager = evt.data;
			var params = pager.paramsGenerator(pager, 1);	
			if (params == null) return;
			var selector = filter.selector;
			disableField(selector);
			$.ajax({
			    type: "GET",
			    url: pager.url,
			    data: params,
			    success: function(data, status, xhr) {
				enableField(selector);
				pager.onQuerySuccess(data);
			    },
			    error: function(xhr, status, err) {
				enableField(selector);
				pager.onQueryError();
			    }
			});
		});	
	}
}

/*
 * Return a modal handle
 * */
function createModal(par, message, widthRatioPercentage, heightRatioPercentage){
	var pager=$("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='' aria-hidden='true'>", {
		style: "width: " + widthRatioPercentage + "%; height: " + heightRatioPercentage + "%"
	}).appendTo(par);	
	var dialog=$("<div class='modal-dialog modal-lg'>").appendTo(pager);
	var content=$("<div class='modal-content'>", {
		text: message
	}).appendTo(dialog);
	return pager;
}

function showModal(container) {
	container.modal({
                show: true    
        });
}

function hideModal(container) {
	container.modal("hide");
}

function removeModal(container){
	container.empty();
	container.remove();
}

function createBinarySwitch(par, disabled, initVal, disabledText, positiveText, negativeText, inputId){
	var container = $("<div class='onoffswitch'>").appendTo(par);
	var input = $("<input type='checkbox' class='onoffswitch-checkbox' id='"+inputId+"'>").appendTo(container);
	var label = $("<label class='onoffswitch-label' for='"+inputId+"'>").appendTo(container);
	var inner = $("<span class='onoffswitch-inner'>").appendTo(label);
	var sw = $("<span class='onoffswitch-switch'>").appendTo(label);

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
	disableField(input);
}
