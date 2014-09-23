/* 
 * variables
 */

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
var g_indexDeadlinePicker = "deadline-picker"
var g_indexBeginTimePicker = "begin-time-picker";

var g_indexOldImagesNodes = "old_images_nodes";
var g_indexNewImages = "new_images";

var g_indexOldImage = "old_image";
var g_indexNewImage = "new_image";

var g_indexCheckbox = "checkbox";

var g_wImageCell = 72;
var g_hImageCell = 72;

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

        g_activityEditor = generateActivityEditor(activity);
        g_modalActivityEditor.empty();
        g_modalActivityEditor.append(g_activityEditor);

        g_sectionActivityEditor.css("position", "absolute");
        g_sectionActivityEditor.css("height", "90%");
        g_sectionActivityEditor.css("padding", "5pt");
        g_sectionActivityEditor.modal({
                show: true
        });
}

function countImages(newImagesNodes, oldImagesNodes){
	var ret = Object.keys(newImagesNodes).length;
	for(var key in oldImagesNodes) {
		var node = oldImagesNodes[key];
		var checkbox = node.data(g_indexCheckbox);
		if(!check.is(":checked")) continue;
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

function setNonSavable(){
    g_savable = false;
}

function setSavable(){
    g_savable = true;
}

function setNonSubmittable(){
    g_submittable = false;
}

function setSubmittable(){
    g_submittable = true;
}

// Assistive Callback Functions
function onSave(evt){
	evt.preventDefault();

        if(!g_savable){
            alert("You haven't made any changes!");
            return;
        }

        var data = evt.data;

        var formData = new FormData();
	
	// check files
        var newImages = data[g_indexNewImages];
        for(var key in newImages){
            var file = newImages[key];
            formData.append(g_indexNewImage + "-" + i.toString(), file);
        }

        var oldImagesNodes = data[g_indexOldImagesNodes]
        var oldImagesCount = oldImagesNodes.length;
        var selectedOldImages = new Array();
        for(var i = 0; i < oldImagesCount; i++){
            var node = oldImagesNodes[i];
            var checkbox = node.data(g_indexCheckbox);
            if(checkbox == null || !checkbox.is(":checked")) continue;
            var imageId = node.data(g_keyImageId);
            selectedOldImages.push(imageId);
        }
        formData.append(g_indexOldImage, JSON.stringify(selectedOldImages));

        // append user token and activity id for identity
        var token = $.cookie(g_keyToken.toString());
	if(token == null) {
		alert("Are you logged out?");
		return;
	}
        formData.append(g_keyToken, token);

        // append activity title and content
        var title = data[g_keyTitle].val();
	var content = data[g_keyContent].val();
	if(title == null || content == null || title == "" || content == "") {
		alert("Neither title nor content could be empty.");
		return;
	}
        formData.append(g_keyTitle, data[g_keyTitle].val());
        formData.append(g_keyContent, data[g_keyContent].val());

        // append activity begin time and deadline
        var beginTimePicker = data[g_indexBeginTimePicker];
        var beginTime = getDateTime(beginTimePicker);

        var deadlinePicker = data[g_indexDeadlinePicker];
        var deadline = getDateTime(deadlinePicker);
		
	if(compareYmdhisDate(deadline, beginTime) > 0) {
		alert("Deadline can not be after the begin time of an activity.");
		return;
	}
        formData.append(g_keyBeginTime, beginTime);
        formData.append(g_keyDeadline, deadline);

        var isNewActivity = false;
	var activityId = (data.hasOwnProperty(g_keyActivityId) && data[g_keyActivityId] != null) ? data[g_keyActivityId] : g_activityEditor.data(g_keyActivityId);
        if(activityId == null) isNewActivity = true;

        if(!isNewActivity)	formData.append(g_keyActivityId, activityId.toString());

        setNonSavable();
        setNonSubmittable();

	if(g_loadingWidget == null) g_loadingWidget = createModal($("#wrap"), "Saving changes, please wait or click shadow area to dismiss", 80, 20);
		
	showModal(g_loadingWidget);

        $.ajax({
                method: "POST",
                url: "/activity/save",
                data: formData,
                mimeType: "mutltipart/form-data",
                contentType: false, // tell jQuery not to set contentType
                processData: false, // tell jQuery not to process the data
                success: function(data, status, xhr){
			hideModal(g_loadingWidget);
			setSubmittable();
			var jsonResponse = JSON.parse(data);
			if(jsonResponse.hasOwnProperty(g_keyActivityId)) {
				alert("Activity created!");
				g_activityEditor.data(g_keyActivityId, parseInt(jsonResponse[g_keyActivityId]));
			} else {
				alert("Changes saved.");
			}
			if(g_onActivitySaveSuccess == null) return;
			g_onActivitySaveSuccess();
                },
                error: function(xhr, status, err){
			hideModal(g_loadingWidget)
                        setSavable();
                }
        });
}

function onSubmit(evt){

	evt.preventDefault();

        if(!g_submittable) {
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
	
	var activityId = (data.hasOwnProperty(g_keyActivityId) && data[g_keyActivityId] != null) ? data[g_keyActivityId] : g_activityEditor.data(g_keyActivityId);
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

function previewImage(par, newImages, newImagesNodes, oldImagesNodes) {
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

		var offset = Object.keys(newImages).length;
		newImages[key] = newImage; // add new image to model map
		var node = $('<span>', {
			style: "position: absolute; padding: 2pt"
		}).appendTo(par);
		node.css("width", g_wImageCell);
		node.css("height", g_hImageCell);
		node.css("left", offset * g_wImageCell);
		newImagesNodes[key] = node; // add new image node to view map		

		var img = $('<img>', {
			src: e.target.result
		}).appendTo(node);
		img.css("width", g_wImageCell);
		img.css("height", g_hImageCell);

		var btnDelete = $("<button>", {
			text: "delete",
			style: "color: white; background-color: red"
		}).appendTo(node);
		
		refreshAddButton(par, newImages, newImagesNodes, oldImagesNodes);
		
		btnDelete.click(function(evt){
			evt.preventDefault();
			setSavable();
			setNonSubmittable();
			if(!newImages.hasOwnProperty(key)) return;
			delete newImages[key];	
			if(!newImagesNodes.hasOwnProperty(key)) return;
			newImagesNodes[key].remove();
			delete newImagesNodes[key];
			for(var otherKey in newImagesNodes) {
				if(otherKey < key) continue;
				var newImageNode = newImagesNodes[otherKey];	
				var l = parseInt(newImageNode.css("left"));
				newImageNode.css("left", l - g_wImageCell);
			}
			refreshAddButton(par, newImages, newImagesNodes, oldImagesNodes);
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

function refreshAddButton(par, newImages, newImagesNodes, oldImagesNodes) {
	
	removeAddButton();

	var offset = Object.keys(newImages).length;

	g_nodeBtnAdd = $("<span>", {
		style: "display:inline-block; position: absolute; cursor: pointer"
	}).appendTo(par);
	g_nodeBtnAdd.css("height", g_hImageCell);
	g_nodeBtnAdd.css("width", g_wImageCell);
	g_nodeBtnAdd.css("left", offset * g_wImageCell);	

	var picBtnAdd = $("<img>", {
		src: "/assets/icons/add.png",
		style: "position: absolute;"
	}).appendTo(g_nodeBtnAdd);
	picBtnAdd.css("height", g_hImageCell);
	picBtnAdd.css("width", g_wImageCell);

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
		previewImage(par, newImages, newImagesNodes, oldImagesNodes);
	});

	var imagesTot = countImages(newImagesNodes, oldImagesNodes);
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

	var ret=$('<form>', {
		style: "display: block; padding: 5pt"
	});

	var titleText=$('<p>', {
		text: "Title",
		style: "margin-top: 5pt"	
	}).appendTo(ret);

	var titleInput=$('<input>', {
		class: "input-title",
		type: 'text',
		value: activityTitle
	}).appendTo(ret);

	titleInput.on("input paste keyup", function(evt){
	        evt.preventDefault();
		setSavable();
		setNonSubmittable();
		activityTitle = $(this).val();
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
		activityContent = $(this).val();
	});

        var oldImagesNodes = new Array();
	if(activity != null && activity.images != null) {
		var oldImagesRow = $("<p>", {
			style: "overflow-x: auto; height: 75pt"	
		}).appendTo(ret);
		for(var key in activity.images){
			var node = $("<span>").appendTo(oldImagesRow);
                        var img = activity.images[key];
                        var imageNode = $('<img>',{
                                src: img.url
                        }).appendTo(node);
                        var checkbox = $('<input>',{
                                type: "checkbox",
                                checked: true
                        }).appendTo(node);
                        checkbox.on("change", function(){
                                setSavable();
                                setNonSubmittable();
                        });
                        node.data(g_keyImageId, img.id);
                        node.data(g_indexCheckbox, checkbox);
                        oldImagesNodes.push(node);
		}
	}

	$('<br>').appendTo(ret);

        var newImages = {};
	var newImagesRow = $("<p>", {
		style: "overflow-x: auto; margin-left: 5pt; height: 75pt"
	}).appendTo(ret);
	
	var newImagesNodes = {};
	refreshAddButton(newImagesRow, newImages, newImagesNodes, oldImagesNodes);
	
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
	var dSave = {};
	dSave[g_keyActivityId] = activityId;
        dSave[g_keyTitle] = titleInput;
        dSave[g_keyContent] = contentInput;
        dSave[g_indexDeadlinePicker] = deadlinePicker;
        dSave[g_indexBeginTimePicker] = beginTimePicker;
        dSave[g_indexOldImagesNodes] = oldImagesNodes;
        dSave[g_indexNewImages] = newImages;
	btnSave.on("click", dSave, onSave);

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

	if(!isNewActivity){
		var btnDelete = $('<button>',{
			class: g_classBtnDelete,
			text: 'Delete'
		}).appendTo(buttons);
                var dDelete = {};
                dDelete[g_keyActivityId] = activityId;
		btnDelete.on("click", dDelete, onDelete);
	}
	ret.data(g_keyActivityId, activityId);			
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
