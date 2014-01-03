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

    public static String homepageName="homepage.html";

    public static Result index() {
        return show(homepageName);
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
    	  do{
      		RequestBody body = request().body();
      		Map<String, String[]> formData=body.asFormUrlEncoded();
      		String[] emails=formData.get(BasicUser.emailKey);
      		String[] passwords=formData.get(BasicUser.passwordKey);
      		String email=emails[0];
      		String password=passwords[0];
      		
      		String passwordDigest=Converter.md5(password);
  		
          BasicUser user=SQLCommander.queryUserByEmail(email);

          if(user==null || user.getPassword().equals(passwordDigest)==false) break;
          	
      		String token = Converter.generateToken(email, password);
      		Integer userId = user.getUserId();

  		    session(token, userId.toString());

          int imageId=user.getAvatar();
          Image image=SQLCommander.queryImageByImageId(imageId);

  		    ObjectNode result = Json.newObject();
  		    result.put(BasicUser.idKey, user.getUserId());
  		    result.put(BasicUser.emailKey, user.getEmail());
  		    result.put(BasicUser.tokenKey, token);
          if(image!=null){
              result.put(Image.urlKey, image.getImageURL());
          }
  		    return ok(result);
        
        }while(false);
        return badRequest("User does not exist!");
    }
    
    public static Result register(){
      	// define response attributes
  		  response().setContentType("text/plain");
    	  do{
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
            if(lastId==SQLCommander.invalidId) break;
            return ok("Registered");
        }while(false);
        return badRequest("Register failed");
    }
    
    public static Result checkLoginStatus(){
        do{
        		// define response attributes
        		response().setContentType("text/plain");

        		String token=DataUtils.getUserToken(request().body());
        		Integer userId=DataUtils.getUserIdByToken(token);
        		BasicUser user=SQLCommander.queryUserByUserId(userId);
        		
        		if(user==null) break;
            try {
                session(token, userId.toString());
                String emailKey=BasicUser.emailKey;
                String tokenKey=BasicUser.tokenKey;

                int imageId=user.getAvatar();
                Image image=SQLCommander.queryImageByImageId(imageId);

                ObjectNode result = Json.newObject();
                result.put(emailKey, user.getEmail());
                result.put(tokenKey, token);
                if(image!=null){
                    result.put(Image.urlKey, image.getImageURL());
                }
                return ok(result);
            } catch (Exception e) {
                System.out.println("Application.checkLoginStatus:"+e.getMessage());
            }
        }while(false);
  		  return badRequest("User doesn't exist or not logged in");
    }
    
    public static Result uploadAvatar() {
    	  // define response attributes
    	  response().setContentType("text/plain");
    	  
    	  RequestBody body = request().body();
    	  
    	  // get file data from request body stream
    	  MultipartFormData data = body.asMultipartFormData();
    	  FilePart avatarFile = data.getFile("Avatar");

    	  // get user token from request body stream
    	  String token=DataUtils.getUserToken(data);
    	  
    	  do{
    		  	if(avatarFile==null) break;
      	    String fileName = avatarFile.getFilename();
      	    File file = avatarFile.getFile();
      		  String contentType=avatarFile.getContentType();
        		try {
      	    		if(DataUtils.isImage(contentType)==false) break;
      	    		int userId=DataUtils.getUserIdByToken(token);
      	    		if(userId==DataUtils.invalidId) break;
      	    		BasicUser user=SQLCommander.queryUserByUserId(userId);
      	    		if(user==null) break;

                int previousImageId=user.getAvatar();

                String urlFolderName="assets/images";
      	    		String newImageName=DataUtils.generateUploadedImageName(fileName, token);
                String imageURL="/"+urlFolderName+"/"+newImageName;

                String rootDir=Play.application().path().getAbsolutePath();
                String absoluteFolderName="public/images";
                String imageAbsolutePath=rootDir+"/"+absoluteFolderName+"/"+newImageName;

      	    		int imageId=SQLCommander.uploadUserAvatar(user, imageAbsolutePath, imageURL);
      	    		if(imageId==SQLCommander.invalidId) break;
      	    		
      	    		// Save renamed file to server storage at the final step
      	    		boolean renamingResult=file.renameTo(new File(imageAbsolutePath));

                if(renamingResult==false){
                    System.out.println("Application.uploadAvatar: "+newImageName+" could not be saved.");
                    // recover table `Image`
                    boolean isRecovered=SQLCommander.deleteImageByImageId(imageId);
                    // TODO...
                    break;
                } else{
                    boolean isPreviousAvatarDeleted=SQLCommander.deleteImageByImageId(previousImageId);
                    // TODO...
                }

      	    		return ok(newImageName);
      	    		
            } catch (Exception e) {
                System.out.println("Application.uploadAvatar: "+e.getMessage());
            }
    	  }while(false);
    	  return badRequest();
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
  	  	do{
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
    	  	if(SQLCommander.isActivityEditable(userId, activity)==false) break;
    	  	
    	  	activity.setTitle(title);
    	  	activity.setContent(content);
    	  
      	  try{
      	  		boolean res=SQLCommander.updateActivity(activity);
      	  		if(res==false) break;
      	  } catch(Exception e){
      	  		System.out.println("Application.updateActivity:"+e.getMessage());
      	  }
    	  	return ok("Activity updated");
        } while(false);
        return badRequest("Activity not updated!"); 
    }
    
    public static Result deleteActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(BasicUser.tokenKey);
            
            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
          
            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            
            Activity activity=SQLCommander.queryActivityByActivityId(activityId);
            if(SQLCommander.isActivityEditable(userId, activity)==false) break;
            
            try{
                boolean res=SQLCommander.deleteActivity(userId, activityId);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.deleteActivity:"+e.getMessage());
            }
            return ok("Activity deleted");
        } while(false);
        return badRequest("Activity not deleted!");
    }

    public static Result queryActivitiesHostedByUser(){
        response().setContentType("text/plain");
        do{
            String token=DataUtils.getUserToken(request().body());
            Integer userId=DataUtils.getUserIdByToken(token);
            
            if(userId==DataUtils.invalidId) break;
            
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
      }while(false);
      return badRequest();
    }
    
    public static Result submitActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        do{
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
            if(SQLCommander.isActivityEditable(userId, activity)==false) break;
            
            try{
        	  		if(DataUtils.validateTitle(title)==false || DataUtils.validateContent(content)==false) break;
        	  		boolean res=SQLCommander.submitActivity(userId, activity);
        	  		if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.submitActivity:"+e.getMessage());
            }
            return ok("Activity submitted");
        }while(false);
        return badRequest("Activity not submitted!");
    }

    public static Result joinActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        do{  
          Map<String, String[]> formData=request().body().asFormUrlEncoded();
    	  	String[] ids=formData.get(Activity.idKey);
    	  	String[] tokens=formData.get(BasicUser.tokenKey);
      	  
        	Integer activityId=Integer.parseInt(ids[0]);
      		String token=tokens[0];
      		try{
        			Integer userId=DataUtils.getUserIdByToken(token);
        			Activity activity=SQLCommander.queryActivityByActivityId(activityId);
        			if(SQLCommander.isActivityJoinable(userId, activity)==false) break;
        			boolean result=SQLCommander.joinActivity(userId, activityId);
        			if(result==false) break;
      		} catch(Exception e){
        			System.out.println("Application.joinActivity:"+e.getMessage());
      		}
      		return ok("Successfully joined activity");
        }while(false);
        return badRequest("Could not join activity");
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
    
    public static Result queryDefaultActivitiesByUser(){
		response().setContentType("text/plain");
		try{
			Map<String, String[]> formData=request().body().asFormUrlEncoded();
	  	  	String[] tokens=formData.get(BasicUser.tokenKey);
	    	  
	    		String token=tokens[0];
	    		int userId=DataUtils.getUserIdByToken(token);
	    		if(userId==DataUtils.invalidId){
	    			return badRequest();
	    		}
			List<JSONObject> records=SQLCommander.queryAcceptedActivitiesByStatusAndChronologicalOrderByUser(userId);
  			Iterator<JSONObject> itRecord=records.iterator();
  			ObjectNode result = Json.newObject();
  		
  			while(itRecord.hasNext()){
    				JSONObject recordJson=itRecord.next();
    				Integer activityId=(Integer)recordJson.get(Activity.idKey);
    				String activityTitle=(String)recordJson.get(Activity.titleKey);
    				String activityContent=(String)recordJson.get(Activity.contentKey);
    				Integer userActivityRelationId=(Integer)recordJson.get(UserActivityRelationTable.relationIdKey);
    				
    				ObjectNode singleRecordNode=Json.newObject();
    				singleRecordNode.put(Activity.idKey, activityId.toString());
    				singleRecordNode.put(Activity.titleKey, activityTitle);
    				singleRecordNode.put(Activity.contentKey, activityContent);
    				if(userActivityRelationId!=null){
    					singleRecordNode.put(UserActivityRelationTable.relationIdKey, userActivityRelationId.toString());
    				}
    				result.put(activityId.toString(), singleRecordNode);
  			}
  			return ok(result);
		} catch(Exception e){
			System.out.println("Activity.queryDefaultActivities:"+e.getMessage());
		    return badRequest();
	    }
}
    
    public static Result queryRelationOfUserAndActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
  	  	String[] ids=formData.get(Activity.idKey);
  	  	String[] tokens=formData.get(BasicUser.tokenKey);
    	  
     	Integer activityId=Integer.parseInt(ids[0]);
    		String token=tokens[0];
    		
    		ObjectNode ret=null;
    		
    		try{
    			Integer userId=DataUtils.getUserIdByToken(token);
    			
    			UserActivityRelation.RelationType relation=SQLCommander.queryRelationOfUserAndActivity(userId, activityId);
    			
    			if(relation==null){
    				return badRequest();
    			}
    			ret=Json.newObject();
    			ret.put(UserActivityRelationTable.relationIdKey, new Integer(relation.ordinal()).toString());
  
    		} catch(Exception e){
    			System.out.println("Application.joinActivity:"+e.getMessage());
    			return badRequest();
    		}
    		return ok(ret);
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
