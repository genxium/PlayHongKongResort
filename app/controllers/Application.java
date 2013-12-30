package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import model.*;
import dao.SQLHelper;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.util.*;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

    public static Result index() {
       //return ok(index.render("Your new application is ready."));
    	   return ok("Got request " + request() + "!");
    }
    
    public static Result show(String page){
    		try{
    			String fullPath=Play.application().path()+"/app/views/"+page;
    			File file=new File(fullPath);
    			String content = new Scanner(file).useDelimiter("\\A").next();
    			response().setContentType("text/html");
    			return ok(content);
    		} catch(IOException e){
    			return badRequest();
    		}
    }
    
    public static Result login(){
		// define response attributes
		response().setContentType("text/plain");
	
		RequestBody body = request().body();
		Map<String, String[]> formData=body.asFormUrlEncoded();
		String[] emails=formData.get(BasicUser.emailKey);
		String[] passwords=formData.get(BasicUser.passwordKey);
		String email=emails[0];
		String password=passwords[0];
		
		String passwordDigest=Converter.md5(password);
		
        BasicUser user=SQLCommander.queryUserByEmail(email);

        if(user!=null && user.getPassword().equals(passwordDigest)){
        	
    		String token = Converter.generateToken(email, password);
    		Integer userId = user.getUserId();

		    session(token, userId.toString());

		    ObjectNode result = Json.newObject();
		    result.put(BasicUser.idKey, user.getUserId());
		    result.put(BasicUser.emailKey, user.getEmail());
		    result.put(BasicUser.tokenKey, token);
		    return ok(result);
        }
        return badRequest("User does not exist!");
    }
    
    public static Result register(){
    		// define response attributes
		response().setContentType("text/plain");
	
		RequestBody body = request().body();
		Map<String, String[]> formData=body.asFormUrlEncoded();
		String[] emails=formData.get(BasicUser.emailKey);
		String[] passwords=formData.get(BasicUser.passwordKey);
		String email=emails[0];
		String password=passwords[0];
		String name=DataUtils.getNameByEmail(email);
		
		String passwordDigest=Converter.md5(password);
        Guest guest=Guest.create(email, passwordDigest, name);
        int lastId=SQLCommander.registerUser(guest);
        if(lastId!=SQLCommander.invalidId){
        		return ok("Registered");
        } else{
        		return ok("Register failed");
        }
    }
    
    public static Result checkLoginStatus(){
		// define response attributes
		response().setContentType("text/plain");

		String token=DataUtils.getUserToken(request().body());
		Integer userId=DataUtils.getUserIdByToken(token);
		BasicUser user=SQLCommander.queryUserByUserId(userId);
		
		if(user!=null){
            try {
                  session(token, userId.toString());
                  String emailKey=BasicUser.emailKey;
                  String tokenKey=BasicUser.tokenKey;
                  ObjectNode result = Json.newObject();
                  result.put(emailKey, user.getEmail());
                  result.put(tokenKey, token);
                  return ok(result);
            } catch (Exception e) {
                  // TODO Auto-generated catch block
            }
		}
  		return ok("User doesn't exist or not logged in");
    }
    
    public static Result uploadingHandler() {
    	  // define response attributes
    	  response().setContentType("text/plain");
    	  
    	  RequestBody body = request().body();
    	  // get file data from request body stream
    	  MultipartFormData data = body.asMultipartFormData();
    	  FilePart picture = data.getFile("picture");
    	  
    	  // get user token from request body stream
    	  String token=DataUtils.getUserToken(data);
    	  
    	  if (picture != null) {
    	    String fileName = picture.getFilename();
    	    File file = picture.getFile();
    		String contentType=picture.getContentType();
    		try {
    	    		if(DataUtils.isImage(contentType)){
    	    			String rootDir=Play.application().path().getAbsolutePath();
    	    			file.renameTo(new File(rootDir+"/uploadedImages/"+fileName));
    	        	    return ok("File " + fileName +" uploaded token="+token);
    	    		}
        } catch (Exception e) {
            System.out.println("Problem operating on filesystem");
        }
    	    return ok("File " + fileName +"("+contentType+") upload failed");
    	  } else {
    	    flash("error", "Missing file");
    	    return redirect("/assets/homepage.html");
    	  }
    }
    
    public static Result createActivity(){
    	// define response attributes
  	  	response().setContentType("text/plain");
  	  	
  	  	Map<String, String[]> formData=request().body().asFormUrlEncoded();
     		String[] tokens=formData.get(BasicUser.tokenKey);
  	  	String token=tokens[0];

        String resultStr="Activity not created!";

        do{
      	  	Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;

            // create blank draft
      	  	Activity activity=Activity.create();
      	  	
      	  	try{

      	  			int lastActivityId=SQLCommander.createActivity(activity, userId);
      	  			if(lastActivityId!=SQLCommander.invalidId){
                    activity.setId(lastActivityId);
                    ObjectNode activityNode=Json.newObject();
                    activityNode.put(Activity.idKey, new Integer(lastActivityId).toString());
                    return ok(activityNode);
      	  			}

      	  	} catch(Exception e){
      	  	    System.out.println("Application.createActivity:"+e.getMessage());
      	  	}

        }while(false);

  	  	return badRequest();
    }
    
    public static Result updateActivity(){
    	// define response attributes
  	  	response().setContentType("text/plain");
  	  	
  	  	Map<String, String[]> formData=request().body().asFormUrlEncoded();
  	  	String[] ids=formData.get(Activity.idKey);
  	  	String[] titles=formData.get(Activity.titleKey);
  	  	String[] contents=formData.get(Activity.contentKey);
  	  	String[] tokens=formData.get(BasicUser.tokenKey);
    	  
     	Integer activityId=Integer.parseInt(ids[0]);
    		String title=titles[0];
    		String content=contents[0];
    		String token=tokens[0];
     	
     	Integer userId=DataUtils.getUserIdByToken(token); 
  	  	Activity activity=SQLCommander.queryActivityByActivityId(activityId);
  	  	if(SQLCommander.isActivityEditable(userId, activity)==false){
  	  		return badRequest();
  	  	}
  	  	
  	  	activity.setTitle(title);
  	  	activity.setContent(content);
  	  	
  	  	String resultStr="Activity not updated!";
  	  	try{
  	  		boolean res=SQLCommander.updateActivity(activity);
  	  		if(res==true){
  	  			resultStr="Activity updated";
  	  		}
  	  	} catch(Exception e){
  	  		System.out.println("Application.updateActivity:"+e.getMessage());
  	  	}
  	  	return ok(resultStr);
    }
    
    public static Result deleteActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
        String[] ids=formData.get(Activity.idKey);
        String[] tokens=formData.get(BasicUser.tokenKey);
        
        Integer activityId=Integer.parseInt(ids[0]);
        String token=tokens[0];
      
        Integer userId=DataUtils.getUserIdByToken(token);
        if(userId==DataUtils.invalidId){
        		return badRequest();
        }
        
        Activity activity=SQLCommander.queryActivityByActivityId(activityId);
        if(SQLCommander.isActivityEditable(userId, activity)==false){
        		return badRequest();
        }
        
        String resultStr="Activity not deleted!";
        try{
            boolean res=SQLCommander.deleteActivity(userId, activityId);
            if(res==true){
              resultStr="Activity deleted";
            }
        } catch(Exception e){
            System.out.println("Application.deleteActivity:"+e.getMessage());
        }
      return ok(resultStr);
    }

    public static Result queryActivitiesHostedByUser(){
        response().setContentType("text/plain");
        String token=DataUtils.getUserToken(request().body());
        Integer userId=DataUtils.getUserIdByToken(token);
        
        if(userId==DataUtils.invalidId) return badRequest();
        
        ObjectNode result = null;
        
        BasicUser user=SQLCommander.queryUserByUserId(userId);
        UserActivityRelation.RelationType relation=UserActivityRelation.RelationType.host;
        
        try{
        		List<JSONObject> activities=SQLCommander.queryActivitiesByUserAndRelation(user, relation);
        		Iterator<JSONObject> itActivity=activities.iterator();
        		result=Json.newObject();
        		
        		while(itActivity.hasNext()){
        			JSONObject activityJSON=itActivity.next();
        			Integer activityId=(Integer)activityJSON.get(Activity.idKey);
        			String activityTitle=(String)activityJSON.get(Activity.titleKey);
        			String activityContent=(String)activityJSON.get(Activity.contentKey);
        			Integer activityStatus=(Integer)activityJSON.get(Activity.statusKey);

        			ObjectNode singleActivityNode=Json.newObject();
        			singleActivityNode.put(Activity.idKey, activityId.toString());
        			singleActivityNode.put(Activity.titleKey, activityTitle);
        			singleActivityNode.put(Activity.contentKey, activityContent);
        			singleActivityNode.put(Activity.statusKey, activityStatus.toString());
        			result.put(activityId.toString(), singleActivityNode);
        		}
        } catch(Exception e){
        		System.out.println("Application.queryActivitiesHostedByUser:"+e.getMessage());
        }
        return ok(result);
    }
    
    public static Result submitActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
  	  	String[] ids=formData.get(Activity.idKey);
  	  	String[] titles=formData.get(Activity.titleKey);
  	  	String[] contents=formData.get(Activity.contentKey);
  	  	String[] tokens=formData.get(BasicUser.tokenKey);
    	  
     	Integer activityId=Integer.parseInt(ids[0]);
    		String title=titles[0];
    		String content=contents[0];
    		String token=tokens[0];
      
        Integer userId=DataUtils.getUserIdByToken(token);
        Activity activity=SQLCommander.queryActivityByActivityId(activityId);
        if(SQLCommander.isActivityEditable(userId, activity)==false){
        		return badRequest();
        }
        
        String resultStr="Activity not submitted!";
        try{
  	  		if(DataUtils.validateTitle(title)==false || DataUtils.validateContent(content)==false){
  	  			resultStr="Invalid title or content!";
  	  		} else{
  	  			boolean res=SQLCommander.submitActivity(userId, activity);
  	  			if(res==true){
  	  				resultStr="Activity submitted";
  	  			}
  	  		}
        } catch(Exception e){
            System.out.println("Application.submitActivity:"+e.getMessage());
        }
        return ok(resultStr);

    }

    public static Result joinActivity(){
    	// define response attributes
        response().setContentType("text/plain");
        
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
  	  	String[] ids=formData.get(Activity.idKey);
  	  	String[] tokens=formData.get(BasicUser.tokenKey);
    	  
     	Integer activityId=Integer.parseInt(ids[0]);
    		String token=tokens[0];
    		try{
    			Integer userId=DataUtils.getUserIdByToken(token);
    			Activity activity=SQLCommander.queryActivityByActivityId(activityId);
    			if(SQLCommander.isActivityJoinable(userId, activity)==false){
    				return badRequest();
    			}
        
    			boolean ret=SQLCommander.joinActivity(userId, activityId);
    			if(ret==false){
    				return badRequest();
    			}
    		} catch(Exception e){
    			System.out.println("Application.joinActivity:"+e.getMessage());
    		}
    		return ok();
    }

    public static Result queryDefaultActivities(){
    		response().setContentType("text/plain");
    		try{
          	List<JSONObject> activities=SQLCommander.queryAcceptedActivitiesByStatusAndChronologicalOrder();
      			Iterator<JSONObject> itActivity=activities.iterator();
      			ObjectNode result = Json.newObject();
      		
      			while(itActivity.hasNext()){
        				JSONObject activityJSON=itActivity.next();
        				Integer activityId=(Integer)activityJSON.get(Activity.idKey);
        				String activityTitle=(String)activityJSON.get(Activity.titleKey);
        				String activityContent=(String)activityJSON.get(Activity.contentKey);
        			
        				ObjectNode singleActivityNode=Json.newObject();
        				singleActivityNode.put(Activity.idKey, activityId.toString());
        				singleActivityNode.put(Activity.titleKey, activityTitle);
        				singleActivityNode.put(Activity.contentKey, activityContent);
        			
        				result.put(activityId.toString(), singleActivityNode);
      			}
      			return ok(result);
    		} catch(Exception e){
            System.out.println("Activity.queryDefaultActivities:"+e.getMessage());
  			    return badRequest();
		    }
    }

    public static Result logout(){
    		response().setContentType("text/plain");
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
        String[] tokens=formData.get(BasicUser.tokenKey);
        String token=tokens[0];
        session().remove(token);
        return ok();
    }
}
