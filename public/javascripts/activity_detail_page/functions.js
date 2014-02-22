function displayCurrentActivityDetail(){
    var url=window.location().toString();
    var params=loc.split('?')[1];
    alert(params);
}

function displayActivityDetail(activityId){
    do{
        if(activityId==null) break;

        try{
            $.ajax({
                method: "POST",
                url: "/queryActivityDetail",
                data:{
                    ActivityId: activityId.toString()
                },
                success: function(data, status, xhr){
                    var jsonResponse=JSON.parse(data);
                    if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
                        var activityJson=jsonResponse;
                        var targetSection=$("#"+g_idSectionActivityDetail);
                        // clean target section
                        targetSection.empty();
                        // update page index of the target section
                        var cell=generateActivityDetailViewByJson(jsonActivity);
                        targetSection.append(cell);
                    }
                }
                error: function(xhr, status, errorThrown){

                }
            });
        } catch(err){

        }
        
    }while(false);
}
