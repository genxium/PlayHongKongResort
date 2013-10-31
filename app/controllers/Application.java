package controllers;

import play.*;
import play.mvc.*;
import scala.collection.immutable.Page;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
       //return ok(index.render("Your new application is ready."));
    	   return ok("Got request " + request() + "!");
    }
    
    public static Result show(){
    	  response().setContentType("text/html");
    	  response().setHeader(CACHE_CONTROL, "max-age=3600");
    	  response().setHeader(ETAG, "xxx");
    	  response().setCookie("theme", "blue");
    	  return ok("<h1>Hello World!</h1>");
    }

}
