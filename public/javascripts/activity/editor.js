/* 
 * variables
 */

// general DOM elements
var g_activityEditor=null;
var g_modalActivityEditor=null;
var g_sectionActivityEditor=null;

// input-box keys
var g_classFieldActivityTitle="classFieldActivityTitle";
var g_classFieldActivityContent="classFieldActivityContent";
var g_classOldImage="classOldImage";
var g_classNewImage="classNewImage";

// button keys
var g_classBtnEdit="classBtnEdit";
var g_classBtnSubmit="classBtnSubmit";
var g_classBtnDelete="classBtnDelete";
var g_classBtnSave="classBtnSave";
var g_classBtnCancel="classBtnCancel";

// DOM indexes for cascaded DOM element search
var g_indexBtnEdit="indexBtnEdit";
var g_indexStatusIndicator="indexStatusIndicator";
var g_indexImageOfActivityPrefix="indexImageOfActivityPrefix";
var g_indexBeginTimePicker="beginTimePicker";
var g_indexDeadlinePicker="deadlinePicker";
var g_indexAssociatedImage="indexAssociatedImage";

var g_indexOldImage="indexOldImage";
var g_indexNewImage="indexNewImage";
var g_indexCheckbox="indexCheckbox";

// general variables
var g_maxNumberOfImagesForSingleActivity=3;
var g_savable=false;
var g_submittable=true;

// callback functions
var g_onEditorRemoved=null;
var g_onEditorCancelled=null;

// Assistive functions
function formatDigits(value, numberOfDigits){
       var valueStr=value.toString();
       while(valueStr.length<numberOfDigits) valueStr="0"+valueStr;
       return valueStr;
}

function formatDate(time){
	time=time.replace(/-/g,"/");
	var date=new Date(Date.parse(time));	
	var year=date.getFullYear();
	var month=date.getMonth()+1;
	var day=date.getDate();
	var hour=date.getHours();
	var min=date.getMinutes();
	return year+"-"+formatDigits(month, 2)+"-"+formatDigits(day, 2)+" "+formatDigits(hour, 2)+":"+formatDigits(min, 2);	
}

function reformatDate(date){
	var year=date.getFullYear();
	var month=date.getMonth()+1;
	var day=date.getDate();
	var hour=date.getHours();
	var min=date.getMinutes();
	return year+"-"+formatDigits(month, 2)+"-"+formatDigits(day, 2)+" "+formatDigits(hour, 2)+":"+formatDigits(min, 2);	
}

function removeActivityEditor(){
	do{	
		if(g_sectionActivityEditor==null) break;
		g_sectionActivityEditor.hide();
		g_sectionActivityEditor.modal("hide");
		if(g_modalActivityEditor==null) break;
		g_modalActivityEditor.empty();
		if(g_activityEditor==null) break;
		g_activityEditor.remove();
	}while(false);
}

function initActivityEditor(){
	var wrap=$("#wrap");
	/*
		Note: ALL attributes, especially the `class` attribute MUST be written INSIDE the div tag, bootstrap is NOT totally compatible with jQuery!!!
	*/
	g_sectionActivityEditor=$("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='Create an activity!' aria-hidden='true'>", {
		style: "height: 80%; position: absolute"
	}).appendTo(wrap);
	var dialog=$("<div>", {
		class: "modal-dialog modal-lg"
	}).appendTo(g_sectionActivityEditor);
	g_modalActivityEditor=$("<div>", {
		class: "modal-content"
	}).appendTo(dialog);
	removeActivityEditor();
}

function countSelectedImages(){

}

function isFileValid(file){
	var ret=false;
	do{
		var fileSizeLimit= (1<<20)// 2 mega bytes
		if(file.size>fileSizeLimit) break;
		ret=true;
	}while(false);
 	return ret;
}

function setNonSavable(){
    g_savable=false;
}

function setSavable(){
    g_savable=true;
}

function setNonSubmittable(){
    g_submittable=false;
}

function setSubmittable(){
    g_submittable=true;
}

