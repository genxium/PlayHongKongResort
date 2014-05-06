function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);
	var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryDefaultActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

    var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryDefaultActivities(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}
