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

var g_savable = false;
var g_submittable = true;

// callback functions
var g_onEditorRemoved = null;
var g_onEditorCancelled = null;

var g_onQueryActivitiesSuccess = null;
var g_onQueryActivitiesError = null;

var g_onActivitySaveSuccess = null;

var g_loadingWidget = null;

var g_nodeBtnAdd = null;
var g_btnAdd = null;

// Existing images selector
function ImageSelector(id, image, indicator) {
	this.id = id;
	this.image = image;
	this.indicator = indicator;
	this.checked = true;
}

function ActivityEditor(id, titleField, contentField, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete) {
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

function disableEditorButtons(editor) {
	if (editor == null) return;
	if (!g_savable) disableField(editor.btnSave);
	if (!g_submittable) disableField(editor.btnSubmit);
	if (editor.btnDelete != null) disableField(editor.btnDelete);
}

function enableEditorButtons(editor) {
	if (editor == null) return;
	if (g_savable) enableField(editor.btnSave);
	if (g_submittable) enableField(editor.btnSubmit);
	if (editor.btnDelete != null) enableField(editor.btnDelete);
}

function setNonSavable(){
	g_savable = false;
	disableEditorButtons(g_editor);
}

function setSavable(){
	g_savable = true;
	enableEditorButtons(g_editor);
}

function setNonSubmittable(){
	g_submittable = false;
	disableEditorButtons(g_editor);
}

function setSubmittable(){
	g_submittable = true;
	enableEditorButtons(g_editor);
}

// Assistive Callback Functions
function onSave(evt){
	evt.preventDefault();
	
	if (g_editor == null) return;

        if (!g_savable){
            alert("You haven't made any changes!");
            return;
        }

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

        setNonSavable();
        setNonSubmittable();

        $.ajax({
                method: "POST",
                url: "/activity/save",
                data: formData,
                mimeType: "mutltipart/form-data",
                contentType: false, // tell jQuery not to set contentType
                processData: false, // tell jQuery not to process the data
                success: function(data, status, xhr){
			setSubmittable();
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
                        setSavable();
			alert("Activity not saved!");
                }
        });
}

function onSubmit(evt){

	evt.preventDefault();

	if (g_editor == null) return;

        if (!g_submittable) {
            alert("You have to save your changes before submission!");
            return;
        }

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

        setNonSavable();
        setNonSubmittable();

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
                        setSubmittable();
                }
        });
}

