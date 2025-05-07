<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="dao.*" %>
<%
	String uid = request.getParameter("id");
	String frid = request.getParameter("frid");
	System.out.println("add friend: uid=" + uid + ", frid=" + frid); // 로그 추가
	out.print((new FriendDAO()).insert(uid, frid));
%>