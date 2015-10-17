/**
 * BaseWidget
 * */

function BaseWidget() {
	this.refresh = function(data) {
		if (!this.content) return;
		this.content.empty();
		// method variable 'composeContent(data)' should be implemented by subclasses 
		this.composeContent(data);
	};

	this.appendTo = function(par) {
		this.content= $('<div>').appendTo(par);
	};	

	this.show = function() {
		this.content.show();
	};

	this.hide = function() {
		this.content.hide();
	};

	this.remove = function() {
		this.content.remove();
	};
}

/**
 * BaseModalWidget 
 */

function BaseModalWidget() {
}

BaseModalWidget.inherits(BaseWidget);
BaseModalWidget.method('appendTo', function(par, isStatic, containerClass) {
		this.container = $("<div class='modal fade' data-keyboard='false' tabindex='-1' role='dialog' aria-labelledby='create' aria-hidden='true'>").appendTo(par);
		if (!(!containerClass)) this.container.addClass(containerClass);
		if (!(!isStatic)) this.container.attr("data-backdrop", "static"); 
		this.dialog = $("<div class='modal-dialog modal-lg'>").appendTo(this.container);
		this.content= $("<div class='modal-content'>").appendTo(this.dialog);
});
BaseModalWidget.method('show', function() {
		this.container.modal("show");
});
BaseModalWidget.method('hide', function() {
		this.container.modal("hide");
});
BaseModalWidget.method('remove', function() {
		this.container.remove();
});

/**
 * ImageNode
 * */

var SLOT_IDLE = 0;
var SLOT_UPLOADING = 1;
var SLOT_UPLOAD_FAILED = 2;
var SLOT_AJAX_PENDING = 3;

function ImageNode() {

	// TODO: only supports ProfileEditor and ActivityEditor at the moment
	this.state = SLOT_IDLE; 
	this.uploader = null;
	this.wrap = null;
	this.preview = null;
	this.editor = null;
	this.btnChoose = null;

	this.setCDNCredentials = function(cdn, bucketDomain, token, player) {
		this.cdn = cdn;
		this.bucketDomain = bucketDomain;
		var tick = currentMillis();
		this.remoteName =  '{0}_{1}'.format(player.id, tick);
		var uptokenParams = [g_keyToken + '=' + token, g_keyDomain + '=' + this.bucketDomain, g_keyRemoteName + '=' + this.remoteName];
		this.uptokenUrl = '/image/cdn/qiniu/uptoken?' + uptokenParams.join('&'); 
	};
}

ImageNode.inherits(BaseWidget);

/**
 * AjaxButton
 * */

function AjaxButton(text, url, clickData, type, extraParams, onSuccess, onError) {
	this.text = text;
	this.url = url;
	this.clickData = clickData;
	this.type = type;
	this.extraParams = extraParams;
	this.onSuccess = onSuccess;
	this.onError = onError;
	this.composeContent = function(dButton) {
		this.button = $("<button>", {
			text: this.text,
		}).appendTo(this.content).click(dButton, function(evt){
			evt.preventDefault();
			var aClickData = evt.data.clickData;
			var aUrl = evt.data.url;
			var aType = evt.data.type;
			var aExtraParams = evt.data.extraParams; 
			var aOnSuccess = evt.data.onSuccess;
			var aOnError = evt.data.onError;
			var aButton = getTarget(evt) 
			disableField(aButton);
			$.ajax({
				url: aUrl,
				type: aType,
				data: aExtraParams,
				success: function(data, status, xhr) {
					enableField(aButton);
					if (!aOnSuccess) return;
					aOnSuccess(data);
				},
				error: function(xhr, status, err) {
					enableField(aButton);
					if (!aOnError) return;
					aOnError(err);
				}
			});
		});
	};
}

AjaxButton.inherits(BaseWidget);

/*
 * Datetime Picker
 */
