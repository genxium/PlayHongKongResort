// Assistant Callback Functions
function onUploadAvatarFormSubmit(formEvt){
		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);
		
		$.ajax({
			method: "POST",
			url: "/uploadAvatar", 
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
		formEvt.preventDefault(); // prevent default action.
}

// Event Handlers
function onBtnUploadAvatarClicked(evt){

	var file = document.getElementById(g_idFieldAvatar);
	if(validateImage(file)==false){
		return;
	}
	
	var form = $("#"+g_idFormAvatar);
	// set callback function of form submission
	form.submit(onUploadAvatarFormSubmit);
	form.submit();
}