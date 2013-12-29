package controllers;

import java.util.Map;

import model.BasicUser;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;

public class DataUtils{

    public static Integer invalidId=(-1);

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
		String[] tokens=formData.get(BasicUser.tokenKey);
		String token=tokens[0];
		return token;
    }
    
    public static String getUserToken(RequestBody body){
    	Map<String, String[]> formData= body.asFormUrlEncoded();
		String[] tokens=formData.get(BasicUser.tokenKey);
		String token=tokens[0];
		return token;
    }
    
    public static Integer getUserIdByToken(String token){
        Integer userId=invalidId;
        try{
        		userId=Integer.parseInt(Controller.session(token));
        } catch(Exception e){
        		System.out.println("DataUtils.getUserIdByToken:"+e.getMessage());
        }
    		return userId;
    }
    
    public static String getNameByEmail(String email){
    		int lastAtSignPos=email.lastIndexOf('@');
    		String name=email.substring(0, lastAtSignPos);
    		return name;
    }
    
    public static boolean validateTitle(String title){
    		return title.length()>=0;
    }
    
    public static boolean validateContent(String content){
    		return content.length()>=0;
    }
}