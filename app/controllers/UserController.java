package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import model.*;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.RequestBody;

import java.util.*;

import utilities.Converter;
import utilities.DataUtils;
import utilities.General;

public class UserController extends Controller {

    public static Result login(){
      	// define response attributes
  		response().setContentType("text/plain");
    	do{
            try{
                Http.RequestBody body = request().body();
                Map<String, String[]> formData=body.asFormUrlEncoded();
                String email=formData.get(User.emailKey)[0];
                String password=formData.get(User.passwordKey)[0];

                if( (email==null || General.validateEmail(email)==false) || password==null) break;
                
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
            } catch(Exception e){
                    
            }        
        }while(false);
        return badRequest("User does not exist!");
    }
    
    public static Result register(){
      	// define response attributes
  		response().setContentType("text/plain");
    	do{
            try{
        		RequestBody body = request().body();
        		Map<String, String[]> formData=body.asFormUrlEncoded();
                String name=formData.get(User.nameKey)[0];
        		String email=formData.get(User.emailKey)[0];
        		String password=formData.get(User.passwordKey)[0];

                if(name==null || (email==null || General.validateEmail(email)==false) || password==null) break;
        		UserGroup.GroupType userGroup=UserGroup.GroupType.user;
        		String passwordDigest=Converter.md5(password);    
                User user=User.create(email, passwordDigest, name, userGroup);
                int lastId=SQLCommander.registerUser(user);
                if(lastId==SQLCommander.INVALID) break;
                return ok("Registered");
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest("Register failed");
    }
    
    public static Result status(String token){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                User user=SQLCommander.queryUser(userId);
                
                if(user==null) break;
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
    		  Http.MultipartFormData data = body.asMultipartFormData();
    		  Http.MultipartFormData.FilePart avatarFile = data.getFile("Avatar");

    		  // get user token from request body stream
    		  String token=DataUtils.getUserToken(data);
    		  int userId=DataUtils.getUserIdByToken(token);
    		  if(userId==DataUtils.invalidId) break;
    		  User user=SQLCommander.queryUser(userId);
    		  if(user==null) break;

    		  if(avatarFile==null) break;
    		  int previousAvatarId=user.getAvatar();
    		  int newAvatarId=ExtraCommander.saveAvatarFile(avatarFile, user);
    		  if(newAvatarId==ExtraCommander.INVALID) break;
                
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
          	  	Integer activityId=Integer.valueOf(formData.get(Activity.idKey)[0]);
          	  	String token=formData.get(User.tokenKey)[0];
            	  
        		ObjectNode ret=null;
    			Integer userId=DataUtils.getUserIdByToken(token);
    			UserActivityRelation.RelationType relation=SQLCommander.queryRelationOfUserIdAndActivity(userId, activityId);
    			
    			if(relation==null) break;
    			ret=Json.newObject();
    			ret.put(UserActivityRelationTable.relationIdKey, new Integer(relation.ordinal()).toString());

        		return ok(ret);
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }

    public static Result logout(){
      	// define response attributes
  		response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String token=formData.get(User.tokenKey)[0];
                session().remove(token);
                return ok();
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest();
    }
     
    public static Result nameDuplicate(String username){
      	// define response attributes
  		response().setContentType("text/plain");
        do{
            try{
                if(username==null) break;
                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(User.idKey);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(User.nameKey+"="+SQLHelper.convertToQueryValue(username));
                
                List<JSONObject> userJsons=sqlHelper.query("User", columnNames, whereClauses, SQLHelper.logicAND);
                if(userJsons!=null && userJsons.size()>0) break;
                return ok();
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest();
    }

    public static Result emailDuplicate(String email){
      	// define response attributes
  		response().setContentType("text/plain");
        do{
            try{
                if(email==null || General.validateEmail(email)==false) break;
                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(User.idKey);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(User.emailKey+"="+SQLHelper.convertToQueryValue(email));
                
                List<JSONObject> userJsons=sqlHelper.query("User", columnNames, whereClauses, SQLHelper.logicAND);
                if(userJsons!=null && userJsons.size()>0) break;
                return ok();
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest();
    }

	public static Result emailVerification(String code){
		return null;
	}
}
