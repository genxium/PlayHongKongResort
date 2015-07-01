/* 
 * variables
 */

var g_deleteConfirmation = null;
var g_activityEditor = null;

// DOM indexes for cascaded DOM element search
var g_indexOldImage = "old_image";
var g_indexNewImage = "new_image";

var g_wImageCell = 200;
var g_hImageCell = 200;
var g_hDelete = 30;

// general variables
var g_imagesLimit = 3;

// callback functions
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

function ActivityEditor() {
	this.id = null;
	this.container = null;
	this.dialog = null;
	this.titleField = null;
	this.titleCounter = null;
	this.addressField = null;
	this.addressCounter = null;
	this.contentField = null;
	this.contentCounter = null;
	this.newImageFiles = null;
	this.newImageNodes = null;
	this.imageSelectors = null;
	this.beginTimePicker = null;
	this.deadlinePicker = null;
	this.btnSave = null;
	this.btnSubmit = null;
	this.btnDelete = null;
	this.explorerTrigger = null;
	this.hint = null;
	this.captcha = null;
	this.refresh = function(activity) {
		this.id = null;
		var isNewActivity = false;
		if(activity == null || activity.id == null) isNewActivity = true;

		var activityId = null;
		var activityTitle = "";
		var activityAddress = "";
		var activityContent = "";

		if(!isNewActivity) {
			this.id = activity.id;
			activityId = activity.id;
			activityTitle = activity.title;
			activityAddress = activity.address;
			activityContent = activity.content;
		}

		this.content.empty();
		var form = $('<form>', {
			"class": "activity-editor-form"
		}).appendTo(this.content);

		this.titleField = $('<input>', {
			placeholder: HINTS["activity_title"],
			"class": "input-title",
			type: 'text',
			value: activityTitle
		}).appendTo(form);
		this.titleCounter = new WordCounter(activityTitle, 1, 64, g_activityTitlePattern, "");
		this.titleCounter.appendCounter(form);

		this.titleField.on("input paste keyup", this.titleCounter, function(evt){
				g_activityEditor.setSavable();
				g_activityEditor.setNonSubmittable();
				evt.data.update($(this).val());
		});

		var addressInput = $("<input>", {
			placeholder: HINTS["activity_address"],
			"class": "input-address",
			type: "text",
			value: activityAddress
		}).appendTo(form);
		this.addressCounter = new WordCounter(activityAddress, 1, 256, g_activityAddressPattern, "");
		this.addressCounter.appendCounter(form);

		addressInput.on("input paste keyup", this.addressCounter, function(evt){
				g_activityEditor.setSavable();
				g_activityEditor.setNonSubmittable();
				evt.data.update($(this).val());
		});

		this.addressField = new AddressField(addressInput, null);

		this.contentField = $("<textarea>",	{
			placeholder: HINTS["activity_content"],
			"class": "input-content" 
		}).appendTo(form);
		this.contentField.val(activityContent);
		this.contentCounter = new WordCounter(activityContent, 1, 1024, g_activityContentPattern, "");
		this.contentCounter.appendCounter(form);
		this.contentField.on("input paste keyup", this.contentCounter, function(evt){
				g_activityEditor.setSavable();
				g_activityEditor.setNonSubmittable();
				evt.data.update($(this).val());
		});

		$("<div>", {
			"class": "warning",
			html: MESSAGES["image_selection_requirement"]
		}).appendTo(form);

		this.newImageFiles = {};
		this.newImageNodes = {};
		this.imageSelectors = new Array();

		var newImagesRow = $("<div>", {
			"class": "image-row new clearfix patch-block-epsilon"
		});

		if(activity != null && activity.images != null)	generateOldImagesRow(form, this, activity);

		newImagesRow.appendTo(form);

		var onChange = function(evt) {
			evt.preventDefault();
					if (g_activityEditor == null) return;
					if (countImages(g_activityEditor) >= g_imagesLimit) {
						alert(ALERTS["image_selection_limit_exceeded"].format(g_imagesLimit));	
						return;
					}
			g_activityEditor.setSavable();
			g_activityEditor.setNonSubmittable();
			previewImage(newImagesRow, g_activityEditor);
		};

		this.explorerTrigger = generateExplorerTriggerSpan(newImagesRow, onChange, "/assets/icons/add.png", g_wImageCell, g_hImageCell, g_wImageCell*2/3, g_hImageCell*2/3);

		// Schedules
		var deadline = reformatDate(new Date());
		if(activity != null && activity.applicationDeadline != null) deadline = activity.applicationDeadline;

		var beginTime = reformatDate(new Date());
		if(activity != null && activity.beginTime != null) beginTime = activity.beginTime;

		var scheduleRow1 = $("<div>", {
			"class": "edit-deadline clearfix"
		}).appendTo(form);
		var scheduleCell11 = $("<div>", {
			text: TITLES["deadline"],
			"class": "left edit-label"
		}).appendTo(scheduleRow1);
		var scheduleCell12 = $("<div>", {
			"class": "datetime-picker left"
		}).appendTo(scheduleRow1);
		this.deadlinePicker = generateDateSelection(scheduleCell12, gmtMiilisecToLocalYmdhi(deadline));
		
		var scheduleRow2 = $("<div>", {
			"class": "edit-begin clearfix"
		}).appendTo(form);
		var scheduleCell21 = $("<div>", {
			text: TITLES["begin_time"],
			"class": "left edit-label"
		}).appendTo(scheduleRow2);
		var scheduleCell22 = $("<div>", {
			"class": "datetime-picker left"
		}).appendTo(scheduleRow2);
		this.beginTimePicker = generateDateSelection(scheduleCell22, gmtMiilisecToLocalYmdhi(beginTime));

		var sid = null;
		this.captcha = null;
		if (isNewActivity) {
			sid = generateUuid();  
			this.captcha = new Captcha(sid);
			this.captcha.appendTo(form);
		}
		
		var buttons = $("<div>", {
			"class": "edit-button-rows"
		}).appendTo(form);

		/* Associated Buttons */
		this.btnSave = $('<button>',{
			"class": "btn-save positive-button",
			text: TITLES["save"]
		}).appendTo(buttons);
		this.btnSave.click(onSave);

		this.btnSubmit = $('<button>',{
			"class": "btn-submit positive-button",
			text: TITLES["submit"]
		}).appendTo(buttons);
		var dSubmit = {};
		dSubmit[g_keyActivityId] = activityId;
		this.btnSubmit.click(dSubmit, onSubmit);

		this.btnCancel = $('<button>',{
			"class": "btn-cancel negative-button",
			text: TITLES["cancel"]
		}).appendTo(buttons);
		this.btnCancel.click(onCancel);

		this.btnDelete = null;
		if(!isNewActivity){
			this.btnDelete = $('<button>',{
				"class": "btn-delete caution-button",
				text: TITLES["delete"]
			}).appendTo(buttons);
			this.btnDelete.click(function(evt) {
				evt.preventDefault();
				if (g_deleteConfirmation != null) g_deleteConfirmation.remove();
				g_deleteConfirmation = generateDeleteConfirmation(g_activityEditor.content, activity);
			});
		}

		this.hint = $("<div>", {
			"class": "hint"
		}).appendTo(form);

		this.setNonSavable();
		this.setSubmittable();

	};
	this.appendTo = function(par) {
		// DOM elements
		this.container = $("<div class='modal fade activity-editor' data-keyboard='false' data-backdrop='static' tabindex='-1' role='dialog' aria-labelledby='create' aria-hidden='true'>").appendTo(par);
		this.dialog = $("<div class='modal-dialog modal-lg'>").appendTo(this.container);
		this.content= $("<div class='modal-content'>").appendTo(this.dialog);
	};
	
	this.savable = false;
	this.submittable = true;

	this.disableEditorButtons = function() {
		disableField(this.btnSave);
		this.btnSave.addClass("disabled-button");
		disableField(this.btnSubmit);
		this.btnSubmit.addClass("disabled-button");
		if (this.btnDelete == null) return;
		disableField(this.btnDelete);
		this.btnDelete.addClass("disabled-button");
	};

	this.enableEditorButtons = function() {
		if (this.savable) {
			enableField(this.btnSave);
			this.btnSave.removeClass("disabled-button");
		}	
		if (this.submittable) {
			enableField(this.btnSubmit);
			this.btnSubmit.removeClass("disabled-button");
		}	
		if (this.btnDelete != null) {
			enableField(this.btnDelete);
			this.btnDelete.removeClass("disabled-button");
		}
	};

	this.setNonSavable = function() {
		this.savable = false;
		disableField(this.btnSave);
		this.btnSave.addClass("disabled-button");
	};

	this.setSavable = function() {
		this.savable = true;
		this.enableEditorButtons();
	};

	this.setNonSubmittable = function() {
		this.submittable = false;
		disableField(this.btnSubmit);
		this.btnSubmit.addClass("disabled-button");
	};

	this.setSubmittable = function() {
		this.submittable = true;
		this.enableEditorButtons();
	};
	
	this.show = function() {
		this.container.modal("show");
	};

	this.hide = function() {
		this.container.modal("hide");
	};

	this.remove = function() {
		this.container.remove();
	};	
}

