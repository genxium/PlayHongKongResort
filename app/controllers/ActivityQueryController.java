package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.List;
import java.util.Map;

public class ActivityQueryController extends Controller {

    public static String s_refIndex="refIndex";
	public static String s_numItems="numItems";
	public static String s_direction="direction";
	public static Integer s_directionForward=(+1);
	public static Integer s_directionBackward=(-1);

    public static Result queryActivityDetail(Integer activityId){
        response().setContentType("text/plain");
        do{
            ObjectNode result = null;
            try{
                ActivityDetail activityDetail=SQLCommander.queryActivityDetailByActivityId(activityId);
                if(activityDetail==null) break;
                result=activityDetail.toObjectNode();
                return ok(result);
            } catch(Exception e){
                System.out.println("ActivityController.queryActivityDetail: "+e.getMessage());
            }
        }while(false);
        return ok();
    }

    public static Result queryDefaultActivities(Integer refIndex, Integer numItems, Integer direction){
        response().setContentType("text/plain");
        do{
            try{
                List<Activity> activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(lastActivityId, s_itemsPerPage);
                ObjectNode result = play.libs.Json.newObject();

                for(Activity activity : activities){
                    result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages());
                }
                return ok(result);
            } catch(Exception e){
                System.out.println("Application.queryDefaultActivities: "+e.getMessage());
            }
        }while(false);
        return badRequest();
    }

    public static Result queryDefaultActivitiesByUser(Integer refIndex, Integer numItems, Integer direction, String token){
        response().setContentType("text/plain");
        do{
            try{
                int userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                List<Activity> activities=SQLCommander.queryAcceptedActivitiesByUserIdInChronologicalOrder(lastActivityId, s_itemsPerPage, userId);
                if(activities==null) break;
                ObjectNode result = Json.newObject();

                for(Activity activity : activities){
                    result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages());
                }
                return ok(result);
            } catch(Exception e){
                System.out.println("Application.queryDefaultActivitiesByUser: "+e.getMessage());
            }
        }while(false);
        return badRequest();
    }
    public static Result queryActivitiesHostedByUser(){
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] lastActivityIds=formData.get(s_lastActivityId);
            if(lastActivityIds==null) break;
            Integer lastActivityId=Integer.parseInt(lastActivityIds[0]);

            String[] tokens=formData.get(User.tokenKey);
            if(tokens==null) break;
            String token=tokens[0];

            Integer userId= DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;

            User user=SQLCommander.queryUser(userId);
            UserActivityRelation.RelationType relation=UserActivityRelation.RelationType.host;

            try{
                List<Activity> activities=SQLCommander.queryActivitiesByUserAndRelation(user, relation, lastActivityId, s_itemsPerPage);
                if(activities==null) break;
                ObjectNode result = play.libs.Json.newObject();

                for(Activity activity : activities){
                    result.put(String.valueOf(activity.getId()), activity.toObjectNode());
                }
                return ok(result);
            } catch(Exception e){
                System.out.println("Application.queryActivitiesHostedByUser:"+e.getMessage());
            }
        }while(false);
        return ok();
    }

}
