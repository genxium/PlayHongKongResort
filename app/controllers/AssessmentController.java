package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import model.Assessment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class AssessmentController extends Controller {
    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Integer userId, Integer activityId){
        response().setContentType("text/plain");
        try{
            List<Assessment> assessments=SQLCommander.queryAssessments(refIndex, Assessment.GENERATED_TIME, SQLHelper.DESCEND, numItems, direction, activityId);
            ObjectNode result = Json.newObject();
            for(Assessment assessment : assessments){
                result.put(String.valueOf(assessment.getId()), assessment.toObjectNode());
            }
            return ok(result);
        } catch(Exception e){
            System.out.println(AssessmentController.class.getName()+".query, "+e.getCause());
        }
        return badRequest();
    }

    public static Result submit(Integer activityId, String token, String bundle){
        return ok();
    }
}