function DatetimePicker(input) {
	this.input = input;	
	this.getDatetime = function() {
		var dateStr = this.input.val();
		if (!dateStr || dateStr === "" || dateStr.length === 0) return null;
		return dateStr + ":00";
	};
}

function generateDatePicker(par, time, onEdit) {
    
	var container = $('<div>', {
		//"class": 'col-sm-6'    
	}).appendTo(par);

	var formGroup = $('<div>', {
		//"class": 'form-group'    
	}).appendTo(container);

	var inputGroup = $("<div>", {
		"class": 'input-group date'    
	}).appendTo(formGroup);

	var input = $("<input>", {
		type: 'text',
		value: time,
		disabled: true,
		"class": "form-control",
	}).appendTo(inputGroup);

	var inputGroupAddon = $('<span>', {
		"class": 'input-group-addon'    
	}).appendTo(inputGroup);

	var glyphiconCalendar = $('<span>', {
		"class": 'glyphicon glyphicon-calendar'   
	}).appendTo(inputGroupAddon);

	inputGroup.datetimepicker({
		format: 'YYYY-MM-DD HH:mm',
		pickSeconds: false,
		pick12HourFormat: false  
	});

	container.on("input change keyup", onEdit);
	return new DatetimePicker(input);
}

/*
 * Pager Widgets
 */

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
	};
}

function PagerButton(pager, page) {
	// the pager button is determined to trigger only "GET" ajax
	this.pager = pager;
	this.page = page;
}

// TODO: create subclasses HomeActivityPager, ProfileActivityPager, CommentPager, AssessmentPager etc. 
function Pager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	
	this.init = function(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {

		this.nItems = numItemsPerPage; 
		this.page = 1; 
		this.total = 0; 

		// starting & ending indices of the current page
		// the indices are set to -infinity & infinity respectively by default to facilitate initialization after the first query of items
		// they could be either integers or strings	
		this.st = -g_inf; 
		this.ed = g_inf; 

		this.url = url;
		// prototype: paramsGenerator(Pager, page)
		this.paramsGenerator = paramsGenerator;
		this.extraParams = extraParams;

		this.onSuccess = onSuccess;
		this.onError = onError;

		this.cache = new PagerCache(cacheSize);

		// filter map in {key: [titleList, valueList]} format
		this.filterMap = filterMap;		
	};

	this.refreshFilters = function() {
		this.filterList = [];
		if (!this.filterMap) return;
		for (var key in this.filterMap) {
			var tuplet = this.filterMap[key];	
			var titleList = tuplet[0];
			var valueList = tuplet[1];	
			
			var selector  = $("<select>").appendTo(this.filterBar);
			
			var length = titleList.length;
			for (var i = 0; i < length; ++i) {
				var title = titleList[i];
				var value = valueList[i];
				$("<option>", {
					text: title,
					value: value
				}).appendTo(selector);
			}	
			var selectorOnChange = function(evt){
				var pager = evt.data;
				var params = pager.paramsGenerator(pager, 1);	
				if (!params) return;
				var selector = filter.selector;
				disableField(selector);
				$.ajax({
				    type: "GET",
				    url: pager.url,
				    data: params,
				    success: function(data, status, xhr) {
						var size = pager.cache.size;
						pager.cache = new PagerCache(size);
						enableField(selector);
						pager.onSuccess(data);
				    },
				    error: function(xhr, status, err) {
						enableField(selector);
						pager.onError(err);
				    }
				});
			};
			selector.change(this, selectorOnChange);	
			var filter = new PagerFilter(key, selector);
			this.filterList.push(filter);
		}	
	};
	this.createFilters = function() {
		this.filterBar = $("<div>").appendTo(this.content);
		this.refreshFilters();
	};

	// pager bar
	this.refreshBar = function() {
		var pager = this;
		var page = pager.page;
		var pagerCache = pager.cache;

		pager.bar.empty();
		var length = Object.keys(pagerCache.map).length;
		if (length <= 1) return;
		var indicatorOnClick = function(evt) {
			if (!pager.url) return;
			var indicator = getTarget(evt);
			var pagerButton = evt.data;
			pager.page = pagerButton.page;
			var params = pager.paramsGenerator(pager, pagerButton.page);
			if (!params) return;
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
		};
		for(var key in pagerCache.map) {
			var index = parseInt(key);
			var indicator = $("<button>", {
				text: index,
				"class": "plain-button pager-button"
			}).appendTo(pager.bar);
			var pagerButton = new PagerButton(pager, index);
			indicator.click(pagerButton, indicatorOnClick);
			
			if (index != page) continue;
			indicator.off("mouseenter mouseleave");
			indicator.addClass("active-button");
		}	
	};	
	this.createBar = function() {
		this.bar = $("<div>").appendTo(this.content); // control bar of the pager
		this.refreshBar();
	}; 

	this.refreshScreen = function(data) {
		this.screen.empty();
		this.updateScreen(data);
		this.refreshBar();
	};

	this.createScreen = function(data) {
		this.screen = $("<div>").appendTo(this.content);
		this.refreshScreen(data);
	};

	this.composeContent = function(data) {
		if (!this.filterBar) this.createFilters();
		if (!this.bar) this.createBar();
		if (!this.screen) this.createScreen(data);
	}; 
}

