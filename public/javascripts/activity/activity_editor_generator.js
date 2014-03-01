// Assistive Callback Functions
function onUpdateFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		/*
		// check files
		var iFiles=0;
		for (iFiles = 0; iFiles < g_maxNumberOfImagesForSingleActivity; iFiles++) {
		 	var imageName=g_indexImageOfActivityPrefix+iFiles;
		 	var imageField=formObj.data(imageName);
		 	var files=imageField.files;
		 	var length=files.length;
		 	if(length>1) break; // invalid field exist
		 	var file=files[0];
		 	var checkResult=isFileValid(file);
		 	if(checkResult==false) break;
		}
		if(iFiles<g_maxNumberOfImagesForSingleActivity) break;
		*/
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyActivityTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyActivityContent, activityContent);

		// append activity begin time and deadline
		var sectionBeginTime=$(this).data(g_indexSectionBeginTime);
		var beginTimeNodes=sectionBeginTime.children();
		var beginTimeYear=formatDigits(beginTimeNodes[0].value, 4);
		var beginTimeMonth=formatDigits(beginTimeNodes[1].value, 2);
		var beginTimeDay=formatDigits(beginTimeNodes[2].value, 2);
		var beginTimeHour=formatDigits(beginTimeNodes[3].value, 2);
		var beginTimeMinute=formatDigits(beginTimeNodes[4].value, 2);
		var beginTime=beginTimeYear+"-"+beginTimeMonth+"-"+beginTimeDay+" "+beginTimeHour+":"+beginTimeMinute+":00";
		formData.append(g_keyActivityBeginTime, beginTime);

		var sectionDeadline=$(this).data(g_indexSectionDeadline);
		var deadlineNodes=sectionDeadline.children();
		var deadlineYear=formatDigits(deadlineNodes[0].value, 4);
		var deadlineMonth=formatDigits(deadlineNodes[1].value, 2);
		var deadlineDay=formatDigits(deadlineNodes[2].value, 2);
		var deadlineHour=formatDigits(deadlineNodes[3].value, 2);
		var deadlineMinute=formatDigits(deadlineNodes[4].value, 2);
		var deadline=deadlineYear+"-"+deadlineMonth+"-"+deadlineDay+" "+deadlineHour+":"+deadlineMinute+":00";
		formData.append(g_keyActivityDeadline, deadline);

		$.ajax({
			method: "POST",
			url: "/updateActivity", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				formObj.remove();
				if(g_callbackOnActivityEditorRemoved!=null){
					g_callbackOnActivityEditorRemoved(0);
				}
			},
			error: function(xhr, status, errorThrown){

			}
		});
	}while(false);
}

function onSubmitFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		/*
		// check files
		var iFiles=0;
		for (iFiles = 0; iFiles < g_maxNumberOfImagesForSingleActivity; iFiles++) {
		 	var imageName=g_indexImageOfActivityPrefix+iFiles;
		 	var imageField=formObj.data(imageName);
		 	var files=imageField.files;
		 	var length=files.length;
		 	if(length>1) break; // invalid field exist
		 	var file=files[0];
		 	var checkResult=isFileValid(file);
		 	if(checkResult==false) break;
		}
		if(iFiles<g_maxNumberOfImagesForSingleActivity) break;
		*/
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyActivityTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyActivityContent, activityContent);
		
		var sectionBeginTime=$(this).data(g_indexSectionBeginTime);
		var beginTimeNodes=sectionBeginTime.children();
		var beginTimeYear=formatDigits(beginTimeNodes[0].value, 4);
		var beginTimeMonth=formatDigits(beginTimeNodes[1].value, 2);
		var beginTimeDay=formatDigits(beginTimeNodes[2].value, 2);
		var beginTimeHour=formatDigits(beginTimeNodes[3].value, 2);
		var beginTimeMinute=formatDigits(beginTimeNodes[4].value, 2);
		var beginTime=beginTimeYear+"-"+beginTimeMonth+"-"+beginTimeDay+" "+beginTimeHour+":"+beginTimeMinute+":00";
		formData.append(g_keyActivityBeginTime, beginTime);

		var sectionDeadline=$(this).data(g_indexSectionDeadline);
		var deadlineNodes=sectionDeadline.children();
		var deadlineYear=formatDigits(deadlineNodes[0].value, 4);
		var deadlineMonth=formatDigits(deadlineNodes[1].value, 2);
		var deadlineDay=formatDigits(deadlineNodes[2].value, 2);
		var deadlineHour=formatDigits(deadlineNodes[3].value, 2);
		var deadlineMinute=formatDigits(deadlineNodes[4].value, 2);
		var deadline=deadlineYear+"-"+deadlineMonth+"-"+deadlineDay+" "+deadlineHour+":"+deadlineMinute+":00";
		formData.append(g_keyActivityDeadline, deadline);

		$.ajax({
			method: "POST",
			url: "/submitActivity", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				formObj.remove();
				if(g_callbackOnActivityEditorRemoved!=null){
					g_callbackOnActivityEditorRemoved(0);
				}
			},
			error: function(xhr, status, errorThrown){

			}
		});
	}while(false);
}