function previewImage(par, editor) {
	var input = g_btnAdd[0]; // pull DOM
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
		node.css("width", g_wImageCell);
		node.css("height", g_hImageCell);
		node.css("left", offset * g_wImageCell);
		editor.newImageNodes[key] = node; // add new image node to view map		

		var img = $('<img>', {
			src: e.target.result
		}).appendTo(node);
		img.css("width", g_wImageCell);
		img.css("height", g_hImageCell);

		var btnDelete = $("<button>", {
			text: "delete",
			style: "color: white; background-color: red;"
		}).appendTo(node);
		btnDelete.css("width", g_wImageCell);
		
		refreshAddButton(par, editor);
		
		btnDelete.click(function(evt){
			evt.preventDefault();
			setSavable();
			setNonSubmittable();
			if(!editor.newImageFiles.hasOwnProperty(key)) return;
			delete editor.newImageFiles[key];	
			if(!editor.newImageNodes.hasOwnProperty(key)) return;
			editor.newImageNodes[key].remove();
			delete editor.newImageNodes[key];
			for(var otherKey in editor.newImageNodes) {
				if(otherKey < key) continue;
				var newImageNode = editor.newImageNodes[otherKey];	
				var l = parseInt(newImageNode.css("left"));
				newImageNode.css("left", l - g_wImageCell);
			}
			refreshAddButton(par, editor);
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

function refreshAddButton(par, editor) {
	
	removeAddButton();

	var offset = Object.keys(editor.newImageFiles).length;

	g_nodeBtnAdd = $("<span>", {
		style: "display:inline-block; position: absolute;"
	}).appendTo(par);
	g_nodeBtnAdd.css("height", g_hImageCell);
	g_nodeBtnAdd.css("width", g_wImageCell);
	g_nodeBtnAdd.css("left", offset * g_wImageCell);	

	var picBtnAdd = $("<img>", {
		src: "/assets/icons/add.png",
		style: "position: absolute; cursor: pointer;"
	}).appendTo(g_nodeBtnAdd);
	picBtnAdd.css("height", g_hImageCell - 5);
	picBtnAdd.css("width", g_wImageCell - 5);

	g_btnAdd = $("<input>", {
		type: "file",
		style: "filter: alpha(opacity=0); opacity: 0; position: absolute;"
	}).appendTo(g_nodeBtnAdd);
	g_btnAdd.css("height", g_hImageCell);
	g_btnAdd.css("width", g_wImageCell);

	g_btnAdd.change(function(evt) {
		evt.preventDefault();
		setSavable();
		setNonSubmittable();
		previewImage(par, editor);
	});

	var imagesTot = countImages(editor);
	if(imagesTot < g_imagesLimit) enableField(g_btnAdd);
	else disableField(g_btnAdd);
}

function removeAddButton() {
	if(g_nodeBtnAdd != null) {
		g_nodeBtnAdd.empty();
		g_nodeBtnAdd.remove();
		g_nodeBtnAdd = null;
	}
	if(g_btnAdd != null) {
		g_btnAdd.remove();
		g_btnAdd = null;
	}
}

// Generators
function generateActivityEditor(activity){
	
	removeAddButton();
	setNonSavable();
	setSubmittable();
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
		setSavable();
		setNonSubmittable();
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
		setSavable();
		setNonSubmittable();
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

	newImagesRow.css("height", g_hImageCell + 5);

	if(activity != null && activity.images != null) {
		var oldImagesRow = $("<p>", {
			style: "overflow-x: auto;"	
		}).appendTo(ret);
		oldImagesRow.css("height", g_hImageCell + 5);

		var countOldImages = Object.keys(activity.images).length;
		for(var i =0; i < countOldImages; i++){
			var node = $("<span>").appendTo(oldImagesRow);
			node.css("width", g_wImageCell);
			node.css("height", g_hImageCell);

                        var image = $('<img>',{
                                src: activity.images[i].url,
				style: "position: absolute; padding: 2pt"
                        }).appendTo(node);
			image.css("width", g_wImageCell);
			image.css("height", g_hImageCell);
			image.css("left", i * g_wImageCell + 10);

                        var indicator = $('<img>',{
				src: "/assets/icons/checked.png",
				style: "position: absolute;"
                        }).appendTo(node);
			indicator.css("width", 0.3 * g_wImageCell);
			indicator.css("height", 0.3 * g_hImageCell);
			indicator.css("left", i * g_wImageCell + 10);		

			var selector = new ImageSelector(activity.images[i].id, image, indicator);
			node.click(selector, function(evt) {
				evt.preventDefault();
				var selector = evt.data;
				var val = !(selector.checked);
				if (val && countImages(g_editor) >= g_imagesLimit) return;
				selector.checked = val;
				if (val) selector.indicator.show();
				else selector.indicator.hide();
                                setSavable();
                                setNonSubmittable();
				refreshAddButton(newImagesRow, g_editor);
			});
			imageSelectors.push(selector);
		}
	}

	newImagesRow.appendTo(ret);
	$('<br>').appendTo(ret);
	
	// Schedules
	var deadline = reformatDate(new Date());
	if(activity != null && activity.applicationDeadline != null) deadline = activity.applicationDeadline;
	var deadlinePicker = generateDateSelection(formatDate(deadline));
        deadlinePicker.on("input keyup change", function(evt){
            evt.preventDefault();
            setSavable();
            setNonSubmittable();
        });

	var beginTime = reformatDate(new Date());
	if(activity != null && activity.beginTime != null) beginTime = activity.beginTime;
	var beginTimePicker = generateDateSelection(formatDate(beginTime));
        beginTimePicker.on("input keyup change", function(evt){
            evt.preventDefault();
            setSavable();
            setNonSubmittable();
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

	// ActivityEditor(id, titleField, contentField, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete)
	g_editor = new ActivityEditor(activityId, titleInput, contentInput, newImageFiles, newImageNodes, imageSelectors, beginTimePicker, deadlinePicker, btnSave, btnSubmit, btnDelete);	

	refreshAddButton(newImagesRow, g_editor);

	return ret;
}

function generateDateSelection(time){
        var ret = generateDatePicker(time);
        ret.on("input change keyup", function(evt){
                evt.preventDefault();
                setSavable();
                setNonSubmittable();
        });
     	return ret;
}
