package controllers;

import play.api.libs.json.JsArray;
import play.api.libs.json.JsValue;
import play.mvc.*;
import model.Activity;
import model.ActivityDetail;
import model.Image;
import model.BasicUser;
import model.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import utilities.DataUtils;
import views.html.*;

public class ActivityController extends Controller {

	public static String s_pageIndexKey="pageIndex";
    public static Integer s_usersPerPage=10; // hard coded for now
 
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
                String[] appliedParticipants= formData.get(ActivityDetail.appliedParticipantsKey);
                String[] selectedParticipants= formData.get(ActivityDetail.selectedParticipantsKey);

                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                if(SQLCommander.validateOwnershipOfActivity(userId, activityId)==false) break;
                

      	  	} catch(Exception e){
      	  	    System.out.println("ActivityController.updateActivityParticipants: "+e.getMessage());
      	  	}

        }while(false);

  	  	return badRequest();
    }
}
