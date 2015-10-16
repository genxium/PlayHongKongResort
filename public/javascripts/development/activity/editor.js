/* 
 * variables
 */

var g_deleteConfirmation = null;
var g_activityEditor = null;

// DOM indexes for cascaded DOM element search
var g_keyOldImage = "old_image";
var g_keyNewImage = "new_image";

// general variables
var g_imagesLimit = 3;

// callback functions
var g_onActivitySaveSuccess = null;
var g_loadingWidget = null;

/**
 * ActivityEditorImageNode
 * */

function ActivityEditorImageNode(cdn, domain) {
	this.setCDNCredentials(cdn, domain, getToken(), g_loggedInPlayer);	
	this.requestDel = function(onSuccess, onError) {
		// async process
		var token = getToken();				
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
	this.titleField = null;
	this.titleCounter = null;
	this.addressField = null;
	this.addressCounter = null;
	this.contentField = null;
	this.contentCounter = null;
	this.newImageRow = null;
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

	this.addNewImageNode = function(fromImageSelector, fromDeletion) {
		// Note that in any valid state, there should be a node for selection on the form
		fromImageSelector = (fromImageSelector === undefined ? false : fromImageSelector);
		fromDeletion = (fromDeletion === undefined ? false : fromDeletion);
		if (!fromImageSelector && !fromDeletion) {
		        var domain = queryCDNDomainSync();
			var node = new ActivityEditorImageNode(g_cdnQiniu, domain);
			node.appendTo(this.newImageRow);
			node.refresh(this);
		}

		var countNew = Object.keys(this.newImageNodes).length;
		var countTot = countImages(this);
		var countOld = countTot - countTot;

		var lastKey = null;
		for (var key in this.newImageNodes) {
			if (!lastKey) lastKey = key;
			else if (key > lastKey) lastKey = key;	
			else;
		}
		var lastNewImageNode = this.newImageNodes[lastKey]; 
		
		if (countTot - 1 >= g_imagesLimit) {
			lastNewImageNode.uploader.disableBrowse();
			disableField(lastNewImageNode.btnChoose);
		} else {
			lastNewImageNode.uploader.disableBrowse(false);
			enableField(lastNewImageNode.btnChoose);
		}
	};

	this.composeContent = function(activity) {
		this.id = null;
		var isNewActivity = false;
		if(!activity || !activity.id) isNewActivity = true;

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

		var form = $('<form>', {
			"class": "activity-editor-form"
		}).appendTo(this.content);

		this.titleField = $('<input>', {
			placeholder: HINTS.activity_title,
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
			placeholder: HINTS.activity_address,
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
			placeholder: HINTS.activity_content,
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
			html: MESSAGES.image_selection_requirement
		}).appendTo(form);

		this.newImageNodes = {};
		this.imageSelectors = [];

		this.newImageRow = $("<div>", {
			"class": "image-row clear"
		}).appendTo(form);
		if(!(!activity) && !(!activity.images))	generateOldImagesRow(form, this, activity);

		this.addNewImageNode(false, false);

		// Schedules
		var deadline = reformatDate(new Date());
		if(!(!activity) && !(!activity.applicationDeadline)) deadline = activity.applicationDeadline;

		var beginTime = reformatDate(new Date());
		if(!(!activity) && !(!activity.beginTime)) beginTime = activity.beginTime;

		var scheduleRow1 = $("<div>", {
			"class": "edit-deadline clearfix"
		}).appendTo(form);
		var scheduleCell11 = $("<div>", {
			text: TITLES.deadline,
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
			text: TITLES.begin_time,
			"class": "left edit-label"
		}).appendTo(scheduleRow2);
		var scheduleCell22 = $("<div>", {
			"class": "datetime-picker left"
		}).appendTo(scheduleRow2);
		this.beginTimePicker = generateDateSelection(scheduleCell22, gmtMiilisecToLocalYmdhi(beginTime));

		this.captcha = null;
		if (isNewActivity) {
			this.captcha = new Captcha(generateUuid());
			this.captcha.appendTo(form);
			this.captcha.refresh();
		}
		
		var buttons = $("<div>", {
			"class": "edit-button-rows clear"
		}).appendTo(form);

		/* Associated Buttons */
		this.btnSave = $('<button>',{
			"class": "btn-save positive-button",
			text: TITLES.save
		}).appendTo(buttons);
		this.btnSave.click(onSave);

		this.btnSubmit = $('<button>',{
			"class": "btn-submit positive-button",
			text: TITLES.submit
		}).appendTo(buttons);
		var dSubmit = {};
		dSubmit[g_keyActivityId] = activityId;
		this.btnSubmit.click(dSubmit, onSubmit);

		this.btnCancel = $('<button>',{
			"class": "btn-cancel negative-button",
			text: TITLES.cancel
		}).appendTo(buttons);
		this.btnCancel.click(onCancel);

		this.btnDelete = null;
		if(!isNewActivity){
			this.btnDelete = $('<button>',{
				"class": "btn-delete caution-button",
				text: TITLES.del
			}).appendTo(buttons);
			this.btnDelete.click(function(evt) {
				evt.preventDefault();
				if (!(!g_deleteConfirmation)) g_deleteConfirmation.remove();
				g_deleteConfirmation = generateDeleteConfirmation(g_activityEditor.content, activity);
			});
		}

		this.hint = $("<div>", {
			"class": "clear"
		}).appendTo(form);

		this.setNonSavable();
		this.setSubmittable();

	};

	this.savable = false;
	this.submittable = true;

	this.disableEditorButtons = function() {
		disableField(this.btnSave);
		this.btnSave.addClass("disabled-button");
		disableField(this.btnSubmit);
		this.btnSubmit.addClass("disabled-button");
		if (!this.btnDelete) return;
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
		if (!(!this.btnDelete)) {
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
}

ActivityEditor.inherits(BaseModalWidget);

function initActivityEditor(par) {
	g_activityEditor = new ActivityEditor();
	g_activityEditor.appendTo(par, true, "activity-editor");	
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
	var ret = Object.keys(editor.newImageNodes).length;
	for(var key in editor.imageSelectors) {
		var selector = editor.imageSelectors[key];
		if(!selector.checked) continue;
		++ret;
	}
	return ret;
}

function onSave(evt){
	evt.preventDefault();
	
	if (!g_activityEditor || !g_activityEditor.savable) return;
	var formData = {};

	// append player token and activity id for identity
	var token = getToken();
	if (!token) {
		alert(ALERTS.please_log_in);
		return;
	}


	formData[g_keyToken] = token;

	var countTot = Object.keys(g_activityEditor.newImageNodes).length - 1;
	var lastKey = null;
	for (var key in g_activityEditor.newImageNodes) {
		if (!lastKey) lastKey = key;
		else if (key > lastKey) lastKey = key;	
		else;
	}
        var newImages = [];
        for (var k in g_activityEditor.newImageNodes) {
		if (k == lastKey) continue;
                var node = g_activityEditor.newImageNodes[k];
                newImages.push(node.remoteName);
        }
	formData[g_keyNewImage] = JSON.stringify(newImages);

	var selectedOldImages = [];
	for (var i = 0; i < g_activityEditor.imageSelectors.length; ++i){
		var selector = g_activityEditor.imageSelectors[i];
		if(!selector.checked) continue;
		selectedOldImages.push(selector.id);
		++countTot;
	}
	formData[g_keyOldImage] = JSON.stringify(selectedOldImages);

	if (countTot > g_imagesLimit) {
		alert(ALERTS.image_selection_requirement);
		return;
	}

	// append activity title and content
	var title = g_activityEditor.titleField.val();
	var address = g_activityEditor.addressField.input.val();
	var content = g_activityEditor.contentField.val();

	var titleCounter = g_activityEditor.titleCounter;
	var addressCounter = g_activityEditor.addressCounter;
	var contentCounter = g_activityEditor.contentCounter;

	if (!titleCounter.valid() || !addressCounter.valid() || !contentCounter.valid()) {
		alert(ALERTS.please_follow_activity_field_instructions);
		return;
	}
	formData[g_keyTitle] = title;
	formData[g_keyAddress] = address;
	formData[g_keyContent] = content;

	// append activity begin time and deadline
	var beginTime = localYmdhisToGmtMillisec(g_activityEditor.beginTimePicker.getDatetime());
	var deadline = localYmdhisToGmtMillisec(g_activityEditor.deadlinePicker.getDatetime());
		
	if (deadline < 0 || beginTime < 0 || deadline > beginTime) {
		alert(ALERTS.deadline_behind_begin_time);
		return;
	}

	formData[g_keyBeginTime] = beginTime;
	formData[g_keyDeadline] = deadline;

	var isNewActivity = false;
	var activityId = g_activityEditor.id; 
	if (!activityId) isNewActivity = true;

	if (!isNewActivity)	formData[g_keyActivityId] = activityId;
	else {
		formData[g_keySid] = g_activityEditor.captcha.sid;
		formData[g_keyCaptcha] = g_activityEditor.captcha.input.val();
	}

	g_activityEditor.disableEditorButtons();
	g_activityEditor.setNonSavable();
	g_activityEditor.setNonSubmittable();
	g_activityEditor.hint.text(MESSAGES.activity_saving);

	$.ajax({
			type: "POST",
			url: "/activity/save",
			data: formData,
			success: function(data, status, xhr){
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
				if (isCaptchaNotMatched(data)) {
					alert(ALERTS.captcha_not_matched);
					g_activityEditor.setSavable();
					g_activityEditor.enableEditorButtons();
					g_activityEditor.hint.text(MESSAGES.activity_not_saved);
					return;
				}
				if (isCreationLimitExceeded(data)) {
					alert(ALERTS.creation_limit_exceeded);
					g_activityEditor.setSavable();
					g_activityEditor.enableEditorButtons();
					g_activityEditor.hint.text(MESSAGES.activity_not_saved);
					return;
				}
				g_activityEditor.setSubmittable();
				g_activityEditor.enableEditorButtons();
				var activity = new Activity(data);
				if (!g_activityEditor.id) {
					g_activityEditor.hint.text(MESSAGES.activity_created);
				} else {
					g_activityEditor.hint.text(MESSAGES.activity_saved);
				}
				g_activityEditor.id = activity.id;
				g_activityEditor.refresh(activity);
				if(!g_onActivitySaveSuccess) return;
				g_onActivitySaveSuccess();
			},
			error: function(xhr, status, err){
				g_activityEditor.setSavable();
				g_activityEditor.enableEditorButtons();
				g_activityEditor.hint.text(MESSAGES.activity_not_saved);
			}
	});
}

function onSubmit(evt){

	evt.preventDefault();

	if (!g_activityEditor || !g_activityEditor.submittable) return;

        var data = evt.data;

        var params = {};

        // append player token and activity id for identity
        var token = getToken();
        if(!token) {
            alert(ALERTS.please_log_in);
            return;
        }
        params[g_keyToken] = token;
	
        var activityId = g_activityEditor.id;
        params[g_keyActivityId] = activityId;

	g_activityEditor.disableEditorButtons();
        g_activityEditor.setNonSavable();
        g_activityEditor.setNonSubmittable();

        $.ajax({
                type: "POST",
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
                    if (!g_onActivitySaveSuccess) return;
                    g_onActivitySaveSuccess();
                },
                error: function(xhr, status, err){
                        g_activityEditor.enableEditorButtons();
                        g_activityEditor.setSubmittable();
                }
        });
}

function onDelete(evt){

	evt.preventDefault();
	var activityId = evt.data;
	var token = getToken();

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
			if (!g_onActivitySaveSuccess) return;
			g_onActivitySaveSuccess();
		},
		error: function(xhr, status, err){
			enableField(aButton);
		}
	});
}

