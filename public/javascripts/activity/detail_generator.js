var g_activityId=null;

// Assistive Functions
function refreshOnEnter(){
	queryActivityDetail(g_activityId);
}

function refreshOnLoggedIn(){
	queryActivityDetail(g_activityId);
}

function queryActivityDetail(activityId){

	var token=$.cookie(g_keyToken);
    	var params={};
    	params[g_keyActivityId]=activityId;
        if(token!=null)	params[g_keyToken]=token;

        try{
            $.ajax({
                method: "GET",
                url: "/activity/detail",
                data: params,
                success: function(data, status, xhr){
                    var activityDetailJson=JSON.parse(data);
                    displayActivityDetail(activityDetailJson);
                },
                error: function(xhr, status, err){

                }
            });
        } catch(err){

        }
}

function displayActivityDetail(activityDetailJson){
	var activityTitle=activityDetailJson[g_keyTitle];
	var activityContent=activityDetailJson[g_keyContent];
	var activityImages=activityDetailJson[g_keyImages];

	var sectionActivity=$("#section_activity");
	var detailView=generateActivityDetailViewByJson(activityDetailJson);
	sectionActivity.empty();
	sectionActivity.append(detailView);
}

// Assistive Callback Functions
function onParticipantsSelectionFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.
		var inputs = $("."+g_classParticipantsSelection);
		var appliedParticipants=new Array();
		var selectedParticipants=new Array();
		inputs.each(function() {
			var value = $(this).val();
			if(this.checked){
				selectedParticipants.push(value);
			} else{
				appliedParticipants.push(value);
			}
		});

		// append user token and activity id for identity
		var token = $.cookie(g_keyToken).toString();
		if(token==null) break;
		var activityId = $(this).data(g_keyActivityId);
		if(activityId==null) break;

		var params={};
		params[g_keyToken]=token;
		params[g_keyActivityId]=activityId;
		params[g_keyAppliedParticipants]=JSON.stringify(appliedParticipants);
		params[g_keySelectedParticipants]=JSON.stringify(selectedParticipants);

		$.ajax({
			type: "POST",
			url: "/activity/participants/update",
			data: params,
			success: function(data, status, xhr){
                    inputs.each(function() {
                        var label=$(this).data(g_indexParticipantsSelectionLabel);
                        var value = $(this).val();
                        if(this.checked){
                            label.css("background-color", "aquamarine");
                        } else{
                            label.css("background-color", "pink");
                        }
                    });
			},
			error: function(xhr, status, err) {

			}
		});
	}while(false);
}

// Assistive Event Handlers
function onBtnSubmitClicked(evt){
	evt.preventDefault();
	var selectionForm=$(this).parent();
	try{
		selectionForm.submit(onParticipantsSelectionFormSubmission);
		selectionForm.submit();
	} catch(err){

	}
}

