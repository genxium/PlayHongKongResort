/* 
 * variables
 */

// general DOM elements
var g_activityEditor = null;
var g_modalActivityEditor = null;
var g_sectionActivityEditor = null;

// input-box keys
var g_classFieldActivityTitle = "classFieldActivityTitle";
var g_classFieldActivityContent = "classFieldActivityContent";

// button keys
var g_classBtnEdit = "classBtnEdit";
var g_classBtnSubmit = "classBtnSubmit";
var g_classBtnDelete = "classBtnDelete";
var g_classBtnSave = "classBtnSave";
var g_classBtnCancel = "classBtnCancel";

// DOM indexes for cascaded DOM element search
var g_indexImageOfActivityPrefix = "indexImageOfActivityPrefix";
var g_indexDeadlinePicker = "deadlinePicker"
var g_indexBeginTimePicker = "beginTimePicker";
var g_indexAssociatedImage = "indexAssociatedImage";

var g_indexOldImages = "old_images";
var g_indexNewImages = "new_images";

var g_indexOldImage = "old_image";
var g_indexNewImage = "new_image";

var g_indexCheckbox = "checkbox";

// general variables
var g_maxNumberOfImagesForSingleActivity=3;
var g_savable = false;
var g_submittable = true;

// callback functions
var g_onEditorRemoved = null;
var g_onEditorCancelled = null;

var g_onQueryActivitiesSuccess = null;
var g_onQueryActivitiesError = null;

// Assistive functions
function formatDigits(value, numberOfDigits){
       var valueStr = value.toString();
       while(valueStr.length<numberOfDigits) valueStr="0"+valueStr;
       return valueStr;
}

function formatDate(time){
	time=time.replace(/-/g,"/");
	var date = new Date(Date.parse(time));
	var year = date.getFullYear();
	var month = date.getMonth()+1;
	var day = date.getDate();
	var hour = date.getHours();
	var min = date.getMinutes();
	return year+"-"+formatDigits(month, 2)+"-"+formatDigits(day, 2)+" "+formatDigits(hour, 2)+":"+formatDigits(min, 2);	
}

function reformatDate(date){
	var year = date.getFullYear();
	var month = date.getMonth()+1;
	var day = date.getDate();
	var hour = date.getHours();
	var min = date.getMinutes();
	return year+"-"+formatDigits(month, 2)+"-"+formatDigits(day, 2)+" "+formatDigits(hour, 2)+":"+formatDigits(min, 2);	
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
	var wrap=$("#wrap");
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
	removeActivityEditor();
}

