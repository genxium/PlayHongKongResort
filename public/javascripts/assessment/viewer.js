var g_pagerAssessments = null;

/**
 * Modal VIewer
 **/

var g_assessmentModalWidget = null;

function AssessmentModalWidget() {
	this.composeContent = function(data) {
		if(data == null || Object.keys(data).length == 0) {
			alert(ALERTS["no_assessment"]);
			return;
		}
		var assessments = [];
		for(var key in data) {
			var assessmentJson = data[key];
			var assessment = new Assessment(assessmentJson);
			assessments.push(assessment);
		}

		var ret = $("<div>", {
			style: "padding: 10%"
		}).appendTo(par);

		var tbl = $("<table class='assessments-viewer'>").appendTo(ret);
		var head = $("<tr class='assessments-viewer-row'>").appendTo(tbl);

		$('<th>', {
			text: TITLES["content"],
			"class": "assessments-viewer-header-content"
		}).appendTo(head);

		$('<th>', {
			text: TITLES["from"],
			"class": "assessments-viewer-header-from"
		}).appendTo(head);

		for(var i = 0; i < assessments.length; i++) {
			var assessment = assessments[i];
			var row = $("<tr class='assessments-viewer-row'>").appendTo(tbl);
			$('<td>', {
				text: assessment.content
			}).appendTo(row);
			var fromCell = $("<td>").appendTo(row);
			var iconSlot = $("<img>", {
				src: assessment.fromPlayer.avatar,
				"class": "assessments-viewer-avatar"
			}).appendTo(fromCell);
			var nameSlot = $("<span>", {
				text: assessment.fromPlayer.name,
				"class": "assessments-viewer-name"
			}).appendTo(fromCell);
		}
	}; 
}

AssessmentModalWidget.inherits(BaseModalWidget);

function initAssessmentModalWidget(par){
	g_assessmentModalWidget = new AssessmentModalWidget();
	g_assessmentModalWidget.appendTo(par, false);
}

function onQueryAssessmentsSuccess(data) {
	g_assessmentModalWidget.refresh(data);	
	g_assessmentModalWidget.show();
}

function onQueryAssessmentsError(err) {

}

function queryAssessments(refIndex, numItems, direction, to, activityId) {
	
	var token = getToken();
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
	queryAssessments(0, 20, g_directionForward, to, activityId);
}

/**
 * Pager Viewer
 **/

function AssessmentPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		// TODO: handle the 'page' param
		if (!data || Object.keys(data).length === 0) return;
		for (var key in data) {
			var json = data[key];
			var assessment = new Assessment(json);
			generateAssessmentTag(this.screen, assessment);	
		}
	};	
}

AssessmentPager.inherits(Pager);

function generateAssessmentTag(par, assessment) {
	$("<span>", {
		text: assessment.content,
		"class": "assessment-tag"
	}).click(assessment, function(evt) {
		evt.preventDefault();
		var aAssessment = evt.data;
		window.location.hash = ("detail?" + g_keyActivityId + "=" + aAssessment.activityId);
	}).appendTo(par);	
}

function onListAssessmentsSuccess(data) {
	g_pagerAssessments.refreshScreen(data);
}

function onListAssessmentsError(err) {

}

function generateAssessmentsListParams(pager, page) {
	if (page == null) return null;
	var token = getToken();
	if (token == null) {
		focusLogin();
		return;
	}

	var params = {};
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
	for (var key in pager.extraParams) {
		params[key] = pager.extraParams[key];
	}

	return params;
}

function listAssessments(page, onSuccess, onError) {
	// prototypes: onSuccess(data), onError(err)
	var params = generateAssessmentsListParams(g_pagerAssessments, page);
	$.ajax({
		type: "GET",
		url: "/assessment/list",
		data: params,
		success: function(data, status, xhr) {
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			onSuccess(data);
		},
		error: function(xhr, status, err) {
			onError(err);
		}
	});
}

function listAssessmentsAndRefresh() {
	var page = 1;
	listAssessments(page, onListAssessmentsSuccess, onListAssessmentsError);
}
