package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
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

    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Integer userId, Integer relation, Integer status){
        response().setContentType("text/plain");
        do{
            try{
                Integer id=null;
                if(token!=null)	id=DataUtils.getUserIdByToken(token);
                List<Activity> activities=null;
                if(relation!=null && userId!=null){
                    activities=SQLCommander.queryActivities(userId, relation);
                } else{
                    activities=SQLCommander.queryActivities(refIndex, Activity.ID, SQLHelper.DESCEND, numItems, direction, status);
                }
                if(activities==null) break;
                ObjectNode result = Json.newObject();
                for(Activity activity : activities){
                    result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages(id));
                }
                return ok(result);
            } catch(Exception e){
                System.out.println("ActivityController.query, "+e.getCause());
            }
        } while(false);
        return badRequest();
    }

    public static Result detail(Integer activityId, String token){
        response().setContentType("text/plain");
        do{
		ObjectNode result = null;
		try{
			if(activityId!=null) System.out.println("ActivityController.detail, activityId="+activityId.toString());
			ActivityDetail activityDetail=SQLCommander.queryActivityDetail(activityId);
			if(activityDetail==null) break;
			Integer userId=null;
			if(token!=null)	userId=DataUtils.getUserIdByToken(token);
			if(userId!=null) System.out.println("ActivityController.detail, userId="+userId.toString());
			result=activityDetail.toObjectNode(userId);
			return ok(result);
		} catch(Exception e){
			System.out.println("ActivityController.detail, "+e.getMessage());
		}
        }while(false);
        return badRequest();
    }

    public static Result ownership(String token, Integer activityId){
	do{
		try{
			Integer ownerId=DataUtils.getUserIdByToken(token);
			if(ownerId==null) break;
			if(SQLCommander.validateOwnershipOfActivity(ownerId, activityId)==false) break;
			return ok();
		} catch(Exception e){
			System.out.println("ActivityController.ownership, "+e.getMessage());
		}
	}while(false);
        return badRequest();
    }

    public static Result updateParticipants(){
    	// define response attributes
	response().setContentType("text/plain");
        
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String token=formData.get(User.TOKEN)[0];
                Integer activityId=Integer.valueOf(formData.get(Activity.ID)[0]);
                String[] appliedParticipantsJsonStrs= formData.get(ActivityDetail.APPLIED_PARTICIPANTS);
                String[] selectedParticipantsJsonStrs= formData.get(ActivityDetail.SELECTED_PARTICIPANTS);

                String appliedParticipantsJsonStr=appliedParticipantsJsonStrs.length>0?appliedParticipantsJsonStrs[0]:"[]";
                String selectedParticipantsJsonStr=selectedParticipantsJsonStrs.length>0?selectedParticipantsJsonStrs[0]:"[]";

                JSONArray appliedParticipantsJson= (JSONArray)JSONValue.parse(appliedParticipantsJsonStr);
                JSONArray selectedParticipantsJson= (JSONArray)JSONValue.parse(selectedParticipantsJsonStr);

                Integer ownerId=DataUtils.getUserIdByToken(token);
                if(ownerId==null) break;
                if(SQLCommander.validateOwnershipOfActivity(ownerId, activityId)==false) break;

                for(int i=0;i<appliedParticipantsJson.size();i++){
                    Integer userId=Integer.valueOf((String)appliedParticipantsJson.get(i));
                    boolean result=SQLCommander.updateUserActivityRelation(ownerId, userId, activityId, UserActivityRelation.applied);
                    if(result==false){
                        System.out.println("uid: "+userId+" activityid: "+activityId+" to relation: 0 failed");
                    }
                }

                for(int i=0;i<selectedParticipantsJson.size();i++){
                    Integer userId=Integer.valueOf((String)selectedParticipantsJson.get(i));
                    boolean result=SQLCommander.updateUserActivityRelation(ownerId, userId, activityId, UserActivityRelation.selected);
                    if(result==false){
                        System.out.println("uid: "+userId+" activityid: "+activityId+" to relation: 1 failed");
                    }
                }
                return ok();
      	  	} catch(Exception e){
			    System.out.println("ActivityController.updateParticipants: "+e.getMessage());
      	  	}

        }while(false);

	return badRequest();
    }

    public static Result save(){
        // define response attributes
        response().setContentType("text/plain");

        do{
            try{
                Http.RequestBody body = request().body();

                // get file data from request body stream
                Http.MultipartFormData data = body.asMultipartFormData();
                List<Http.MultipartFormData.FilePart> imageFiles=data.getFiles();

                Map<String, String[]> formData= data.asFormUrlEncoded();

                String token=formData.get(User.TOKEN)[0];
                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==null || userId==null) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                String activityTitle=formData.get(Activity.TITLE)[0];
                String activityContent=formData.get(Activity.CONTENT)[0];
                String activityBeginTime=formData.get(Activity.BEGIN_TIME)[0];
                String activityDeadline=formData.get(Activity.DEADLINE)[0];

                if(DataUtils.validateTitle(activityTitle)==false || DataUtils.validateContent(activityContent)==false) break;

                boolean isNewActivity=true;
                Integer activityId=null;
                if(formData.containsKey(Activity.ID)==true){
                    activityId=Integer.valueOf(formData.get(Activity.ID)[0]);
                    isNewActivity=false;
                }
                Activity activity=null;

                if(isNewActivity==true){
                    // create activity
                    activityId=SQLCommander.createActivity(activityTitle, activityContent, userId);
                    if(activityId==null || activityId.equals(SQLHelper.INVALID)) break;
                }

                // update activity
                activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                activity.setTitle(activityTitle);
                activity.setContent(activityContent);
                activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
                activity.setDeadline(Timestamp.valueOf(activityDeadline));

                boolean res=SQLCommander.updateActivity(activity);

                if(res==false) break;

                // save new images
                List<Image> previousImages=SQLCommander.queryImages(activityId);
                if(imageFiles!=null && imageFiles.size()>0){
                    Iterator<Http.MultipartFormData.FilePart> imageIterator=imageFiles.iterator();
                    while(imageIterator.hasNext()){
                        Http.MultipartFormData.FilePart imageFile=imageIterator.next();
                        int newImageId=ExtraCommander.saveImageOfActivity(imageFile, user, activity);
                        if(newImageId==ExtraCommander.INVALID) break;
                    }
                }

                // selected old images
                Set<Integer> selectedOldImagesSet=new HashSet<Integer>();

                if(formData.containsKey("indexOldImage")==true){
                    JSONArray selectedOldImagesJson=(JSONArray)JSONValue.parse(formData.get("indexOldImage")[0]);
                    for(int i=0;i<selectedOldImagesJson.size();i++){
                        Integer imageId=((Long)selectedOldImagesJson.get(i)).intValue();
                        selectedOldImagesSet.add(imageId);
                    }
                }

                // delete previous images
                if(previousImages!=null && previousImages.size()>0){
                    Iterator<Image> itPreviousImage=previousImages.iterator();
                    while(itPreviousImage.hasNext()){
                        Image previousImage=itPreviousImage.next();
                        if(selectedOldImagesSet.contains(previousImage.getImageId())==false){
                            boolean isDeleted=ExtraCommander.deleteImageRecordAndFile(previousImage, activityId);
                            if(isDeleted==false) break;
                        }
                    }
                }

                ObjectNode ret = Json.newObject();
                if(isNewActivity==true){
                    ret.put(Activity.ID, activityId.toString());
                }
                return ok(ret);
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }

    public static Result submit(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Http.RequestBody body = request().body();

                // get user token and activity id from request body stream
                Map<String, String[]> formData= body.asFormUrlEncoded();

                String token=formData.get(User.TOKEN)[0];
                Integer activityId=Integer.valueOf(formData.get(Activity.ID)[0]);

                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==null) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                SQLHelper sqlHelper=new SQLHelper();
                List<String> names=new LinkedList<String>();
                names.add(Activity.STATUS);

                List<Object> values=new LinkedList<Object>();
                values.add(Activity.PENDING);

                List<String> where=new LinkedList<String>();
                where.add(Activity.ID +"="+activity.getId());

                boolean res=sqlHelper.update(Activity.TABLE, names, values, where, SQLHelper.AND);
                if(res==false) break;

                return ok();

            } catch(Exception e){

            }

        }while(false);
        return badRequest();
    }


    public static Result delete(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String[] ids=formData.get(Activity.ID);
                String[] tokens=formData.get(User.TOKEN);

                Integer activityId=Integer.parseInt(ids[0]);
                String token=tokens[0];

                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==null) break;

                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                boolean res=ExtraCommander.deleteActivity(activityId);
                if(res==false) break;
            } catch(Exception e){

            }
            return ok();
        } while(false);
        return badRequest();
    }

    public static Result join(){
        // define response attributes
        response().setContentType("text/plain");
        do{
		try{
			Map<String, String[]> formData=request().body().asFormUrlEncoded();
			Integer activityId=Integer.parseInt(formData.get(Activity.ID)[0]);
			String token=formData.get(User.TOKEN)[0];
			if(token==null) break;
			Integer userId=DataUtils.getUserIdByToken(token);
			if(userId==null) break;

			Activity activity=SQLCommander.queryActivity(activityId);
			if(activity==null) break;
			boolean joinable=SQLCommander.isActivityJoinable(userId, activity);
			if(joinable==false) break;

			SQLHelper sqlHelper=new SQLHelper();
			java.util.Date date= new java.util.Date();
			Timestamp currentTime=new Timestamp(date.getTime());

			List<String> names=new LinkedList<String>();
			names.add(UserActivityRelation.ACTIVITY_ID);
			names.add(UserActivityRelation.USER_ID);
			names.add(UserActivityRelation.RELATION);
			names.add(UserActivityRelation.GENERATED_TIME);

			List<Object> values=new LinkedList<Object>();
			values.add(activityId);
			values.add(userId);
			values.add(UserActivityRelation.applied);
			values.add(currentTime.toString());

			int lastRelationTableId=sqlHelper.insert(UserActivityRelation.TABLE, names, values);
			if(lastRelationTableId==SQLHelper.INVALID) break;

			return ok();
		} catch(Exception e){
			System.out.println("ActivityController.join, "+e.getMessage());
		}
        }while(false);
        return badRequest();
    }
}
