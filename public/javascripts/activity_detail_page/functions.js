function queryActivityDetail(activityId){
    var params={};
    params['activityId']=activityId;
    try{
        $.ajax({
            method: "GET",
            url: "/activity/detail",
            data: params,
            success: function(data, status, xhr){
                var activityDetailJson=JSON.parse(data);
                displayActivityDetail(activityDetailJson);
            },
            error: function(xhr, status, errThrown){

            }
        });

    } catch(err){

    }
}

function displayActivityDetail(activityDetailJson){
    var activityTitle=activityDetailJson[g_keyActivityTitle];
    var activityContent=activityDetailJson[g_keyActivityContent];
    var activityImages=activityDetailJson[g_keyActivityImages];

    var body=$("body");
    var detailView=generateActivityDetailViewByJson(activityDetailJson);
    body.append(detailView);
}
