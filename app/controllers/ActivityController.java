package controllers;

import play.mvc.*;
import model.*;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ActivityController extends Controller {

	public static String s_pageIndexKey="pageIndex";
    public static Integer s_usersPerPage=10; // hard coded for now
 
    public static Result queryActivityDetail(){
    	response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] activityIds=formData.get(Activity.idKey);
            if(activityIds==null) break;
            int activityId=Integer.parseInt(activityIds[0]);
                        
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
}
