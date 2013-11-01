<%@ page language="java" import="dao.SQLHelper, utilities.Converter, org.json.simple.JSONObject, java.util.Iterator, java.util.List, java.util.ArrayList, java.io.*,java.util.*" %>

<%
	// distinguish request type
    String requestToken="r";
	int requestType=Integer.parseInt(request.getParameter(requestToken));

	// Branching
	switch(requestType){
		case 0: //requestType==0, checkConnection request
		{
			// DAO
			SQLHelper sqlHelper=new SQLHelper();
			String status = sqlHelper.checkConnectionWithStringResult();
			out.println(status);
		}
		break;
		case 1: // requestType==1, login request
		{
			String email=request.getParameter("email");
			String password=request.getParameter("password");
			String passwordDigest=Converter.md5(password);
			//out.println("passwordDigest is "+passwordDigest);
			// DAO
			SQLHelper sqlHelper=new SQLHelper();
			boolean status = sqlHelper.checkConnection();
			if(status==true){
				String query=("SELECT * FROM User WHERE email='"+email+"' AND password='"+passwordDigest+"'");
				List<JSONObject> results=sqlHelper.executeSelect(query);
				if(results!=null){
					if(results.size()>0){
						Iterator it=results.iterator();
				        while(it.hasNext())
				        {
				          JSONObject jsonObject=(JSONObject)it.next();
				          String token = Converter.generateToken(email, password);
				          session.setAttribute(token, email);
				          out.println(token);
				          break;
				        }
					}else{
						out.println("not found");
					}		
				} else{
					out.println("Failed to login");
				}
			} else{
				out.println("Failed to connect to database");
			}		
		}
		break;
		case 2: // requestType==2, register request
		{
			String email=request.getParameter("email");
			String password=request.getParameter("password");
			String name=email;
			// DAO
			SQLHelper sqlHelper=new SQLHelper();
			
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("INSERT INTO User(email,password,name) VALUES(");
			queryBuilder.append("'"+email+"'");
			queryBuilder.append(",");
			queryBuilder.append("md5('"+password+"')");
			queryBuilder.append(",");
			queryBuilder.append("'"+name+"'");
			queryBuilder.append(")");
			String query=queryBuilder.toString();
			out.println("query is "+query);
			sqlHelper.executeInsert(query);
		}
		break;
		case 3: // requestType==3, validateToken
		{
			String token=request.getParameter("token");
			String email = session.getAttribute(token).toString();
			if(email.length()>0){
				out.println(email+"logged in");
			} else{
				out.println("not logged in");
			}
		}
		break;
		default:
		break;
	}
%>