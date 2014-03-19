package controllers;

import play.mvc.*;
import model.*;

import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.*;

import play.libs.Json;
import utilities.DataUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AdminController extends Controller {

    public static String s_lastActivityId="lastActivityId";
    public static Integer s_itemsPerPage=6; // hard coded for now

    public static Result acceptActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(User.tokenKey);
            
            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
          
            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            User user=SQLCommander.queryUser(userId);
            if(user==null) break;
            
            Activity activity=SQLCommander.queryActivity(activityId);
            if(activity==null) break;
            
            try{
                boolean res=SQLCommander.acceptActivity(user, activity);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.acceptActivity: "+e.getMessage());
            }
            return ok("Activity accepted");
        } while(false);
        return badRequest("Activity not accepted!");
    }
    
    public static Result deleteActivityByAdmin(){
    	// define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(User.tokenKey);
            
            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
          
            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            
            User user=SQLCommander.queryUser(userId);
            if(user==null) break;
            
            if(SQLCommander.validateAdminAccess(user)==false) break;

            try{
                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("AdminController.deleteActivityByAdmin: "+e.getMessage());
            }
            return ok("Activity deleted");
        } while(false);
        return badRequest("Activity not completely deleted!");
    }

    public static Result queryPendingActivitiesByAdmin(){
		response().setContentType("text/plain");
        do{
    		try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] lastActivityIds=formData.get(s_lastActivityId);
                if(lastActivityIds==null) break;
                Integer lastActivityId=Integer.parseInt(lastActivityIds[0]);
                
    			List<Activity> activities=SQLCommander.queryPendingActivitiesInChronologicalOrder(lastActivityId, s_itemsPerPage);
      			if(activities==null) break;
                ObjectNode result = Json.newObject();
      		
      			for(Activity activity : activities){
                    result.put(String.valueOf(activity), activity.toObjectNodeWithImages());
      			}
      			return ok(result);
    		} catch(Exception e){
                System.out.println("AdminController.queryPendingActivitiesByAdmin: "+e.getMessage());
    	    }
        }while(false);
        return badRequest();
    }
    
    public static Result queryAcceptedActivitiesByAdmin(){
		response().setContentType("text/plain");
        do{
    		try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] lastActivityIds=formData.get(s_lastActivityId);
                if(lastActivityIds==null) break;
                Integer lastActivityId=Integer.parseInt(lastActivityIds[0]);
                
    			List<Activity> activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(lastActivityId, s_itemsPerPage);
      			if(activities==null) break;

                ObjectNode result = Json.newObject();
      		
      			for(Activity activity : activities){
    				result.put(String.valueOf(activity), activity.toObjectNodeWithImages());
      			}
      			return ok(result);
    		} catch(Exception e){
                System.out.println("AdminController.queryAcceptedActivitiesByAdmin: "+e.getMessage());
    	    }
        }while(false);
        return ok();
    }
}
