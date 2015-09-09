/**
 * BaseWidget
 * */

function BaseWidget() {
	this.content = null;
		
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
var SLOT_UPLOADING = 1;
var SLOT_UPLOAD_FAILED = 2;
var SLOT_AJAX_PENDING = 3;

function ImageNode() {

	// TODO: only supports ProfileEditor and ActivityEditor at the moment
	this.state = SLOT_IDLE; 
	this.cdn = null;
	this.bucketDomain = null;
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
		var uptokenParams = [g_keyToken + '=' + token, g_keyRemoteName + '=' + this.remoteName]; 
		this.uptokenUrl = '/image/cdn/qiniu/uptoken?' + uptokenParams.join('&'); 
	};
}

ImageNode.inherits(BaseWidget);

/**
 * ActivityEditorImageNode
 * */

function ActivityEditorImageNode(cdn, domain) {
	this.btnDel = null;
	this.requestDel = function(onSuccess, onError) {
		// async process
		var token = $.cookie(g_keyToken);				
		if (!token) return;
		if (!this.remoteName) return;
		if (this.state != SLOT_IDLE) return;

		var params = {};
		params[g_keyToken] = token;
		var remoteNameList = [];
		remoteNameList.push(this.remoteName);
		params[g_keyBundle] = JSON.stringify(remoteNameList);

		this.state = SLOT_AJAX_PENDING;

		$.ajax({
			type: 'POST',
			url: '/image/cdn/qiniu/delete',
			data: params,
			success: function(data, status, xhr) {
			        this.state = SLOT_IDLE;
			        if (!onSuccess) return;
			        onSuccess(data);
			},
			error: function(xhr, status, err) {
			        this.state = SLOT_IDLE;
				if (!onError) return;
				onError(err);
			}
		});
	};

	this.composeContent = function(data) {
		this.setCDNCredentials(cdn, domain, $.cookie(g_keyToken), g_loggedInPlayer);	
		this.editor = data;
		this.editor.newImageNodes[this.remoteName] = this;
		this.wrap = $('<div>', {
			"class": "preview-container left"
		}).appendTo(this.content);

		this.preview = $('<img>').hide().appendTo(this.wrap);
		
		this.btnChoose = $('<button>', {
			text: TITLES.choose_picture,
			'class': 'positive-button'
		}).appendTo(this.wrap);
		setDimensions(this.btnChoose, "100%", "100%");

		this.btnDel = $('<button>', {
			text: TITLES.del,
			"class": "positive-button"
		}).hide().appendTo(this.wrap).click(this, function(evt){
			evt.preventDefault();
			var remoteName = evt.data.remoteName;
			var editor = evt.data.editor;

			if(!editor.newImageNodes.hasOwnProperty(remoteName)) return;
			var thatNode = editor.newImageNodes[remoteName];
			var aButton = getTarget(evt);
			var onSuccess = function(data) {
                                enableField(aButton);
				delete editor.newImageNodes[remoteName];
				thatNode.remove();
				editor.addNewImageNode(false, true);
			};
			var onError = function(err) {
                                enableField(aButton);
			};
			disableField(aButton);
			thatNode.requestDel(onSuccess, onError);
		});
		setDimensions(this.btnDel, "90%", "10%");

		if (cdn == g_cdnQiniu) {
			// reference http://developer.qiniu.com/docs/v6/sdk/javascript-sdk.html
			var node = this;
			this.uploader = Qiniu.uploader({
				runtimes: 'html5,flash,html4',		    
				browse_button: node.btnChoose[0],
				uptoken_url: node.uptokenUrl,
				unique_names: false,
				save_key: false,
				domain: node.bucketDomain,
				container: node.preview[0],
				max_file_size: '2mb',
				max_retries: 2,
				// dragdrop: true, 
				// drop_element: node.preview[0],
				chunk_size: '4mb',
				auto_start: true, 
				init: {
					'FilesAdded': function(up, files) {
						if (!files) return null;
						if (files.length != 1) {
							alert(ALERTS.choose_one_image);
							return;
						}

						var file = files[0];
						if (!validateImage(file)) return;

						node.state = SLOT_UPLOADING;

                                                node.uploader.disableBrowse();
						disableField(node.btnChoose);
					},
					'BeforeUpload': function(up, file) {
						node.state = SLOT_IDLE;
					},
					'UploadProgress': function(up, file) {
						// TODO: show progress
					},
					'FileUploaded': function(up, file, info) {
					},
					'Error': function(up, err, errTip) {
						node.state = SLOT_UPLOAD_FAILED; 
					},
					'UploadComplete': function() {
						if (node.state == SLOT_UPLOAD_FAILED) {
						        node.uploader.disableBrowse(false);
						        enableField(node.btnChoose);
						        return;
						}
						node.btnChoose.remove();
						var refreshParams = ["ts=" + currentMillis()];
						var protocolPrefix = "http://";
						var imageUrl = protocolPrefix + node.bucketDomain + "/" + node.remoteName + "?" + refreshParams.join('&');
						node.preview.show();
						node.preview.attr("src", imageUrl);
						node.state = SLOT_IDLE; 
						node.btnDel.show();

						node.editor.setNonSubmittable();
						node.editor.setSavable();
						
						node.editor.addNewImageNode(false);
					},
					 'Key': function(up, file) {
						// would ONLY be invoked when {unique_names: false , save_key: false}
						return node.remoteName;
					 }
				}
			});
		}	
	};
}

