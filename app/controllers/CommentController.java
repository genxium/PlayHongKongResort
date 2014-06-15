package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dao.SQLHelper;
import model.CommentOnActivity;
import model.User;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommentController extends Controller {

    public static Result query(Integer activityId, String refIndex, Integer numItems, Integer direction, String token){
        response().setContentType("text/plain");
        do{
            try{
                Integer userId=null;
                if(token!=null){
                    userId= DataUtils.getUserIdByToken(token);
                }
                if(userId==DataUtils.invalidId) break;

                List<CommentOnActivity> comments=SQLCommander.queryTopLevelComments(activityId, refIndex, CommentOnActivity.ID, SQLHelper.DESCEND, numItems, direction, CommentOnActivity.TYPE_QA);

		ArrayNode result=new ArrayNode(JsonNodeFactory.instance);
                for(CommentOnActivity comment : comments){
                    result.add(comment.toObjectNodeWithSubComments());
                }
                return ok(result);
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
                Map<String, String[]> formData=request().body().asFormUrlEncoded();
                String content=formData.get(CommentOnActivity.CONTENT)[0];
				if(content==null || content.length()<=CommentOnActivity.MIN_CONTENT_LENGTH) break;

                String token=formData.get(User.TOKEN)[0];
                Integer activityId=Integer.valueOf(formData.get(CommentOnActivity.ACTIVITY_ID)[0]);

                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                int lastCommentId= SQLHelper.INVALID_ID;

                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();

                columnNames.add(CommentOnActivity.CONTENT);
                columnNames.add(CommentOnActivity.ACTIVITY_ID);
                columnNames.add(CommentOnActivity.COMMENT_TYPE);
                columnNames.add(CommentOnActivity.COMMENTER_ID);

                List<Object> columnValues=new LinkedList<Object>();

                columnValues.add(content);
                columnValues.add(SQLHelper.convertToQueryValue(activityId));
                columnValues.add(SQLHelper.convertToQueryValue(CommentOnActivity.TYPE_QA));
                columnValues.add(SQLHelper.convertToQueryValue(userId));

                if(formData.containsKey(CommentOnActivity.PREDECESSOR_ID)){
                    Integer predecessorId=Integer.valueOf(formData.get(CommentOnActivity.PREDECESSOR_ID)[0]);
                    columnNames.add(CommentOnActivity.PREDECESSOR_ID);
                    columnValues.add(SQLHelper.convertToQueryValue(predecessorId));
                }
                if(formData.containsKey(CommentOnActivity.PARENT_ID)){
                    Integer parentId=Integer.valueOf(formData.get(CommentOnActivity.PARENT_ID)[0]);
                    columnNames.add(CommentOnActivity.PARENT_ID);
                    columnValues.add(SQLHelper.convertToQueryValue(parentId));
                }

                lastCommentId=sqlHelper.insert("CommentOnActivity", columnNames, columnValues);
                if(lastCommentId==SQLHelper.INVALID_ID) break;

                return ok();

            } catch(Exception e){

            }

        }while(false);

        return badRequest();
    }
}
