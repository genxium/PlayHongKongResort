package controllers;

import model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.api.libs.json.JsArray;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.mvc.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import utilities.DataUtils;
import views.html.*;

public class ActivityController extends Controller {

	public static String s_pageIndexKey="pageIndex";
    public static Integer s_usersPerPage=10; // hard coded for now

    public static Result queryActivityOwnership(){
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] tokens=formData.get(User.tokenKey);
            String token=tokens[0];
            String[] activityIds=formData.get(Activity.idKey);
            Integer activityId=Integer.valueOf(activityIds[0]);

            Integer ownerId=DataUtils.getUserIdByToken(token);
            if(ownerId==DataUtils.invalidId) break;
            if(SQLCommander.validateOwnershipOfActivity(ownerId, activityId)==false) break;
            return ok();
        }while(false);
        return badRequest();
    }
 
    public static Result queryActivityDetail(Integer activityId){
		response().setContentType("text/plain");
		do{
		    ObjectNode result = null;
		    try{
				ActivityDetail activityDetail=SQLCommander.queryActivityDetailByActivityId(activityId);
		    	if(activityDetail==null) break;
		    	result=activityDetail.toObjectNode();   			
		        return ok(result);
		    } catch(Exception e){
		    	System.out.println("ActivityController.queryActivityDetail: "+e.getMessage());
	        }
		}while(false);
        return ok();
    }

    public static Result updateActivityParticipants(){
    	// define response attributes
  	  	response().setContentType("text/plain");
        
        do{
      	  	try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] tokens=formData.get(User.tokenKey);
                String token=tokens[0];
                String[] activityIds=formData.get(Activity.idKey);
                Integer activityId=Integer.valueOf(activityIds[0]);
                String[] appliedParticipantsJsonStrs= formData.get(ActivityDetail.appliedParticipantsKey);
                String[] selectedParticipantsJsonStrs= formData.get(ActivityDetail.selectedParticipantsKey);

                String appliedParticipantsJsonStr=appliedParticipantsJsonStrs.length>0?appliedParticipantsJsonStrs[0]:"[]";
                String selectedParticipantsJsonStr=selectedParticipantsJsonStrs.length>0?selectedParticipantsJsonStrs[0]:"[]";

                JSONArray appliedParticipantsJson= (JSONArray)JSONValue.parse(appliedParticipantsJsonStr);
                JSONArray selectedParticipantsJson= (JSONArray)JSONValue.parse(selectedParticipantsJsonStr);

                Integer ownerId=DataUtils.getUserIdByToken(token);
                if(ownerId==DataUtils.invalidId) break;
                if(SQLCommander.validateOwnershipOfActivity(ownerId, activityId)==false) break;

                for(int i=0;i<appliedParticipantsJson.size();i++){
                    Integer userId=Integer.valueOf((String)appliedParticipantsJson.get(i));
                    boolean result=SQLCommander.updateRelationOfUserIdAndActivity(ownerId, userId, activityId, UserActivityRelation.RelationType.applied);
                    if(result==false){
                        System.out.println("uid: "+userId+" activityid: "+activityId+" to relation: 0 failed");    
                    }
                }

                for(int i=0;i<selectedParticipantsJson.size();i++){
                    Integer userId=Integer.valueOf((String)selectedParticipantsJson.get(i));
                    boolean result=SQLCommander.updateRelationOfUserIdAndActivity(ownerId, userId, activityId, UserActivityRelation.RelationType.selected);
                    if(result==false){
                        System.out.println("uid: "+userId+" activityid: "+activityId+" to relation: 1 failed");    
                    }
                }
                return ok();
      	  	} catch(Exception e){
      	  	    System.out.println("ActivityController.updateActivityParticipants: "+e.getMessage());
      	  	}

        }while(false);

  	  	return badRequest();
    }
}
