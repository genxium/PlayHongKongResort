// Assistive functions
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

function formatDigits(value, numberOfDigits){
	var valueStr=value.toString();
	while(valueStr.length<numberOfDigits) valueStr="0"+valueStr;
	return valueStr;
}

function setNonSubmittable(){
    g_submittable=false;
}

function setSubmittable(){
    g_submittable=true;
}

// Assistive Callback Functions
function onUpdateFormSubmission(editor){
	do{
		var formObj = $(editor);
		var formData = new FormData();

		// check files
		var newImages=formObj.children("."+g_classNewImage);
		var newImagesCount=newImages.length;
		for(var i=0;i<newImagesCount;i++){
		    do{
                var field=newImages[i];
                var checkbox=$(field).data(g_indexCheckbox);
                if(checkbox==null) break;
                // Note that checkbox.checked doesn't work here because of jQuery encapsulation!
                var isChecked=checkbox.is(':checked');
                if(isChecked.checked==false) break;
                var files=field.files;
                var count=files.length;
                if(count==1) {
                    var file=files[0];
                    formData.append(g_indexNewImage+"-"+i.toString(), file);
                }
            }while(false);
		}

        var oldImages=formObj.children("."+g_classOldImage);
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
                var imageId=$(field).data(g_keyImageId);
                selectedOldImages.push(imageId);
            }while(false);     
        }
        formData.append(g_indexOldImage, JSON.stringify(selectedOldImages));

		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = formObj.data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyActivityTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyActivityContent, activityContent);

		// append activity begin time and deadline
		var sectionBeginTime=formObj.data(g_indexSectionBeginTime);
		var beginTimeNodes=sectionBeginTime.children();
		var beginTimeYear=formatDigits(beginTimeNodes[0].value, 4);
		var beginTimeMonth=formatDigits(beginTimeNodes[1].value, 2);
		var beginTimeDay=formatDigits(beginTimeNodes[2].value, 2);
		var beginTimeHour=formatDigits(beginTimeNodes[3].value, 2);
		var beginTimeMinute=formatDigits(beginTimeNodes[4].value, 2);
		var beginTime=beginTimeYear+"-"+beginTimeMonth+"-"+beginTimeDay+" "+beginTimeHour+":"+beginTimeMinute+":00";
		formData.append(g_keyActivityBeginTime, beginTime);

		var sectionDeadline=formObj.data(g_indexSectionDeadline);
		var deadlineNodes=sectionDeadline.children();
		var deadlineYear=formatDigits(deadlineNodes[0].value, 4);
		var deadlineMonth=formatDigits(deadlineNodes[1].value, 2);
		var deadlineDay=formatDigits(deadlineNodes[2].value, 2);
		var deadlineHour=formatDigits(deadlineNodes[3].value, 2);
		var deadlineMinute=formatDigits(deadlineNodes[4].value, 2);
		var deadline=deadlineYear+"-"+deadlineMonth+"-"+deadlineDay+" "+deadlineHour+":"+deadlineMinute+":00";
		formData.append(g_keyActivityDeadline, deadline);

		$.ajax({
			method: "PUT",
			url: "/activity/update",
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false, // tell jQuery not to set contentType
			processData: false, // tell jQuery not to process the data
			success: function(data, status, xhr){
			    setSubmittable();
                alert("You can submit the application now!");
			},
			error: function(xhr, status, err){

			}
		});
	}while(false);
}

