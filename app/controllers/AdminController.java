package controllers;

import model.Activity;
import model.User;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.Map;

public class AdminController extends Controller {

	public static Result accept(){
		// define response attributes
		response().setContentType("text/plain");
		do{
			try{
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				Integer activityId = Integer.valueOf(formData.get(Activity.ID)[0]);
				String token = formData.get(User.TOKEN)[0];
			  
				Integer userId=DataUtils.getUserIdByToken(token);
				if(userId==null) break;
				User user=SQLCommander.queryUser(userId);
				if(user==null) break;
				if(validateAdminAccess(user)==false) break;

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
				if(validateAdminAccess(user)==false) break;
					
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
				if(validateAdminAccess(user)==false) break;

				boolean res=ExtraCommander.deleteActivity(activityId);
				if(res==false) break;
				return ok();
			} catch(Exception e){
				System.out.println("AdminController.delete: "+e.getMessage());
			}
		} while(false);
		return badRequest("Activity not completely deleted!");
	}
	
	public static boolean validateAdminAccess(User user){
                boolean ret=false;
		do{
			if(user==null) break;
			if(user.getGroupId()!=User.ADMIN) break;
			ret=true;
		}while(false);
		return ret;
	}
}
