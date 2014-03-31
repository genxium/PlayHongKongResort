package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.*;

public class ActivityController extends Controller {

    public static String s_indexOldImage="indexOldImage";

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

    public static Result createActivity(){
        // define response attributes
        response().setContentType("text/plain");

        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] tokens=formData.get(User.tokenKey);
                String token=tokens[0];

                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                // create blank draft
                Activity activity=new Activity();

                int lastActivityId=SQLCommander.createActivity(activity, userId);
                if(lastActivityId!=SQLCommander.invalidId){
                    activity.setId(lastActivityId);
                    ObjectNode activityNode= Json.newObject();
                    activityNode.put(Activity.idKey, new Integer(lastActivityId).toString());
                    return ok(activityNode);
                }

            } catch(Exception e){
                System.out.println("Application.createActivity: "+e.getMessage());
            }

        }while(false);

        return badRequest();
    }

    public static Result updateActivity(){
        // define response attributes
        response().setContentType("text/plain");

        do{
            try{
                Http.RequestBody body = request().body();

                // get file data from request body stream
                Http.MultipartFormData data = body.asMultipartFormData();

                List<Http.MultipartFormData.FilePart> imageFiles=data.getFiles();

                // get user token and activity id from request body stream
                Map<String, String[]> formData= data.asFormUrlEncoded();

                String[] tokens=formData.get(User.tokenKey);
                String[] activityIds=formData.get(Activity.idKey);

                String token=tokens[0];
                int userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                int activityId=Integer.parseInt(activityIds[0]);

                // get activity title and content
                String[] activityTitles=formData.get(Activity.titleKey);
                String[] activityContents=formData.get(Activity.contentKey);

                String activityTitle=activityTitles[0];
                String activityContent=activityContents[0];

                // get activity begin time and deadline
                String[] activityBeginTimes=formData.get(Activity.beginTimeKey);
                String[] activityDeadlines=formData.get(Activity.deadlineKey);

                String activityBeginTime=activityBeginTimes[0];
                String activityDeadline=activityDeadlines[0];

                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                activity.setTitle(activityTitle);
                activity.setContent(activityContent);
                activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
                activity.setDeadline(Timestamp.valueOf(activityDeadline));

                boolean res=SQLCommander.updateActivity(activity);
                if(res==false) break;

                // save new images
                List<Image> previousImages=SQLCommander.queryImagesByActivityId(activityId);
                if(imageFiles!=null && imageFiles.size()>0){
                    Iterator<Http.MultipartFormData.FilePart> imageIterator=imageFiles.iterator();
                    while(imageIterator.hasNext()){
                        Http.MultipartFormData.FilePart imageFile=imageIterator.next();
                        int newImageId=ExtraCommander.saveImageOfActivity(imageFile, user, activity);
                        if(newImageId==ExtraCommander.invalidId) break;
                    }
                }

                // selected old images
                String[] selectedOldImagesRaw=formData.get(s_indexOldImage);
                JSONArray selectedOldImagesJson=(JSONArray)JSONValue.parse(selectedOldImagesRaw[0]);
                Set<Integer> selectedOldImagesSet=new HashSet<Integer>();
                for(int i=0;i<selectedOldImagesJson.size();i++){
                    String imageIdStr=(String)selectedOldImagesJson.get(i);
                    Integer imageId=Integer.valueOf(imageIdStr);
                    selectedOldImagesSet.add(imageId);
                }

                // delete previous images
                if(previousImages!=null && previousImages.size()>0){
                    Iterator<Image> itPreviousImage=previousImages.iterator();
                    while(itPreviousImage.hasNext()){
                        Image previousImage=itPreviousImage.next();
                        if(selectedOldImagesSet.contains(previousImage.getImageId())==false){
                            boolean isDeleted=ExtraCommander.deleteImageRecordAndFileOfActivity(previousImage, activityId);
                            if(isDeleted==false) break;
                        }
                    }
                }

            } catch(Exception e){
                System.out.println("Application.updateActivity:"+e.getMessage());
                break;
            }
            return ok("Activity updated");

        }while(false);
        return badRequest("Activity not updated!");
    }

    public static Result submitActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Http.RequestBody body = request().body();

                // get file data from request body stream
                Http.MultipartFormData data = body.asMultipartFormData();

                List<Http.MultipartFormData.FilePart> imageFiles=data.getFiles();

                // get user token and activity id from request body stream
                Map<String, String[]> formData= data.asFormUrlEncoded();

                String[] tokens=formData.get(User.tokenKey);
                String[] activityIds=formData.get(Activity.idKey);

                String token=tokens[0];
                int userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                int activityId=Integer.parseInt(activityIds[0]);

                // get activity title and content
                String[] activityTitles=formData.get(Activity.titleKey);
                String[] activityContents=formData.get(Activity.contentKey);

                String activityTitle=activityTitles[0];
                String activityContent=activityContents[0];

                // get activity begin time and deadline
                String[] activityBeginTimes=formData.get(Activity.beginTimeKey);
                String[] activityDeadlines=formData.get(Activity.deadlineKey);

                String activityBeginTime=activityBeginTimes[0];
                String activityDeadline=activityDeadlines[0];

                if(DataUtils.validateTitle(activityTitle)==false || DataUtils.validateContent(activityContent)==false) break;
                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                activity.setTitle(activityTitle);
                activity.setContent(activityContent);
                activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
                activity.setDeadline(Timestamp.valueOf(activityDeadline));

                boolean res=SQLCommander.submitActivity(userId, activity);
                if(res==false) break;

                // save new images
                List<Image> previousImages=SQLCommander.queryImagesByActivityId(activityId);
                if(imageFiles!=null && imageFiles.size()>0){
                    Iterator<Http.MultipartFormData.FilePart> imageIterator=imageFiles.iterator();
                    while(imageIterator.hasNext()){
                        Http.MultipartFormData.FilePart imageFile=imageIterator.next();
                        int newImageId=ExtraCommander.saveImageOfActivity(imageFile, user, activity);
                        if(newImageId==ExtraCommander.invalidId) break;
                    }
                }

                // delete previous images
                if(previousImages!=null && previousImages.size()>0){
                    Iterator<Image> itPreviousImage=previousImages.iterator();
                    while(itPreviousImage.hasNext()){
                        Image previousImage=itPreviousImage.next();
                        boolean isDeleted=ExtraCommander.deleteImageRecordAndFileOfActivity(previousImage, activityId);
                        if(isDeleted==false) break;
                    }
                }
                return ok("Activity submitted");

            } catch(Exception e){
                System.out.println("Application.submitActivity:"+e.getMessage());
            }

        }while(false);
        return badRequest("Activity not submitted!");
    }


    public static Result deleteActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(User.tokenKey);

            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];

            Integer userId=DataUtils.getUserIdByToken(token);
            if(userId==DataUtils.invalidId) break;

            Activity activity=SQLCommander.queryActivity(activityId);
            if(SQLCommander.isActivityEditable(userId, activity)==false) break;

            try{
                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
            } catch(Exception e){
                System.out.println("Application.deleteActivity: "+e.getMessage());
            }
            return ok("Activity deleted");
        } while(false);
        return badRequest("Activity not completely deleted!");
    }

    public static Result joinActivity(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            Map<String, String[]> formData=request().body().asFormUrlEncoded();
            String[] ids=formData.get(Activity.idKey);
            String[] tokens=formData.get(User.tokenKey);

            Integer activityId=Integer.parseInt(ids[0]);
            String token=tokens[0];
            try{
                Integer userId=DataUtils.getUserIdByToken(token);
                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityJoinable(userId, activity)==false) break;
                boolean result=SQLCommander.joinActivity(userId, activityId);
                if(result==false) break;
            } catch(Exception e){
                System.out.println("Application.joinActivity:"+e.getMessage());
            }
            return ok("Successfully joined activity");
        }while(false);
        return badRequest("Could not join activity");
    }
}
