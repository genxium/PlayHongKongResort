var g_activityId=null;
var g_url=null;

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
	var activityId=activityJson[g_keyId];
	var activityTitle=activityJson[g_keyTitle];
	var activityContent=activityJson[g_keyContent];
	var activityImages=activityJson[g_keyImages];
	var appliedParticipants=activityJson[g_keyAppliedParticipants];
	var selectedParticipants=activityJson[g_keySelectedParticipants]; 
	var hostId=activityJson[g_keyHostId];
	var hostName=activityJson[g_keyHostName];

	var ret=$('<div>');

	var title=$('<p>',{
		text: activityTitle.toString(),
		style: "font-size: 18pt; color: blue"
	}).appendTo(ret);

	if(hostId!=null && hostName!=null){
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
			href: '/user/profile?'+g_keyUserId+"="+hostId.toString(),
			text: hostName
		}).appendTo(sp2);
	}	

	var content=$('<div>',{
		text: activityContent.toString(),
		style: "font-size: 15pt"
	}).appendTo(ret); 	

	do{
		if(activityImages==null) break;
		var imagesNode=$('<p>',{
		    
		}).appendTo(ret);

		for(var key in activityImages){
		   if(activityImages.hasOwnProperty(key)){
		       var activityImage=activityImages[key];
		       var imageUrl=activityImage[g_keyUrl];
		       var imageNode=$('<img>',{
			    src: imageUrl.toString()   
		       }).appendTo(imagesNode);
		   }
		}

		var sectionParticipant=$("#section_participant");
		sectionParticipant.empty();
		var selectionForm=$('<form>',{
		    id: g_idParticipantsSelectionForm
		}).appendTo(sectionParticipant);

		var labels=new Array();
		for(var key in selectedParticipants){
		    if(selectedParticipants.hasOwnProperty(key)){
			selectedParticipant=selectedParticipants[key];
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
		}

		for(var key in appliedParticipants){
		    if(appliedParticipants.hasOwnProperty(key)){
			appliedParticipant=appliedParticipants[key];
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
		}
	
		var params={};
		params[g_keyActivityId]=activityId;
		params[g_keyRefIndex]=0;
		params[g_keyNumItems]=20;
		params[g_keyDirection]=1;		

		// query comments
		$.ajax({
		    type: "GET",
		    url: "/comment/query",
		    data: params,
		    success: function(data, status, xhr){
				var sectionComment=$("#section_comment");
				sectionComment.empty();
				var jsonResponse=JSON.parse(data);
				if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
				    for(var key in jsonResponse){
						var commentJson=jsonResponse[key];
						var row=generateCommentCell(commentJson, activityId).appendTo(sectionComment);
						$('<br>').appendTo(sectionComment);
				    }
				}
		    },
		    error: function(xhr, status, err){

		    }
		});

		var token=$.cookie(g_keyToken);
		if(token==null) break;

		var params={};
		params[g_keyToken]=token;
		params[g_keyActivityId]=activityId;

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
					selectionForm.data(g_keyActivityId, activityId);
				}
				$('<hr>').appendTo(selectionForm);
			},
			error: function(xhr, status, errThrown){

			}
		});

		var commentEditor=generateCommentEditor(activityId);
		ret.append(commentEditor);

	}while(false);
	return ret;
}

// execute on start
$(document).ready(function(){
	g_url=window.location.href;
	g_activityId=$('#'+g_keyActivityId).attr("value");

	initTopbar();

	g_callbackOnLoginSuccess=refreshOnLoggedIn;
	g_callbackOnLoginError=null;

	g_callbackOnEnter=refreshOnEnter;

	initActivityEditor();
	
	checkLoginStatus();
});
