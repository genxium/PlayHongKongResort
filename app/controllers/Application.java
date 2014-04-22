package controllers;

import play.*;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import model.*;

import java.io.*;
import java.util.*;

import play.libs.Json;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

    public static String s_homepageName="homepage.html";
    public static String s_indexImageOfActivityPrefix="indexImageOfActivityPrefix";

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

        }
        return badRequest();
    }

    public static Result detail(Integer activityId){
        try{

            Content html = views.html.detail.render(activityId);
            return ok(html);
        } catch (Exception e){

        }
        return badRequest();
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
                if(lastId==SQLCommander.s_invalidId) break;
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
        		User user=SQLCommander.queryUser(userId);
        		
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
    		  User user=SQLCommander.queryUser(userId);
    		  if(user==null) break;

    		  if(avatarFile==null) break;
    		  int previousAvatarId=user.getAvatar();
    		  int newAvatarId=ExtraCommander.saveAvatarFile(avatarFile, user);
    		  if(newAvatarId==ExtraCommander.s_invalidId) break;
                
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
    
    public static Result queryRelationOfUserAndActivity(){
    	// define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
          	  	String[] activityIds=formData.get(Activity.idKey);
          	  	String[] tokens=formData.get(User.tokenKey);
            	  
             	Integer activityId=Integer.parseInt(activityIds[0]);
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