Pager.inherits(BaseWidget);

function Announcement() {
	this.composeContent = function(data) {
		setDimensions(this.content, "80%", "90%");
		var message = data;
		var div = $("<div>", {
			"class": "general-popup-paragraph",
			text: message
		}).appendTo(this.content);
	};
}

Announcement.inherits(BaseModalWidget);

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

function DropdownMenu(toggle, items, reactions) {
	this.toggle = toggle; // toggle is button element
	this.items = items; // items are <li> elements
	this.reactions = reactions; // reactions are onClick(evt) functions
	this.setReactionParams = function(params) {
		this.reactionParams = params;	
		var length = params.length;
		// assign reactions to items
		for (var i = 0; i < length; i++) {
			this.items[i].click(params[i], reactions[i]);
		}
	};
}

function createDropdownMenu(par, id, menuTitle, icons, actionNames, titleList, reactions) {
	var length = titleList.length;
	if (length != icons.length) return; 
	var container = $("<div>", { "class": "menu-actions" }).appendTo(par);
	// these params indicate that the container is centred
	//container.css("position", "absolute");
	//container.css("width", "90%")
	//container.css("left", "5%")
	//container.css("height", "60%");
	//container.css("top", "20%")
	
	/*var toggle = $("<button id='" + id + "' class='btn btn-default dropdown-toggle' type='button' data-toggle='dropdown'>").appendTo(container);
	toggle.css("font-size", "1em");
	toggle.css("width", "100%")
	toggle.css("height", "100%");
	
	toggle.text(menuTitle);*/
	var toggle = null;
	//var sp = $("<span class='caret'>").appendTo(toggle);	
	//var ul = $("<ul  aria-labelledby='" + id + "' class='dropdown-menu' role = 'menu'>").appendTo(container);
	var ul = $("<ul>", {
	}).appendTo(container); 
	var lis = [];
	for (var i = 0; i < titleList.length; i++) {
		var li = $("<li class='action-" + actionNames[i] + " patch-block-gamma'>").appendTo(ul);
		var action = $("<a class='patch-block-gamma' tabindex='-1' href='#' title='"+titleList[i]+"'>").appendTo(li);
		//action.css("font-size", "15pt");
		//action.css("display", "block"); // increase the size of the link target, ref: http://css-tricks.com/keep-margins-out-of-link-lists/
		//action.css("padding", "5px");
		//action.css("text-align", "center");
		//action.css("vertical-align", "middle");
		//setBackgroundImage(action, icons[i], "contain", "no-repeat", "left center");
		action.text(titleList[i]);
		lis.push(li);
	}
	return new DropdownMenu(toggle, lis, reactions);
}

/*
 * Navigation tabs and tab panes
 * */

function NavTab(panes) {
	this.panes = panes;
}

