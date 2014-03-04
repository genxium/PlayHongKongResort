// Assistive Callback Functions
function onParticipantsSelectionFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		$.ajax({
			method: "POST",
			url: "/updateActivityParticipants", 
			data: formData,
			success: function(data, status, xhr){
                alert(data);
			},
			error: function(xhr, status, errorThrown){

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
    
        var selectionForm=$('<form>').appendTo(ret);
        for(var key in selectedParticipants){
            if(selectedParticipants.hasOwnProperty(key)){
                selectedParticipant=selectedParticipants[key];
                var email=selectedParticipant[g_keyUserEmail];
                var label=$('<label>', {
                    text: email  
                }).appendTo(selectionForm);
                label.css("background-color", "blue");
                var checkbox=$('<input>',{
                    type: "checkbox",
                    value: email,
                }).appendTo(label);
                $('<br>').appendTo(selectionForm);
                
            }
        }

        for(var key in appliedParticipants){
            if(appliedParticipants.hasOwnProperty(key)){
                appliedParticipant=appliedParticipants[key];
                var email=appliedParticipant[g_keyUserEmail];
                var label=$('<label>', {
                    text: email,
                }).appendTo(selectionForm);
                label.css("background-color", "pink");
                var checkbox=$('<input>',{
                    type: "checkbox",
                    value: email,
                }).appendTo(label);
                $('<br>').appendTo(selectionForm);
            }
        }
	 
        var btnSubmit=$('<button>',{
	 					text: 'Submit'
	 				}).appendTo(selectionForm);
         btnSubmit.data(g_keyActivityId, activityId);
         btnSubmit.bind("click", onBtnSubmitClicked);

    }while(false);
	return ret;
}
