/*
 * ExplorerTrigger
 * */

function ExplorerTrigger(node, pic, btn) {
	this.node = node;
	this.pic = pic;
	this.btn = btn;
	this.disable = function() {
		disableField(btn);
	};
	this.enable = function() {
		enableField(btn);
	};
	this.getFile = function() {
		if (this.btn == null) return null;	
		var files = btn[0].files;
		if (files == null) return null;	
		if (files.length != 1) {
			alert("Choose only 1 image at a time!!!");
			return null;
		}
		return files[0];
	}
	this.hide = function() {
		if (this.node == null)	return;
		this.node.hide();
	};
	this.show = function() {
		if (this.node == null)	return;
		this.node.show();
	};
	this.remove = function() {
		if (this.pic == null) return;
		this.pic.remove();
		this.pic = null;
		if (this.btn == null)	return;
		this.btn.remove();
		this.btn = null;
		if (this.node == null)	return;
		this.node.empty();
		this.node.remove();
		this.node = null;
	};
	this.shift = function(direction, distance) {
		// direction {
		//	left: -1,
		//	right: +1
		// }
		//
		// distance is an integer
		var currentOffset = getOffset(this.node);
		switch (direction) {
			case -1:
				var left = currentOffset.left - distance;
				setOffset(this.node, left, null);
			break;
			case +1:
				var left = currentOffset.left + distance;
				setOffset(this.node, left, null);
			break;
		}

	};
	this.changePic = function(imgSrc) {
		this.pic.attr("src", imgSrc);
	};
}

function generateExplorerTriggerSpan(par, onChange, imgSrc, nodeW, nodeH, picW, picH) {
	var node = $("<span>", {
		style: "position: absolute;"
	}).appendTo(par);
	setDimensions(node, nodeW, nodeH);
	setOffset(node, 0, 0); 	
	
	var pic = $("<img>", {
		style: "position: absolute; left: 0; top:0; right: 0; bottom:0; margin: auto;",
		src: imgSrc
	}).appendTo(node); 
	setDimensions(pic, picW, picH);

	// btn should have the same dimensions as node to be clickable
	var btn = $("<input>", {
		type: "file",
		style: "filter: alpha(opacity=0); opacity: 0; position: absolute;"
	}).appendTo(node);
	setDimensions(btn, nodeW, nodeH);

	btn.change(onChange);
	return new ExplorerTrigger(node, pic, btn); 
}

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
	this.first = 1;
	this.last = 1;
	this.prependPage = function(content) {

		var oldSize = Object.keys(this.map).length;
		if (this.map.hasOwnProperty(this.last) && oldSize == this.size)	{
			delete this.map[this.last];
			--this.last;
		}
		this.map[--this.first] = content;
		
	};

	this.appendPage = function(content) {

		var oldSize = Object.keys(this.map).length;
		if (this.map.hasOwnProperty(this.first) && oldSize == this.size)	{
			delete this.map[this.first];
			++this.first;
		}
		this.map[++this.last] = content;

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

function Pager(screen, bar, numItemsPerPage, url, paramsGenerator, pagerCache, filters, onSuccess, onError) {
	this.screen = screen; // screen of the pager
	this.nItems = numItemsPerPage; // number of items per page

	this.page = 1; // current page
	this.total = 0; // initial number of total pages should always be 0

	// starting & ending indices of the current page
	// the indices are set to -infinity & infinity respectively by default to facilitate initialization after the first query of items
	// they could be either integers or strings	
	this.st = -g_inf; 
	this.ed = g_inf; 
	this.url = url;

	// prototype: paramsGenerator(Pager, page)
	this.paramsGenerator = paramsGenerator;

	// prototypes: onSuccess(data), onError(err)
	this.onSuccess = onSuccess;
	this.onError = onError;
		
	// pager cache
	this.cache = pagerCache;

	// pager bar
	this.bar = bar; // control bar of the pager
	if (bar == null) return;
	this.refreshBar = function() {
		var pager = this;
		var page = pager.page;
		var pagerCache = pager.cache;
		// display pager bar 
		pager.bar.empty();
		var length = Object.keys(pagerCache.map).length;
		for(var key in pagerCache.map) {

			var index = parseInt(key);
			var indicator = $("<button>", {
				text: index,
				style: "color: cadetblue; font-size: 14pt; margin-left: 5px; margin-right: 5px;"
			}).appendTo(pager.bar);
		
			var pagerButton = new PagerButton(pager, index);
			indicator.click(pagerButton, function(evt) {
				if (pager.url == null) return;
				var button = evt.data;
				pager.page = button.page;
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
					pager.onSuccess(data);
				    },
				    error: function(xhr, status, err) {
					enableField(indicator);
					pager.onError(err);
				    }
				});
			});
			
			if (index != page) continue;
			indicator.css("font-weight", "bold");
			indicator.css("color", "black");
				
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
				pager.onSuccess(data);
			    },
			    error: function(xhr, status, err) {
				enableField(selector);
				pager.onError(err);
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

/*
 * Dropdown widget
 * */

function createDropdown(par, id) {
	var container = $("<div class='dropdown'>").appendTo(par);
	var toggle = $("<button id='" + id + "' class='btn btn-default dropdown-toggle' type='button' data-toggle='dropdown'>", {
		text: "Dropdown"
	}).appendTo(container);
	var sp = $("<span class='caret'>").appendTo(toggle);	
	var ul = $("<ul  aria-labelledby='" + id + "' class='dropdown-menu' role = 'menu'>").appendTo(container);
	var texts = ["Action", "Another Action", "Something else here"];
	for (var i = 0; i < texts.length; i++) {
		var li = $("<li role='presentation'>").appendTo(ul);
		var action = $("<a role='menuitem' tabindex='-1' href='#'>", {
			text: texts[i]
		}).appendTo(li);
	}
}
