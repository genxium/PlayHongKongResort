package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import scala.collection.immutable.Page;
import views.html.*;
import dao.SQLHelper;
import utilities.Converter; 

import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

public class Application extends Controller {

    public static Result index() {
       //return ok(index.render("Your new application is ready."));
    	   return ok("Got request " + request() + "!");
    }
    
    public static Result show(){
    	  return ok(new java.io.File("/public/images/favicon.png"));
    }

    public static Result checkConnection(){
    		// DAO
    		SQLHelper sqlHelper=new SQLHelper();
    		String status = sqlHelper.checkConnectionWithStringResult();
    		response().setContentType("text/plain");
    		return ok(status);
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
			// DAO
			SQLHelper sqlHelper=new SQLHelper();
			boolean status = sqlHelper.checkConnection();
			if(status==true){
				String query=("SELECT * FROM User WHERE email='"+email+"' AND password='"+passwordDigest+"'");
				List<JSONObject> results=sqlHelper.executeSelect(query);
				if(results!=null){
					if(results.size()>0){
						Iterator it=results.iterator();
				        while(it.hasNext())
				        {
				          JSONObject jsonObject=(JSONObject)it.next();
				          String token = Converter.generateToken(email, password);
				          session(token, email);
				          return ok(token);
				        }
					}else{
						return ok("not found");
					}		
				} else{
					return ok("Failed to login");
				}
			} 
			
			return ok("Failed to connect to database");
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
		String name=email;
		
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		
		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("INSERT INTO User(email,password,name) VALUES(");
		queryBuilder.append("'"+email+"'");
		queryBuilder.append(",");
		queryBuilder.append("md5('"+password+"')");
		queryBuilder.append(",");
		queryBuilder.append("'"+name+"'");
		queryBuilder.append(")");
		String query=queryBuilder.toString();
		sqlHelper.executeInsert(query);
		
		return ok(query);
    }
    
    public static Result checkLoginStatus(){
    		// define response attributes
    		response().setContentType("text/plain");
    		
   		RequestBody body = request().body();
    		Map<String, String[]> formData=body.asFormUrlEncoded();
  		String[] tokens=formData.get("token");
  		String token=tokens[0];
  		String email=session(token);
  		if(email!=null && email.length()>0){
  			session(token, email);
  			return ok(email+" has logged in with token "+token);
  		}
  		return ok("User doesn't exist or not logged in");
    }
    
    public static Result uploadingHandler() {
    	  // define response attributes
    	  response().setContentType("text/plain");
    	  
    	  RequestBody body = request().body();
    	  MultipartFormData data = body.asMultipartFormData();
    	  FilePart picture = data.getFile("picture");
    	  if (picture != null) {
    	    String fileName = picture.getFilename();
    	    File file = picture.getFile();
    	    try {
    	    	
    	    		BufferedImage image = ImageIO.read(file);
    	    		String extension = getFileExt(fileName);
    	    		String rootDir=Play.application().path().getAbsolutePath();
    	    		ImageIO.write(image, extension, new File(rootDir+"/uploadedImages/"+fileName));
    	    		
        } catch (IOException ioe) {
            System.out.println("Problem operating on filesystem");
        }
    	    return ok("File " + fileName +" uploaded");
    	  } else {
    	    flash("error", "Missing file");
    	    return redirect("/assets/homepage.html");    
    	  }
    	}
    
    public static String getFileExt(String fileName){
    		int dotPos=fileName.lastIndexOf('.');
    		String ext=fileName.substring(dotPos+1, fileName.length()-1);
    		return ext;
    }
}