// Assistive Callback Functions
function onSave(){
	do{
	    if(g_savable==false){
	        alert("You haven't made any changes!");
	        break;
	    }
		setNonSavable();
		setNonSubmittable();

		var formData = new FormData();

		// check files
		var newImages=g_activityEditor.children("."+g_classNewImage);
		var newImagesCount=newImages.length;
		for(var i=0;i<newImagesCount;i++){
		    do{
                var field=newImages[i];
                var checkbox=$(field).data(g_indexCheckbox);
                if(checkbox==null) break;
                // Note that checkbox.checked doesn't work here because of jQuery encapsulation!
                var isChecked=checkbox.is(':checked');
                if(isChecked==false) break;
                var files=field.files;
                var count=files.length;
                if(count==1) {
                    var file=files[0];
                    formData.append(g_indexNewImage+"-"+i.toString(), file);
                }
            }while(false);
		}

        var oldImages=g_activityEditor.children("."+g_classOldImage);
        var oldImagesCount=oldImages.length;
        var selectedOldImages=new Array();
        for(var i=0;i<oldImagesCount;i++){
            do{
                var field=oldImages[i];
                var checkbox=$(field).data(g_indexCheckbox);
                if(checkbox==null) break;
                // Note that checkbox.checked doesn't work here because of jQuery encapsulation!
                var isChecked=checkbox.is(':checked');
                if(isChecked==false) break;
                var imageId=$(field).data(g_keyId);
                selectedOldImages.push(imageId);
            }while(false);     
        }
        formData.append(g_indexOldImage, JSON.stringify(selectedOldImages));

		// append user token and activity id for identity
		var token = $.cookie(g_keyToken.toString());
		formData.append(g_keyToken, token);
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyContent, activityContent);

		// append activity begin time and deadline
		var beginTimePicker=g_activityEditor.data(g_indexBeginTimePicker);
		var beginTime=getDateTime(beginTimePicker);
		formData.append(g_keyBeginTime, beginTime);

		var deadlinePicker=g_activityEditor.data(g_indexDeadlinePicker);
		var deadline=getDateTime(deadlinePicker);
		formData.append(g_keyDeadline, deadline);

        var isNewActivity=false;
		var activityId = g_activityEditor.data(g_keyActivityId);
		if(activityId==null) isNewActivity=true;

		if(isNewActivity==false){
		    formData.append(g_keyActivityId, activityId.toString());
        }
		
		$.ajax({
			method: "POST",
			url: "/activity/save",
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false, // tell jQuery not to set contentType
			processData: false, // tell jQuery not to process the data
			success: function(data, status, xhr){
			    setSubmittable();
			    var jsonResponse=JSON.parse(data);
			    if(jsonResponse.hasOwnProperty(g_keyId)){
			        g_activityEditor.data(g_keyActivityId, jsonResponse[g_keyId]);
			    }
                alert("You can submit the application now!");
			},
			error: function(xhr, status, err){
				setSavable();
			}
		});
	}while(false);
}

function onSubmit(){
	do{
        if(g_submittable==false) {
            alert("You have to save your changes before submission!");
            break;
        }
		setNonSavable();
		setNonSubmittable();

		var params={};

		// append user token and activity id for identity
		var token = $.cookie(g_keyToken.toString());
		params[g_keyToken]=token;

		var activityId = g_activityEditor.data(g_keyActivityId);
		params[g_keyActivityId]=activityId.toString();

		$.ajax({
			method: "PUT",
			url: "/activity/submit",
			data: params,
			success: function(data, status, xhr){
				removeActivityEditor();
				if(g_OnEditorRemoved!=null){
					g_onEditorRemoved(0, g_numItemsPerPage, g_directionForward);
				}
			},
			error: function(xhr, status, err){
				setSubmittable();
			}
		});
	}while(false);
}

function previewImage(input) {
    do{
        var images=input.files;
        if (images==null) break;
        var image=images[0];
        if(image==null) break;
        var count=images.length;
        if(count==0 || count>1){
            alert("Choose only 1 image at a time!!!");
            break;
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
            checkbox.on("change", function(){
				setSavable();
                setNonSubmittable();
            });
            $(input).after(node);
            $(input).data(g_indexAssociatedImage, node);
            $(input).data(g_indexCheckbox, checkbox);
        }

        reader.readAsDataURL(image);
    }while(false);
}

