package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dao.SQLHelper;
import model.Comment;
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
                if(token!=null)	userId= DataUtils.getUserIdByToken(token);

                List<Comment> comments=SQLCommander.queryTopLevelComments(activityId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction, Comment.TYPE_QA);

		        ArrayNode result=new ArrayNode(JsonNodeFactory.instance);
                for(Comment comment : comments){
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
                String content=formData.get(Comment.CONTENT)[0];
				if(content==null || content.length()<= Comment.MIN_CONTENT_LENGTH) break;

                String token=formData.get(User.TOKEN)[0];
                Integer activityId=Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);

                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==null) break;

                int lastCommentId= SQLHelper.INVALID;

                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();

                columnNames.add(Comment.CONTENT);
                columnNames.add(Comment.ACTIVITY_ID);
                columnNames.add(Comment.TYPE);
                columnNames.add(Comment.COMMENTER_ID);

                List<Object> columnValues=new LinkedList<Object>();

                columnValues.add(content);
                columnValues.add(SQLHelper.convertToQueryValue(activityId));
                columnValues.add(SQLHelper.convertToQueryValue(Comment.TYPE_QA));
                columnValues.add(SQLHelper.convertToQueryValue(userId));

                if(formData.containsKey(Comment.PREDECESSOR_ID)){
                    Integer predecessorId=Integer.valueOf(formData.get(Comment.PREDECESSOR_ID)[0]);
                    columnNames.add(Comment.PREDECESSOR_ID);
                    columnValues.add(SQLHelper.convertToQueryValue(predecessorId));
                }
                if(formData.containsKey(Comment.PARENT_ID)){
                    Integer parentId=Integer.valueOf(formData.get(Comment.PARENT_ID)[0]);
                    columnNames.add(Comment.PARENT_ID);
                    columnValues.add(SQLHelper.convertToQueryValue(parentId));
                }

                lastCommentId=sqlHelper.insert(Comment.TABLE, columnNames, columnValues);
                if(lastCommentId==SQLHelper.INVALID) break;

                return ok();

            } catch(Exception e){

            }

        }while(false);

        return badRequest();
    }
}