function onCancel(evt){

	evt.preventDefault();
	g_activityEditor.hide();

	var token = getToken();
	if (!token)	return; 

	var newImageNodes = g_activityEditor.newImageNodes;
	if (Object.keys(newImageNodes).length <= 1) return;

	var lastKey = null;
	for (var key in this.newImageNodes) {
		if (!lastKey) lastKey = key;
		else if (key > lastKey) lastKey = key;	
		else;
	}

        var newImages = [];
        for (var k in newImageNodes) {
		if (k == lastKey) continue;
                var node = newImageNodes[k];
                newImages.push(node.remoteName);
        }
	var params = {};
	params[g_keyBundle] = JSON.stringify(newImages);
	params[g_keyToken] = token;

	$.ajax({
		type: 'POST',
		url: '/image/cdn/qiniu/delete',
		data: params,
		success: function(data, status, xhr) {
			if (!isStandardSuccess(data))	return;
		},
		error: function(xhr, status, err) {

		}
	});	

}

// Generators
function generateOldImagesRow(par, editor, activity) {
	var oldImagesRow = $("<div>", {
		"class": "image-row clear"
	}).appendTo(par);

	var countOldImages = Object.keys(activity.images).length;
	var nodeOnClick = function(evt) {
		evt.preventDefault();
		var selector = evt.data;
		var val = !(selector.checked);
		if (val && countImages(editor) >= g_imagesLimit) return;
		selector.checked = val;
		if (val) selector.indicator.show();
		else selector.indicator.hide();
		editor.setSavable();
		editor.setNonSubmittable();
		editor.addNewImageNode(true, false);
	};
	for (var i =0; i < countOldImages; ++i) {
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
		node.click(selector, nodeOnClick);
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
	if (!g_activityEditor) return;

	var ret = $("<div>", {
		style: "padding: 5pt;"
	}).appendTo(par);
	
	var title = $("<p>", {
		text: MESSAGES.delete_activity_confirmation,
		"class": "delete-confirmation-question"
	}).appendTo(ret);

	var buttons = $("<p>").appendTo(ret);
	var btnYes = $("<button>", {
		text: TITLES.yes,
		"class": "delete-confirmation-button negative-button"
	}).appendTo(buttons);
	var btnNo = $("<button>", {
		text: TITLES.no,
		"class": "delete-confirmation-button positive-button"
	}).appendTo(buttons);
	
	return new DeleteConfirmation(ret, activity, btnYes, btnNo);
}
