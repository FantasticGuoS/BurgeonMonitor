<%@ page language="java" import="java.util.*,java.util.List,nds.query.QueryEngine,nds.query.QueryUtils,org.json.*,java.sql.Date" pageEncoding="utf-8"%>
<%
String login = request.getParameter("email");
String email = login.trim();
List al = QueryEngine.getInstance().doQueryList("SELECT passwordhash, NAME, to_char(SYSDATE, 'yyyy-mm-dd hh24:mi:ss') AS time FROM users WHERE ad_client_id = 37 AND email = "
		+ QueryUtils.TO_STRING(email));
JSONObject json = new JSONObject();
if(al.size()>0){
	String ph = (String)((List)al.get(0)).get(0);
	String name = (String)((List)al.get(0)).get(1);
	String time = (String)((List)al.get(0)).get(2);
	json.put("isSuccess", true);
	json.put("name", name);
	json.put("ph", ph);
	json.put("serverTime", time);
	json.put("message", "BOS运行正常");
%>
	<%=json%>
<%
} else {
	json.put("isSuccess", false);
	json.put("message", "BOS运行异常或用户不存在");
%>
	<%=json%>
<%
}
%>