// Assistant Handlers
function onBtnEditClicked(evt){
    var activityJson=$(this).data(g_keyActivity);
	g_callbackOnEditorCancelled=function(){
		g_sectionActivityEditor.modal("hide");
	};
	g_activityEditor=generateActivityEditorByJson(activityJson);	
	g_modalActivityEditor.empty();
	g_modalActivityEditor.append(g_activityEditor);

	g_sectionActivityEditor.css("position", "absolute");
	g_sectionActivityEditor.css("height", "90%");
	g_sectionActivityEditor.css("padding", "5pt");
	g_sectionActivityEditor.modal({
		show: true
	});
}

function onMouseEnterOwnedActivityCell(evt){
	
}

function onMouseLeaveOwnedActivityCell(evt){

}

function onMouseEnterDefaultActivityCell(evt){
	var btnJoin=$(this).data(g_indexBtnJoin);
	btnJoin.show();
}

function onMouseLeaveDefaultActivityCell(evt){
	var btnJoin=$(this).data(g_indexBtnJoin);
	btnJoin.hide();
}

function onBtnJoinClicked(evt){
	evt.preventDefault();
	var btnJoin=this;
	var token = $.cookie(g_keyToken).toString();
	var activityId=$(this).data(g_keyId);

	var params={};
	params[g_keyId]=activityId;
	params[g_keyToken]=token;

	try{
		$.ajax({
			type: "POST",
			url: "/activity/join",
			data: params,
			success: function(data, status, xhr){
				var cellNode=btnJoin.parentNode; // javascript dom element
				var cell=$(cellNode); // convert to jQuery element object
				btnJoin.remove();

				var appliedIndicator=$('<div>', {
					class: g_classAppliedIndicator,
					text: 'Applied'
				});
				cell.append(appliedIndicator);
				cell.data(g_indexStatusIndicator, appliedIndicator);
			},
			error: function(xhr, status, errThrown){

			}
		});
	} catch(err){

	}
}

function onBtnDetailClicked(evt){
	evt.preventDefault();
	var activityId=$(this).data(g_keyId);
    
	try{
		var detailPagePath="/app/detail?"+g_keyActivityId+"="+activityId;
		window.open(detailPagePath);
    	} catch(err){
        	alert(err.toString());
	}
}

// Generators

function generateActivityCell(activityJson){

	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=activityJson[g_keyId];
	var activityTitle=activityJson[g_keyTitle];
   	 
	var coverImageUrl=null;
	var activityImages=activityJson[g_keyImages];
	if(activityImages!=null) {
		for(var key in activityImages){
		   if(activityImages.hasOwnProperty(key)){
		       var activityImage=activityImages[key];
		       coverImageUrl=activityImage[g_keyUrl];
		       break;
		   }
		}
	}
	var relation=activityJson[g_keyRelation];
	var activityStatus=activityJson[g_keyStatus];
	var statusStr=arrayStatusName[parseInt(activityStatus)];

	var ret=$('<div>', {
		class: g_classCellActivityContainer
	});

	if(coverImageUrl!=null){
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageUrl
		}).appendTo(ret);
	}

	var cellActivityTitle=$('<div>', {	
		class: g_classCellActivityTitle,
		html: activityTitle
	}).appendTo(ret);

	if(relation==null){
		var btnJoin=$('<button>', {
			class: g_classBtnJoin,
			text: 'Join'
		}).appendTo(ret);
		btnJoin.data(g_keyId, activityId);
		btnJoin.bind("click", onBtnJoinClicked);

		ret.bind("mouseenter", onMouseEnterDefaultActivityCell);
		ret.bind("mouseleave", onMouseLeaveDefaultActivityCell);
		btnJoin.hide();

		ret.data(g_indexBtnJoin, btnJoin);
	} else if(relation==2) {
		// hard-coded, relation 2 is applied
		
		var appliedIndicator=$('<div>', {
			class: g_classAppliedIndicator,
			html: 'Applied'
		}).appendTo(ret);

		ret.data(g_indexStatusIndicator, appliedIndicator);
	} else;

	if(activityStatus!=null){

		var statusIndicator=$('<div>',{
		    class: g_classActivityStatusIndicator,
		    html: statusStr 
		}).appendTo(ret);

		if(parseInt(activityStatus)==0){ 
		    // this condition is temporarily hard-coded
		    var btnEdit=$('<button>', {
			class: g_classBtnEdit,
			text: 'Edit'
		    }).appendTo(ret);
		    btnEdit.bind("click", onBtnEditClicked);
		    btnEdit.data(g_keyActivity, activityJson); 
		    ret.data(g_indexBtnEdit, btnEdit);
		}
	}

	var btnDetail=$('<button>', {
		class: g_classBtnDetail,
		text: 'Detail'
	}).appendTo(ret);
	btnDetail.data(g_keyId, activityId);
	btnDetail.bind("click", onBtnDetailClicked);

	ret.data(g_indexBtnDetail, btnDetail);

	ret.data(g_keyId, activityId);
	ret.data(g_keyTitle, activityTitle);
	
	return ret;
}
