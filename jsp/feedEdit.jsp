<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="java.io.*" %>
<%@ page import="dao.*" %>
<%@ page import="org.json.*" %>

<%
    request.setCharacterEncoding("utf-8");

    String jsonstr = null, ufname = null;
    byte[] ufile = null;

    // 세션에서 사용자 ID 가져오기
    String currentUserId = (String) session.getAttribute("id");
    String postNoStr = null;  // 파라미터로 전달된 게시글 번호를 저장할 변수

    // 디버깅 로그: 받은 파라미터 확인
    System.out.println("받은 파라미터 currentUserId: " + currentUserId);

    // 파일 업로드 처리
    ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
    List<FileItem> items = sfu.parseRequest(request);

    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
        FileItem item = iter.next();
        String name = item.getFieldName();
        if (item.isFormField()) {
            String value = item.getString("utf-8");

            // jsonstr을 받기
            if (name.equals("jsonstr")) {
                jsonstr = value;  // jsonstr을 받는다
            }
        } else {
            if (name.equals("image")) {
                ufname = item.getName();
                ufile = item.get();

                String root = getServletContext().getRealPath("/images/feed");
                File dir = new File(root);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, ufname);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(ufile);
                    out.println("File saved at: " + file.getAbsolutePath()); // 저장된 파일 경로 출력
                } catch (IOException e) {
                    e.printStackTrace();
                    out.print("Error occurred while saving the file: " + e.getMessage());
                    return;
                }

                try {
                    // 파일 이름을 jsonstr에 추가
                    JSONObject feedJson = new JSONObject(jsonstr);
                    feedJson.put("images", new JSONArray().put(ufname));
                    jsonstr = feedJson.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    out.print("Error parsing JSON");
                    return;
                }
            }
        }
    }

    // JSON 문자열 파싱 후 postNo와 content 추출
    try {
        JSONObject jsonObj = new JSONObject(jsonstr);
        postNoStr = jsonObj.optString("no");  // jsonstr에서 'no' 값을 추출
        String newContent = jsonObj.optString("content");

        // 디버깅 로그: jsonstr 내용 출력
        System.out.println("최종 JSON 데이터: " + jsonstr);
        System.out.println("추출된 게시글 번호: " + postNoStr);

        // 게시글 번호가 올바른지 확인하고 게시글 수정
        if (postNoStr != null && !postNoStr.isEmpty()) {
            int postNo = Integer.parseInt(postNoStr);  // 게시글 번호를 정수로 변환

            FeedDAO dao = new FeedDAO();
            if (dao.update(postNo, currentUserId, newContent)) {
                out.print("OK");
            } else {
                out.print("Error updating post.");
            }
        } else {
            out.print("Invalid post number.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.print("Error updating post.");
    }
%>