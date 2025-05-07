<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="dao.*" %>
<%@ page import="javax.naming.NamingException" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="org.json.simple.parser.ParseException" %>
<%
    // 요청 파라미터에서 필요한 값들 가져오기
    String frids = request.getParameter("frids");
    String maxNo = request.getParameter("maxNo");
    String noStr = request.getParameter("no"); // 특정 게시물 번호

    FeedDAO dao = new FeedDAO();
    String result = "";

    try {
        if (noStr != null) { // no와 userId 값이 있을 경우 특정 게시글만 가져오기
            int no = Integer.parseInt(noStr);
            result = dao.getFeedByNo(no); // 특정 게시글 가져오기
            if (result == null) {
                result = "[]"; // 게시글이 없는 경우 빈 배열 반환
            } else {
                result = "[" + result + "]"; // 단일 게시글을 배열 형식으로 반환
            }
        } else { // no 값이 없으면 기존 방식으로 여러 게시글 가져오기
            result = dao.getGroup(frids, maxNo);
        }
    } catch (SQLException | NamingException | ParseException e) {
        e.printStackTrace();
        result = "[]"; // 오류 발생 시 빈 배열 반환
    }

    out.print(result);
%>
