/* 
 * variables
 */

var g_editor = null;

// general DOM elements
var g_activityEditor = null;
var g_modalActivityEditor = null;
var g_sectionActivityEditor = null;

// button keys
var g_classBtnEdit = "btn-edit";
var g_classBtnSubmit = "btn-submit";
var g_classBtnDelete = "btn-delete";
var g_classBtnSave = "btn-save";
var g_classBtnCancel = "btn-cancel";

// DOM indexes for cascaded DOM element search
var g_indexOldImage = "old_image";
var g_indexNewImage = "new_image";

var g_wImageCell = 200;
var g_hImageCell = 200;

// general variables
var g_imagesLimit = 3;

// callback functions
var g_onEditorRemoved = null;
var g_onEditorCancelled = null;

var g_onQueryActivitiesSuccess = null;
var g_onQueryActivitiesError = null;

var g_onActivitySaveSuccess = null;

var g_loadingWidget = null;

// Existing images selector
function ImageSelector(id, image, indicator) {
	this.id = id;
	this.image = image;
	this.indicator = indicator;
	this.checked = true;
}

function ExplorerTrigger(node, btn) {
	this.node = node;
	this.btn = btn;
}

function ActivityEditor(id, titleField, contentField, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete, explorerTrigger, hint) {
	if (id != null) this.id = id;
	this.titleField = titleField;
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
	this.savable = false;
	this.submittable = true;

	this.disableEditorButtons = function() {
		if (!this.savable) {
			disableField(this.btnSave);
			this.btnSave.css("color", "white");
		}
		if (!this.submittable) {
			disableField(this.btnSubmit);
			this.btnSubmit.css("color", "white");
		}
		if (this.btnDelete != null) {
			disableField(this.btnDelete);
			this.btnDelete.css("color", "white");
		}
	}

	this.enableEditorButtons = function() {
		if (this.savable) {
			enableField(this.btnSave);
			this.btnSave.css("color", "black");
		}	
		if (this.submittable) {
			enableField(this.btnSubmit);
			this.btnSubmit.css("color", "black");
		}	
		if (this.btnDelete != null) {
			enableField(this.btnDelete);
			this.btnDelete.css("color", "black");
		}
	}

	this.setNonSavable = function() {
		this.savable = false;
		this.disableEditorButtons();
	}

	this.setSavable = function() {
		this.savable = true;
		this.enableEditorButtons();
	}

	this.setNonSubmittable = function() {
		this.submittable = false;
		this.disableEditorButtons();
	}

	this.setSubmittable = function() {
		this.submittable = true;
		this.enableEditorButtons();
	}

	this.removeExplorerTrigger = function() {
		if (this.explorerTrigger.node == null)	return;
		this.explorerTrigger.node.empty();
		this.explorerTrigger.node.remove();
		this.explorerTrigger.node = null;
		if (this.explorerTrigger.btn == null)	return;
		this.explorerTrigger.btn.remove();
		this.explorerTrigger.btn = null;
	}
	
	this.shiftExplorerTrigger = function(direction) {
		// direction {
		//	left: -1,
		//	right: +1
		// }
		var currentOffset = getOffset(this.explorerTrigger.node);
		switch (direction) {
			case -1:
				var left = currentOffset.left - g_wImageCell;
				setOffset(this.explorerTrigger.node, left, null);
			break;
			case +1:
				var left = currentOffset.left + g_wImageCell;
				setOffset(this.explorerTrigger.node, left, null);
			break;
		}
	}
}

// Assistive functions
function formatDigits(value, numberOfDigits){
       var valueStr = value.toString();
       while(valueStr.length < numberOfDigits) valueStr = "0" + valueStr;
       return valueStr;
}

function formatDate(time){
	time=time.replace(/-/g,"/");
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
        if(g_activityEditor == null) return;
        g_activityEditor.remove();
}

function initActivityEditor(){
	var wrap = $("#wrap");
	/*
		Note: ALL attributes, especially the `class` attribute MUST be written INSIDE the div tag, bootstrap is NOT totally compatible with jQuery!!!
	*/
	g_sectionActivityEditor = $("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='Create an activity!' aria-hidden='true'>", {
		style: "height: 80%; position: absolute"
	}).appendTo(wrap);
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

        g_sectionActivityEditor.css("position", "absolute");
        g_sectionActivityEditor.css("height", "90%");
        g_sectionActivityEditor.css("padding", "5pt");
        g_sectionActivityEditor.modal({
                show: true
        });
}