function countSelectedImages(){

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

        if(!g_savable){
            alert("You haven't made any changes!");
            return;
        }

        var data = evt.data;

        setNonSavable();
        setNonSubmittable();

        var formData = new FormData();

        // check files
        var newImages = data[g_indexNewImages];
        var newImagesCount = newImages.length;
        for(var i = 0; i < newImagesCount; i++){
            var field = newImages[i][0]; // pull DOM element
            var checkbox = $(field).data(g_indexCheckbox);
            if(checkbox == null) continue;
            // Note that checkbox.checked doesn't work here because of jQuery encapsulation!
            if(!checkbox.is(':checked')) continue;
            var files = field.files;
            var count = files.length;
            if(count != 1) continue;
            var file = files[0];
            formData.append(g_indexNewImage+"-"+i.toString(), file);
        }

        var oldImages = data[g_indexOldImages]
        var oldImagesCount = oldImages.length;
        var selectedOldImages = new Array();
        for(var i = 0; i < oldImagesCount; i++){
            var field = oldImages[i][0]; // pull DOM element
            var checkbox = $(field).data(g_indexCheckbox);
            if(checkbox == null) continue;
            // Note that checkbox.checked doesn't work because of jQuery encapsulation!
            if(!checkbox.is(':checked')) continue;
            var imageId = $(field).data(g_keyImageId);
            selectedOldImages.push(imageId);
        }
        formData.append(g_indexOldImage, JSON.stringify(selectedOldImages));

        // append user token and activity id for identity
        var token = $.cookie(g_keyToken.toString());
        formData.append(g_keyToken, token);

        // append activity title and content
        formData.append(g_keyTitle, data[g_keyTitle].val());
        formData.append(g_keyContent, data[g_keyContent].val());

        // append activity begin time and deadline
        var beginTimePicker = data[g_indexBeginTimePicker];
        var beginTime = getDateTime(beginTimePicker);
        formData.append(g_keyBeginTime, beginTime);

        var deadlinePicker = data[g_indexDeadlinePicker];
        var deadline = getDateTime(deadlinePicker);
        formData.append(g_keyDeadline, deadline);

        var isNewActivity = false;
	var activityId = (data.hasOwnProperty(g_keyActivityId) && data[g_keyActivityId] != null) ? data[g_keyActivityId] : g_activityEditor.data(g_keyActivityId);
        if(activityId == null) isNewActivity = true;

        if(!isNewActivity)	formData.append(g_keyActivityId, activityId.toString());

        $.ajax({
                method: "POST",
                url: "/activity/save",
                data: formData,
                mimeType: "mutltipart/form-data",
                contentType: false, // tell jQuery not to set contentType
                processData: false, // tell jQuery not to process the data
                success: function(data, status, xhr){
                    setSubmittable();
                    var jsonResponse = JSON.parse(data);
                    if(jsonResponse.hasOwnProperty(g_keyActivityId)) {
                        alert("Activity created!");
			g_activityEditor.data(g_keyActivityId, parseInt(jsonResponse[g_keyActivityId]));
                    } else {
                        alert("Changes saved.");
                    }
                },
                error: function(xhr, status, err){
                        setSavable();
                }
        });
}

function onSubmit(evt){

        if(!g_submittable) {
            alert("You have to save your changes before submission!");
            return;
        }

        var data = evt.data;

        setNonSavable();
        setNonSubmittable();

        var params = {};

        // append user token and activity id for identity
        var token = $.cookie(g_keyToken);
        params[g_keyToken] = token;
	
	var activityId = (data.hasOwnProperty(g_keyActivityId) && data[g_keyActivityId] != null) ? data[g_keyActivityId] : g_activityEditor.data(g_keyActivityId);
        params[g_keyActivityId] = activityId;

        $.ajax({
                method: "PUT",
                url: "/activity/submit",
                data: params,
                success: function(data, status, xhr){
                        removeActivityEditor();
                        if(g_OnEditorRemoved == null) return;
            			var refIndex = 0; // temporarily hardcoded
            			var relation = g_activitiesFilter.val();
            			var order = g_activitiesSorter.val();
                        g_onEditorRemoved(refIndex, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, g_onQueryActivitiesSuccess, g_onQueryActivitiesError);
                },
                error: function(xhr, status, err){
                        setSubmittable();
                }
        });
}

function previewImage(input) {
        var images=input.files;
        if (images == null) return;
        var image = images[0];
        if(image == null) return;
        var count = images.length;
        if(count == 0 || count > 1){
            alert("Choose only 1 image at a time!!!");
            return;
        }
        var reader = new FileReader();

        reader.onload = function (e) {
            var legacy=$(input).data(g_indexAssociatedImage);
            if(legacy!=null){
                legacy.remove();
                $(input).removeData(g_indexAssociatedImage);
            }

            var node=$('<p>');
            var imageNode=$('<img>', {
                   src: e.target.result
            }).appendTo(node);
            var checkbox=$('<input>',{
                   type: "checkbox",
                   checked: true
            }).appendTo(node);

            checkbox.on("change", function(evt){
                evt.preventDefault();
                setSavable();
                setNonSubmittable();
            });
            $(input).after(node);
            $(input).data(g_indexAssociatedImage, node);
            $(input).data(g_indexCheckbox, checkbox);
        }

        reader.readAsDataURL(image);
}

