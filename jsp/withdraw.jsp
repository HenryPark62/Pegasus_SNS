<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="dao.*" %>

<%
    request.setCharacterEncoding("utf-8");

    String uid = request.getParameter("id"); // 사용자 아이디
    String jsonstr = request.getParameter("jsonstr"); // JSON 형태의 사용자 정보
    
    UserDAO dao = new UserDAO();

    // password 값 추출
    String password = null;
    try {
        // "password":"값" 형식에서 값을 추출
        int passIndex = jsonstr.indexOf("\"password\":\"");
        if (passIndex != -1) {
            int start = passIndex + 12;
            int end = jsonstr.indexOf("\"", start);
            if (end != -1) {
                password = jsonstr.substring(start, end);
            }
        }
    } catch (Exception e) {
        out.print("ER"); // JSON 파싱 오류
        return;
    }

    // 회원 정보 존재 여부 확인
    if (!dao.exists(uid)) {
        out.print("NF"); // Not Found: 회원 정보를 찾을 수 없음
        return;
    }
    
    // 회원 탈퇴 시도
    if (dao.delete(uid, password)) {
        out.print("OK"); // OK: 회원 탈퇴 성공
        session.removeAttribute("id");
    } else {
        out.print("ER"); // Error: 회원 탈퇴 중 오류 발생
    }
%>