function onbtnSaveClicked(evt){

	evt.preventDefault();

	try{
	    onSave();
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){

	evt.preventDefault();
	
	var activityId=$(this).data(g_keyActivityId);
	var token=$.cookie(g_keyToken).toString();

	var params={};
	params[g_keyActivityId]=activityId;
	params[g_keyToken]=token;

	try{
	    $.ajax({
	        type: "POST",
	        url: "/activity/delete",
	        data: params,
	        success: function(data, status, xhr){
                 g_activityEditor.remove();
                 if(g_onEditorRemoved!=null){
                     g_onEditorRemoved(0);
                 }
				 location.reload();
            },
	        error: function(xhr, status, err){

	        }
	    });
	} catch(err){
		
	}
}

function onBtnSubmitClicked(evt){

	evt.preventDefault();

	try{
        onSubmit();
	} catch(err){
		
	}
}

function onBtnCancelClicked(evt){
	evt.preventDefault();
	removeActivityEditor();
	if(g_onEditorCancelled!=null){
		g_onEditorCancelled();	
	}
}

// Generators
function generateActivityEditorByJson(activityJson){
	setNonSavable();
	setSubmittable();
	var isNewActivity=false;
	if(activityJson==null) isNewActivity=true;

	var activityId=null;
	var activityTitle="";
	var activityContent="";
	var activityImages=null;

	if(isNewActivity==false) {
		activityId=activityJson[g_keyId];
		activityTitle=activityJson[g_keyTitle];
		activityContent=activityJson[g_keyContent];
		activityImages=activityJson[g_keyImages];
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
		value: activityTitle,
		name: g_keyTitle
	}).appendTo(ret);

	titleInput.on("input paste keyup", function(){
		setSavable();
		setNonSubmittable();
	});

	var contentText=$('<p>', {
		text: "Content",
		style: "margin-top: 5pt"
	}).appendTo(ret);

	var contentInput=$('<textarea>',
	{
		class: g_classFieldActivityContent, 
		name: g_keyContent
	}).appendTo(ret);
	contentInput.val(activityContent);
	contentInput.on("input paste keyup", function(){
		setSavable();
		setNonSubmittable();
	});

	do{
		if(activityImages==null) break;

		for(var key in activityImages){
			if(activityImages.hasOwnProperty(key)){
				var node=$('<p>',{
					class: g_classOldImage
				}).appendTo(ret);
				var activityImage=activityImages[key];
				var imageUrl=activityImage[g_keyUrl];
				var imageNode=$('<img>',{
					src: imageUrl.toString()   
				}).appendTo(node);
				var checkbox=$('<input>',{
					type: "checkbox",
					checked: true
				}).appendTo(node);
				checkbox.on("change", function(){
					setSavable();
					setNonSubmittable();
				});
				var imageId=activityImage[g_keyId];
				node.data(g_keyId, imageId);
				node.data(g_indexCheckbox, checkbox);
			}
		}
	}while(false);

	for (var i = 0; i < g_maxNumberOfImagesForSingleActivity; i++) {
		var lineBreak=$('<br>').appendTo(ret);
		var imageField=$('<input>', {
			class: g_classNewImage,
			type: 'file'
		}).appendTo(ret);
		imageField.on("change", function(){
			setSavable();
			setNonSubmittable();
			previewImage(this);
		});
	}

	// Schedules
	var deadline=reformatDate(new Date());
	if(activityJson!=null && activityJson.hasOwnProperty(g_keyDeadline)) deadline=activityJson[g_keyDeadline];
	var deadlinePicker=generateDateSelection(formatDate(deadline));
	ret.data(g_indexDeadlinePicker, deadlinePicker);

	var beginTime=reformatDate(new Date());
	if(activityJson!=null && activityJson.hasOwnProperty(g_keyBeginTime)) beginTime=activityJson[g_keyBeginTime];
	var beginTimePicker=generateDateSelection(formatDate(beginTime)); 
	ret.data(g_indexBeginTimePicker, beginTimePicker);

	var tableSchedule=$("<table>", {
		style: "display: block; margin-top: 15pt; margin-bottom: 5pt"
	}).appendTo(ret);
	var scheduleRow1=$("<tr>").appendTo(tableSchedule);
	var scheduleCell11=$("<td>", {
		text: "Deadline: ",
		style: "white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow1);
	var scheduleCell12=$("<td>", {

	}).appendTo(scheduleRow1);
	scheduleCell12.append(deadlinePicker);

	var scheduleRow2=$("<tr>").appendTo(tableSchedule);
	var scheduleCell21=$("<td>", {
		text: "Begin Time: ",
		style: "white-space: nowrap; vertical-align: text-top"
	}).appendTo(scheduleRow2);
	var scheduleCell22=$("<td>", {

	}).appendTo(scheduleRow2);
	scheduleCell22.append(beginTimePicker);	

	var buttons=$("<p>", {
		style: "display: block; clear: both; margin-top: 5pt"
	}).appendTo(ret);

	/* Associated Buttons */
	var btnSave=$('<button>',{
		class: g_classBtnSave,
		text: 'Save'
	}).appendTo(buttons);
	btnSave.on("click", onbtnSaveClicked);

	var btnSubmit=$('<button>',{
		class: g_classBtnSubmit,
		text: 'Submit'
	}).appendTo(buttons);
	btnSubmit.on("click", onBtnSubmitClicked);

	var btnCancel=$('<button>',{
		class: g_classBtnCancel,
		text: 'Cancel'
	}).appendTo(buttons);
	btnCancel.on("click", onBtnCancelClicked);

	if(isNewActivity==false){
		var btnDelete=$('<button>',{
			class: g_classBtnDelete,
			text: 'Delete'
		}).appendTo(buttons);

		btnDelete.on("click", onBtnDeleteClicked);
	}
	ret.data(g_keyActivityId, activityId);			
	return ret;
}

function generateDateSelection(time){
		var ret=generateDataPicker(time);
		ret.on("input change keyup", function(){
			setSavable();
			setNonSubmittable();
		});
     	return ret;
}