function onSubmitFormSubmission(editor){
	do{
        if(g_submittable==false) {
            alert("You have to update the activity before submission!");
            break;
        }
		var formObj = $(editor);
		var params={};

		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		params[g_keyUserToken]=token;

		var activityId = formObj.data(g_keyActivityId);
		params[g_keyActivityId]=activityId.toString();

		$.ajax({
			method: "PUT",
			url: "/activity/submit",
			data: params,
			success: function(data, status, xhr){
				formObj.remove();
				if(g_callbackOnActivityEditorRemoved!=null){
					g_callbackOnActivityEditorRemoved(0, g_numItemsPerPage, g_directionForward);
				}
			},
			error: function(xhr, status, err){

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
            checkbox.change(function(){
                setNonSubmittable();
            });
            $(input).after(node);
            $(input).data(g_indexAssociatedImage, node);
            $(input).data(g_indexCheckbox, checkbox);
        }

        reader.readAsDataURL(image);
    }while(false);
}

// Assistive Handlers
function onBtnCreateClicked(evt){

	evt.preventDefault();
	$(this).hide();

	sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	var token=$.cookie(g_keyLoginStatus.toString());

	var params={};
	params[g_keyUserToken]=token;

	try{
        $.ajax({
            type: "POST",
            url: "/activity/create",
            data: params,
            success: function(data, status, xhr){
                var activityJson=JSON.parse(data);
                var editor=generateActivityEditorByJson(activityJson);
                sectionActivityEditor.append(editor);
            },
            error: function(xhr, status, errThrown){

            }
        });
	} catch(err){

	}
}

function onBtnUpdateClicked(evt){

	evt.preventDefault();
	var editor=$(this).parent();

	try{
	    onUpdateFormSubmission(editor);
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){

	evt.preventDefault();
	
	var activityId=$(this).data(g_keyActivityId);
	var token=$.cookie(g_keyLoginStatus.toString());
	var editor=$(this).parent();

    var params={};
    params[g_keyActivityId]=activityId.toString();
    params[g_keyUserToken]=token.toString();

	try{
	    $.ajax({
	        type: "POST",
	        url: "activity/delete",
	        data: params,
	        success: function(data, status, xhr){
                 editor.remove();
                 if(g_callbackOnActivityEditorRemoved!=null){
                     g_callbackOnActivityEditorRemoved(0);
                 }
            }
	        error: function(xhr, status, err){

	        }
	    });
	} catch(err){
		
	}
}

function onBtnSubmitClicked(evt){

	evt.preventDefault();
	var editor=$(this).parent();

	try{
        onSubmitFormSubmission(editor);
	} catch(err){
		
	}
}

function onBtnCancelClicked(evt){
	evt.preventDefault();
	var editor=$(this).parent();
	editor.remove();
	$("#"+g_idBtnCreate).show();
}

// Generators
function generateActivityEditorByJson(activityJson){

     setSubmittable();

	 var activityId=activityJson[g_keyActivityId];
	 var activityTitle=activityJson[g_keyActivityTitle];
	 var activityContent=activityJson[g_keyActivityContent];
	 var activityImages=activityJson[g_keyActivityImages];

	 var ret=$('<form>',
	 			{
	 				class: g_classActivityEditor	
	 			});

	 var titleText=$('<p>',
	 			 {
	 			 	html: 'Title'
				 }).appendTo(ret);

	 var titleInput=$('<input>',
	 				{
		 				class: g_classFieldActivityTitle,
		 				type: 'text',
		 				value: activityTitle,
		 			 	name: g_keyActivityTitle
	 				}).appendTo(ret);
    titleInput.change(function(){
        setNonSubmittable();
    });

	 var contentText=$('<p>',
	 			   {
				   		html: 'Content'
 				   }).appendTo(ret);

	 var contentInput=$('<textarea>',
	 				  {
	 				  	class: g_classFieldActivityContent, 
	 				  	name: g_keyActivityContent
	 				  }).appendTo(ret);
	 contentInput.val(activityContent);
     contentInput.change(function(){
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
               var imageUrl=activityImage[g_keyImageURL];
               var imageNode=$('<img>',{
                    src: imageUrl.toString()   
               }).appendTo(node);
               var checkbox=$('<input>',{
                   type: "checkbox",
                   checked: true
               }).appendTo(node);
               checkbox.change(function(){
                   setNonSubmittable();
               });
               var imageId=activityImage[g_keyImageId];
               node.data(g_keyImageId, imageId);
               node.data(g_indexCheckbox, checkbox);
           }
        }
     }while(false);

	 for (var i = 0; i < g_maxNumberOfImagesForSingleActivity; i++) {
	 	var lineBreak=$('<br>').appendTo(ret);
	 	var imageField=$('<input>',
				 		{
				 			class: g_classNewImage,
							type: 'file'
				 		}).appendTo(ret);
		imageField.change(function(){
		    setNonSubmittable();
            previewImage(this);
        });
	 }

	 // schedules
     var sectionBeginTime=generateBeginTimeSelection("Begin Time: ", g_classSelectionBeginTime); 
     ret.append(sectionBeginTime);
     ret.data(g_indexSectionBeginTime, sectionBeginTime);
     
     var sectionDeadline=generateDeadlineSelection("Deadline: ", g_classSelectionDeadline);
     ret.append(sectionDeadline);
     ret.data(g_indexSectionDeadline, sectionDeadline);

	 /* Associated Buttons */
	 var btnUpdate=$('<button>',{
	 					class: g_classBtnUpdate,
	 					text: 'Update' 
	 				}).appendTo(ret);
	 btnUpdate.data(g_keyActivityId, activityId);
	 btnUpdate.bind("click", onBtnUpdateClicked);

	 var btnDelete=$('<button>',{
	 					class: g_classBtnDelete,
	 					text: 'Delete' 
					 }).appendTo(ret);
	 btnDelete.data(g_keyActivityId, activityId);
	 btnDelete.bind("click", onBtnDeleteClicked);

	 var btnSubmit=$('<button>',{
	 					class: g_classBtnSubmit,
	 					text: 'Submit'
	 				}).appendTo(ret);
	 btnSubmit.data(g_keyActivityId, activityId);
	 btnSubmit.bind("click", onBtnSubmitClicked);

	 var btnCancel=$('<button>',{
	 					class: g_classBtnCancel,
	 					text: 'Cancel'
	 				}).appendTo(ret);
	 btnCancel.bind("click", onBtnCancelClicked);

	 ret.data(g_keyActivityId, activityId);
	 
	 return ret;
}

function generateDateSelection(sectionName, className){
	 var currentTime=new Date();
	 var currentYear=currentTime.getFullYear();

	 var numberOfYears=30;
	 var numberOfMonths=12;
	 var numberOfDays=31;
	 var numberOfHours=24;
	 var interval=15; // in minutes
	 var numberOfIntervals=4;

	 var years=[];
	 for(var i=0;i<numberOfYears;i++){
	 	years.push(i+currentYear);
	 }
	 var months=[];
	 for(var i=0;i<numberOfMonths;i++){
	 	months.push(i+1);
	 }
	 var days=[];
	 for(var i=0;i<numberOfDays;i++){
	 	days.push(i+1);
	 }
	 var hours=[];
	 for(var i=0;i<numberOfHours;i++){
	 	hours.push(i);
	 }
	 var intervals=[];
	 for(var i=0;i<numberOfIntervals;i++){
	 	intervals.push(i*interval);
	 }

	 var ret=$('<p>',
                {
                    html: sectionName.toString()
                });

	 var selectionYear=generateSelectionWidget(years, className);
     ret.append(selectionYear);

	 var selectionMonth=generateSelectionWidget(months, className);
     ret.append(selectionMonth);

	 var selectionDay=generateSelectionWidget(days, className);
     ret.append(selectionDay);

	 ret.append('/');

     var selectionHour=generateSelectionWidget(hours, className);
     ret.append(selectionHour);

	 ret.append(':');

	 var selectionInterval=generateSelectionWidget(intervals, className);
     ret.append(selectionInterval);

     return ret;
}

function generateSelectionWidget(arr, className){
    var ret=$('<select>',{
                class: className
            });
    for(var i=0;i<arr.length;i++){
        var element=arr[i];
	 	var option=$('<option>').appendTo(ret);
	 	option.text(element.toString());
	 	option.val(element);
    }
    return ret;
}

function generateBeginTimeSelection(){
    return generateDateSelection("Begin Time: ", g_classSelectionBeginTime);
}

function generateDeadlineSelection(){
    return generateDateSelection("Deadline: ", g_classSelectionDeadline);
}
