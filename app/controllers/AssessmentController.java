package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import dao.EasyPreparedStatementBuilder;
import model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.*;

public class AssessmentController extends Controller {
    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Integer userId, Integer activityId){
        response().setContentType("text/plain");
        do{
            try{
                return ok();
            } catch(Exception e){
                System.out.println(AssessmentController.class.getName()+".query, "+e.getCause());
            }
        } while(false);
        return badRequest();
    }
}
