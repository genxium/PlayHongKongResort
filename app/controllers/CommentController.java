package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import model.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;
import views.html.helper.form;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommentController extends Controller {

    public static Result query(Integer activityId, Integer refIndex, Integer numItems, Integer direction, String token){
        response().setContentType("text/plain");
        do{
            try{
                Integer userId=null;
                if(token!=null){
                    userId= DataUtils.getUserIdByToken(token);
                }
                if(userId==DataUtils.invalidId) break;

                List<CommentOnActivity> comments=SQLCommander.queryComments(activityId, refIndex, SQLCommander.COMMENT_ON_ACTIVITY_ID, SQLHelper.directionDescend, numItems, direction, 0);

                ObjectNode result = Json.newObject();

                for(CommentOnActivity comment : comments){
                    result.put(String.valueOf(comment.getId()), comment.toObjectNode());
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
                String token=formData.get(User.tokenKey)[0];
                Integer activityId=Integer.valueOf(formData.get(CommentOnActivity.ACTIVITY_ID)[0]);
                String content=formData.get(CommentOnActivity.CONTENT)[0];
                Integer predecessorId=Integer.valueOf(formData.get(CommentOnActivity.PREDECESSOR_ID)[0]);

                if(token==null) break;
                Integer userId=DataUtils.getUserIdByToken(token);
                if(userId==DataUtils.invalidId) break;

                // create blank draft
                CommentOnActivity comment=null;

                int lastCommentId= SQLHelper.INVALID_ID;

                SQLHelper sqlHelper=new SQLHelper();
                List<String> columnNames=new LinkedList<String>();

                columnNames.add(CommentOnActivity.CONTENT);
                columnNames.add(CommentOnActivity.ACTIVITY_ID);
                columnNames.add(CommentOnActivity.COMMENT_TYPE);
                columnNames.add(CommentOnActivity.COMMENTER_ID);
                columnNames.add(CommentOnActivity.PREDECESSOR_ID);

                List<Object> columnValues=new LinkedList<Object>();

                columnValues.add(content);
                columnValues.add(SQLHelper.convertToQueryValue(activityId));
                columnValues.add(SQLHelper.convertToQueryValue(CommentOnActivity.TYPE_QA));
                columnValues.add(SQLHelper.convertToQueryValue(userId));
                columnValues.add(SQLHelper.convertToQueryValue(predecessorId));

                lastCommentId=sqlHelper.insertToTableByColumns("CommentOnActivity", columnNames, columnValues);
                if(lastCommentId==SQLHelper.INVALID_ID) break;

                return ok();

            } catch(Exception e){

            }

        }while(false);

        return badRequest();
    }
}
