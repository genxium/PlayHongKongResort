package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import model.Activity;
import model.CommentOnActivity;
import model.UserActivityRelation;
import model.UserActivityRelationTable;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.LinkedList;
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

                List<CommentOnActivity> comments=SQLCommander.queryComments(activityId, refIndex, SQLCommander.COMMENT_ON_ACTIVITY_ID, SQLHelper.directionDescend, numItems, direction, 0);
                return ok();
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }

    public static Result create(String token, Integer activityId, String content, Integer predecessorId){

        return badRequest();
    }
}
