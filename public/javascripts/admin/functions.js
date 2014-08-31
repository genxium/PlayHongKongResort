/*
 * variables
 */

var g_classAcceptedIndicator="classAcceptedIndicator";
var g_classDeletedIndicator="classDeletedIndicator";
var g_sectionActivities=null;

var g_selectFilter=null;
var g_classFilterOption="classFilterOption";

function onBtnPreviousPageClicked(evt){

	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var startingIndex=g_sectionActivities.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivities.data(g_keyEndingIndex);

	var status = g_selectFilter.val();
	queryActivities(startingIndex, g_numItemsPerPage, g_orderDescend, g_directionBackward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onBtnNextPageClicked(evt){

	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var startingIndex=g_sectionActivities.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivities.data(g_keyEndingIndex);

	var status = g_selectFilter.val();
	queryActivities(endingIndex, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onSelectFilterChanged(evt){
	evt.preventDefault();
	var status = g_selectFilter.val();
	queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onQueryActivitiesSuccess(data, status, xhr){

	var jsonResponse=JSON.parse(data);
	if(jsonResponse == null) return;
	var count = Object.keys(jsonResponse).length;
	if(count <= 0) return;

	var oldStartingIndex = g_sectionActivities.data(g_keyStartingIndex);
	var oldEndingIndex = g_sectionActivities.data(g_keyEndingIndex);

	// clean target section
	g_sectionActivities.empty();

	var idx=0;
	var count=Object.keys(jsonResponse).length;

	// display contents
	for(var key in jsonResponse){
	    var activityJson=jsonResponse[key];
	    var activityId=activityJson[g_keyId];
	    if(idx == 0)	g_sectionActivities.data(g_keyStartingIndex, activityId);
	    if(idx == count-1)	g_sectionActivities.data(g_keyEndingIndex, activityId);
	    var cell=generateActivityCellForAdmin(activityJson);
	    g_sectionActivities.append(cell);
	    ++idx;
	}

	var pageIndex = g_sectionActivities.data(g_keyPageIndex);
	var order = (+1);
	var newStartingIndex = g_sectionActivities.data(g_keyStartingIndex);
	var newEndingIndex = g_sectionActivities.data(g_keyEndingIndex);
	if(order == +1 && newStartingIndex > oldEndingIndex) ++pageIndex;
	if(order == +1 && newEndingIndex < oldStartingIndex) --pageIndex;
	if(order == -1 && newStartingIndex < oldEndingIndex) ++pageIndex;
	if(order == -1 && newEndingIndex > oldStartingIndex) --pageIndex; 
	g_sectionActivities.data(g_keyPageIndex, pageIndex);
} 

function onQueryActivitiesError(xhr, status, err){

}
