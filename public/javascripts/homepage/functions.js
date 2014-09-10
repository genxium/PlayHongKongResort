/*
 * variables
 */

// general DOM elements
var g_sectionActivities = null;

var g_pagerContainer = null;
var g_pagerCoordinator = null;

/*
 * functions
 */

function onBtnPreviousPageClicked(evt){
	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var startingIndex=g_sectionActivities.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivities.data(g_keyEndingIndex);

	queryActivities(startingIndex, g_numItemsPerPage, g_orderDescend, g_directionBackward, null, null, g_statusAccepted, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onBtnNextPageClicked(evt){
	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var startingIndex=g_sectionActivities.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivities.data(g_keyEndingIndex);

	queryActivities(endingIndex, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, g_statusAccepted, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() < $(document).height() )	return;
	evt.preventDefault();
}

function showRegisterSection(){
	if(g_sectionRegister)	return;
	g_sectionRegister.show();
}

function hideRegisterSection(){
	if(g_sectionRegister == null)	return;
	g_sectionRegister.hide();
}

function removeRegisterSection(){
	if(g_sectionRegister == null)	return;
	g_sectionRegister.remove();
}

function refreshOnEnter(){
	showRegisterSection();
	emptyRegisterFields();
	g_sectionActivities.show();
}

function refreshOnLoggedIn(){
	hideRegisterSection();
}

function onQueryActivitiesSuccess(data, status, xhr){
    var jsonResponse = JSON.parse(data);
    if(jsonResponse != null && Object.keys(jsonResponse).length > 0){
        g_sectionActivities.empty();
        var idx = 0;
        var count = Object.keys(jsonResponse).length;
        // display contents
        for(var key in jsonResponse){
		var activityJson = jsonResponse[key];
		var activityId = activityJson[g_keyId];
		if(idx == 0)	g_sectionActivities.data(g_keyStartingIndex, activityId);
		if(idx == count-1)	g_sectionActivities.data(g_keyEndingIndex, activityId);
		var token = $.cookie(g_keyToken);
		var cell = generateActivityCell(activityJson);
		g_sectionActivities.append(cell);
		++idx;
        }
    }
} 

function onQueryActivitiesError(xhr, status, err){

}
