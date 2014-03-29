// Assistant Handlers
function onBtnEditClicked(evt){
    var activityJson=$(this).data(g_keyActivityJson);
	sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();

	var editor=generateActivityEditorByJson(activityJson);
	sectionActivityEditor.append(editor);
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

	try{
		$.post("/joinActivity", 
			{
				ActivityId: activityId.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){
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
					
				} else{

				}
			}
		);
	} catch(err){

	}
}

function onBtnDetailClicked(evt){
    evt.preventDefault();
    var activityId=$(this).data(g_keyActivityId);
    
	try{
        var detailPagePath="/show?page=activity_detail_page.html";
        var detailPage=window.open(detailPagePath);
        callbackOnPageLoaded(detailPage, function(){
                                                    loadJavaScript(detailPage, "queryActivityDetail("+activityId.toString()+");");
                                         }
        );
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
