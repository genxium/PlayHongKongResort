var g_sectionAssessmentsViewer = null;
var g_modalAssessmentsViewer = null;
var g_assessmentsViewer = null;
var g_nAssessmentsPerPage = 20;

function removeAssessmentsViewer(){
        if(g_sectionAssessmentsViewer == null) return;
        g_sectionAssessmentsViewer.hide();
        g_sectionAssessmentsViewer.modal("hide");
        if(g_modalAssessmentsViewer == null) return;
        g_modalAssessmentsViewer.empty();
        if(g_assessmentsViewer == null) return;
        g_assessmentsViewer.remove();
}

function initAssessmentsViewer(){
	var wrap = $("#wrap");
	/*
		Note: ALL attributes, especially the `class` attribute MUST be written INSIDE the div tag, bootstrap is NOT totally compatible with jQuery!!!
	*/
	g_sectionAssessmentsViewer = $("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='' aria-hidden='true'>", {
		style: "position: absolute; width: 80%; height: 80%;"
	}).appendTo(wrap);
	var dialog = $("<div>", {
		class: "modal-dialog modal-lg"
	}).appendTo(g_sectionAssessmentsViewer);
	g_modalAssessmentsViewer = $("<div>", {
		class: "modal-content"
	}).appendTo(dialog);	

	removeAssessmentsViewer();
}

function showAssessmentsViewer(assessments) {
		
	g_modalAssessmentsViewer.empty();
	g_assessmentsViewer = generateAssessmentsViewer(g_modalAssessmentsViewer, assessments);
	g_sectionAssessmentsViewer.modal({
		show: true
	});

}

function generateAssessmentsViewer(par, assessments) {
	if (assessments == null) return null;

	var ret = $("<div>", {
		style: "padding: 10%"
	}).appendTo(par);

	var tbl = $("<table class='assessments-viewer'>").appendTo(ret);
	var head = $("<tr class='assessments-viewer-row'>").appendTo(tbl);

	$('<th>', {
		text: "Content"
	}).appendTo(head);

	$('<th>', {
		text: "From"
	}).appendTo(head);

	for(var i = 0; i < assessments.length; i++) {
		var assessment = assessments[i];
		var row = $("<tr class='assessments-viewer-row'>").appendTo(tbl);
		$('<td>', {
			text: assessment.content
		}).appendTo(row);
		$('<td>', {
			text: assessment.from_name
		}).appendTo(row);
	}
	return ret;
} 

function onQueryAssessmentsSuccess(data) {
	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null || Object.keys(jsonResponse).length == 0) return;
	var assessments = new Array();
	for(var key in jsonResponse) {
		var assessmentJson = jsonResponse[key];
		var assessment = new Assessment(assessmentJson);
		assessments.push(assessment);
	}
	
	showAssessmentsViewer(assessments);				
	
}

function onQueryAssessmentsError(err) {

}

function queryAssessments(refIndex, numItems, direction, to, activityId) {
	
	var params = {};	

	if(refIndex != null) params[g_keyRefIndex] = refIndex;
	if(numItems != null) params[g_keyNumItems] = numItems;
	if(direction != null) params[g_keyDirection] = parseInt(direction);

	var token = $.cookie(g_keyToken);
	if(token != null) params[g_keyToken] = token;
        params[g_keyTo] = to;
	params[g_keyActivityId] = activityId;

	$.ajax({
		type: "GET",
		url: "/assessment/query",
		data: params,
		success: function(data, status, xhr) {
			onQueryAssessmentsSuccess(data);
		},
		error: function(xhr, status, err) {
			onQueryAssessmentsError(err);
		}
	});
}

function queryAssessmentsAndRefresh(to, activityId) {
	queryAssessments(0, g_nAssessmentsPerPage, g_directionForward, to, activityId);
}
