package controllers;

import java.util.Map;

import model.BasicUser;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;

public class DataUtils{

	public static String getFileExt(String fileName){
		int dotPos=fileName.lastIndexOf('.');
		String ext=fileName.substring(dotPos+1, fileName.length()-1);
		return ext;
    }
    
    public static boolean isImage(String contentType){
    		int slashPos=contentType.indexOf('/');
		String typePrefix=contentType.substring(0, slashPos);
		if(typePrefix.compareTo("image")==0){
			return true;
		}
		return false;
    }
    
    public static String getUserToken(MultipartFormData data){
    		Map<String, String[]> formData= data.asFormUrlEncoded();
		String[] tokens=formData.get("token");
		String token=tokens[0];
		return token;
    }
    
    public static String getUserToken(RequestBody body){
    		Map<String, String[]> formData= body.asFormUrlEncoded();
		String[] tokens=formData.get("token");
		String token=tokens[0];
		return token;
    }
    
    public static BasicUser queryUserByToken(String token){
    		BasicUser user=null;
    		try{
    			String email=getEmailByToken(token);
    			if(email!=null && email.length()>0){
    				user=SQLCommander.queryUserByEmail(email);
    			}
    		} catch(Exception e){
    			
    		}
    		return user;
    }
    
    public static String getEmailByToken(String token){
    		String email=Controller.session(token);
		return email;
    }
    
    public static String getNameByEmail(String email){
    		int lastAtSignPos=email.lastIndexOf('@');
    		String name=email.substring(0, lastAtSignPos-1);
    		return name;
    }
    
    public static boolean validateTitle(String title){
    		return title.length()>0;
    }
    
    public static boolean validateContent(String content){
    		return content.length()>0;
    }
}