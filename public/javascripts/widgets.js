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
function generateDatePicker(par, time, onEdit) {
    
	var ret = $('<div>', {
		class: 'col-sm-6'    
	}).appendTo(par);

	var formGroup = $('<div>', {
		class: 'form-group'    
	}).appendTo(ret);

	var inputGroup = $("<div>", {
		class: 'input-group date'    
	}).appendTo(formGroup);

	var input = $("<input>", {
		type: 'text',
		value: time,
		disabled: true,
		class: "form-control",
		style: "color: black; background-color: transparent; cursor: default; width: auto; border: none; outline: none; padding: 0; box-sizing: content-box;"    
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

	ret.on("input change keyup", onEdit);
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
		if (length <= 1) return;
		for(var key in pagerCache.map) {

			var index = parseInt(key);
			var indicator = $("<button>", {
				text: index,
				style: "display: inline; border: none; color: white; background-color: gray; font-size: 14pt; margin-left: 2px; margin-right: 2px;"
			}).appendTo(pager.bar);
			indicator.hover(
				function(evt){
					$(this).css("background-color", "cornflowerblue");
				}, 
				function(evt){
					$(this).css("background-color", "gray");
				}
			);
		
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
			indicator.css("background-color", "blue");
			indicator.css("font-weight", "bold");
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

function createDropdownMenu(par, id, menuTitle, icons, titles, reactions) {
	var length = titles.length;
	if (length != icons.length) return; 
	var container = $("<div class='dropdown'>").appendTo(par);
	// these params indicate that the container is centred
	container.css("position", "absolute");
	container.css("width", "90%")
	container.css("left", "5%")
	container.css("height", "60%");
	container.css("top", "20%")
	
	var toggle = $("<button id='" + id + "' class='btn btn-default dropdown-toggle' type='button' data-toggle='dropdown'>").appendTo(container);
	toggle.css("font-size", "1em");
	toggle.css("width", "100%")
	toggle.css("height", "100%");
	
	toggle.text(menuTitle);
	var sp = $("<span class='caret'>").appendTo(toggle);	
	var ul = $("<ul  aria-labelledby='" + id + "' class='dropdown-menu' role = 'menu'>").appendTo(container);
	var lis = [];
	for (var i = 0; i < titles.length; i++) {
		var li = $("<li>").appendTo(ul);
		var action = $("<a tabindex='-1' href='#'>").appendTo(li);
		action.css("font-size", "15pt");
		action.css("display", "block"); // increase the size of the link target, ref: http://css-tricks.com/keep-margins-out-of-link-lists/
		action.css("padding", "5px");
		action.css("text-align", "center");
		action.css("vertical-align", "middle");
		setBackgroundImage(action, icons[i], "contain", "no-repeat", "left center");
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
	if (isPreactive) pane = $("<div role ='tabpanel' class='tab-pane fade in active' id='" + ref + "' style='padding-left: 5pt; padding-right:5pt'>").appendTo(par);
	else pane = $("<div role='tabpanel' class='tab-pane' fade id='" + ref + "' style='padding-left: 5pt; padding-right:5pt'>").appendTo(par);
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
			placeHolder: "Captcha"
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

function WordCounter(current, min, max) {
	this.current = current;
	this.min = min;
	this.max = max;
	this.currentText = null;
	this.maxText = null;
	this.update = function(value) {
		this.current = value;
		this.currentText.text(value);
		if (this.min <= value && value <= this.max) this.currentText.css("color", "gray");
		else this.currentText.css("color", "red");
	};
	this.decrease = function(value) {
		this.update(this.current - value);
	};
	this.increase = function(value) {
		this.update(this.current + value);
	};
	this.appendCounter = function(par) {
		var row = $("<p>").appendTo(par);
		this.currentText = $("<span>", {
			class: "word-counter-current",
			text: this.current
		}).appendTo(row);	
		this.maxText = $("<span>", {
			class: "word-counter-max",
			text: "/" + this.max.toString()
		}).appendTo(row);
		this.update(this.current);
	};
	this.valid = function() {
		return (this.current <= this.max && this.current >= this.min);
	};
}

function AvatarEditor(image, btnChoose, btnUpload, hint) {
	this.image = image;
	this.btnChoose = btnChoose;
	this.btnUpload = btnUpload;
	this.hint = hint;
	this.getFile = function() {
		if (this.btnChoose == null) return null;	
		var files = this.btnChoose[0].files;
		if (files == null) return null;	
		if (files.length != 1) {
			alert("Choose only 1 image at a time!!!");
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
		
		$.ajax({
			method: "POST",
			url: "/user/avatar/upload", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				editor.hint.text("Uploaded");
			},
			error: function(xhr, status, err){
				editor.hint.text("Failed");
			}
		});

	});
}

function generateAvatarEditor(par) {
	if (g_loggedInUser == null) return null;
	
	var avatar = (!g_loggedInUser.hasAvatar()) ? "assets/icons/anonymous.png" : g_loggedInUser.avatar;
	var pic = $("<img>", {
		style: "position: absolute; left: 10px; top:10px; margin: auto;",
		src: avatar
	}).appendTo(par); 
	setDimensions(pic, "70px", "70px");

	var btnChoose = $("<input>", {
		type: "file",
		text: "Choose a picture",
		style: "position: absolute; left: 100px; top: 10px;"
	}).appendTo(par);
	setDimensions(btnChoose, "80px", "30px");

	var btnUpload = $("<button>", {
		text: "Upload",	
		style: "position: absolute; left: 100px; top: 50px;"
	}).appendTo(par);	
	setDimensions(btnUpload, "80px", "30px");
	
	var hint = $("<p>").appendTo(par);
	return new AvatarEditor(pic, btnChoose, btnUpload, hint); 
}
