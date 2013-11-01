package controllers;

import play.*;
import play.api.mvc.MultipartFormData;
import play.mvc.*;
import play.mvc.Http.RequestBody;
import scala.collection.immutable.Page;
import views.html.*;
import dao.SQLHelper;
import utilities.Converter; 

import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

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
    		RequestBody body = request().body();
    		Map<String, String[]> formData=body.asFormUrlEncoded();
    		String[] emails=formData.get("email");
    		String[] passwords=formData.get("password");
    		String email=emails[0];
    		String password=passwords[0];
    		response().setContentType("text/plain");
    		return ok("email: "+email+", password: "+password);
    }
}
