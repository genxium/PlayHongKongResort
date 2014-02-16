package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import model.*;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.util.*;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;

public class Application extends Controller {

    public static String s_homepageName="homepage.html";
    public static String s_indexImageOfActivityPrefix="indexImageOfActivityPrefix";
    public static String s_pageIndexKey="pageIndex";

    public static Integer s_itemsPerPage=6; // hard coded for now

    public static Result index() {
        return show(s_homepageName);
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
      		String[] emails=formData.get(User.emailKey);
      		String[] passwords=formData.get(User.passwordKey);
      		String email=emails[0];
      		String password=passwords[0];
      		
      		String passwordDigest=Converter.md5(password);
  		
          User user=SQLCommander.queryUserByEmail(email);

          if(user==null || user.getPassword().equals(passwordDigest)==false) break;
          	
      		String token = Converter.generateToken(email, password);
      		Integer userId = user.getUserId();

  		    session(token, userId.toString());

          int imageId=user.getAvatar();
          Image image=SQLCommander.queryImageByImageId(imageId);

  		    ObjectNode result = Json.newObject();
  		    result.put(User.idKey, user.getUserId());
  		    result.put(User.emailKey, user.getEmail());
  		    result.put(User.tokenKey, token);
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
        		String[] emails=formData.get(User.emailKey);
        		String[] passwords=formData.get(User.passwordKey);
        		String email=emails[0];
        		String password=passwords[0];
        		String name=DataUtils.getNameByEmail(email);
        		UserGroup.GroupType userGroup=UserGroup.GroupType.user;

        		String passwordDigest=Converter.md5(password);    
                User user=User.create(email, passwordDigest, name, userGroup);
                int lastId=SQLCommander.registerUser(user);
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
        		User user=SQLCommander.queryUserByUserId(userId);
        		
        		if(user==null) break;
            try {
                session(token, userId.toString());
                String emailKey=User.emailKey;
                String tokenKey=User.tokenKey;

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

    	  do{
    		  RequestBody body = request().body();
    	  
    		  // get file data from request body stream
    		  MultipartFormData data = body.asMultipartFormData();
    		  FilePart avatarFile = data.getFile("Avatar");

    		  // get user token from request body stream
    		  String token=DataUtils.getUserToken(data);
    		  int userId=DataUtils.getUserIdByToken(token);
    		  if(userId==DataUtils.invalidId) break;
    		  User user=SQLCommander.queryUserByUserId(userId);
    		  if(user==null) break;

    		  if(avatarFile==null) break;
    		  int previousAvatarId=user.getAvatar();
    		  int newAvatarId=ExtraCommander.saveAvatarFile(avatarFile, user);
    		  if(newAvatarId==ExtraCommander.invalidId) break;
                
           // delete previous avatar record and file
           Image previousAvatar=SQLCommander.queryImageByImageId(previousAvatarId);
           boolean isPreviousAvatarDeleted=ExtraCommander.deleteImageRecordAndFile(previousAvatar);
           if(isPreviousAvatarDeleted==true){
                System.out.println("Application.saveAvatarFile: previous avatar file and record deleted.");    
           }
         
    		  return ok("Avatar uploaded");
    	  
    	  }while(false);
    	  return badRequest("Avatar not uploaded!");
    }
    
    public static Result createActivity(){
    	// define response attributes
  	  	response().setContentType("text/plain");
  	  	
  	  	Map<String, String[]> formData=request().body().asFormUrlEncoded();
     		String[] tokens=formData.get(User.tokenKey);
  	  	String token=tokens[0];

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

    public static Result editActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] ids=formData.get(Activity.idKey);
                String[] tokens=formData.get(User.tokenKey);
                
                Integer activityId=Integer.parseInt(ids[0]);
                String token=tokens[0];
              
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                
                Activity activity=SQLCommander.queryActivityByActivityId(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;
                
                ObjectNode singleActivityNode=Json.newObject();
                singleActivityNode.put(Activity.idKey, activityId.toString());
                singleActivityNode.put(Activity.titleKey, activity.getTitle());
                singleActivityNode.put(Activity.contentKey, activity.getContent());
                  
                List<Image> images=SQLCommander.queryImagesByActivityId(activityId);
                if(images!=null && images.size()>0){
                    Integer index=0;
                    Iterator<Image> itImage=images.iterator();
                    while(itImage.hasNext()){
                        Image image=itImage.next();
                        String protocolKey=s_indexImageOfActivityPrefix+index.toString();
                        singleActivityNode.put(protocolKey, image.getImageURL());
                        ++index;
                    }
                }

                return ok(singleActivityNode);
            } catch(Exception e){
                System.out.println("Application.editActivity:"+e.getMessage());
            }
        } while(false);
        return badRequest("Not allowed to edit!");
    }
    
    public static Result updateActivity(){
        // define response attributes
        response().setContentType("text/plain");
        
        do{
            try{
                RequestBody body = request().body();
                
                // get file data from request body stream
                MultipartFormData data = body.asMultipartFormData();
                
                List<FilePart> imageFiles=data.getFiles();
                
                // get user token and activity id from request body stream
                Map<String, String[]> formData= data.asFormUrlEncoded();
                
                String[] tokens=formData.get(User.tokenKey);
                String[] activityIds=formData.get(Activity.idKey);

                String token=tokens[0];
                int userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
      	    		User user=SQLCommander.queryUserByUserId(userId);
      	    		if(user==null) break;

      	        int activityId=Integer.parseInt(activityIds[0]);
    	            
                // get activity title and content
                String[] activityTitles=formData.get(Activity.titleKey);
                String[] activityContents=formData.get(Activity.contentKey);
                
                String activityTitle=activityTitles[0];
                String activityContent=activityContents[0];
               
                // get activity begin time and deadline
                String[] activityBeginTimes=formData.get(Activity.beginTimeKey);
                String[] activityDeadlines=formData.get(Activity.deadlineKey);

                String activityBeginTime=activityBeginTimes[0];
                String activityDeadline=activityDeadlines[0];

	  			Activity activity=SQLCommander.queryActivityByActivityId(activityId);
    	  		if(SQLCommander.isActivityEditable(userId, activity)==false) break;
    	  	
    	  		activity.setTitle(activityTitle);
    	  		activity.setContent(activityContent);
                activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
    	  		activity.setDeadline(Timestamp.valueOf(activityDeadline));

      	  		boolean res=SQLCommander.updateActivity(activity);
      	  		if(res==false) break;
      	  		
      	  		// save new images
      	  		List<Image> previousImages=SQLCommander.queryImagesByActivityId(activityId);
                if(imageFiles!=null && imageFiles.size()>0){
      	  			Iterator<FilePart> imageIterator=imageFiles.iterator();
      	  			while(imageIterator.hasNext()){
            			FilePart imageFile=imageIterator.next();
            			int newImageId=ExtraCommander.saveImageOfActivity(imageFile, user, activity);
              		    if(newImageId==ExtraCommander.invalidId) break;
            		}
                }
                
                // delete previous images
                if(previousImages!=null && previousImages.size()>0){
                    Iterator<Image> itPreviousImage=previousImages.iterator();
                    while(itPreviousImage.hasNext()){
                    		Image previousImage=itPreviousImage.next();
                    		boolean isDeleted=ExtraCommander.deleteImageRecordAndFileOfActivity(previousImage, activityId);
                    		if(isDeleted==false) break;
                    }
                }
            
	  		} catch(Exception e){
  	  		    System.out.println("Application.updateActivity:"+e.getMessage());
	  		}
	  		return ok("Activity updated");
      
        }while(false);
        return badRequest("Activity not updated!");
    }
    
    public static Result submitActivity(){
		// define response attributes
    		response().setContentType("text/plain");
    		do{
                try{
        			RequestBody body = request().body();
            
        			// get file data from request body stream
        			MultipartFormData data = body.asMultipartFormData();
            
        			List<FilePart> imageFiles=data.getFiles();
            
        			// get user token and activity id from request body stream
        			Map<String, String[]> formData= data.asFormUrlEncoded();
         
        			String[] tokens=formData.get(User.tokenKey);
        			String[] activityIds=formData.get(Activity.idKey);

        			String token=tokens[0];
        			int userId=DataUtils.getUserIdByToken(token);
        			if(userId==DataUtils.invalidId) break;
        			User user=SQLCommander.queryUserByUserId(userId);
        			if(user==null) break;

        			int activityId=Integer.parseInt(activityIds[0]);
              
        			// get activity title and content
        			String[] activityTitles=formData.get(Activity.titleKey);
            		String[] activityContents=formData.get(Activity.contentKey);
            
            		String activityTitle=activityTitles[0];
            		String activityContent=activityContents[0];

                    // get activity begin time and deadline
                    String[] activityBeginTimes=formData.get(Activity.beginTimeKey);
                    String[] activityDeadlines=formData.get(Activity.deadlineKey);

                    String activityBeginTime=activityBeginTimes[0];
                    String activityDeadline=activityDeadlines[0];
        		
        			if(DataUtils.validateTitle(activityTitle)==false || DataUtils.validateContent(activityContent)==false) break;
        			Activity activity=SQLCommander.queryActivityByActivityId(activityId);
        			if(SQLCommander.isActivityEditable(userId, activity)==false) break;
          
        			activity.setTitle(activityTitle);
        			activity.setContent(activityContent);
                    activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
                    activity.setDeadline(Timestamp.valueOf(activityDeadline));
            
        			boolean res=SQLCommander.submitActivity(userId, activity);
        			if(res==false) break;
            
        			// save new images
                    List<Image> previousImages=SQLCommander.queryImagesByActivityId(activityId);
                    if(imageFiles!=null && imageFiles.size()>0){
                        Iterator<FilePart> imageIterator=imageFiles.iterator();
                        while(imageIterator.hasNext()){
                            FilePart imageFile=imageIterator.next();
                            int newImageId=ExtraCommander.saveImageOfActivity(imageFile, user, activity);
                            if(newImageId==ExtraCommander.invalidId) break;
                        }
                    }
                    
                    // delete previous images
                    if(previousImages!=null && previousImages.size()>0){
                        Iterator<Image> itPreviousImage=previousImages.iterator();
                        while(itPreviousImage.hasNext()){
                                Image previousImage=itPreviousImage.next();
                                boolean isDeleted=ExtraCommander.deleteImageRecordAndFileOfActivity(previousImage, activityId);
                                if(isDeleted==false) break;
                        }
                    }
        			return ok("Activity submitted");

        		} catch(Exception e){
        			System.out.println("Application.submitActivity:"+e.getMessage());
        		}

      	}while(false);
    		return badRequest("Activity not submitted!");	
    }

    
    public static Result deleteActivity(){
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
            
            Activity activity=SQLCommander.queryActivityByActivityId(activityId);
            if(SQLCommander.isActivityEditable(userId, activity)==false) break;
            
            try{
                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.deleteActivity: "+e.getMessage());
            }
            return ok("Activity deleted");
        } while(false);
        return badRequest("Activity not completely deleted!");
    }

    public static Result queryDefaultActivities(){
        response().setContentType("text/plain");
         do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] pageIndexes=formData.get(s_pageIndexKey);
                if(pageIndexes==null) break;

                Integer pageIndex=Integer.parseInt(pageIndexes[0]);

                List<JSONObject> activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(pageIndex, s_itemsPerPage);
                Iterator<JSONObject> itActivity=activities.iterator();
                ObjectNode result = Json.newObject();
            
                while(itActivity.hasNext()){
                    JSONObject activityJSON=itActivity.next();
                    Integer activityId=(Integer)activityJSON.get(Activity.idKey);
                    String activityTitle=(String)activityJSON.get(Activity.titleKey);
                    String activityContent=(String)activityJSON.get(Activity.contentKey);
                    List<Image> images=SQLCommander.queryImagesByActivityId(activityId);

                    ObjectNode singleActivityNode=Json.newObject();
                    singleActivityNode.put(Activity.idKey, activityId.toString());
                    singleActivityNode.put(Activity.titleKey, activityTitle);
                    singleActivityNode.put(Activity.contentKey, activityContent);
                    if(images!=null && images.size()>0){
                        Iterator<Image> itImage=images.iterator();
                        if(itImage.hasNext()){
                            Image firstImage=itImage.next();
                            String firstImageURL=firstImage.getImageURL();
                            singleActivityNode.put(Image.urlKey, firstImageURL);
                        }
                    }
                        
                    result.put(activityId.toString(), singleActivityNode);
                }
                return ok(result);
            } catch(Exception e){
                System.out.println("Application.queryDefaultActivities: "+e.getMessage());
            }
        }while(false);
        return badRequest();
    }

    public static Result queryActivitiesHostedByUser(){
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] pageIndexes=formData.get(s_pageIndexKey);
            if(pageIndexes==null) break;
            Integer pageIndex=Integer.parseInt(pageIndexes[0]);
            
            String[] tokens=formData.get(User.tokenKey);
            if(tokens==null) break;
            String token=tokens[0];

            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            
            ObjectNode result = null;
            
            User user=SQLCommander.queryUserByUserId(userId);
            UserActivityRelation.RelationType relation=UserActivityRelation.RelationType.host;
            
            try{
        		List<JSONObject> activities=SQLCommander.queryActivitiesByUserAndRelation(user, relation, pageIndex, s_itemsPerPage);
				if(activities==null) break;
        		Iterator<JSONObject> itActivity=activities.iterator();
        		result=Json.newObject();
        		
        		while(itActivity.hasNext()){
        			JSONObject activityJSON=itActivity.next();
        			Integer activityId=(Integer)activityJSON.get(Activity.idKey);
        			String activityTitle=(String)activityJSON.get(Activity.titleKey);
        			String activityContent=(String)activityJSON.get(Activity.contentKey);
        			Integer activityStatus=(Integer)activityJSON.get(Activity.statusKey);
        			List<Image> images=SQLCommander.queryImagesByActivityId(activityId);

        			ObjectNode singleActivityNode=Json.newObject();
        			singleActivityNode.put(Activity.idKey, activityId.toString());
        			singleActivityNode.put(Activity.titleKey, activityTitle);
        			singleActivityNode.put(Activity.contentKey, activityContent);
        			singleActivityNode.put(Activity.statusKey, activityStatus.toString());
        			
        			if(images!=null && images.size()>0){
      			       Iterator<Image> itImage=images.iterator();
      			       if(itImage.hasNext()){
        				  Image firstImage=itImage.next();
        				  String firstImageURL=firstImage.getImageURL();
        				  singleActivityNode.put(Image.urlKey, firstImageURL);
      			       }
        			}
        			result.put(activityId.toString(), singleActivityNode);
        		}
                return ok(result);
            } catch(Exception e){
            	System.out.println("Application.queryActivitiesHostedByUser:"+e.getMessage());
            }
      }while(false);
      return ok();
    }
    
    
    public static Result joinActivity(){
    		// define response attributes
        response().setContentType("text/plain");
        do{  
          Map<String, String[]> formData=request().body().asFormUrlEncoded();
    	  	String[] ids=formData.get(Activity.idKey);
    	  	String[] tokens=formData.get(User.tokenKey);
      	  
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

    public static Result queryDefaultActivitiesByUser(){
		response().setContentType("text/plain");
        do{
    		try{
    			Map<String, String[]> formData=request().body().asFormUrlEncoded();

                String[] pageIndexes=formData.get(s_pageIndexKey);
                if(pageIndexes==null) break;
                Integer pageIndex=Integer.parseInt(pageIndexes[0]);
                
    	  	  	String[] tokens=formData.get(User.tokenKey);
    	    	if(tokens==null) break;
    	    	String token=tokens[0];

    	    	int userId=DataUtils.getUserIdByToken(token);
    	    	if(userId==DataUtils.invalidId) break;
    			List<JSONObject> records=SQLCommander.queryAcceptedActivitiesByUserIdInChronologicalOrder(pageIndex, s_itemsPerPage, userId);
				if(records==null) break;
      			Iterator<JSONObject> itRecord=records.iterator();
      			ObjectNode result = Json.newObject();
      		
      			while(itRecord.hasNext()){
        				JSONObject recordJson=itRecord.next();
        				Integer activityId=(Integer)recordJson.get(Activity.idKey);
        				String activityTitle=(String)recordJson.get(Activity.titleKey);
        				String activityContent=(String)recordJson.get(Activity.contentKey);
        				Integer userActivityRelationId=(Integer)recordJson.get(UserActivityRelationTable.relationIdKey);
                        List<Image> images=SQLCommander.queryImagesByActivityId(activityId);

        				ObjectNode singleRecordNode=Json.newObject();
        				singleRecordNode.put(Activity.idKey, activityId.toString());
        				singleRecordNode.put(Activity.titleKey, activityTitle);
        				singleRecordNode.put(Activity.contentKey, activityContent);
        				if(userActivityRelationId!=null){
        					singleRecordNode.put(UserActivityRelationTable.relationIdKey, userActivityRelationId.toString());
        				}
                        if(images!=null && images.size()>0){
                            Iterator<Image> itImage=images.iterator();
                            if(itImage.hasNext()){
                                Image firstImage=itImage.next();
                                String firstImageURL=firstImage.getImageURL();
                                singleRecordNode.put(Image.urlKey, firstImageURL);
                            }
                        }
                
        				result.put(activityId.toString(), singleRecordNode);
      			}
      			return ok(result);
    		} catch(Exception e){
    			System.out.println("Application.queryDefaultActivitiesByUser: "+e.getMessage());   
    	    }
        }while(false);
        return badRequest();  
    }
    
    public static Result queryRelationOfUserAndActivity(){
    	// define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
          	  	String[] ids=formData.get(Activity.idKey);
          	  	String[] tokens=formData.get(User.tokenKey);
            	  
             	Integer activityId=Integer.parseInt(ids[0]);
        		String token=tokens[0];
        		
        		ObjectNode ret=null;
    			Integer userId=DataUtils.getUserIdByToken(token);
    			UserActivityRelation.RelationType relation=SQLCommander.queryRelationOfUserIdAndActivity(userId, activityId);
    			
    			if(relation==null) break;
    			ret=Json.newObject();
    			ret.put(UserActivityRelationTable.relationIdKey, new Integer(relation.ordinal()).toString());

        		return ok(ret);
            } catch(Exception e){
                System.out.println("Application.queryRelationOfUserAndActivity: "+e.getMessage());
            }
        }while(false);
        return badRequest();
    }

    public static Result logout(){
    		response().setContentType("text/plain");
        Map<String, String[]> formData=request().body().asFormUrlEncoded();
        String[] tokens=formData.get(User.tokenKey);
        String token=tokens[0];
        session().remove(token);
        return ok();
    }
}
