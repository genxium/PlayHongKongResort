var g_sectionAssessmentsViewer = null;
var g_modalAssessmentsViewer = null;
var g_assessmentsViewer = null;
var g_nAssessmentsPerPage = 20;
var g_pagerAssessments = null;

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
	
	var token = $.cookie(g_keyToken);
	if (token == null) {
		focusLogin();
		return;
	}

	var params = {};	

	if(refIndex != null) params[g_keyRefIndex] = refIndex;
	if(numItems != null) params[g_keyNumItems] = numItems;
	if(direction != null) params[g_keyDirection] = parseInt(direction);

	params[g_keyToken] = token;
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

function onListAssessmentsSuccess(data) {
	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null || Object.keys(jsonResponse).length == 0) return;
	var assessments = new Array();
	for(var key in jsonResponse) {
		var assessmentJson = jsonResponse[key];
		var assessment = new Assessment(assessmentJson);
		assessments.push(assessment);
	}
	/*
	 * should show results in a pager widget
	 * */	
}

function onLisAssessmentsError(err) {

}

function generateAssessmentsListParams(pager, page) {
	if (page == null) return null;
	if (g_tmpTo == null) return null;
	var token = $.cookie(g_keyToken);
	if (token == null) {
		focusLogin();
		return;
	}

	var params = {};
	params[g_keyTo] = g_tmpTo;
	params[g_keyToken] = token;
	
	var pageSt = page - 2;
	var pageEd = page + 2;
	var offset = pageSt < 1 ? (pageSt - 1) : 0;
	pageSt -= offset;
	pageEd -= offset;
	params[g_keyPageSt] = pageSt;
	params[g_keyPageEd] = pageEd;
	if (pager.nItems != null) params[g_keyNumItems] = pager.nItems;
	if (g_activityId != null) params[g_keyActivityId] = g_activityId;

	if (pager.filters != null) {
		for (var i = 0; i < pager.filters.length; ++i) {
			var filter = pager.filters[i];
			params[filter.key] = filter.selector.val();	
		}
	}

	if (pager.extraParams == null) return params;
	if (var key in pager.extraParams) {
		params[key] = pager.extraParams[key];
	}

	return params;
}

function listAssessments(page, to, onSuccess, onError) {
	// prototypes: onSuccess(data), onError(err)
	g_tmpTo = to;
	var params = generateAssessmentsListParams(g_pagerAssessments, page);
	g_tmpTo = null;

	$.ajax({
		type: "GET",
		url: "/assessment/list",
		data: params,
		success: function(data, status, xhr) {
		    onSuccess(data);
		},
		error: function(xhr, status, err) {
		    onError(err);
		}
	});
}

function listAssessmentsAndRefresh(to) {
	var page = 1;
	listAssessments(page, to, onListAssessmentsSuccess, onListAssessmentsError);
}
