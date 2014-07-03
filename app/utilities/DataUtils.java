package utilities;

import java.util.Map;
import java.sql.Timestamp;
import model.User;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;
import play.mvc.Http.MultipartFormData.FilePart;

public class DataUtils{

	public static String getFileExt(String fileName){
		int dotPos=fileName.lastIndexOf('.');
		String ext=fileName.substring(dotPos+1, fileName.length()-1);
		return ext;
    	}
    
    public static boolean isImage(FilePart imageFile){
    		String contentType=imageFile.getContentType();
    		int slashPos=contentType.indexOf('/');
		String typePrefix=contentType.substring(0, slashPos);
		if(typePrefix.compareTo("image")==0){
			return true;
		}
		return false;
    }
    
    public static boolean validateImage(FilePart imageFile){
    		boolean ret=false;
    		do{
    			if(isImage(imageFile)==false) break;
    			ret=true;
    		}while(false);
    		return ret;
    }
    
    public static String getUserToken(MultipartFormData data){
    	Map<String, String[]> formData= data.asFormUrlEncoded();
		String[] tokens=formData.get(User.TOKEN);
		String token=tokens[0];
		return token;
    }
    
    public static String getUserToken(RequestBody body){
    	Map<String, String[]> formData= body.asFormUrlEncoded();
		String[] tokens=formData.get(User.TOKEN);
		String token=tokens[0];
		return token;
    }
    
    public static Integer getUserIdByToken(String token){
        Integer userId=null;
        try{
		userId=Integer.parseInt(Controller.session(token));
        } catch(Exception e){
		System.out.println("DataUtils.getUserIdByToken, "+e.getMessage());
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

    public static String generateUploadedImageName(String originalName, Integer userId){
            String ret=null;
            try{
                java.util.Date date= new java.util.Date();
                Timestamp currentTime=new Timestamp(date.getTime());
                Long epochTime=currentTime.getTime();
				String[] nameComponents = originalName.split("\\.(?=[^\\.]+$)");
				String base=nameComponents[0];
				String ext=nameComponents[1];
                ret="UID"+userId.toString()+"_"+epochTime.toString()+"_"+Converter.md5(base)+"."+ext;
            } catch (Exception e){
                System.out.println("DataUtils.generateUploadedImageName:"+e.getMessage());
            }
            return ret;
    }
}
