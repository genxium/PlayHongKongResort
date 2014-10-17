function queryActivitiesAndRefresh() {
	queryActivities(0, 0, g_pagerContainer.nItems, g_pagerContainer.orientation, g_directionForward, g_vieweeId, g_pagerContainer.relation, g_pagerContainer.status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function showRegisterSection(){
	if(g_sectionRegister == null)	return;
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
	g_pagerContainer.screen.show();
}

function refreshOnLoggedIn(){
	hideRegisterSection();
}

