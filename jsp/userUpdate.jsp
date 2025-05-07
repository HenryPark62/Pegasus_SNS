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

    String uid = null, jsonstr = null, ufname = null; 
    byte[] ufile = null;

    // 파일 업로드 처리 시작
    ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
    List<FileItem> items = sfu.parseRequest(request);
    
    Iterator<FileItem> iter = items.iterator();
    while(iter.hasNext()) {
        FileItem item = iter.next();
        String name = item.getFieldName();
        if(item.isFormField()) {
            String value = item.getString("utf-8");
            if (name.equals("id")) uid = value;
            if (name.equals("jsonstr")) jsonstr = value;
        } else {
            if (name.equals("image")) {
                // 이미지가 선택된 경우
                ufname = item.getName(); // 원본 파일 이름 가져오기
                ufile = item.get(); // 파일 데이터 가져오기
                
                // 서버의 경로 설정
                String root = getServletContext().getRealPath("/images/profile");
                File dir = new File(root);
                if (!dir.exists()) {
                    dir.mkdirs(); // 폴더가 없으면 생성
                }
                
             // 파일 이름 중복 처리 제거
                File file = new File(dir, ufname); // 원본 파일 이름 사용
                
            
                // 이미지 저장
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(ufile);
                    out.println("File saved at: " + file.getAbsolutePath()); // 저장된 파일 경로 출력
                } catch (IOException e) {
                    e.printStackTrace(); // 오류 로그 출력
                    out.print("Error occurred while saving the file: " + e.getMessage()); // 오류 메시지 출력
                    return; // 함수 종료
                }
                
             // JSON에 이미지 파일 이름 추가
                try {
                    JSONObject userJson = new JSONObject(jsonstr);
                    userJson.put("images", new JSONArray().put(ufname)); // 이미지 파일 이름 추가
                    jsonstr = userJson.toString(); // 업데이트된 JSON 문자열
                } catch (JSONException e) {
                    e.printStackTrace();
                    out.print("ER"); // JSON 처리 오류 시
                    return;
                }
            }
        }
    }

    UserDAO dao = new UserDAO();
    if (dao.update(uid, jsonstr)) {
        out.print("OK");	
    } else {
        out.print("ER");	//ER: Error
    }
%>