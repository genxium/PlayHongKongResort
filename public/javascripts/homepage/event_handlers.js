function onBtnPreviousPageClicked(evt){
	var pageIndex=g_sectionDefaultActivities.data(g_keyPageIndex);
    var startingIndex=g_sectionDefaultActivities.data(g_keyStartingIndex);
    var endingIndex=g_sectionDefaultActivities.data(g_keyEndingIndex);

    queryDefaultActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
    var pageIndex=g_sectionDefaultActivities.data(g_keyPageIndex);
    var startingIndex=g_sectionDefaultActivities.data(g_keyStartingIndex);
    var endingIndex=g_sectionDefaultActivities.data(g_keyEndingIndex);

    queryDefaultActivities(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}
