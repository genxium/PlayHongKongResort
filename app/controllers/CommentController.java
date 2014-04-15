package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Activity;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.List;

public class CommentController extends Controller {
    public static Result query(Integer activityId, Integer refIndex, Integer numItems, Integer direction, String token){
        response().setContentType("text/plain");
        do{
            try{
                Integer userId=null;
                if(token!=null){
                    userId= DataUtils.getUserIdByToken(token);
                }
                if(userId==DataUtils.invalidId) break;
                return ok();
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }
}