// Generators
function generateActivityDetailViewByJson(activityJson){
    var activity=new Activity(activityJson);
	var ret=$('<div>');

	var title=$('<p>',{
		text: activity.title.toString(),
		style: "font-size: 18pt; color: blue"
	}).appendTo(ret);

	// deadline and begin time
	var times=$("<table border='1'>", {
		style: "margin-bottom: 5pt"
	}).appendTo(ret);
	var deadlineRow=$('<tr>').appendTo(times);
	var deadlineTitle=$('<td>', {
		text: "Application Deadline",
		style: "padding-left: 5pt; padding-right: 5pt"
	}).appendTo(deadlineRow);		
	var deadline=$('<td>', {
		text: activity.applicationDeadline.toString(),
		style: "color: red; padding-left: 8pt; padding-right: 5pt"
	}).appendTo(deadlineRow);

	var beginTimeRow=$('<tr>').appendTo(times);
	var beginTimeTitle=$('<td>', {
		text: "Begin Time",
		style: "padding-left: 5pt; padding-right: 5pt"
	}).appendTo(beginTimeRow);		
	var beginTime=$('<td>', {
		text: activity.beginTime.toString(),
		style: "color: blue; padding-left: 8pt; padding-right: 5pt"
	}).appendTo(beginTimeRow);

	if(activity.host.id=null && activity.host.name!=null){
		var d=$('<div>', {
			style: "margin-bottom: 5pt"
		}).appendTo(ret);
		var sp1=$('<span>', {
			text: "--by",
			style: "margin-left: 20%"
		}).appendTo(d);
		var sp2=$('<span>', {
			style: "margin-left: 5pt"	
		}).appendTo(d);
		var host=$('<a>', {
			href: '/user/profile?'+g_keyUserId+"="+activity.host.id.toString(),
			text: activity.host.name
		}).appendTo(sp2);
	}	

	var content=$('<div>',{
		text: activity.content.toString(),
		style: "font-size: 15pt"
	}).appendTo(ret); 	

	do{
		if(activity.images==null) break;
		var imagesNode=$('<p>',{
		    
		}).appendTo(ret);

		for(var i=0;i<activity.images.length;++i){
			var imageNode=$('<img>',{
				src: activity.images[i].url.toString()
			}).appendTo(imagesNode);
		}

		var sectionParticipant=$("#section_participant");
		sectionParticipant.empty();
		var selectionForm=$('<form>',{
		    id: g_idParticipantsSelectionForm
		}).appendTo(sectionParticipant);

		var labels=new Array();
		for(var i=0;i<activity.selectedParticipants.length;++i){
		    var selectedParticipant=activity.selectedParticipants[i];
			var email=selectedParticipant[g_keyEmail];
			var userId=selectedParticipant[g_keyId];
			var label=$('<label>', {
			    text: email  
			}).appendTo(selectionForm);
			label.css("background-color", "aquamarine ");
			label.data(g_indexParticipantSelectionStatus, g_statusSelected);
			label.data(g_keyId, userId);
			labels.push(label);
			$('<br>').appendTo(selectionForm);
		}

		for(var i=0;i<activity.appliedParticipants.length;++i){
		    var appliedParticipant=activity.appliedParticipants[i];
            var email=appliedParticipant[g_keyEmail];
            var userId=appliedParticipant[g_keyId];
            var label=$('<label>', {
                text: email,
            }).appendTo(selectionForm);
            label.css("background-color", "pink");
            label.data(g_indexParticipantSelectionStatus, g_statusApplied);
            label.data(g_keyId, userId);
            labels.push(label);
            $('<br>').appendTo(selectionForm);
		}
	
		var params={};
		params[g_keyActivityId]=activity.id;
		params[g_keyRefIndex]=0;
		params[g_keyNumItems]=20;
		params[g_keyDirection]=1;		
		
		var onSuccess=function(data, status, xhr){
			var sectionComment=$("#section_comment");
			sectionComment.empty();
			var jsonResponse=JSON.parse(data);
			if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
			    for(var key in jsonResponse){
					var commentJson=jsonResponse[key];
					var row=generateCommentCell(commentJson, activity.id).appendTo(sectionComment);
					$('<br>').appendTo(sectionComment);
			    }
			}
		};
		var onError=function(xhr, status, err){};
		queryComments(params, onSuccess, onError);			

		var sectionAssessment=$("#section_assessment");
		var viewer=null;
		if(activity.hasOwnProperty("viewer")) viewer=activity.viewer;
		var batchAssessmentEditor=generateBatchAssessmentEditor(sectionAssessment, activity, activity.presentParticipants);

		var token=$.cookie(g_keyToken);
		if(token==null) break;

		var params={};
		params[g_keyToken]=token;
		params[g_keyActivityId]=activity.id;

		$.ajax({
			type: "GET",
			url: "/activity/ownership",
			data: params,
			success: function(data, status, xhr){
				for(var i=0;i<labels.length;i++){
					var label=labels[i];
					var userId=$(label).data(g_keyId);
					var selectionStatus=$(label).data(g_indexParticipantSelectionStatus);
					var checkStatus=true;
					if(selectionStatus==g_statusSelected){
						checkStatus=true;
					} else{
						checkStatus=false;
					}
					var checkbox=$('<input>',{
						type: "checkbox",
						class: g_classParticipantsSelection,
						value: userId,
						checked: checkStatus
					}).appendTo(label);
					checkbox.data(g_indexParticipantsSelectionLabel, label);
				}
				if(labels.length>0){
					var btnSubmit=$('<button>',{
						text: 'Confirm Selection',
						style: 'color: white; background-color:black; font-size: 13pt'
					}).appendTo(selectionForm);
					btnSubmit.on("click", onBtnSubmitClicked);
					selectionForm.data(g_keyActivityId, activity.id);
				}
				$('<hr>').appendTo(selectionForm);
			},
			error: function(xhr, status, errThrown){

			}
		});

		var commentEditor=generateCommentEditor(activity.id);
		ret.append(commentEditor);

	}while(false);
	return ret;
}

// execute on start
$(document).ready(function(){
	var params=extractParams(window.location.href);
	for(var i=0;i<params.length;i++){
		var param=params[i];
		var pair=param.split("=");
		if(pair[0]==g_keyActivityId){
			g_activityId=pair[1];
			break;
		}
	}
	initTopbar();

	g_callbackOnLoginSuccess=refreshOnLoggedIn;
	g_callbackOnLoginError=null;

	g_callbackOnEnter=refreshOnEnter;

	initActivityEditor();
	
	checkLoginStatus();
});
