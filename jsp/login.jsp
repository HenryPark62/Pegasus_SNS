<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="dao.UserDAO" %>
<%
    request.setCharacterEncoding("utf-8");    

    String uid = request.getParameter("id");
    String upass = request.getParameter("ps");
    
    UserDAO dao = new UserDAO();
    int code = dao.login(uid, upass);
    if (code == 1) { 
        out.print("NE"); 	//out.print("아이디가 존재하지 않습니다.");
	} 
    else if (code == 2) {
        out.print("PE");	//out.print("패스워드가 일치하지 않습니다.");
    } 
    else {
        session.setAttribute("id", uid);	// 로그인 성공
    	out.print("OK");
    	}
%>