<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="dao.FeedDAO" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%
    request.setCharacterEncoding("utf-8");

    // 세션에서 사용자 ID 가져오기
    HttpSession currentSession = request.getSession();
    String currentUserId = (String) currentSession.getAttribute("id");

    // URL에서 id와 no 값을 추출
    String postId = request.getParameter("fid"); // 게시글 작성자 ID
    String no = request.getParameter("no"); // 게시글 no 값

    System.out.println("유저 ID: " + currentUserId); // 로그 확인
    System.out.println("게시글 작성자 ID: " + postId); // 로그 확인
    System.out.println("게시글 no: " + no); // 로그 확인

    // ID가 null인 경우 처리
    if (currentUserId == null || postId == null || no == null) {
        out.print("ER"); // 파라미터가 유효하지 않음
        return;
    }

    // 작성자 ID와 현재 사용자 ID 비교
    if (!currentUserId.equals(postId)) {
        out.print("ER"); // 삭제 권한 없음
        return;
    }

    // FeedDAO 인스턴스 생성
    FeedDAO dao = new FeedDAO();

    // 게시글 삭제 로직 호출
    boolean isDeleted = dao.delete(Integer.parseInt(no), currentUserId);

    if (isDeleted) {
        out.print("OK"); // 삭제 성공
    } else {
        out.print("ER"); // 삭제 실패
    }
%>