// Assistive functions
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

// Assistive Handlers
function onBtnCreateClicked(evt){

	evt.preventDefault();
	$(this).hide();

	sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	var userToken=$.cookie(g_keyLoginStatus.toString());
	
	try{
		$.post("/createActivity", 
			{
				UserToken: userToken.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var activityJson=JSON.parse(data);
						var editor=generateActivityEditorByJson(activityJson);
						sectionActivityEditor.append(editor);
    				} else{
    					
    				}
			}
		);
	} catch(err){
		
	}
}

function onBtnUpdateClicked(evt){

	evt.preventDefault();
	var editor=$(this).parent();

	try{
		// assign callback function
		editor.submit(onUpdateFormSubmission);
		// invoke submission
		editor.submit();
	} catch(err){
		// $("."+g_classFieldActivityContent).val(err);
	}
}

function onBtnDeleteClicked(evt){

	evt.preventDefault();
	
	var activityId=$(this).data(g_keyActivityId);
	var id=activityId;
	var token=$.cookie(g_keyLoginStatus.toString());
	var editor=$(this).parent();

	try{
		$.post("/deleteActivity", 
			{
				ActivityId: id.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){
					editor.remove();
					if(g_callbackOnActivityEditorRemoved!=null){
						g_callbackOnActivityEditorRemoved(0);
					}
				}
				else{

				}
			}
		);
	} catch(err){
		
	}
}