ActivityEditorImageNode.inherits(ImageNode);

/**
 * ProfileEditorImageNode
 * */

function ProfileEditorImageNode(cdn, domain) {
	this.composeContent = function(data) {
		this.setCDNCredentials(cdn, domain, $.cookie(g_keyToken), g_loggedInPlayer);	
		this.editor = data;
		this.wrap = $('<div>', {
			"class": "preview-container"
		}).appendTo(this.content);

		this.preview = $('<img>', {
			src: this.editor.player.avatar
		}).appendTo(this.wrap);
		
		this.btnChoose = $('<button>', {
			text: TITLES.choose_picture,
			"class": "positive-button"
		}).appendTo(this.wrap);
		setDimensions(this.btnChoose, "100%", null);

		if (cdn == g_cdnQiniu) {
			// reference http://developer.qiniu.com/docs/v6/sdk/javascript-sdk.html
			var node = this;
			this.uploader = Qiniu.uploader({
				runtimes: 'html5,flash,html4',		    
				browse_button: node.btnChoose[0],
				uptoken_url: node.uptokenUrl,
				unique_names: false,
				save_key: false,
				domain: node.bucketDomain,
				container: node.preview[0],
				max_file_size: '2mb',
				max_retries: 2,
				// dragdrop: false, 
				// drop_element: node.preview[0],
				chunk_size: '4mb',
				auto_start: true, 
				init: {
					'FilesAdded': function(up, files) {
						if (!files) return null;
						if (files.length != 1) {
							alert(ALERTS.choose_one_image);
							return;
						}

						var file = files[0];
						if (!validateImage(file)) return;

						node.state = SLOT_UPLOADING; 
						disableField(node.btnChoose);
					},
					'BeforeUpload': function(up, file) {
						node.state = SLOT_IDLE;
					},
					'UploadProgress': function(up, file) {
						// TODO: show progress
					},
					'FileUploaded': function(up, file, info) {
					},
					'Error': function(up, err, errTip) {
						node.state = SLOT_UPLOAD_FAILED; 
					},
					'UploadComplete': function() {
						enableField(node.btnChoose);
						if (node.state == SLOT_UPLOAD_FAILED) return;
						var protocolPrefix = "http://";
						var imageUrl = protocolPrefix + node.bucketDomain + "/" + node.remoteName;
						node.preview.attr("src", imageUrl);
						node.state = SLOT_IDLE; 
					},
					 'Key': function(up, file) {
						// would ONLY be invoked when {unique_names: false , save_key: false}
						return node.remoteName;
					 }
				}
			});
		}	
	};
}
 
ProfileEditorImageNode.inherits(ImageNode);

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
	this.remove = function() {
		if (!this.button) return;
		this.button.remove();
		this.button = null;
	};
}

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
	};
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
	if (!this.bar) return;
	this.refreshBar = function() {
		var pager = this;
		var page = pager.page;
		var pagerCache = pager.cache;
		// display pager bar 
		pager.bar.empty();
		var length = Object.keys(pagerCache.map).length;
		if (length <= 1) return;
		var indicatorOnClick = function(evt) {
			if (!pager.url) return;
			var pagerButton = evt.data;
			pager.page = pagerButton.page;
			var params = pager.paramsGenerator(pager, pagerButton.page);
			if (!params) return;
			var indicator = getTarget(evt);
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

	this.squeeze = function() {
		// encapsulated for convenience
		setDimensions(this.screen.parent(), "0px", null);
		this.screen.parent().hide();
	};

	this.expand = function(width) {
		// encapsulated for convenience
		if (!width) width = "100%";
		setDimensions(this.screen.parent(), width, null);
		this.screen.parent().show();
	};
		
	this.remove = function() {
		if (!this.screen) return;
		this.screen.remove();
	};
	
	// multi-level filters
	this.filters = filters;
	
	if (!filters) return;
	var pager = this;
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
	for (var i = 0; i < filters.length; ++i) {
		var filter = filters[i];
		filter.selector.change(pager, selectorOnChange);	
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
			placeHolder: HINTS.captcha
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
	};
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