function refreshActivityEditor(activity) {
        g_activityEditor = generateActivityEditor(activity);
        g_modalActivityEditor.empty();
        g_modalActivityEditor.append(g_activityEditor);
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

function isFileValid(file){
	var ret=false;
	var fileSizeLimit= (1<<20)// 2 mega bytes
	if(file.size > fileSizeLimit) return false;
	return true;
}

// Assistive Callback Functions
function onSave(evt){
	evt.preventDefault();
	
	if (g_editor == null || !g_editor.savable) return;
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
        for (var key in g_editor.newImageFiles) {
            var file = g_editor.newImageFiles[key];
            formData.append(g_indexNewImage + "-" + key.toString(), file);
	}
	
	countImages = Object.keys(g_editor.newImageFiles).length;

        var selectedOldImages = new Array();
        for(var i = 0; i < g_editor.imageSelectors.length; i++){
            var selector = g_editor.imageSelectors[i];
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
        var title = g_editor.titleField.val();
	var content = g_editor.contentField.val();
	if(title == null || content == null || title == "" || content == "") {
		alert("Neither title nor content could be empty.");
		return;
	}
        formData.append(g_keyTitle, title);
        formData.append(g_keyContent, content);

        // append activity begin time and deadline
        var beginTime = getDateTime(g_editor.beginTimePicker);
        var deadline = getDateTime(g_editor.deadlinePicker);
		
	if(compareYmdhisDate(deadline, beginTime) > 0) {
		alert("Deadline can not be after the begin time of an activity.");
		return;
	}
        formData.append(g_keyBeginTime, beginTime);
        formData.append(g_keyDeadline, deadline);

        var isNewActivity = false;
	var activityId = g_editor.id; 
        if(activityId == null) isNewActivity = true;

        if(!isNewActivity)	formData.append(g_keyActivityId, activityId);

        g_editor.setNonSavable();
        g_editor.setNonSubmittable();

        $.ajax({
                method: "POST",
                url: "/activity/save",
                data: formData,
                mimeType: "mutltipart/form-data",
                contentType: false, // tell jQuery not to set contentType
                processData: false, // tell jQuery not to process the data
                success: function(data, status, xhr){
			g_editor.setSubmittable();
			var activityJson = JSON.parse(data);
			var activity = new Activity(activityJson);
			if (g_editor.id == null) {
				alert("Activity created.");
			} else {
				alert("Changes saved.");
			}
			g_editor.id = activity.id;
			refreshActivityEditor(activity);	
			if(g_onActivitySaveSuccess == null) return;
			g_onActivitySaveSuccess();
                },
                error: function(xhr, status, err){
                        g_editor.setSavable();
			alert("Activity not saved!");
                }
        });
}

function onSubmit(evt){

	evt.preventDefault();

	if (g_editor == null || !g_editor.submittable) return;

        var data = evt.data;

        var params = {};

        // append user token and activity id for identity
        var token = $.cookie(g_keyToken);
	if(token == null) {
		alert("Are you logged out?");
		return;
	}
        params[g_keyToken] = token;
	
	var activityId = g_editor.id; 
        params[g_keyActivityId] = activityId;

        g_editor.setNonSavable();
        g_editor.setNonSubmittable();

        $.ajax({
                method: "PUT",
                url: "/activity/submit",
                data: params,
                success: function(data, status, xhr){
                        removeActivityEditor();
                        if(g_onEditorRemoved == null) return;
			g_onEditorRemoved();
                },
                error: function(xhr, status, err){
                        g_editor.setSubmittable();
                }
        });
}

function previewImage(par, editor) {
	var input = editor.explorerTrigger.btn[0]; // pull DOM
        var images  = input.files;
        if (images == null) return;
        var newImage = images[0];
        if(newImage == null) return;
        var count = images.length;
        if(count != 1){
            alert("Choose only 1 image at a time!!!");
            return;
        }
        var reader = new FileReader();

        reader.onload = function (e) {
		var key = new Date().getTime(); // the key which identifies an image in the map newImages

		var offset = Object.keys(editor.newImageFiles).length;
		editor.newImageFiles[key] = newImage; // add new image to file map
		var node = $('<span>', {
			style: "position: absolute; padding: 2pt"
		}).appendTo(par);
		setDimensions(node, g_wImageCell, g_hImageCell);
		setOffset(node, offset * g_wImageCell, null);
		editor.newImageNodes[key] = node; // add new image node to view map		

		var img = $('<img>', {
			src: e.target.result
		}).appendTo(node);
		setDimensions(img, g_wImageCell, g_hImageCell);

		var btnDelete = $("<button>", {
			text: "delete",
			style: "color: white; background-color: red;"
		}).appendTo(node);
		setDimensions(btnDelete, g_wImageCell, null);
		
		editor.shiftExplorerTrigger(+1);
		
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
			editor.shiftExplorerTrigger(-1);
		});
        }

        reader.readAsDataURL(newImage);
}

