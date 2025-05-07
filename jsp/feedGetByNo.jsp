<%@ page contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="dao.FeedDAO" %>
<%@ page import="org.json.simple.JSONObject" %>
<%
    // 요청 파라미터로 받은 id와 no 값
    String id = request.getParameter("id");
    String noStr = request.getParameter("no");
    int no = -1;

    // 디버깅 로그
    System.out.println("받은 파라미터 id: " + id);
    System.out.println("받은 파라미터 noStr: " + noStr);

    // id 또는 no 값이 없으면 400 오류
    if (id == null || id.isEmpty()) {
        response.setStatus(400);
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "사용자 ID가 누락되었습니다.");
        out.print(errorResponse.toJSONString());
        return;
    }

    if (noStr != null && !noStr.isEmpty()) {
        try {
            no = Integer.parseInt(noStr);  // 게시글 번호 파싱
            System.out.println("파싱된 게시글 번호 no: " + no);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(400); // 잘못된 게시글 번호 포맷
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "잘못된 게시글 번호입니다.");
            out.print(errorResponse.toJSONString());
            return;
        }
    } else {
        response.setStatus(400); // 게시글 번호가 없을 때
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "게시글 번호가 누락되었습니다.");
        out.print(errorResponse.toJSONString());
        return;
    }

    // FeedDAO 객체 생성
    FeedDAO dao = new FeedDAO();
    String jsonstr = dao.getFeedByNo(no); // 게시글 번호와 사용자 ID로 JSON 데이터를 가져옴

    // JSON 데이터가 있으면 반환, 없으면 404 오류 처리
    response.setContentType("application/json; charset=UTF-8");
    if (jsonstr != null) {
        out.print(jsonstr); // JSON 데이터를 클라이언트로 전송
    } else {
        response.setStatus(404); // 404 상태 코드 반환
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "게시글을 찾을 수 없습니다.");
        out.print(errorResponse.toJSONString());
    }
%>
