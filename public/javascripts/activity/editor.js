/* 
 * variables
 */

var g_activityEditor = null;
var g_deleteConfirmation = null;

// general DOM elements
var g_modalActivityEditor = null;
var g_sectionActivityEditor = null;

// button keys
var g_classBtnSubmit = "btn-submit";
var g_classBtnDelete = "btn-delete";
var g_classBtnSave = "btn-save";
var g_classBtnCancel = "btn-cancel";

// DOM indexes for cascaded DOM element search
var g_indexOldImage = "old_image";
var g_indexNewImage = "new_image";

var g_wImageCell = 200;
var g_hImageCell = 200;
var g_hDelete = 30;

// general variables
var g_imagesLimit = 3;

// callback functions
var g_onEditorRemoved = null;
var g_onEditorCancelled = null;

var g_onActivitySaveSuccess = null;

var g_loadingWidget = null;

// Existing images selector
function ImageSelector(id, image, indicator) {
	this.id = id;
	this.image = image;
	this.indicator = indicator;
	this.checked = true;
}

function AddressField(input, map) {
	this.input = input;
	this.map = map;
}

function ActivityEditor(container, id, titleField, addressField, contentField, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete, explorerTrigger, hint, captcha) {
	this.container = container;
	if (id != null) this.id = id;
	this.titleField = titleField;
	this.addressField = addressField;
	this.contentField = contentField;
	this.newImageFiles = newImageFiles;
	this.newImageNodes = newImageNodes;
	this.imageSelectors = imageSelectors;
	this.beginTimePicker = beginTimePicker;
	this.deadlinePicker = deadlinePicker;
	this.btnSave = btnSave;
	this.btnSubmit = btnSubmit;
	if (btnDelete != null) this.btnDelete = btnDelete; 
	this.explorerTrigger = explorerTrigger;
	this.hint = hint;
	this.captcha = captcha;
	this.savable = false;
	this.submittable = true;

	this.disableEditorButtons = function() {
		disableField(this.btnSave);
		this.btnSave.css("color", "dimgray");
		disableField(this.btnSubmit);
		this.btnSubmit.css("color", "dimgray");
		if (this.btnDelete == null) return;
		disableField(this.btnDelete);
		this.btnDelete.css("color", "dimgray");
	};

	this.enableEditorButtons = function() {
		if (this.savable) {
			enableField(this.btnSave);
			this.btnSave.css("color", "white");
		}	
		if (this.submittable) {
			enableField(this.btnSubmit);
			this.btnSubmit.css("color", "white");
		}	
		if (this.btnDelete != null) {
			enableField(this.btnDelete);
			this.btnDelete.css("color", "white");
		}
	};

	this.setNonSavable = function() {
		this.savable = false;
		disableField(this.btnSave);
		this.btnSave.css("color", "dimgray");
	};

	this.setSavable = function() {
		this.savable = true;
		this.enableEditorButtons();
	};

	this.setNonSubmittable = function() {
		this.submittable = false;
		disableField(this.btnSubmit);
		this.btnSubmit.css("color", "dimgray");
	};

	this.setSubmittable = function() {
		this.submittable = true;
		this.enableEditorButtons();
	};
	
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

function DeleteConfirmation(container, activity, btnYes, btnNo) {
	this.container = container;
	this.activity = activity;
	this.btnYes = btnYes;
	this.btnNo = btnNo;	
	this.btnYes.click(this.activity.id, onDelete);
	this.btnNo.click(this.container, function(evt) {
		evt.preventDefault();
		var aContainer = evt.data;
		aContainer.remove();
	});
	this.remove = function() {
		this.container.remove();	
	};
}

// Assistive functions
function formatDigits(value, numberOfDigits){
       var valueStr = value.toString();
       while(valueStr.length < numberOfDigits) valueStr = "0" + valueStr;
       return valueStr;
}

function formatDate(time){
	time = time.replace(/-/g,"/");
	var date = new Date(Date.parse(time));
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	var hour = date.getHours();
	var min = date.getMinutes();
	return year + "-" + formatDigits(month, 2) + "-" + formatDigits(day, 2) + " " + formatDigits(hour, 2) + ":" + formatDigits(min, 2);	
}

function reformatDate(date){
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	var hour = date.getHours();
	var min = date.getMinutes();
	return year + "-" + formatDigits(month, 2) + "-" + formatDigits(day, 2) + " " + formatDigits(hour, 2) + ":" + formatDigits(min, 2);	
}

function removeActivityEditor(){
	if(g_sectionActivityEditor == null) return;
	g_sectionActivityEditor.hide();
	g_sectionActivityEditor.modal("hide");
	if(g_modalActivityEditor == null) return;
	g_modalActivityEditor.empty();
	if (g_activityEditor == null) return;
	g_activityEditor.remove();
}

function initActivityEditor(par, onRemove){
	g_onEditorRemoved = onRemove;
	/*
		Note: ALL attributes, especially the `class` attribute MUST be written INSIDE the div tag, bootstrap is NOT totally compatible with jQuery!!!
		backdrop being 'static' prevents modal from closing on losing focus
		keyboard being 'false' prevents modal from closing on pressing `esc`
	*/
	g_sectionActivityEditor = $("<div class='modal fade activity-editor' data-keyboard='false' data-backdrop='static' tabindex='-1' role='dialog' aria-labelledby='Create an activity!' aria-hidden='true'>").appendTo(par);
	var dialog = $("<div>", {
		class: "modal-dialog modal-lg"
	}).appendTo(g_sectionActivityEditor);
	g_modalActivityEditor = $("<div>", {
		class: "modal-content"
	}).appendTo(dialog);

	g_onEditorCancelled = function(){
		g_sectionActivityEditor.modal("hide");
	};

	removeActivityEditor();
}

function showActivityEditor(activity) {
	refreshActivityEditor(activity);
	g_sectionActivityEditor.modal({
			show: true
	});
}

function refreshActivityEditor(activity) {
	g_modalActivityEditor.empty();
	g_activityEditor = generateActivityEditor(g_modalActivityEditor, activity);
}

function countImages(editor){
	var ret = Object.keys(editor.newImageFiles).length;
	for(var key in editor.imageSelectors) {
		var selector = editor.imageSelectors[key];
		if(!selector.checked) continue;
		++ret;
	}
	return ret;
}

function onSave(evt){
	evt.preventDefault();
	
	if (g_activityEditor == null || !g_activityEditor.savable) return;
	var formData = new FormData();

	// append user token and activity id for identity
	var token = $.cookie(g_keyToken.toString());
	if (token == null) {
		alert("Are you logged out?");
		return;
	}
	formData.append(g_keyToken, token);

	var countImages = 0;
	
	// check files
	for (var key in g_activityEditor.newImageFiles) {
		var file = g_activityEditor.newImageFiles[key];
		if (!validateImage(file)) {
			formData = null;
			return;
		} 
		formData.append(g_indexNewImage + "-" + key.toString(), file);
	}
	
	countImages = Object.keys(g_activityEditor.newImageFiles).length;

	var selectedOldImages = new Array();
	for(var i = 0; i < g_activityEditor.imageSelectors.length; i++){
		var selector = g_activityEditor.imageSelectors[i];
		if(!selector.checked) continue;
		selectedOldImages.push(selector.id);
		++countImages;
	}
	formData.append(g_indexOldImage, JSON.stringify(selectedOldImages));
	
	if(countImages > g_imagesLimit) {
		alert("No more than 3 images! Don't cheat!");
		return;
	}

	// append activity title and content
	var title = g_activityEditor.titleField.val();
	var address = g_activityEditor.addressField.input.val();
	var content = g_activityEditor.contentField.val();
	if(title == null || address == null || content == null || title == "" || address == "" || content == "") {
		alert("All text fields (title, address and content) should be filled.");
		return;
	}
	formData.append(g_keyTitle, title);
	formData.append(g_keyAddress, address);
	formData.append(g_keyContent, content);

	// append activity begin time and deadline
	var beginTime = localYmdhisToGmtMillisec(getDateTime(g_activityEditor.beginTimePicker));
	var deadline = localYmdhisToGmtMillisec(getDateTime(g_activityEditor.deadlinePicker));
		
	if(deadline > beginTime) {
		alert("Deadline can not be after the begin time of an activity.");
		return;
	}

	formData.append(g_keyBeginTime, beginTime);
	formData.append(g_keyDeadline, deadline);

	var isNewActivity = false;
	var activityId = g_activityEditor.id; 
	if(activityId == null) isNewActivity = true;

	if(!isNewActivity)	formData.append(g_keyActivityId, activityId);
	else {
		formData.append(g_keySid, g_activityEditor.captcha.sid);
		formData.append(g_keyCaptcha, g_activityEditor.captcha.input.val());
	}

	g_activityEditor.disableEditorButtons();
	g_activityEditor.setNonSavable();
	g_activityEditor.setNonSubmittable();
	g_activityEditor.hint.text("Saving...");

	$.ajax({
			method: "POST",
			url: "/activity/save",
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false, // tell jQuery not to set contentType
			processData: false, // tell jQuery not to process the data
			success: function(data, status, xhr){
				var activityJson = JSON.parse(data);
				g_activityEditor.setSubmittable();
				g_activityEditor.enableEditorButtons();
				var activity = new Activity(activityJson);
				if (g_activityEditor.id == null) {
					g_activityEditor.hint.text("Activity created.");
				} else {
					g_activityEditor.hint.text("Changes saved.");
				}
				g_activityEditor.id = activity.id;
				refreshActivityEditor(activity);	
				if(g_onActivitySaveSuccess == null) return;
				g_onActivitySaveSuccess();
			},
			error: function(xhr, status, err){
				g_activityEditor.setSavable();
				g_activityEditor.enableEditorButtons();
				g_activityEditor.hint.text("Activity not saved!");
			}
	});
}

function onSubmit(evt){

	evt.preventDefault();

	if (g_activityEditor == null || !g_activityEditor.submittable) return;

        var data = evt.data;

        var params = {};

        // append user token and activity id for identity
        var token = $.cookie(g_keyToken);
	if(token == null) {
		alert("Are you logged out?");
		return;
	}
        params[g_keyToken] = token;
	
	var activityId = g_activityEditor.id; 
        params[g_keyActivityId] = activityId;

	g_activityEditor.disableEditorButtons();
        g_activityEditor.setNonSavable();
        g_activityEditor.setNonSubmittable();

        $.ajax({
                method: "POST",
                url: "/activity/submit",
                data: params,
                success: function(data, status, xhr){
			g_activityEditor.enableEditorButtons();
                        removeActivityEditor();
                        if(g_onEditorRemoved == null) return;
			g_onEditorRemoved();
                },
                error: function(xhr, status, err){
			g_activityEditor.enableEditorButtons();
                        g_activityEditor.setSubmittable();
                }
        });
}

function previewImage(par, editor) {
        var newImage = editor.explorerTrigger.getFile();
	if (!validateImage(newImage)) return;

        var reader = new FileReader();

        reader.onload = function (e) {
		var key = new Date().getTime(); // the key which identifies an image in the map newImages

		var offset = Object.keys(editor.newImageFiles).length;
		editor.newImageFiles[key] = newImage; // add new image to file map
		var node = $('<span>', {
			style: "position: absolute; padding: 2pt"
		}).appendTo(par);
		setDimensions(node, g_wImageCell, g_hImageCell + g_hDelete);
		setOffset(node, offset * g_wImageCell, null);
		editor.newImageNodes[key] = node; // add new image node to view map		

		var img = $('<img>', {
			src: e.target.result
		}).appendTo(node);
		setDimensions(img, g_wImageCell, g_hImageCell);

		var btnDelete = $("<button>", {
			text: "delete",
			style: "font-size: 12pt; color: white; background-color: red;"
		}).appendTo(node);
		setDimensions(btnDelete, g_wImageCell, g_hDelete);
		
		editor.explorerTrigger.shift(+1, g_wImageCell);
		
		btnDelete.click(function(evt){
			evt.preventDefault();
			editor.setSavable();
			editor.setNonSubmittable();
			if(!editor.newImageFiles.hasOwnProperty(key)) return;
			delete editor.newImageFiles[key];	
			if(!editor.newImageNodes.hasOwnProperty(key)) return;
			editor.newImageNodes[key].remove();
			delete editor.newImageNodes[key];
			for(var otherKey in editor.newImageNodes) {
				if(otherKey < key) continue;
				var newImageNode = editor.newImageNodes[otherKey];	
				var offset = getOffset(newImageNode);
				setOffset(newImageNode, offset.left - g_wImageCell, null);
			}
			editor.explorerTrigger.shift(-1, g_wImageCell);
		});
        }

        reader.readAsDataURL(newImage);
}

function onDelete(evt){

	evt.preventDefault();
	var activityId = evt.data;
	var token = $.cookie(g_keyToken);

	var params={};
	params[g_keyActivityId] = activityId;
	params[g_keyToken] = token;

	$.ajax({
		type: "POST",
		url: "/activity/delete",
		data: params,
		success: function(data, status, xhr){
			removeActivityEditor();
			if(g_onEditorRemoved == null)   return;
			g_onEditorRemoved();
		},
		error: function(xhr, status, err){

		}
	});

}

function onCancel(evt){

	evt.preventDefault();
	removeActivityEditor();
	if(g_onEditorCancelled == null) return;
	g_onEditorCancelled();

}

// Generators
function generateActivityEditor(par, activity){
	
	var isNewActivity = false;
	if(activity == null || activity.id == null) isNewActivity = true;

	var activityId = null;
	var activityTitle = "";
	var activityAddress = "";
	var activityContent = "";

	if(!isNewActivity) {
		activityId = activity.id;
		activityTitle = activity.title;
		activityAddress = activity.address;
		activityContent = activity.content;
	}

	var ret = $('<form>', {
		style: "padding: 5pt"
	}).appendTo(par);

	var titleInput = $('<input>', {
		placeholder: "Title",
		class: "input-title",
		type: 'text',
		value: activityTitle
	}).appendTo(ret);
	var titleCounter = new WordCounter(0, 1, 64);
	titleCounter.appendCounter(ret);

	titleInput.on("input paste keyup", function(evt){
			evt.preventDefault();
			g_activityEditor.setSavable();
			g_activityEditor.setNonSubmittable();
			var count = $(this).val().length;
			titleCounter.update(count);
	});

	var addressInput = $("<input>", {
		placeholder: "Where",
		class: "input-address",
		type: "text",
		value: activityAddress
	}).appendTo(ret);
	var addrCounter = new WordCounter(0, 1, 256);
	addrCounter.appendCounter(ret);
	addressInput.on("input paste keyup", function(evt){
			evt.preventDefault();
			g_activityEditor.setSavable();
			g_activityEditor.setNonSubmittable();
			var count = $(this).val().length;
			addrCounter.update(count);
	});

	var addressField = new AddressField(addressInput, null);

	var contentInput = $("<textarea>",	{
		placeholder: "What to do",
		class: "input-content" 
	}).appendTo(ret);
	contentInput.val(activityContent);
	var contentCounter = new WordCounter(0, 1, 1024);
	contentCounter.appendCounter(ret);

	contentInput.on("input paste keyup", function(evt){
			evt.preventDefault();
			g_activityEditor.setSavable();
			g_activityEditor.setNonSubmittable();
			var count = $(this).val().length;
			contentCounter.update(count);
	});

	$("<p>", {
		style: "font-size: 14pt; color: red; padding: 5pt",
		html: "Up to 3 images can be saved for an activity. Single image size is limited to 2MB(2048KB), please go to <a href='http://www.pixlr.com'>Pixlr</a> to compress your image if necessary"
	}).appendTo(ret);

	var newImageFiles = {};
	var newImageNodes = {};
	var imageSelectors = new Array();

	var newImagesRow = $("<p>", {
		style: "position: relative; overflow-x: auto; overflow-y: hidden"
	});

	setDimensions(newImagesRow, null, g_hImageCell + g_hDelete + 5);

	if(activity != null && activity.images != null) {
		var oldImagesRow = $("<p>", {
			style: "position: relative; overflow-x: auto;"	
		}).appendTo(ret);
		setDimensions(oldImagesRow, null, g_hImageCell + 5);

		var countOldImages = Object.keys(activity.images).length;
		for(var i =0; i < countOldImages; i++){
			var node = $("<span>").appendTo(oldImagesRow);
			setDimensions(node, g_wImageCell, g_hImageCell);

			var image = $('<img>', {
				src: activity.images[i].url,
				style: "position: absolute; padding: 2pt"
			}).appendTo(node);
			setDimensions(image, g_wImageCell, g_hImageCell);
			setOffset(image, i * g_wImageCell + 10, null);

			var indicator = $('<img>',{
				src: "/assets/icons/checked.png",
				style: "position: absolute;"
			}).appendTo(node);
			setDimensions(indicator, 0.2 * g_wImageCell, 0.2 * g_hImageCell);
			setOffset(indicator, i * g_wImageCell + 10, null);

			var selector = new ImageSelector(activity.images[i].id, image, indicator);
			node.click(selector, function(evt) {
				evt.preventDefault();
				var selector = evt.data;
				var val = !(selector.checked);
				if (val && countImages(g_activityEditor) >= g_imagesLimit) return;
				selector.checked = val;
				if (val) selector.indicator.show();
				else selector.indicator.hide();
				g_activityEditor.setSavable();
				g_activityEditorsetNonSubmittable();
			});
			imageSelectors.push(selector);
		}
	}

	newImagesRow.appendTo(ret);

        var onChange = function(evt) {
                evt.preventDefault();
		if (g_activityEditor == null) return;
		if (countImages(g_activityEditor) >= g_imagesLimit) return;
                g_activityEditor.setSavable();
                g_activityEditor.setNonSubmittable();
                previewImage(newImagesRow, g_activityEditor);
        };

	var explorerTrigger = generateExplorerTriggerSpan(newImagesRow, onChange, "/assets/icons/add.png", g_wImageCell, g_hImageCell, g_wImageCell/2, g_hImageCell/2);

	$('<br>').appendTo(ret);
	
	// Schedules
	var deadline = reformatDate(new Date());
	if(activity != null && activity.applicationDeadline != null) deadline = activity.applicationDeadline;

	var beginTime = reformatDate(new Date());
	if(activity != null && activity.beginTime != null) beginTime = activity.beginTime;

	var tableSchedule = $("<table>", {
		style: "position: relative; margin-top: 15pt; margin-bottom: 5pt"
	}).appendTo(ret);
	var scheduleRow1 = $("<tr>").appendTo(tableSchedule);
	var scheduleCell11 = $("<td>", {
		text: "Deadline ",
		style: "font-size: 14pt; font-variant: small-caps; font-family: verdana, sans-serif; white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow1);
	var scheduleCell12 = $("<td>").appendTo(scheduleRow1);
	var deadlinePicker = generateDateSelection(scheduleCell12, gmtMiilisecToLocalYmdhi(deadline));

	var scheduleRow2 = $("<tr>").appendTo(tableSchedule);
	var scheduleCell21 = $("<td>", {
		text: "Begin Time ",
		style: "font-size: 14pt; font-variant: small-caps; font-family: verdana, sans-serif; white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow2);
	var scheduleCell22 = $("<td>").appendTo(scheduleRow2);
	var beginTimePicker = generateDateSelection(scheduleCell22, gmtMiilisecToLocalYmdhi(beginTime));

	var sid = null;
	var captcha = null;
	if (isNewActivity) {
		sid = generateUuid();  
		var captcha = new Captcha(sid);
		captcha.appendCaptcha(ret);
	}

	var buttons = $("<p>", {
		style: "position: relative; margin-top: 5pt; width: 100%; height: auto;"
	}).appendTo(ret);

	/* Associated Buttons */
	var btnSave = $('<button>',{
		class: g_classBtnSave,
		text: 'Save'
	}).appendTo(buttons);
	btnSave.click(onSave);

	var btnSubmit = $('<button>',{
		class: g_classBtnSubmit,
		text: 'Submit'
	}).appendTo(buttons);
	var dSubmit = {};
	dSubmit[g_keyActivityId] = activityId;
	btnSubmit.click(dSubmit, onSubmit);

	var btnCancel = $('<button>',{
		class: g_classBtnCancel,
		text: 'Cancel'
	}).appendTo(buttons);
	btnCancel.click(onCancel);

	var btnDelete = null;
	if(!isNewActivity){
		btnDelete = $('<button>',{
			class: g_classBtnDelete,
			text: 'Delete'
		}).appendTo(buttons);
		var dDelete = {};
		btnDelete.click(function(evt) {
			evt.preventDefault();
			if (g_deleteConfirmation != null) g_deleteConfirmation.remove();
			g_deleteConfirmation = generateDeleteConfirmation(g_modalActivityEditor, activity);
		});
	}

	var hint = $("<p>", {
		style: "color: blue"
	}).appendTo(ret);

	var editor = new ActivityEditor(ret, activityId, titleInput, addressField, contentInput, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete, explorerTrigger, hint, captcha);	
	editor.setNonSavable();
	editor.setSubmittable();

	return editor;
}

function generateDateSelection(par, time){
	return generateDatePicker(par, time, function(evt){
			evt.preventDefault();
			g_activityEditor.setSavable();
			g_activityEditor.setNonSubmittable();
	});
}

function generateDeleteConfirmation(par, activity) {
	if (g_activityEditor == null) return;

	var ret = $("<div>", {
		style: "padding: 5pt;"
	}).appendTo(par);
	
	var title = $("<p>", {
		text: "Are you sure to delete this activity?",
		style: "font-size: 18pt;"
	}).appendTo(ret);

	var buttons = $("<p>").appendTo(ret);
	var btnYes = $("<button>", {
		text: "YES",
		style: "font-size: 16pt; background-color: cornflowerblue; color: white; margin-right: 10pt;"
	}).appendTo(buttons);
	var btnNo = $("<button>", {
		text: "NO",
		style: "font-size: 16pt; background-color: crimson; color: white; margin-left: 10pt;"
	}).appendTo(buttons);
	
	return new DeleteConfirmation(ret, activity, btnYes, btnNo);
}
