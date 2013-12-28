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
		String[] emails=formData.get("email");
		String[] passwords=formData.get("password");
		String email=emails[0];
		String password=passwords[0];
		
		String passwordDigest=Converter.md5(password);
		
        BasicUser user=SQLCommander.queryUserByEmail(email);

        if(user!=null && user.getPassword().equals(passwordDigest)){
        	
        		String token = Converter.generateToken(email, password);
        		Integer userId = user.getUserId();
		    session(token, userId.toString());
		    String userIdKey="userId";
		    String emailKey="email";
		    String tokenKey="token";

		    ObjectNode result = Json.newObject();
		    result.put(userIdKey, user.getUserId());
		    result.put(emailKey, user.getEmail());
		    result.put(tokenKey, token);
		    return ok(result);
        }
        return badRequest("User does not exist!");
    }
    
    public static Result register(){
    		// define response attributes
		response().setContentType("text/plain");
	
		RequestBody body = request().body();
		Map<String, String[]> formData=body.asFormUrlEncoded();
		String[] emails=formData.get("email");
		String[] passwords=formData.get("password");
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
                  String tokenKey="token";
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
     		String[] tokens=formData.get("token");
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
    	  String[] ids=formData.get("activityId");
  	  	String[] titles=formData.get("activityTitle");
    	  String[] contents=formData.get("activityContent");
    	  String[] tokens=formData.get("token");
    	  
     		Integer activityId=Integer.parseInt(ids[0]);
     		String title=titles[0];
     		String content=contents[0];
     		String token=tokens[0];
     	
     	Integer userId=DataUtils.getUserIdByToken(token);
     	if(userId==DataUtils.invalidId){
     		return badRequest();
     	}

      // validate host relation
      UserActivityRelation.RelationType type=SQLCommander.queryRelationOfUserAndActivity(userId, activityId);
      if(type!=UserActivityRelation.RelationType.host){
        return badRequest();
      }
      
  	  	Activity activity=SQLCommander.queryActivityByActivityId(activityId);
  	  	activity.setTitle(title);
  	  	activity.setContent(content);
  	  	
  	  	String resultStr="Activity not updated!";
  	  	try{
  	  		if(DataUtils.validateTitle(title)==false || DataUtils.validateContent(content)==false){
  	  			resultStr="Invalid title or content!";
  	  		} else{
  	  			boolean res=SQLCommander.updateActivity(activity);
  	  			if(res==true){
  	  				resultStr="Activity updated";
  	  			}
  	  		}
  	  	} catch(Exception e){
  	  		System.out.println("Application.updateActivity:"+e.getMessage());
  	  	}
    	return ok(resultStr);
    }
    
    public static Result deleteActivity(){
    	return badRequest();
    }

    public static Result queryActivitiesHostedByUser(){
        response().setContentType("text/plain");
        String token=DataUtils.getUserToken(request().body());
        Integer userId=DataUtils.getUserIdByToken(token);
        
        if(userId==DataUtils.invalidId) return badRequest();
        
        BasicUser user=SQLCommander.queryUserByUserId(userId);
        UserActivityRelation.RelationType relation=UserActivityRelation.RelationType.host;
        
        try{
        		List<JSONObject> activities=SQLCommander.queryActivitiesByUserAndRelation(user, relation);
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
        		return badRequest();
        }
        
    }
    
    public static Result submitActivity(){
    		// define response attributes
  	  	response().setContentType("text/plain");
  	  	String token=DataUtils.getUserToken(request().body());
  	  	Integer userId=DataUtils.getUserIdByToken(token);
    		return ok();
    }

    public static Result joinActivity(){
    		// define response attributes
  	  	response().setContentType("text/plain");
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
			return badRequest();
		}
    }

    public static Result logout(){
    		response().setContentType("text/plain");
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
        String[] tokens=formData.get("token");
        String token=tokens[0];
        session().remove(token);
        return ok();
    }
}