function onBtnSaveClicked(evt){
	evt.preventDefault();
        onSave(evt);
}

function onBtnDeleteClicked(evt){

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
                    var refIndex = 0; // temporarily hardcoded
                    var relation = g_activitiesFilter.val();
                    var order = g_activitiesSorter.val();
                    g_onEditorRemoved(refIndex, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, g_onQueryActivitiesSuccess, g_onQueryActivitiesError);
                },
	        error: function(xhr, status, err){

	        }
	    });
	} catch(err){
		
	}
}

function onBtnSubmitClicked(evt){
	evt.preventDefault();
        onSubmit(evt);
}

function onBtnCancelClicked(evt){
	evt.preventDefault();
	removeActivityEditor();
	if(g_onEditorCancelled == null) return;
        g_onEditorCancelled();
}

// Generators
function generateActivityEditor(activity){
	setNonSavable();
	setSubmittable();
	var isNewActivity = false;
	if(activity == null || activity.id == null) isNewActivity = true;

	var activityId = null;
	var activityTitle = "";
	var activityContent = "";
	var activityImages = null;

	if(!isNewActivity) {
		activityId = activity.id;
		activityTitle = activity.title;
		activityContent = activity.content;
		activityImages = activity.images;
	}

	var ret=$('<form>', {
		style: "display: block; padding: 5pt"
	});

	var titleText=$('<p>', {
		text: "Title",
		style: "margin-top: 5pt"	
	}).appendTo(ret);

	var titleInput=$('<input>', {
		class: g_classFieldActivityTitle,
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

	var contentInput = $('<textarea>',
	{
		class: g_classFieldActivityContent
	}).appendTo(ret);
	contentInput.val(activityContent);
	contentInput.on("input paste keyup", function(evt){
	        evt.preventDefault();
		setSavable();
		setNonSubmittable();
		activityContent = $(this).val();
	});

        var oldImages = new Array();
	if(activityImages != null) {

		for(var key in activityImages){
                        var node=$('<p>').appendTo(ret);
                        var img = activityImages[key];
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
                        oldImages.push(node);
		}
	}

        var newImages = new Array();
	for (var i = 0; i < g_maxNumberOfImagesForSingleActivity; i++) {
		$('<br>').appendTo(ret);
		var imageField = $('<input>', {
			type: 'file'
		}).appendTo(ret);
		imageField.on("change", function(evt){
		        evt.preventDefault();
			setSavable();
			setNonSubmittable();
			previewImage(this);
		});
		newImages.push(imageField);
	}

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
        dSave[g_indexOldImages] = oldImages;
        dSave[g_indexNewImages] = newImages;
	btnSave.on("click", dSave, onBtnSaveClicked);

	var btnSubmit = $('<button>',{
		class: g_classBtnSubmit,
		text: 'Submit'
	}).appendTo(buttons);
	var dSubmit = {};
	dSubmit[g_keyActivityId] = activityId;
	btnSubmit.on("click", dSubmit, onBtnSubmitClicked);

	var btnCancel = $('<button>',{
		class: g_classBtnCancel,
		text: 'Cancel'
	}).appendTo(buttons);
	btnCancel.on("click", onBtnCancelClicked);

	if(!isNewActivity){
		var btnDelete = $('<button>',{
			class: g_classBtnDelete,
			text: 'Delete'
		}).appendTo(buttons);
                var dDelete = {};
                dDelete[g_keyActivityId] = activityId;
		btnDelete.on("click", dDelete, onBtnDeleteClicked);
	}
	ret.data(g_keyActivityId, activityId);			
	return ret;
}

function generateDateSelection(time){
        var ret=generateDataPicker(time);
        ret.on("input change keyup", function(evt){
                evt.preventDefault();
                setSavable();
                setNonSubmittable();
        });
     	return ret;
}
