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
                        alert("successful!");
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

        var token=$.cookie(g_keyLoginStatus.toString());
        if(token==null) break;     
        var selectionForm=$('<form>',{
            id: g_idParticipantsSelectionForm
        }).appendTo(ret);
        for(var key in selectedParticipants){
            if(selectedParticipants.hasOwnProperty(key)){
                selectedParticipant=selectedParticipants[key];
                var email=selectedParticipant[g_keyUserEmail];
                var id=appliedParticipant[g_keyUserId];
                var label=$('<label>', {
                    text: email  
                }).appendTo(selectionForm);
                label.css("background-color", "blue");
                var checkbox=$('<input>',{
                    type: "checkbox",
                    class: g_classParticipantsSelection,
                    value: id,
                }).appendTo(label);
                $('<br>').appendTo(selectionForm);
                
            }
        }

        for(var key in appliedParticipants){
            if(appliedParticipants.hasOwnProperty(key)){
                appliedParticipant=appliedParticipants[key];
                var email=appliedParticipant[g_keyUserEmail];
                var id=appliedParticipant[g_keyUserId];
                var label=$('<label>', {
                    text: email,
                }).appendTo(selectionForm);
                label.css("background-color", "pink");
                var checkbox=$('<input>',{
                    type: "checkbox",
                    class: g_classParticipantsSelection,
                    value: id,
                }).appendTo(label);
                $('<br>').appendTo(selectionForm);
            }
        }
	 
        var btnSubmit=$('<button>',{
	 					text: 'Submit'
	 				}).appendTo(selectionForm);
        btnSubmit.bind("click", onBtnSubmitClicked);
        selectionForm.data(g_keyActivityId, activityId);

    }while(false);
	return ret;
}