function initActivityEditor(par) {
	g_activityEditor = new ActivityEditor();
	g_activityEditor.appendTo(par);	
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
		alert(ALERTS["please_log_in"]);
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
		alert(ALERTS["image_selection_requirement"]);
		return;
	}

	// append activity title and content
	var title = g_activityEditor.titleField.val();
	var address = g_activityEditor.addressField.input.val();
	var content = g_activityEditor.contentField.val();

	var titleCounter = g_activityEditor.titleCounter;
	var addressCounter = g_activityEditor.addressCounter;
	var contentCounter = g_activityEditor.contentCounter;

	if(!titleCounter.valid() || !addressCounter.valid() || !contentCounter.valid()) {
		alert(ALERTS["please_follow_activity_field_instructions"]);
		return;
	}
	formData.append(g_keyTitle, title);
	formData.append(g_keyAddress, address);
	formData.append(g_keyContent, content);

	// append activity begin time and deadline
	var beginTime = localYmdhisToGmtMillisec(g_activityEditor.beginTimePicker.getDatetime());
	var deadline = localYmdhisToGmtMillisec(g_activityEditor.deadlinePicker.getDatetime());
		
	if(deadline < 0 || beginTime < 0 || deadline > beginTime) {
		alert(ALERTS["deadline_behind_begin_time"]);
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
	g_activityEditor.hint.text(MESSAGES["activity_saving"]);

	$.ajax({
			method: "POST",
			url: "/activity/save",
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false, // tell jQuery not to set contentType
			processData: false, // tell jQuery not to process the data
			success: function(data, status, xhr){
				var activityJson = JSON.parse(data);
				if (isTokenExpired(activityJson)) {
					logout(null);
					return;
				}
				if (isCaptchaNotMatched(activityJson)) {
					alert(ALERTS["captcha_not_matched"]);
					g_activityEditor.setSavable();
					g_activityEditor.enableEditorButtons();
					g_activityEditor.hint.text(MESSAGES["activity_not_saved"]);
					return;
				}
				if (isCreationLimitExceeded(activityJson)) {
					alert(ALERTS["creation_limit_exceeded"]);
					g_activityEditor.setSavable();
					g_activityEditor.enableEditorButtons();
					g_activityEditor.hint.text(MESSAGES["activity_not_saved"]);
					return;
				}
				g_activityEditor.setSubmittable();
				g_activityEditor.enableEditorButtons();
				var activity = new Activity(activityJson);
				if (g_activityEditor.id == null) {
					g_activityEditor.hint.text(MESSAGES["activity_created"]);
				} else {
					g_activityEditor.hint.text(MESSAGES["activity_saved"]);
				}
				g_activityEditor.id = activity.id;
				g_activityEditor.refresh(activity);
				if(g_onActivitySaveSuccess == null) return;
				g_onActivitySaveSuccess();
			},
			error: function(xhr, status, err){
				g_activityEditor.setSavable();
				g_activityEditor.enableEditorButtons();
				g_activityEditor.hint.text(MESSAGES["activity_not_saved"]);
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
            alert(ALERTS["please_log_in"]);
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
                    if (isTokenExpired(data)) {
                        logout(null);
                        return;
                    }
                    if (!isStandardSuccess(data)) return;
                    g_activityEditor.enableEditorButtons();
                    g_activityEditor.hide();
                    if (g_onActivitySaveSuccess == null) return;
                    g_onActivitySaveSuccess();
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
			var node = $('<div>', {
				"class": "preview-container left"
			}).appendTo(par);
			editor.newImageNodes[key] = node; // add new image node to view map		
			var imgHelper = $('<span>', {
				"class": "image-helper"
			}).appendTo(node);
			var img = $('<img>', {
				src: e.target.result
			}).appendTo(node);

			var btnDelete = $("<button>", {
				text: TITLES["delete"],
				"class": "purple image-delete"
			}).appendTo(node);
			
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
					//var offset = getOffset(newImageNode);
					//setOffset(newImageNode, offset.left - g_wImageCell, null);
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
	
	var aButton = getTarget(evt);
	disableField(aButton);
	$.ajax({
		type: "POST",
		url: "/activity/delete",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			if (isTokenExpired(data)) {
				logout(null);
				return;	
			}
			g_activityEditor.hide();
		},
		error: function(xhr, status, err){
			enableField(aButton);
		}
	});

}

function onCancel(evt){

	evt.preventDefault();
	g_activityEditor.hide();

}

// Generators
function generateOldImagesRow(par, editor, activity) {
	var oldImagesRow = $("<div>", {
		"class": "image-row old clearfix patch-block-epsilon"
	}).appendTo(par);

	var countOldImages = Object.keys(activity.images).length;
	for(var i =0; i < countOldImages; i++) {
		var node = $("<div>", {
			"class": "preview-container left"
		}).appendTo(oldImagesRow);
		var imageHelper = $('<span>', {
			"class": "image-helper"
		}).appendTo(node);
		var image = $('<img>', {
			src: activity.images[i].url
		}).appendTo(node);

		var indicator = $('<img>',{
			"class": "checked-image",
			src: "/assets/icons/checked.png"
		}).appendTo(node);

		var selector = new ImageSelector(activity.images[i].id, image, indicator);
		node.click(selector, function(evt) {
			evt.preventDefault();
			var selector = evt.data;
			var val = !(selector.checked);
			if (val && countImages(editor) >= g_imagesLimit) return;
			selector.checked = val;
			if (val) selector.indicator.show();
			else selector.indicator.hide();
			editor.setSavable();
			editor.setNonSubmittable();
		});
		editor.imageSelectors.push(selector);
	}
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
		text: MESSAGES["delete_activity_confirmation"],
		style: "font-size: 18pt;"
	}).appendTo(ret);

	var buttons = $("<p>").appendTo(ret);
	var btnYes = $("<button>", {
		text: TITLES["yes"],
		style: "font-size: 16pt; background-color: cornflowerblue; color: white; margin-right: 10pt;"
	}).appendTo(buttons);
	var btnNo = $("<button>", {
		text: TITLES["no"],
		style: "font-size: 16pt; background-color: crimson; color: white; margin-left: 10pt;"
	}).appendTo(buttons);
	
	return new DeleteConfirmation(ret, activity, btnYes, btnNo);
}
