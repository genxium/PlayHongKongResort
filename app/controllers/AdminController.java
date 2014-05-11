package controllers;

import play.mvc.*;
import model.*;

import java.util.List;
import java.util.*;

import play.libs.Json;
import utilities.DataUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AdminController extends Controller {

    public static Result acceptActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.ID);
            String[] tokens=formData.get(User.TOKEN);
            
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
            String[] ids=formData.get(Activity.ID);
            String[] tokens=formData.get(User.TOKEN);
            
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

    public static Result queryPendingActivitiesByAdmin(Object refIndex, Integer numItems, Integer direction, String token){
		response().setContentType("text/plain");
        do{
    		try{
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                if(SQLCommander.validateAdminAccess(user)==false) break;

                List<Activity> activities=SQLCommander.queryPendingActivitiesInChronologicalOrder(refIndex, numItems, direction, null);
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
    
    public static Result queryAcceptedActivitiesByAdmin(Object refIndex, Integer numItems, Integer direction, String token){
		response().setContentType("text/plain");
        do{
    		try{
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                if(SQLCommander.validateAdminAccess(user)==false) break;

                List<Activity> activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(refIndex, numItems, direction, null);
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