function onBtnSubmitClicked(evt){

	evt.preventDefault();
	var editor=$(this).parent();

	try{
		// assign callback function
		editor.submit(onSubmitFormSubmission);
		// invoke submission
		editor.submit();
	} catch(err){
		// $("."+g_classFieldActivityContent).val(err);
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
	var activityId=activityJson[g_keyActivityId];
	var activityTitle=activityJson[g_keyActivityTitle];
	var activityContent=activityJson[g_keyActivityContent];
	
	var ret=generateActivityEditor(activityId, activityTitle, activityContent);
	return ret;
}

function generateActivityEditor(activityId, activityTitle, activityContent){
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
	 
	 var contentText=$('<p>',
	 			   {
				   		html: 'Content'
 				   }).appendTo(ret);

	 var contentInput=$('<TEXTAREA>',
	 				  {
	 				  	class: g_classFieldActivityContent, 
	 				  	name: g_keyActivityContent
	 				  }).appendTo(ret);
	 contentInput.val(activityContent);

	 for (var i = 0; i < g_maxNumberOfImagesForSingleActivity; i++) {
	 	var imageName=g_indexImageOfActivityPrefix+i;
	 	var imageField=$('<input>',
				 		{
				 			class: g_classFieldImageOfActivity,
							type: 'file',
							name: imageName 	 			
				 		}).appendTo(ret);
	 	ret.data(imageName, imageField);
	 }

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

	 
	 /* Begin Time */
	 var sectionBeginTime=$('<p>',
	 						{
	 							html: "Begin Time: "
	 						}).appendTo(ret);

	 ret.data(g_indexSectionBeginTime, sectionBeginTime);

	 var selectionBeginTimeYear=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	}).appendTo(sectionBeginTime);

	 for(var i=0;i<years.length;i++){
	 	var year=years[i];
	 	var option=$('<option>').appendTo(selectionBeginTimeYear);
	 	option.text(year.toString());
	 	option.val(year);
	 }

	 var selectionBeginTimeMonth=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	}).appendTo(sectionBeginTime);

	 for(var i=0;i<months.length;i++){
	 	var month=months[i];
	 	var option=$('<option>').appendTo(selectionBeginTimeMonth);
	 	option.text(month.toString());
	 	option.val(month);
	 }

	 var selectionBeginTimeDay=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	}).appendTo(sectionBeginTime);

	 for(var i=0;i<days.length;i++){
	 	var day=days[i];
	 	var option=$('<option>').appendTo(selectionBeginTimeDay);
	 	option.text(day.toString());
	 	option.val(day);
	 }	 

	 sectionBeginTime.append('/');

	 var selectionBeginTimeHours=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	}).appendTo(sectionBeginTime);
 
	 for(var i=0;i<hours.length;i++){
	 	var hour=hours[i];
	 	var option=$('<option>').appendTo(selectionBeginTimeHours);
	 	option.text(hour.toString());
	 	option.val(hour);
	 }

	 sectionBeginTime.append(':');

	 var selectionBeginTimeInterval=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	}).appendTo(sectionBeginTime);

	 for(var i=0;i<intervals.length;i++){
	 	var interval=intervals[i];
	 	var option=$('<option>').appendTo(selectionBeginTimeInterval);
	 	option.text(interval.toString());
	 	option.val(interval);
	 }		 

	 /* Deadline */
	 var sectionDeadline=$('<p>',
	 						{
	 							html: "Deadline: "
	 						}).appendTo(ret);

	 ret.data(g_indexSectionDeadline, sectionDeadline);

	 var selectionDeadlineYear=$('<select>',
						 	{
						 		class: g_classSelectionDeadline	
						 	}).appendTo(sectionDeadline);

	 for(var i=0;i<years.length;i++){
	 	var year=years[i];
	 	var option=$('<option>').appendTo(selectionDeadlineYear);
	 	option.text(year.toString());
	 	option.val(year);
	 }

	 var selectionDeadlineMonth=$('<select>',
						 	{
						 		class: g_classSelectionDeadline	
						 	}).appendTo(sectionDeadline);

	 for(var i=0;i<months.length;i++){
	 	var month=months[i];
	 	var option=$('<option>').appendTo(selectionDeadlineMonth);
	 	option.text(month.toString());
	 	option.val(month);
	 }

	 var selectionDeadlineDay=$('<select>',
						 	{
						 		class: g_classSelectionDeadline	
						 	}).appendTo(sectionDeadline);

	 for(var i=0;i<days.length;i++){
	 	var day=days[i];
	 	var option=$('<option>').appendTo(selectionDeadlineDay);
	 	option.text(day.toString());
	 	option.val(day);
	 }	 

	 sectionDeadline.append('/');

	 var selectionDeadlineHours=$('<select>',
						 	{
						 		class: g_classSelectionDeadline	
						 	}).appendTo(sectionDeadline);
 
	 for(var i=0;i<hours.length;i++){
	 	var hour=hours[i];
	 	var option=$('<option>').appendTo(selectionDeadlineHours);
	 	option.text(hour.toString());
	 	option.val(hour);
	 }

	 sectionDeadline.append(':');

	 var selectionDeadlineInterval=$('<select>',
						 	{
						 		class: g_classSelectionDeadline	
						 	}).appendTo(sectionDeadline);

	 for(var i=0;i<intervals.length;i++){
	 	var interval=intervals[i];
	 	var option=$('<option>').appendTo(selectionDeadlineInterval);
	 	option.text(interval.toString());
	 	option.val(interval);
	 }

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

