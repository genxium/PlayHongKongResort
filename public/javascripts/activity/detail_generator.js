// Assistive Functions
function queryActivityDetail(activityId){
    var params={};
    params['activityId']=activityId;
    try{
        $.ajax({
            method: "GET",
            url: "/activity/detail",
            data: params,
            success: function(data, status, xhr){
                var activityDetailJson=JSON.parse(data);
                displayActivityDetail(activityDetailJson);
            },
            error: function(xhr, status, errThrown){

            }
        });

    } catch(err){

    }
}

function displayActivityDetail(activityDetailJson){
    var activityTitle=activityDetailJson[g_keyActivityTitle];
    var activityContent=activityDetailJson[g_keyActivityContent];
    var activityImages=activityDetailJson[g_keyActivityImages];

    var body=$("body");
    var detailView=generateActivityDetailViewByJson(activityDetailJson);
    body.append(detailView);
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
		var token = $.cookie(g_keyLoginStatus.toString());
        if(token==null) break;
		var activityId = $(this).data(g_keyActivityId);
        if(activityId==null) break;
        try{
            $.post("/updateActivityParticipants", 
                {
                    ActivityId: activityId.toString(),
                    UserToken: token.toString(),
                    ActivityAppliedParticipants: JSON.stringify(appliedParticipants),
                    ActivitySelectedParticipants: JSON.stringify(selectedParticipants)
                },
                function(data, status, xhr){
                    if(status=="success"){
                        inputs.each(function() {
                            var label=$(this).data(g_indexParticipantsSelectionLabel);
                            var value = $(this).val();
                            if(this.checked){
                                label.css("background-color", "aquamarine");
                            } else{
                                label.css("background-color", "pink");
                            }
                        });
                    }
                    else{
                        alert("unsuccessful!");
                    }
                }
            );
        } catch(err){
            alert(err); 
        }
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
	var activityId=activityJson[g_keyActivityId];
	var activityTitle=activityJson[g_keyActivityTitle];
	var activityContent=activityJson[g_keyActivityContent];
	var activityImages=activityJson[g_keyActivityImages];
    var appliedParticipants=activityJson[g_keyActivityAppliedParticipants];
    var selectedParticipants=activityJson[g_keyActivitySelectedParticipants]; 

    var ret=$('<div>');
    
    var title=$('<p>',{
            html: activityTitle.toString()
        }).appendTo(ret);
        
    var content=$('<div>',{
            html: activityContent.toString()
        }).appendTo(ret); 	

    do{
        if(activityImages==null) break;
        var imagesNode=$('<p>',{
            
        }).appendTo(ret);

        for(var key in activityImages){
           if(activityImages.hasOwnProperty(key)){
               var activityImage=activityImages[key];
               var imageUrl=activityImage[g_keyImageURL];
               var imageNode=$('<img>',{
                    src: imageUrl.toString()   
               }).appendTo(imagesNode);
           }
        }
        
        var selectionForm=$('<form>',{
            id: g_idParticipantsSelectionForm
        }).appendTo(ret);

        var labels=new Array();
        for(var key in selectedParticipants){
            if(selectedParticipants.hasOwnProperty(key)){
                selectedParticipant=selectedParticipants[key];
                var email=selectedParticipant[g_keyUserEmail];
                var userId=selectedParticipant[g_keyUserId];
                var label=$('<label>', {
                    text: email  
                }).appendTo(selectionForm);
                label.css("background-color", "aquamarine ");
                label.data(g_indexParticipantSelectionStatus, g_statusSelected);
                label.data(g_keyUserId, userId);
                labels.push(label);
                $('<br>').appendTo(selectionForm);
                
            }
        }

        for(var key in appliedParticipants){
            if(appliedParticipants.hasOwnProperty(key)){
                appliedParticipant=appliedParticipants[key];
                var email=appliedParticipant[g_keyUserEmail];
                var userId=appliedParticipant[g_keyUserId];
                var label=$('<label>', {
                    text: email,
                }).appendTo(selectionForm);
                label.css("background-color", "pink");
                label.data(g_indexParticipantSelectionStatus, g_statusApplied);
                label.data(g_keyUserId, userId);
                labels.push(label);
                $('<br>').appendTo(selectionForm);
            }
        }
        
        var token=$.cookie(g_keyLoginStatus.toString());
	    if(token==null) break;

	    var params={};
	    params["token"]=token;
	    params["activityId"]=activityId;

	    $.ajax({
	        type: "GET",
	        url: "/activity/ownership",
	        data: params,
	        success: function(data, status, xhr){
                 for(var i=0;i<labels.length;i++){
                     var label=labels[i];
                     var userId=$(label).data(g_keyUserId);
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
                                     text: 'Submit'
                                 }).appendTo(selectionForm);
                     btnSubmit.css("font-size", 18);
                     btnSubmit.bind("click", onBtnSubmitClicked);
                     selectionForm.data(g_keyActivityId, activityId);
                 }
            },
            error: function(xhr, status, errThrown){

            }
	    });

        // query comments
        $.ajax({
            type: "GET",
            url: "/comment/query",
            data: {
                "activityId": activityId,
                "refIndex": 0,
                "numItems": 20,
                "direction": 1,
                "token": token
            },
            success: function(data, status, xhr){
                var tb=$('<table>', {
                    border: "1pt"
                }).appendTo(ret);

                var jsonResponse=JSON.parse(data);
                if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
                    for(var key in jsonResponse){
                        var commentJson=jsonResponse[key];
                        var row=$('<tr>').appendTo(tb);
                        $('<td>',{
                            text: key
                        }).appendTo(row);
                        $('<td>',{
                            text: commentJson[g_keyCommentAContent]
                        }).appendTo(row);
                        $('<td>',{
                            text: commentJson[g_keyCommenterName]
                        }).appendTo(row);
                        $('<td>',{
                            text: commentJson[g_keyGeneratedTime]
                        }).appendTo(row);
                    }
                }
            },
            error: function(xhr, status, err){

            }
        });

        var commentEditor=generateCommentEditor(activityId);
        ret.append(commentEditor);

    }while(false);
	return ret;
}

// execute on start
$(document).ready(function(){
	// execute on page loaded
	var activityId=$('#activityId').attr("value");
	queryActivityDetail(activityId);
});