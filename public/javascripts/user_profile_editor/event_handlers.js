// Assistant Callback Functions
function onUploadAvatarFormSubmission(formEvt){

		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);
		
		$.ajax({
			method: "POST",
			url: "/user/uploadAvatar", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
		    	$("#"+g_idSectionResponse).html(data);
			},
			error: function(xhr, status, errorThrown){
				$("#"+g_idSectionResponse).html("Failed");
			}
		});

}

// Event Handlers
function onBtnUploadAvatarClicked(evt){

	var file = document.getElementById(g_idFieldAvatar);
	if(validateImage(file)==false){
		return;
	}
	
	var form = $("#"+g_idFormAvatar);
	// set callback function of form submission
	form.submit(onUploadAvatarFormSubmission);
	// invoke submission
	form.submit();
}

function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionOwnedActivities);
	var pageIndex=targetSection.data(g_pageIndexKey);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

	queryActivitiesHostedByUser(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionOwnedActivities);
	var pageIndex=targetSection.data(g_pageIndexKey);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

	queryActivitiesHostedByUser(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSectionOwnedActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}