function onDelete(evt){

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);

	var params={};
	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	try{
	    $.ajax({
	        type: "POST",
	        url: "/activity/delete",
	        data: params,
	        success: function(data, status, xhr){
                    g_activityEditor.remove();
                    if(g_onEditorRemoved == null)   return;
		    g_onEditorRemoved();
                },
	        error: function(xhr, status, err){

	        }
	    });
	} catch(err){
		
	}
}

function onCancel(evt){

	evt.preventDefault();
	removeActivityEditor();
	if(g_onEditorCancelled == null) return;
        g_onEditorCancelled();

}

// Generators
function generateActivityEditor(activity){
	
	var isNewActivity = false;
	if(activity == null || activity.id == null) isNewActivity = true;

	var activityId = null;
	var activityTitle = "";
	var activityContent = "";

	if(!isNewActivity) {
		activityId = activity.id;
		activityTitle = activity.title;
		activityContent = activity.content;
	}

	var ret = $('<form>', {
		style: "display: block; padding: 5pt"
	});

	var titleText = $('<p>', {
		text: "Title",
		style: "margin-top: 5pt"	
	}).appendTo(ret);

	var titleInput = $('<input>', {
		class: "input-title",
		type: 'text',
		value: activityTitle
	}).appendTo(ret);

	titleInput.on("input paste keyup", function(evt){
	        evt.preventDefault();
		g_editor.setSavable();
		g_editor.setNonSubmittable();
	});

	var contentText = $('<p>', {
		text: "Content",
		style: "margin-top: 5pt"
	}).appendTo(ret);

	var contentInput = $('<textarea>',	{
		class: "input-content" 
	}).appendTo(ret);
	contentInput.val(activityContent);
	contentInput.on("input paste keyup", function(evt){
	        evt.preventDefault();
		g_editor.setSavable();
		g_editor.setNonSubmittable();
	});

	$("<p>", {
		style: "font-size: 14pt; color: red; padding: 5pt",
		text: "Up to 3 images can be saved for an activity"
	}).appendTo(ret);

        var newImageFiles = {};
	var newImageNodes = {};
	var imageSelectors = new Array();

	var newImagesRow = $("<p>", {
		style: "overflow-x: auto;"
	});

	setDimensions(newImagesRow, null, g_hImageCell + 5);

	if(activity != null && activity.images != null) {
		var oldImagesRow = $("<p>", {
			style: "overflow-x: auto;"	
		}).appendTo(ret);
		setDimensions(oldImagesRow, null, g_hImageCell + 5);

		var countOldImages = Object.keys(activity.images).length;
		for(var i =0; i < countOldImages; i++){
			var node = $("<span>").appendTo(oldImagesRow);
			setDimensions(node, g_wImageCell, g_hImageCell);

                        var image = $('<img>',{
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
				if (val && countImages(g_editor) >= g_imagesLimit) return;
				selector.checked = val;
				if (val) selector.indicator.show();
				else selector.indicator.hide();
                                g_editor.setSavable();
                                g_editorsetNonSubmittable();
			});
			imageSelectors.push(selector);
		}
	}

	newImagesRow.appendTo(ret);
	var nodeBtnAdd = $("<span>", {
                style: "display:inline-block; position: absolute;"
        }).appendTo(newImagesRow);
	setDimensions(nodeBtnAdd, g_wImageCell, g_hImageCell);
        setOffset(nodeBtnAdd, 0, null);        

        var picBtnAdd = $("<img>", {
                src: "/assets/icons/add.png",
                style: "position: absolute; cursor: pointer;"
        }).appendTo(nodeBtnAdd);
	setDimensions(picBtnAdd, g_wImageCell - 10, g_hImageCell - 10);

        btnAdd = $("<input>", {
                type: "file",
                style: "filter: alpha(opacity=0); opacity: 0; position: absolute;"
        }).appendTo(nodeBtnAdd);
	setDimensions(btnAdd, g_wImageCell, g_hImageCell);
        btnAdd.change(function(evt) {
                evt.preventDefault();
                g_editor.setSavable();
                g_editor.setNonSubmittable();
                previewImage(newImagesRow, g_editor);
        });
	var explorerTrigger = new ExplorerTrigger(nodeBtnAdd, btnAdd);
	$('<br>').appendTo(ret);
	
	// Schedules
	var deadline = reformatDate(new Date());
	if(activity != null && activity.applicationDeadline != null) deadline = activity.applicationDeadline;
	var deadlinePicker = generateDateSelection(formatDate(deadline));
        deadlinePicker.on("input keyup change", function(evt){
            evt.preventDefault();
            g_editor.setSavable();
            g_editor.setNonSubmittable();
        });

	var beginTime = reformatDate(new Date());
	if(activity != null && activity.beginTime != null) beginTime = activity.beginTime;
	var beginTimePicker = generateDateSelection(formatDate(beginTime));
        beginTimePicker.on("input keyup change", function(evt){
            evt.preventDefault();
            g_editor.setSavable();
            g_editor.setNonSubmittable();
        });

	var tableSchedule = $("<table>", {
		style: "display: block; margin-top: 15pt; margin-bottom: 5pt"
	}).appendTo(ret);
	var scheduleRow1 = $("<tr>").appendTo(tableSchedule);
	var scheduleCell11 = $("<td>", {
		text: "Deadline: ",
		style: "white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow1);
	var scheduleCell12 = $("<td>", {

	}).appendTo(scheduleRow1);
	scheduleCell12.append(deadlinePicker);

	var scheduleRow2 = $("<tr>").appendTo(tableSchedule);
	var scheduleCell21 = $("<td>", {
		text: "Begin Time: ",
		style: "white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow2);
	var scheduleCell22 = $("<td>", {

	}).appendTo(scheduleRow2);
	scheduleCell22.append(beginTimePicker);	

	var buttons = $("<p>", {
		style: "display: block; clear: both; margin-top: 5pt"
	}).appendTo(ret);

	/* Associated Buttons */
	var btnSave = $('<button>',{
		class: g_classBtnSave,
		text: 'Save'
	}).appendTo(buttons);
	btnSave.on("click", onSave);

	var btnSubmit = $('<button>',{
		class: g_classBtnSubmit,
		text: 'Submit'
	}).appendTo(buttons);
	var dSubmit = {};
	dSubmit[g_keyActivityId] = activityId;
	btnSubmit.on("click", dSubmit, onSubmit);

	var btnCancel = $('<button>',{
		class: g_classBtnCancel,
		text: 'Cancel'
	}).appendTo(buttons);
	btnCancel.on("click", onCancel);

	var btnDelete = null;
	if(!isNewActivity){
		btnDelete = $('<button>',{
			class: g_classBtnDelete,
			text: 'Delete'
		}).appendTo(buttons);
                var dDelete = {};
                dDelete[g_keyActivityId] = activityId;
		btnDelete.on("click", dDelete, onDelete);
	}

	var hint = $("<p>", {
		style: "color: blue"
	}).appendTo(ret);

	g_editor = new ActivityEditor(activityId, titleInput, contentInput, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete, explorerTrigger, hint);	
	g_editor.setNonSavable();
	g_editor.setSubmittable();

	return ret;
}

function generateDateSelection(time){
        var ret = generateDatePicker(time);
        ret.on("input change keyup", function(evt){
                evt.preventDefault();
                g_editor.setSavable();
                g_editor.setNonSubmittable();
        });
     	return ret;
}
