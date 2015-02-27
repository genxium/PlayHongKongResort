/**
 * AjaxButton
 * */

function AjaxButton(url, clickData, method, extraParams, onSuccess, onError) {
	this.url = url;
	this.clickData = clickData;
	this.method = method;
	this.extraParams = extraParams;
	this.onSuccess = onSuccess;
	this.onError = onError;
	this.button = null;
	this.appendTo = function(par) {
		this.remove();
		this.button = $("<button>").appendTo(par);
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
		class: "add-image"
	}).appendTo(par);
	//setDimensions(node, nodeW, nodeH);
	//setOffset(node, 0, 0); 	
	var pic = $("<span>", {
		text: "add-image",
		class: "purple"
	}).appendTo(node); 
	//setDimensions(pic, picW, picH);

	// btn should have the same dimensions as node to be clickable
	var btn = $("<input>", {
		type: "file"
	}).appendTo(node);
	//setDimensions(btn, nodeW, nodeH);
	

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
		//class: 'col-sm-6'    
	}).appendTo(par);

	var formGroup = $('<div>', {
		//class: 'form-group'    
	}).appendTo(container);

	var inputGroup = $("<div>", {
		class: 'input-group date'    
	}).appendTo(formGroup);

	var input = $("<input>", {
		type: 'text',
		value: time,
		disabled: true,
		class: "form-control",
	}).appendTo(inputGroup);

	var inputGroupAddon = $('<span>', {
		class: 'input-group-addon'    
	}).appendTo(inputGroup);

	var glyphiconCalendar = $('<span>', {
		class: 'glyphicon glyphicon-calendar'   
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
	/*setMargin(ret, "3pt", null, "3pt", null);
	setDimensions(ret, width, height); 
	setOffset(ret, left, top);*/
	
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
				class: "gray"
			}).appendTo(pager.bar);
			/*indicator.hover(
				function(evt){
					$(this).css("background-color", "cornflowerblue");
				}, 
				function(evt){
					$(this).css("background-color", "gray");
				}
			);*/
		
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
			indicator.off("mouseenter mouseleave"); // unbind hovering
			//indicator.css("background-color", "blue");
			//indicator.css("font-weight", "bold");
			indicator.addClass("active");
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
		class: "general-popup-paragraph",
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
	var container = $("<div>", { class: "menu-actions" }).appendTo(par);
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
		var li = $("<li class='action-"+actionNames[i]+"'>").appendTo(ul);
		var action = $("<a tabindex='-1' href='#' title='"+titles[i]+"'>").appendTo(li);
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
	this.appendCaptcha = function(par) {
		var row = $("<div>", {
			class: "captcha"
		}).appendTo(par);
		this.input = $("<input>", {
			placeHolder: HINTS["captcha"]
		}).appendTo(row);
		this.img = $("<img>", {
			src: "/captcha?" + g_keySid + "=" + this.sid
		}).appendTo(row);
		var btnChange = $("<button>", {
			class: "change"
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
			class: "word-counter-current",
			text: this.text.length
		}).appendTo(row);	
		this.maxText = $("<span>", {
			class: "word-counter-max",
			text: "/" + this.max.toString()
		}).appendTo(row);
		this.hintText = $("<span>", {
			class: "word-counter-violation-hint",
			text: ""
		}).appendTo(row);	
		this.update(this.text);
	};
	this.valid = function() {
		return (regex.test(this.text));
	};
}

var g_avatarEditor = null;
var g_sectionAvatarEditor = null;
var g_modalAvatarEditor = null;
function AvatarEditor(container, image, btnChoose, btnUpload, hint) {
	this.container = container;
	this.image = image;
	this.btnChoose = btnChoose;
	this.btnUpload = btnUpload;
	this.hint = hint;
	this.getFile = function() {
		if (this.btnChoose == null) return null;	
		var files = this.btnChoose[0].files;
		if (files == null) return null;	
		if (files.length != 1) {
			alert(ALERTS["choose_one_image"]);
			return null;
		}
		return files[0];
	};
	this.btnChoose.change(this, function(evt) {
		evt.preventDefault();
		var editor = evt.data;
		var file = editor.getFile();
		if (file == null) return;
		if (!validateImage(file)) return;
		var reader = new FileReader();
		reader.onload = function (e) {
			editor.image.attr("src", e.target.result);
		}
		reader.readAsDataURL(file);
	});
	this.btnUpload.click(this, function(evt) {
		evt.preventDefault();
		var editor = evt.data;	
		var file = editor.getFile();
		if (!validateImage(file))	return;

		var token = $.cookie(g_keyToken);
		if (token == null) return;

		var formData = new FormData();
		formData.append(g_keyAvatar, file);
		formData.append(g_keyToken, token);
		var aButton = $(this);
		disableField(aButton);	
		editor.hint.text(MESSAGES["uploading"]);
		
		$.ajax({
			method: "POST",
			url: "/user/avatar/upload", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				enableField(aButton);	
				editor.hint.text(MESSAGES["uploaded"]);
			},
			error: function(xhr, status, err){
				enableField(aButton);	
				editor.hint.text(MESSAGES["upload_failed"]);
			}
		});
	});

	this.show = function() {
		this.container.show();
	};

	this.hide = function() {
		this.container.hide();
	};

	this.remove = function() {
		this.container.remove();
	};	
}

function removeAvatarEditor() {
	if(g_sectionAvatarEditor == null) return;
	g_sectionAvatarEditor.hide();
	g_sectionAvatarEditor.modal("hide");
	if(g_modalAvatarEditor == null) return;
	g_modalAvatarEditor.empty();
	if (g_avatarEditor == null) return;
	g_avatarEditor.remove();
}

function refreshAvatarEditor(user) {
	g_modalAvatarEditor.empty();
	g_avatarEditor = generateAvatarEditor(g_modalAvatarEditor, user);
}

function showAvatarEditor(user) {
	refreshAvatarEditor(user);
	g_sectionAvatarEditor.modal("show");
}

function initAvatarEditor(par) {
	g_sectionAvatarEditor = $("<div class='modal fade avatar-editor' tabindex='-1' role='dialog' aria-labelledby='AvatarEditor' aria-hidden='true'>").appendTo(par);
	 
	var dialog = $("<div>", {
		class: "modal-dialog modal-lg"
	}).appendTo(g_sectionAvatarEditor);

	g_modalAvatarEditor = $("<div>", {
		class: "modal-content"
	}).appendTo(dialog);
		
	removeAvatarEditor();	
}  

function generateAvatarEditor(par, user) {
	if (user == null) return null;
	
	var ret = $("<div>", {
		class: "avatar-editor-form clearfix"
	}).appendTo(par);

	var picContainer = $("<div>", {
		class: "avatar left"
	}).appendTo(ret);
	var picHelper = $("<span>", {
		class: "image-helper"
	}).appendTo(picContainer);
	var pic = $("<img>", {
		src: user.avatar
	}).appendTo(picContainer); 

	var uploadContainer = $("<div>", {
		class: "upload left"
	}).appendTo(ret);
	var btnChoose = $("<input>", {
		type: "file",
		text: TITLES["choose_picture"]
	}).appendTo(uploadContainer);
	//setDimensions(btnChoose, "250px", "95px");

	var btnUpload = $("<button>", {
		text: TITLES["upload"],	
		class: "purple"
	}).appendTo(uploadContainer);	
	//setDimensions(btnUpload, "250px", "95px");
	
	var hint = $("<p>").appendTo(uploadContainer);
	return new AvatarEditor(par, pic, btnChoose, btnUpload, hint); 
}

