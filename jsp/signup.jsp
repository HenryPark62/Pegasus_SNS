<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="dao.UserDAO" %>
<%
	request.setCharacterEncoding("utf-8");

	String uid = request.getParameter("id"); 	//쿼리스트링의 아이디 (html) 
	//String upass = request.getParameter("ps");
	//String uname = request.getParameter("name");
		
	String jsonstr = request.getParameter("jsonstr");
	
	UserDAO dao = new UserDAO();
	if (dao.exists(uid)) {
		out.print("EX");
		return;
	}
	
	if (dao.insert(uid, jsonstr)) {
		//out.print("회원 가입이 완료되었습니다. 로그인 화면으로 이동합니다.");
		session.setAttribute("id", uid);
		out.print("OK");	//response.sendRedirect("main.jsp");
	}
	else {
		out.print("ER");
	}
%> 