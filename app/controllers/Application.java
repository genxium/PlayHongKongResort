package controllers;

import play.Play;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Application extends Controller {

    public static Result index() {
        try{
        	Content html = views.html.homepage.render();
        	return ok(html);
        } catch (Exception e){

        }
        return badRequest();
    }
    
    public static Result admin(){
        try{
        	Content html = views.html.admin.render();
        	return ok(html);
        } catch (Exception e){

        }
        return badRequest();
    }

    
}