function createNavTab(par, refs, titleList, preactiveRef, sectionPanes, contents) {
	var ul = $("<ul class='nav nav-pills' role='tablist'>").appendTo(par);
	var length = refs.length;
	for (var i = 0; i < length; i++) {
		var li = null;
		if (refs[i] == preactiveRef)	li = $("<li role='presentation' class='active'>").appendTo(ul);
		else li = $("<li role='presentation'>").appendTo(ul);
		var href = $("<a href='#" + refs[i] + "' role='tab' data-toggle='tab'>").appendTo(li);
		href.text(titleList[i]);	
	}
	var panes = [];
	for (var j = 0; j < length; j++) {
		var isPreactive = (refs[j] == preactiveRef); 
		var pane = createNavTabPane(sectionPanes, refs[j], isPreactive, contents[j]);
		panes.push(pane);
	}
	return new NavTab(panes);
}

function createNavTabPane(par, ref, isPreactive, content) {
	var pane = null;	
	if (isPreactive) pane = $("<div role ='tabpanel' class='tab-pane fade in active' id='" + ref + "'>").appendTo(par);
	else pane = $("<div role='tabpanel' class='tab-pane' fade id='" + ref + "'>").appendTo(par);
	if (!content) return pane;
	pane.append(content);
	return pane;	
}

/*
 * Captcha Widget
 * */

function Captcha(sid) {
	this.sid = sid;
	this.hasImg = function() {
		var imgSrc = this.img.attr("src");
		return (!(!imgSrc) && imgSrc.length !== 0);
	};
	this.updateImg = function() {
		this.img.attr("src", "/captcha?" + g_keySid + "=" + this.sid + "&ts=" + new Date().getTime());
	};
	this.composeContent = function(data) {
		var row = $("<div>", {
			"class": "captcha"
		}).appendTo(this.content);
		this.input = $("<input>", {
			placeHolder: HINTS.captcha
		}).on("focusin", this, function(evt) {
			var widget = evt.data;
			if (widget.hasImg()) return;
			widget.updateImg();	 
		}).appendTo(row);
		this.img = $("<img>", {
			src: ""
		}).appendTo(row);
		var btnChange = $("<button>", {
			"class": "change"
		}).appendTo(row);

		btnChange.click(this, function(evt) {
			evt.preventDefault();
			var widget = evt.data;	
			widget.updateImg();
		});
	};
}

Captcha.inherits(BaseWidget);

/*
 * WordCounter Widget
 * */

function WordCounter(text, min, max, regex, violationHint) {
	this.text = text;
	this.min = min;
	this.max = max;
	this.regex = regex;
	this.violationHint = violationHint;
	this.update = function(text) {
		this.text = text;
		this.currentText.text(text.length);	
		if (this.valid()) {
			this.currentText.css("color", "gray");
			this.hintText.text("");
		} else {
			this.currentText.css("color", "red");
			this.hintText.text(this.violationHint);
		}
	};
	this.appendCounter = function(par) {
		var row = $("<p>").appendTo(par);
		this.currentText = $("<span>", {
			"class": "word-counter-current",
			text: this.text.length
		}).appendTo(row);	
		this.maxText = $("<span>", {
			"class": "word-counter-max",
			text: "/" + this.max.toString()
		}).appendTo(row);
		this.hintText = $("<span>", {
			"class": "word-counter-violation-hint",
			text: ""
		}).appendTo(row);	
		this.update(this.text);
	};
	this.valid = function() {
		return (regex.test(this.text));
	};
}

/**
 * RegexInputWidget
 * */

function RegexInputWidget() {

}

/**
 * SearchWidget
 * */

function SearchWidget() {
	this.collapse = function() {

	};
	this.expand = function() {

	};
}

SearchWidget.inherits(BaseWidget);

function createSelector(par, titles, values, width, height, left, top) {
	if (titles.length != values.length) return;
	var length = titles.length;

	var ret = $("<select>").appendTo(par);
	
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
