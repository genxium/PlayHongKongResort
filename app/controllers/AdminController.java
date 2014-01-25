package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import model.*;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.util.*;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;

public class AdminController extends Controller {

    public static String s_homepageName="homepage.html";
    public static String s_indexImageOfActivityPrefix="indexImageOfActivityPrefix";
    public static String s_pageIndexKey="pageIndex";

    public static Integer s_itemsPerPage=6; // hard coded for now

    public static Result acceptActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(BasicUser.tokenKey);
            
            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
          
            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            BasicUser user=SQLCommander.queryUserByUserId(userId);
            if(user==null) break;
            
            Activity activity=SQLCommander.queryActivityByActivityId(activityId);
            if(activity==null) break;
            
            try{
                boolean res=SQLCommander.acceptActivity(user, activity);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.acceptActivity: "+e.getMessage());
            }
            return ok("Activity accepted");
        } while(false);
        return badRequest("Activity not accepted!");
    }
    
    public static Result deleteActivityByAdmin(){
    	// define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(BasicUser.tokenKey);
            
            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
          
            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;
            
            BasicUser user=SQLCommander.queryUserByUserId(userId);
            if(user==null) break;
            
            if(SQLCommander.validateAdminAccess(user)==false) break;
            
            Activity activity=SQLCommander.queryActivityByActivityId(activityId);
 
            try{
                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("AdminController.deleteActivityByAdmin: "+e.getMessage());
            }
            return ok("Activity deleted");
        } while(false);
        return badRequest("Activity not completely deleted!");
    }

    public static Result queryPendingActivitiesByAdmin(){
		response().setContentType("text/plain");
        do{
    		try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] pageIndexes=formData.get(s_pageIndexKey);
                if(pageIndexes==null) break;
                Integer pageIndex=Integer.parseInt(pageIndexes[0]);
                
    			List<JSONObject> activities=SQLCommander.queryPendingActivitiesInChronologicalOrder(pageIndex, s_itemsPerPage);
      			Iterator<JSONObject> itActivity=activities.iterator();
      			ObjectNode result = Json.newObject();
      		
      			while(itActivity.hasNext()){
    				JSONObject activityJSON=itActivity.next();
    				Integer activityId=(Integer)activityJSON.get(Activity.idKey);
    				String activityTitle=(String)activityJSON.get(Activity.titleKey);
    				String activityContent=(String)activityJSON.get(Activity.contentKey);
                    Integer activityStatus=(Integer)activityJSON.get(Activity.statusKey);
    				List<Image> images=SQLCommander.queryImagesByActivityId(activityId);

    				ObjectNode singleActivityNode=Json.newObject();
    				singleActivityNode.put(Activity.idKey, activityId.toString());
    				singleActivityNode.put(Activity.titleKey, activityTitle);
    				singleActivityNode.put(Activity.contentKey, activityContent);
                    singleActivityNode.put(Activity.statusKey, activityStatus.toString());    				
                    if(images!=null && images.size()>0){
                       Iterator<Image> itImage=images.iterator();
                       if(itImage.hasNext()){
                          Image firstImage=itImage.next();
                          String firstImageURL=firstImage.getImageURL();
                          singleActivityNode.put(Image.urlKey, firstImageURL);
                       }
                    }

    				result.put(activityId.toString(), singleActivityNode);
      			}
      			return ok(result);
    		} catch(Exception e){
                System.out.println("AdminController.queryPendingActivitiesByAdmin: "+e.getMessage());
    	    }
        }while(false);
        return badRequest();
    }
    
    public static Result queryAcceptedActivitiesByAdmin(){
		response().setContentType("text/plain");
        do{
    		try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] pageIndexes=formData.get(s_pageIndexKey);
                if(pageIndexes==null) break;
                Integer pageIndex=Integer.parseInt(pageIndexes[0]);
                
    			List<JSONObject> activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(pageIndex, s_itemsPerPage);
      			Iterator<JSONObject> itActivity=activities.iterator();
      			ObjectNode result = Json.newObject();
      		
      			while(itActivity.hasNext()){
    				JSONObject activityJSON=itActivity.next();
    				Integer activityId=(Integer)activityJSON.get(Activity.idKey);
    				String activityTitle=(String)activityJSON.get(Activity.titleKey);
    				String activityContent=(String)activityJSON.get(Activity.contentKey);
    				Integer activityStatus=(Integer)activityJSON.get(Activity.statusKey);
                    List<Image> images=SQLCommander.queryImagesByActivityId(activityId);

    				ObjectNode singleActivityNode=Json.newObject();
    				singleActivityNode.put(Activity.idKey, activityId.toString());
    				singleActivityNode.put(Activity.titleKey, activityTitle);
    				singleActivityNode.put(Activity.contentKey, activityContent);
                    singleActivityNode.put(Activity.statusKey, activityStatus.toString());    				
    				
                    if(images!=null && images.size()>0){
                       Iterator<Image> itImage=images.iterator();
                       if(itImage.hasNext()){
                          Image firstImage=itImage.next();
                          String firstImageURL=firstImage.getImageURL();
                          singleActivityNode.put(Image.urlKey, firstImageURL);
                       }
                    }

    				result.put(activityId.toString(), singleActivityNode);
      			}
      			return ok(result);
    		} catch(Exception e){
                System.out.println("AdminController.queryAcceptedActivitiesByAdmin: "+e.getMessage());
    	    }
        }while(false);
        return ok();
    }
}
