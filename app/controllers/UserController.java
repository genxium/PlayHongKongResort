package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import model.*;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import views.html.email_verification;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserController extends Controller {

	protected static void sendVerificationEmail(String username, String recipient, String code) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		String msgBody = "...";
						
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("hongkongresort@126.com", "The HongKongResort Team"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, username));
			msg.setSubject("Welcome to HongKongResort");
            String link="http://107.170.251.163/user/email/verify?code="+code;
			msg.setText("Dear "+username+", you're our member now! Please click the following link to complete email verification: "+link);
			Transport.send(msg);
		} catch (AddressException e) {
			System.out.println(e.getMessage());	
		} catch (MessagingException e) {
			System.out.println(e.getMessage());	
		} catch (UnsupportedEncodingException e){
			System.out.println(e.getMessage());	
		}
	}

    public static Result login(){
	// define response attributes
	response().setContentType("text/plain");

	do{
		try{
			Http.RequestBody body = request().body();
			Map<String, String[]> formData=body.asFormUrlEncoded();
			String email=formData.get(User.EMAIL)[0];
			String password=formData.get(User.PASSWORD)[0];

			if( (email==null || General.validateEmail(email)==false) || (password==null || General.validatePassword(password)==false) ) break;
			
			String passwordDigest=Converter.md5(password);
			User user=SQLCommander.queryUserByEmail(email);

			if(user==null || user.getPassword().equals(passwordDigest)==false) break;
			
			String token = Converter.generateToken(email, password);
			Integer userId = user.getUserId();

			session(token, userId.toString());

			int imageId=user.getAvatar();
			Image image=SQLCommander.queryImageByImageId(imageId);

			ObjectNode result = Json.newObject();
			result.put(User.ID, user.getUserId());
			result.put(User.EMAIL, user.getEmail());
			result.put(User.TOKEN, token);
			if(image!=null){
			    result.put(Image.URL, image.getImageURL());
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
			String name=formData.get(User.NAME)[0];
        		String email=formData.get(User.EMAIL)[0];
        		String password=formData.get(User.PASSWORD)[0];

			if(name==null || (email==null || General.validateEmail(email)==false) || (password==null || General.validatePassword(password)==false)) break;
        		UserGroup.GroupType userGroup=UserGroup.GroupType.visitor;
        		String passwordDigest=Converter.md5(password);    
			User user=User.create(email, passwordDigest, name, userGroup);
			int lastId=SQLCommander.registerUser(user);
			if(lastId==SQLCommander.INVALID) break;

			String code=generateVerificationCode(user);
			SQLHelper sqlHelper=new SQLHelper();

			List<String> columnNames=new LinkedList<>();
			columnNames.add(User.VERIFICATION_CODE);

			List<Object> columnValues=new LinkedList<>();
			columnValues.add(code);

			List<String> where=new LinkedList<>();
			where.add(User.ID +"="+SQLHelper.convertToQueryValue(lastId));

			boolean res=sqlHelper.update("User", columnNames, columnValues, where, SQLHelper.AND);
			if(res==false) break;
			sendVerificationEmail(user.getName(), user.getEmail(), code);
			return ok();
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest();
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
			String emailKey=User.EMAIL;
			String tokenKey=User.TOKEN;

			int imageId=user.getAvatar();
			Image image=SQLCommander.queryImageByImageId(imageId);

			ObjectNode result = Json.newObject();
			result.put(User.ID, user.getUserId());
			result.put(emailKey, user.getEmail());
			result.put(tokenKey, token);
			if(image!=null){
			    result.put(Image.URL, image.getImageURL());
			}
			return ok(result);
		} catch (Exception e) {
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
    		  Integer userId=DataUtils.getUserIdByToken(token);
    		  if(userId==null) break;
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
    
    public static Result relation(Integer activityId, String token){
    	// define response attributes
        response().setContentType("text/plain");
        ObjectNode ret=null;
        do{
            try{
    			Integer userId=DataUtils.getUserIdByToken(token);
    			int relation=SQLCommander.queryUserActivityRelation(userId, activityId);
    			
    			if(relation==UserActivityRelationTable.invalid) break;
    			ret=Json.newObject();
    			ret.put(UserActivityRelationTable.RELATION, String.valueOf(relation));

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
                String token=formData.get(User.TOKEN)[0];
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
                columnNames.add(User.ID);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(User.NAME +"="+SQLHelper.convertToQueryValue(username));
                
                List<JSONObject> userJsons=sqlHelper.query("User", columnNames, whereClauses, SQLHelper.AND);
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
                columnNames.add(User.ID);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(User.EMAIL +"="+SQLHelper.convertToQueryValue(email));
                
                List<JSONObject> userJsons=sqlHelper.query("User", columnNames, whereClauses, SQLHelper.AND);
                if(userJsons!=null && userJsons.size()>0) break;
                return ok();
            } catch(Exception e){
                
            }
        }while(false);
        return badRequest();
    }

	public static Result emailVerification(String code){
        response().setContentType("text/html");
		do{
			try{
                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(User.GROUP_ID);

				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(SQLHelper.convertToQueryValue(UserGroup.GroupType.user.ordinal()));

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(User.VERIFICATION_CODE+"="+SQLHelper.convertToQueryValue(code));

				boolean res=sqlHelper.update("User", columnNames, columnValues, whereClauses, SQLHelper.AND);

				columnNames.clear();
				columnValues.clear();

				columnNames.add(User.ID);
				columnNames.add(User.EMAIL);
				columnNames.add(User.NAME);
				columnNames.add(User.PASSWORD);
				columnNames.add(User.GROUP_ID);
				columnNames.add(User.AVATAR);

				List<JSONObject> userJsons=sqlHelper.query("User", columnNames, whereClauses, SQLHelper.AND); 
				User user = new User(userJsons.get(0));

				Content html = email_verification.render(res, user.getName(), user.getEmail());
                return ok(html);
			} catch(Exception e) {
				System.out.println("emailVerification: "+e.getMessage());
			}
		}while(false);
		return badRequest();
	}

    protected static String generateVerificationCode(User user){
        String ret=null;
        do {
            java.util.Date date = new java.util.Date();
            Timestamp currentTime = new Timestamp(date.getTime());
            Long epochTime = currentTime.getTime();
            String username = user.getName();
            String tmp=Converter.md5(epochTime.toString()+username);
			int length=tmp.length();
			ret=tmp.substring(0, length/2);
        }while (false);
        return ret;
    }
}
