package controllers;
import dao.*;
import model.Guest;
import model.BasicUser;
import org.json.simple.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SQLCommander {
 	public static BasicUser getBasicUserByEmail(String email){

 		String tableName="User";
 		String idKey="UserId";
 		String emailKey="UserEmail";
 		String nameKey="UserName";
 		String passwordKey="UserPassword";
 		String groupIdKey="UserGroupId";
 		String authenticationStatusKey="UserAuthenticationStatus";
 		String genderKey="UserGender";
 		String lastLoggedInTimeKey="UserLastLoggedInTime";

 		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("SELECT ");
		queryBuilder.append(idKey+",");
		queryBuilder.append(nameKey+",");
		queryBuilder.append(passwordKey+",");
		queryBuilder.append(groupIdKey+",");
		queryBuilder.append(authenticationStatusKey+",");
		queryBuilder.append(genderKey+",");
		queryBuilder.append(lastLoggedInTimeKey);

		queryBuilder.append(" FROM "+tableName+" WHERE ");

		queryBuilder.append(emailKey+"="+email);

		String query=queryBuilder.toString();
		SQLHelper sqlHelper=new SQLHelper();
		List<JSONObject> results=sqlHelper.executeSelect(query);
		if(results!=null && results.size()>0){
            Iterator it=results.iterator();
	        if(it.hasNext()){
		        JSONObject jsonObject=(JSONObject)it.next();
		        try {
		      		int id=Integer.parseInt((String)jsonObject.get(idKey));
		      		String password=(String)jsonObject.get(passwordKey);
	        		String name=(String)jsonObject.get(nameKey);
          		    BasicUser user=new BasicUser(id, email, password, name, false, false, false);
          		    return user;
			    } catch (Exception e) {

		        }
	    	} 	
		}
		
		return null;
	}
};