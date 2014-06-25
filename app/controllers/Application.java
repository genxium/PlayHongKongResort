package controllers;

import play.Play;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Application extends Controller {

    public static String HOMEPAGE ="homepage.html";

    public static Result index() {
        return show(HOMEPAGE);
    }
    
    public static Result show(String page){
        response().setContentType("text/html");
	try{
		String fullPath=Play.application().path()+"/app/views/"+page;
		File file=new File(fullPath);
		String content = new Scanner(file).useDelimiter("\\A").next();
		return ok(content);
	} catch(IOException e){

	}
        return badRequest();
    }

    public static Result detail(Integer arg){
        try{
        	Content html = views.html.detail.render(arg);
        	return ok(html);
        } catch (Exception e){

        }
        return badRequest();
    }
    
    public static Result profile(Integer target){
	try{
		Content html = views.html.profile.render(target);	
		return ok(html);
	} catch (Exception e){
		
	}	
	return badRequest();
    }
}
