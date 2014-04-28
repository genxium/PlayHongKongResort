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
    
}
