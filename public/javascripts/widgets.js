/**
 * BaseWidget
 * */

function BaseWidget() {
	this.content = null;
		
	this.refresh = function(data) {
		if (this.content == null) return;
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
	this.container = null;
	this.dialog = null;
}

BaseModalWidget.inherits(BaseWidget);
BaseModalWidget.method('appendTo', function(par) {
		this.container = $("<div class='modal fade activity-editor' data-keyboard='false' data-backdrop='static' tabindex='-1' role='dialog' aria-labelledby='create' aria-hidden='true'>").appendTo(par);
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
var SLOT_AJAX_PENDING = 0;
var SLOT_UPLOADING = 1;

function ImageNode(remoteName) {
	this.state = SLOT_IDLE; 
	this.remoteName = remoteName; 
	this.requestUptoken = function(onSuccess, onError) {
		// async process
		var token = $.cookie(g_keyToken);				
		if (token == null) return;
		if (this.remoteName == null) return;
		if (this.state != SLOT_IDLE) return;

                // remote name is required for uptoken generation due to the need of CDN validation
		var params = {};
		params[g_keyToken] = token;
		params[g_keyRemoteName] = remoteName;

		this.state = SLOT_AJAX_PENDING;

		$.ajax({
			type: 'POST',
			url: '/image/cdn/qiniu/uptoken',
			data: params,
			success: function(data, status, xhr) {
			        this.state = SLOT_IDLE;
			        if (onSuccess == null) return;
			        onSuccess(data);
			},
			error: function(xhr, status, err) {
			        this.state = SLOT_IDLE;
				if (onError == null) return;
				onError(err);
			}
		})
	};
	this.upload = function() {
	}
	this.requestDel = function(onSuccess, onError) {
		// async process
		var token = $.cookie(g_keyToken);				
		if (token == null) return;
		if (this.remoteName == null) return;
		if (this.state != SLOT_IDLE) return;

		var params = {};
		params[g_keyToken] = token;
		params[g_keyRemoteName] = remoteName;

		this.state = SLOT_AJAX_PENDING;

		$.ajax({
			type: 'POST',
			url: '/image/cdn/qiniu/delete',
			data: params,
			success: function(data, status, xhr) {
			        this.state = SLOT_IDLE;
			        if (onSuccess == null) return;
			        onSuccess(data);
			},
			error: function(xhr, status, err) {
			        this.state = SLOT_IDLE;
				if (onError == null) return;
				onError(err);
			}
		})
	};

	this.composeContent = function(data) {
		
		var editor = data.editor; // profile or activity editor
		var fileref = data.fileref; // from 'e.target.result' of reader(FileReader) 'onload' event 'e' invoked by 'reader.readAsDataURL(<file>)' 
		var preview = $('<div>', {
			"class": "preview-container left"
		}).appendTo(this.content);

		var imgHelper = $('<span>', {
			"class": "image-helper"
		}).appendTo(preview);

		var img = $('<img>', {
			src: fileref 
		}).appendTo(preview);

		var btnDelete = $("<button>", {
			text: TITLES["delete"],
			"class": "image-delete positive-button"
		}).appendTo(preview).click(this.remoteName, function(evt){
			evt.preventDefault();
			editor.setSavable();
			editor.setNonSubmittable();

			var remoteName = evt.data;

			if(!editor.newImageNodes.hasOwnProperty(remoteName)) return;
			var thatNode = editor.newImageNodes[remoteName];
			var aButton = getTarget(evt);
			var onSuccess = function(data) {
                                enableField(aButton);
			}
			var onError = function(err) {
                                enableField(aButton);
			}
			disableField(aButton);
			thatNode.requestDel(onSuccess, onError);
			thatNode.remove();
			delete editor.newImageNodes[remoteName];
			editor.explorerTrigger.shift(-1, g_wImageCell);
		});
	};
}

ImageNode.inherits(BaseWidget);

/**
 * AjaxButton
 * */

function AjaxButton(text, url, clickData, method, extraParams, onSuccess, onError) {
	this.text = text;
	this.url = url;
	this.clickData = clickData;
	this.method = method;
	this.extraParams = extraParams;
	this.onSuccess = onSuccess;
	this.onError = onError;
	this.button = null;
	this.appendTo = function(par) {
		this.remove();
		this.button = $("<button>", {
			text: this.text,
			"class": "indianred"
		}).appendTo(par);
		var dButton = {
			url: this.url,
			method: this.method,
			clickData: this.clickData,
			extraParams: this.extraParams,
			onSuccess: this.onSuccess,
			onError: this.onError
		};
		this.button.click(dButton, function(evt){
			evt.preventDefault();
			var aClickData = evt.data.clickData;
			var aUrl = evt.data.url;
			var aMethod = evt.data.method;
			var aExtraParams = evt.data.extraParams; 
			var aOnSuccess = evt.data.onSuccess;
			var aOnError = evt.data.onError;
			var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
			disableField(aButton);
			$.ajax({
				url: aUrl,
				type: aMethod,
				data: aExtraParams,
				success: function(data, status, xhr) {
					enableField(aButton);
					if (aOnSuccess == null) return;
					aOnSuccess(data);
				},
				error: function(xhr, status, err) {
					enableField(aButton);
					if (aOnError == null) return;
					aOnError(err);
				}
			});
		});
	};
	this.remove = function() {
		if (this.button == null) return;
		this.button.remove();
		this.button = null;
	};
}

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
		var files = this.btn[0].files;
		if (files == null) return null;	
		if (files.length != 1) {
			alert(ALERTS["choose_one_image"]);
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
	var node = $("<div>", {
		"class": "add-image"
	}).appendTo(par);
	setDimensions(node, nodeW, nodeH);
	setOffset(node, 0, 0); 	
	
	var pic = $("<span>", {
		"class": "pic"
	}).appendTo(node); 
	setBackgroundImageDefault(pic, imgSrc);
	setDimensions(pic, picW, picH);
	setOffset(pic, (nodeW - picW)/2, (nodeH - picH)/2); 	

	// btn should have the same dimensions as node to be clickable
	var btn = $("<input>", {
		type: "file"
	}).appendTo(node);
	setDimensions(btn, nodeW, nodeH);

	btn.change(onChange);
	return new ExplorerTrigger(node, pic, btn); 
}

/*
 * Datetime Picker
 */
function DatetimePicker(input) {
	this.input = input;	
	this.getDatetime = function() {
		var dateStr = this.input.val();
		if (dateStr == null || dateStr == "" || dateStr.length == 0) return null;
		return dateStr + ":00";
	}
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

function Pager(screen, bar, numItemsPerPage, url, paramsGenerator, extraParams, pagerCache, filters, onSuccess, onError) {

	// TODO: refactor with container-dialog-appendTo-refresh pattern
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
	this.extraParams = extraParams;

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
		if (length <= 1) return;
		for(var key in pagerCache.map) {

			var index = parseInt(key);
			var indicator = $("<button>", {
				text: index,
				"class": "plain-button pager-button"
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
			indicator.off("mouseenter mouseleave");
			indicator.addClass("active-button");
		}	
	};	

	this.squeeze = function() {
		// encapsulated for convenience
		setDimensions(this.screen.parent(), "0px", null);
		this.screen.parent().hide();
	};

	this.expand = function(width) {
		// encapsulated for convenience
		if (width == null) width = "100%";
		setDimensions(this.screen.parent(), width, null);
		this.screen.parent().show();
	};
		
	this.remove = function() {
		if (this.screen == null) return;
		this.screen.remove();
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
		});	
	}
}

/*
 * Return a modal handle
 * */
function createModal(par, message, widthRatioPercentage, heightRatioPercentage) {
	var pager = $("<div class='modal fade general-popup' tabindex='-1' role='dialog' aria-labelledby='none' aria-hidden='true'>").appendTo(par);	
	var dialog = $("<div class='modal-dialog modal-lg'>").appendTo(pager);
	var content = $("<div class='modal-content'>").appendTo(dialog);
	var div = $("<div>", {
		"class": "general-popup-paragraph",
		text: message
	}).appendTo(content);
	return pager;
}

function showModal(container) {
	container.modal("show");
}

function hideModal(container) {
	container.modal("hide");
}

function removeModal(container){
	container.empty();
	container.remove();
	container = null;
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

function DropdownMenu(toggle, items, reactions) {
	// TODO: refactor with container-dialog-appendTo-refresh pattern
	this.toggle = toggle; // toggle is button element
	this.items = items; // items are <li> elements
	this.reactions = reactions; // reactions are onClick(evt) functions
	this.reactionParams = null;
	this.setReactionParams = function(params) {
		this.reactionParams = params;	
		var length = params.length;
		// assign reactions to items
		for (var i = 0; i < length; i++) {
			this.items[i].click(params[i], reactions[i]);
		}
	};
}

function createDropdownMenu(par, id, menuTitle, icons, actionNames, titles, reactions) {
	var length = titles.length;
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
	for (var i = 0; i < titles.length; i++) {
		var li = $("<li class='action-" + actionNames[i] + " patch-block-gamma'>").appendTo(ul);
		var action = $("<a class='patch-block-gamma' tabindex='-1' href='#' title='"+titles[i]+"'>").appendTo(li);
		//action.css("font-size", "15pt");
		//action.css("display", "block"); // increase the size of the link target, ref: http://css-tricks.com/keep-margins-out-of-link-lists/
		//action.css("padding", "5px");
		//action.css("text-align", "center");
		//action.css("vertical-align", "middle");
		//setBackgroundImage(action, icons[i], "contain", "no-repeat", "left center");
		action.text(titles[i]);
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

function createNavTab(par, refs, titles, preactiveRef, sectionPanes, contents) {
	var ul = $("<ul class='nav nav-pills' role='tablist'>").appendTo(par);
	var length = refs.length;
	for (var i = 0; i < length; i++) {
		var li = null;
		if (refs[i] == preactiveRef)	li = $("<li role='presentation' class='active'>").appendTo(ul);
		else li = $("<li role='presentation'>").appendTo(ul);
		var href = $("<a href='#" + refs[i] + "' role='tab' data-toggle='tab'>").appendTo(li);
		href.text(titles[i]);	
	}
	var panes = [];
	for (var i = 0; i < length; i++) {
		var isPreactive = (refs[i] == preactiveRef); 
		var pane = createNavTabPane(sectionPanes, refs[i], isPreactive, contents[i]);
		panes.push(pane);
	}
	return new NavTab(panes);
}

function createNavTabPane(par, ref, isPreactive, content) {
	var pane = null;	
	if (isPreactive) pane = $("<div role ='tabpanel' class='tab-pane fade in active' id='" + ref + "'>").appendTo(par);
	else pane = $("<div role='tabpanel' class='tab-pane' fade id='" + ref + "'>").appendTo(par);
	if (content == null) return pane;
	pane.append(content);
	return pane;	
}

/*
 * Captcha Widget
 * */

function Captcha(sid) {
	this.sid = sid;
	this.input = null;
	this.img = null;
	this.hide = function() {
		this.input.hide();
		this.img.hide();
		this.input.parent().hide();
	};
	this.show = function() {
		this.input.parent().show();
		this.input.show();
		this.img.show();
	};
	this.appendTo = function(par) {
		var row = $("<div>", {
			"class": "captcha"
		}).appendTo(par);
		this.input = $("<input>", {
			placeHolder: HINTS["captcha"]
		}).appendTo(row);
		this.img = $("<img>", {
			src: "/captcha?" + g_keySid + "=" + this.sid
		}).appendTo(row);
		var btnChange = $("<button>", {
			"class": "change"
		}).appendTo(row);

		btnChange.click(this, function(evt) {
			evt.preventDefault();
			var captcha = evt.data;	
			captcha.img.attr("src", "/captcha?" + g_keySid + "=" + captcha.sid + "&ts=" + new Date().getTime());
		});
	}
}

/*
 * WordCounter Widget
 * */

function WordCounter(text, min, max, regex, violationHint) {
	this.text = text;
	this.min = min;
	this.max = max;
	this.regex = regex;
	this.violationHint = violationHint;
	this.currentText = null;
	this.maxText = null;
	this.hintText = null;
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
