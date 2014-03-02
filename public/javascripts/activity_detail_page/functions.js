function displayActivityDetail(activityDetailJson){
    var activityTitle=g_activityDetailJson[g_keyActivityTitle];
    var activityContent=g_activityDetailJson[g_keyActivityContent];
    var activityImages=g_activityDetailJson[g_keyActivityImages];

    var body=$.("body");
    var detailView=generateActivityDetailViewByJson(activityDetailJson);
    body.append(detailView);
}
