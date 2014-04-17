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

    public static Result query(Integer refIndex, Integer numItems, Integer direction, String token, Integer relation){
        response().setContentType("text/plain");
        do{
            try{
                Integer userId=null;
                if(token!=null){
                    userId=DataUtils.getUserIdByToken(token);
                }
                if(userId==DataUtils.invalidId) break;
                List<Activity> activities=null;

                if(relation!=null){
                    UserActivityRelation.RelationType relationship=UserActivityRelation.RelationType.getTypeForValue(relation);
                    activities=SQLCommander.queryActivities(userId, relationship);
                } else{
                    activities=SQLCommander.queryAcceptedActivitiesInChronologicalOrder(refIndex, numItems, direction, userId);
                }
                if(activities==null) break;
                ObjectNode result = Json.newObject();

                for(Activity activity : activities){
                    if(userId!=null){
                        result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImagesAndRelation(userId));
                    } else{
                        result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages());
                    }
                }
                return ok(result);
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }

    public static Result detail(Integer activityId){
        response().setContentType("text/plain");
        do{
            ObjectNode result = null;
            try{
                ActivityDetail activityDetail=SQLCommander.queryActivityDetail(activityId);
                if(activityDetail==null) break;
                result=activityDetail.toObjectNode();
                return ok(result);
            } catch(Exception e){

            }
        }while(false);
        return badRequest();
    }

    public static Result ownership(String token, Integer activityId){
        do{
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

    public static Result create(){
        // define response attributes
        response().setContentType("text/plain");

        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String token=formData.get(User.tokenKey)[0];

                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                // create blank draft
                Activity activity=new Activity();

                int lastActivityId= SQLHelper.INVALID_ID;

                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();

                columnNames.add(Activity.titleKey);
                columnNames.add(Activity.contentKey);
                columnNames.add(Activity.createdTimeKey);
                columnNames.add(Activity.beginTimeKey);
                columnNames.add(Activity.deadlineKey);
                columnNames.add(Activity.capacityKey);

                List<Object> columnValues=new LinkedList<Object>();

                columnValues.add(activity.getTitle());
                columnValues.add(activity.getContent());
                columnValues.add(activity.getCreatedTime().toString());
                columnValues.add(activity.getBeginTime().toString());
                columnValues.add(activity.getDeadline().toString());
                columnValues.add(activity.getCapacity());

                int tmpLastActivityId=sqlHelper.insertToTableByColumns("Activity", columnNames, columnValues);
                if(tmpLastActivityId!=SQLHelper.INVALID_ID){
                    columnNames.clear();
                    columnValues.clear();

                    columnNames.add(UserActivityRelationTable.activityIdKey);
                    columnNames.add(UserActivityRelationTable.userIdKey);
                    columnNames.add(UserActivityRelationTable.relationIdKey);
                    columnNames.add(UserActivityRelationTable.generatedTimeKey);

                    columnValues.add(tmpLastActivityId);
                    columnValues.add(userId);
                    columnValues.add(UserActivityRelation.RelationType.host.ordinal());
                    columnValues.add(activity.getCreatedTime().toString());

                    int lastRelationTableId=sqlHelper.insertToTableByColumns("UserActivityRelationTable", columnNames, columnValues);
                    if(lastRelationTableId==SQLHelper.INVALID_ID) break;

                    lastActivityId=tmpLastActivityId;
                }

                if(lastActivityId==SQLHelper.INVALID_ID) break;

                activity.setId(lastActivityId);
                ObjectNode activityNode= Json.newObject();
                activityNode.put(Activity.idKey, new Integer(lastActivityId).toString());
                return ok(activityNode);

            } catch(Exception e){
                System.out.println("Application.createActivity: "+e.getMessage());
            }

        }while(false);

        return badRequest();
    }

    public static Result update(){
        // define response attributes
        response().setContentType("text/plain");

        do{
            try{
                Http.RequestBody body = request().body();

                // get file data from request body stream
                Http.MultipartFormData data = body.asMultipartFormData();
                List<Http.MultipartFormData.FilePart> imageFiles=data.getFiles();

                Map<String, String[]> formData= data.asFormUrlEncoded();

                String token=formData.get(User.tokenKey)[0];

                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==null || userId==DataUtils.invalidId) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                Integer activityId=Integer.valueOf(formData.get(Activity.idKey)[0]);
                String activityTitle=formData.get(Activity.titleKey)[0];
                String activityContent=formData.get(Activity.contentKey)[0];
                String activityBeginTime=formData.get(Activity.beginTimeKey)[0];
                String activityDeadline=formData.get(Activity.deadlineKey)[0];
                if(DataUtils.validateTitle(activityTitle)==false || DataUtils.validateContent(activityContent)==false) break;
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
                        if(newImageId==ExtraCommander.s_invalidId) break;
                    }
                }

                // selected old images
                String[] selectedOldImagesRaw=formData.get("indexOldImage");
                JSONArray selectedOldImagesJson=(JSONArray)JSONValue.parse(selectedOldImagesRaw[0]);
                Set<Integer> selectedOldImagesSet=new HashSet<Integer>();
                for(int i=0;i<selectedOldImagesJson.size();i++){
                    Integer imageId=((Long)selectedOldImagesJson.get(i)).intValue();
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
                System.out.println("ActivityController.update:"+e.getMessage());
                break;
            }
            return ok("Activity updated");

        }while(false);
        return badRequest("Activity not updated!");
    }

    public static Result submit(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Http.RequestBody body = request().body();

                // get user token and activity id from request body stream
                Map<String, String[]> formData= body.asFormUrlEncoded();

                String[] tokens=formData.get(User.tokenKey);
                String[] activityIds=formData.get(Activity.idKey);

                String token=tokens[0];
                int userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;
                User user=SQLCommander.queryUser(userId);
                if(user==null) break;

                int activityId=Integer.parseInt(activityIds[0]);

                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityEditable(userId, activity)==false) break;

                String activityTableName="Activity";

                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(Activity.statusKey);

                List<Object> columnValues=new LinkedList<Object>();
                columnValues.add(Activity.StatusType.pending.ordinal());

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(Activity.idKey+"="+activity.getId());

                boolean ret=sqlHelper.updateTableByColumnsAndWhereClauses(activityTableName, columnNames, columnValues, whereClauses, SQLHelper.logicAND);

                if(ret==false) break;
                return ok("Activity submitted");

            } catch(Exception e){
                System.out.println("Application.submitActivity:"+e.getMessage());
            }

        }while(false);
        return badRequest("Activity not submitted!");
    }


    public static Result delete(){
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

    public static Result join(){
        // define response attributes
        response().setContentType("text/plain");
        do{
            try{
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                Integer activityId=Integer.parseInt(formData.get(Activity.idKey)[0]);
                String token=formData.get(User.tokenKey)[0];

                Integer userId=DataUtils.getUserIdByToken(token);
                Activity activity=SQLCommander.queryActivity(activityId);
                if(SQLCommander.isActivityJoinable(userId, activity)==false) break;

                SQLHelper sqlHelper=new SQLHelper();
                java.util.Date date= new java.util.Date();
                Timestamp currentTime=new Timestamp(date.getTime());

                List<String> columnNames=new LinkedList<String>();
                columnNames.add(UserActivityRelationTable.activityIdKey);
                columnNames.add(UserActivityRelationTable.userIdKey);
                columnNames.add(UserActivityRelationTable.relationIdKey);
                columnNames.add(UserActivityRelationTable.generatedTimeKey);

                List<Object> columnValues=new LinkedList<Object>();
                columnValues.add(activityId);
                columnValues.add(userId);
                columnValues.add(UserActivityRelation.RelationType.applied.ordinal());
                columnValues.add(currentTime.toString());

                int lastRelationTableId=sqlHelper.insertToTableByColumns("UserActivityRelationTable", columnNames, columnValues);
                if(lastRelationTableId==SQLHelper.INVALID_ID) break;

                return ok("Successfully joined activity");
            } catch(Exception e){
                System.out.println("Application.joinActivity:"+e.getMessage());
            }
        }while(false);
        return badRequest("Could not join activity");
    }
}
