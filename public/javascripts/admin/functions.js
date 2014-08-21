/*
 * variables
 */

var g_classAcceptedIndicator="classAcceptedIndicator";
var g_classDeletedIndicator="classDeletedIndicator";
var g_sectionActivityMonitor=null;

var g_selectFilter=null;
var g_classFilterOption="classFilterOption";

function onBtnPreviousPageClicked(evt){

	var pageIndex=g_sectionActivityMonitor.data(g_keyPageIndex);
	var startingIndex=g_sectionActivityMonitor.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivityMonitor.data(g_keyEndingIndex);

	var status = g_selectFilter.val();
	queryActivities(startingIndex, g_numItemsPerPage, g_orderDescend, g_directionBackward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onBtnNextPageClicked(evt){

	var pageIndex=g_sectionActivityMonitor.data(g_keyPageIndex);
	var startingIndex=g_sectionActivityMonitor.data(g_keyStartingIndex);
	var endingIndex=g_sectionActivityMonitor.data(g_keyEndingIndex);

	var status = g_selectFilter.val();
	queryActivities(endingIndex, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onSelectFilterChanged(evt){
	evt.preventDefault();
	var status = g_selectFilter.val();
	queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onQueryActivitiesSuccess(data, status, xhr){

	// clean target section
	g_sectionActivityMonitor.empty();

	var jsonResponse=JSON.parse(data);
	if(jsonResponse == null || Object.keys(jsonResponse).length <= 0) return;
	var idx=0;
	var count=Object.keys(jsonResponse).length;

	// display contents
	for(var key in jsonResponse){
	    var activityJson=jsonResponse[key];
	    var activityId=activityJson[g_keyId];
	    if(idx == 0)	g_sectionActivityMonitor.data(g_keyStartingIndex, activityId);
	    if(idx == count-1)	g_sectionActivityMonitor.data(g_keyEndingIndex, activityId);
	    var cell=generateActivityCellForAdmin(activityJson);
	    g_sectionActivityMonitor.append(cell);
	    ++idx;
	}
} 

function onQueryActivitiesError(xhr, status, err){

}
