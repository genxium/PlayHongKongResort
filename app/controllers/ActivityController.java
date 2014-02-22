package controllers;

import play.mvc.*;
import model.Activity;
import model.ActivityDetail;
import model.Image;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import views.html.*;

public class ActivityController extends Controller {

	public static String s_pageIndexKey="pageIndex";
    public static Integer s_usersPerPage=10; // hard coded for now
 
    public static Result queryActivityDetail(){
		response().setContentType("text/plain");
		do{
		    ObjectNode result = null;
		    
		    try{
		    	Map<String, String[]> formData=request().body().asFormUrlEncoded();
		  	  	String[] activityIds=formData.get(Activity.idKey);
		  	  	  
		     	Integer activityId=Integer.parseInt(activityIds[0]);
				
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
    
    public static Result showActivityDetailPage(String activityId){
    	int activityIdInt=Integer.valueOf(activityId);
    	ActivityDetail activityDetail=SQLCommander.queryActivityDetailByActivityId(activityIdInt);
    	List<Image> activityImages=activityDetail.getImages();
    	List<String> activityImageUrls=new ArrayList();
    	for(int i=0;i<activityImages.size();i++){
    		activityImageUrls.add(activityImages.get(i).getImageURL());
    	}
    	return ok(views.html.activity_detail_page.render(activityDetail.getTitle(),  activityDetail.getContent(), activityImageUrls));
    }
}
