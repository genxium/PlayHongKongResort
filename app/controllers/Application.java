package controllers;

import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {
        try {
            Content html = views.html.homepage.render();
            return ok(html);
        } catch (Exception e) {

        }
        return badRequest();
    }

    public static Result admin() {
        try {
            Content html = views.html.admin.render();
            return ok(html);
        } catch (Exception e) {

        }
        return badRequest();
    }


}
