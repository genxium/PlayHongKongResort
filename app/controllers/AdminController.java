package controllers;

import play.mvc.*;
import model.*;

import java.util.List;
import java.util.*;

import play.libs.Json;
import utilities.DataUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AdminController extends Controller {

    public static Result accept(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
				Map<String, String[]> formData=request().body().asFormUrlEncoded();
				String[] ids=formData.get(Activity.ID);
				String[] tokens=formData.get(User.TOKEN);
				
				Integer activityId=Integer.parseInt(ids[0]);
				String token=tokens[0];
			  
				Integer userId=DataUtils.getUserIdByToken(token);
				if(userId==null) break;
				User user=SQLCommander.queryUser(userId);
				if(user==null) break;
				
				Activity activity=SQLCommander.queryActivity(activityId);
				if(activity==null) break;
            
                boolean res=SQLCommander.acceptActivity(user, activity);
                if(res==false) break;
				return ok();
            } catch(Exception e){
                System.out.println("AdminController.accept: "+e.getMessage());
            }
        } while(false);
        return badRequest("Activity not accepted!");
    }
    
    public static Result reject(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
				Map<String, String[]> formData=request().body().asFormUrlEncoded();
				String[] ids=formData.get(Activity.ID);
				String[] tokens=formData.get(User.TOKEN);
				
				Integer activityId=Integer.parseInt(ids[0]);
				String token=tokens[0];
			  
				Integer userId=DataUtils.getUserIdByToken(token);
				if(userId==null) break;
				User user=SQLCommander.queryUser(userId);
				if(user==null) break;
				
				Activity activity=SQLCommander.queryActivity(activityId);
				if(activity==null) break;
            
                boolean res=SQLCommander.rejectActivity(user, activity);
                if(res==false) break;
				return ok();
            } catch(Exception e){
                System.out.println("AdminController.accept: "+e.getMessage());
            }
        } while(false);
        return badRequest("Activity not accepted!");
    }

    public static Result delete(){
    	// define response attributes
        response().setContentType("text/plain");
        do{
            try{
				Map<String, String[]> formData=request().body().asFormUrlEncoded();
				String[] ids=formData.get(Activity.ID);
				String[] tokens=formData.get(User.TOKEN);
				
				Integer activityId=Integer.parseInt(ids[0]);
				String token=tokens[0];
			  
				Integer userId=DataUtils.getUserIdByToken(token);
				if(userId==null) break;
				
				User user=SQLCommander.queryUser(userId);
				if(user==null) break;
				
				if(SQLCommander.validateAdminAccess(user)==false) break;

                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
				return ok();
            } catch(Exception e){
                System.out.println("AdminController.delete: "+e.getMessage());
            }
        } while(false);
        return badRequest("Activity not completely deleted!");
    }
}
