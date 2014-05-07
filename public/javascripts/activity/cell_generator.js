// Assistant Handlers
function onBtnEditClicked(evt){
    var activityJson=$(this).data(g_keyActivityJson);
	sectionActivityEditor=$("#idSectionActivityEditor");
	sectionActivityEditor.empty();

	g_activityEditor=generateActivityEditorByJson(activityJson);
	sectionActivityEditor.append(g_activityEditor);
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
	var token = $.cookie(g_keyLoginStatus.toString());
	var activityId=$(this).data(g_keyActivityId);

    var params={};
    params[g_keyActivityId]=activityId.toString();
    params[g_keyUserToken]=token.toString();

	try{
		$.ajax({
		    type: "POST",
		    url: "/activity/join",
		    data: params,
		    success: function(data, status, xhr){
                 var cellNode=btnJoin.parentNode; // javascript dom element
                 var cell=$(cellNode); // convert to jQuery element object
                 btnJoin.remove();

                 var appliedIndicator=$('<div>',
                 {
                     class: g_classAppliedIndicator,
                     html: 'Applied'
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
    var activityId=$(this).data(g_keyActivityId);
    
	try{
        var detailPagePath="/app/detail?activityId="+activityId;
        window.open(detailPagePath);
    } catch(err){
        alert(err.toString());
	}
}

// Generators

/*

function generateActivityCell(activityJson, isLoggedIn, mode) returns a complete DOM(cell) of an activity

activityJson(string): the json string that contains activity information;
isLoggedIn(boolean): [true,false] indicating user status;
mode: g_modeHomepage indicates that the cell is generated for home page while g_modeProfile indicates that the cell is generated for profile page.

*/
function generateActivityCell(activityJson, isLoggedIn, mode){

	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=activityJson[g_keyActivityId];
	var activityTitle=activityJson[g_keyActivityTitle];
    
    var coverImageURL=null;
    do{
        var activityImages=activityJson[g_keyActivityImages];
        if(activityImages==null) break;
        for(var key in activityImages){
           if(activityImages.hasOwnProperty(key)){
               var activityImage=activityImages[key];
               coverImageURL=activityImage[g_keyImageURL];
               break;
           }
        }
    }while(false);

    var userActivityRelationId=activityJson[g_keyUserActivityRelationId];
	var activityStatus=activityJson[g_keyActivityStatus];
    var statusStr=arrayStatusName[parseInt(activityStatus)];

	var ret=$('<div>',
				{
					class: g_classCellActivityContainer
				});

	if(coverImageURL!=null){
		var coverImage=$('<img>',
							{
								class: g_classActivityCoverImage,
								src: coverImageURL
							}).appendTo(ret);
	}

	var cellActivityTitle=$('<div>',
				{	
					class: g_classCellActivityTitle,
					html: activityTitle
				}).appendTo(ret);

    switch (mode){
        case g_modeHomepage:{
            if(userActivityRelationId==null){
                var btnJoin=$('<button>',
                            {
                                class: g_classBtnJoin,
                                text: 'Join'
                            }).appendTo(ret);
                btnJoin.data(g_keyActivityId, activityId);
                btnJoin.bind("click", onBtnJoinClicked);

                ret.bind("mouseenter", onMouseEnterDefaultActivityCell);
                ret.bind("mouseleave", onMouseLeaveDefaultActivityCell);
                btnJoin.hide();

                ret.data(g_indexBtnJoin, btnJoin);
            } else{
                var appliedIndicator=$('<div>',
                                    {
                                        class: g_classAppliedIndicator,
                                        html: 'Applied'
                                    }).appendTo(ret);

                ret.data(g_indexStatusIndicator, appliedIndicator);
            }
            break;
        }
        case g_modeProfile:{
            if(isLoggedIn==true && activityStatus!=null){

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
                    btnEdit.data(g_keyActivityJson, activityJson); 
                    ret.data(g_indexBtnEdit, btnEdit);
                }
            }
            break;
        }
    }
	
	var btnDetail=$('<button>',
                    {
                        class: g_classBtnDetail,
                        text: 'Detail'
                    }).appendTo(ret);
    btnDetail.data(g_keyActivityId, activityId);
    btnDetail.bind("click", onBtnDetailClicked);

    ret.data(g_indexBtnDetail, btnDetail);

	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	
	return ret;
}
