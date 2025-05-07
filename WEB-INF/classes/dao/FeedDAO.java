package dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.naming.NamingException;

import util.ConnectionPool;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class FeedDAO {
	public boolean insert(String jsonstr) throws SQLException, NamingException, ParseException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {									
			synchronized(this) {
				conn = ConnectionPool.get();
			
				// phase 1. add "no" property
				
				String sql = "SELECT no FROM feed ORDER BY no DESC LIMIT 1";
				stmt = conn.prepareStatement(sql);
				rs = stmt.executeQuery();
				
				int max = (!rs.next()) ? 0 : rs.getInt("no");
				
				
				JSONParser parser = new JSONParser();
				JSONObject jsonobj = (JSONObject) parser.parse(jsonstr);

				jsonobj.put("no", max + 1);
				
				stmt.close(); rs.close();
				
				// phase 2. add "user" property
				
				String uid = jsonobj.get("id").toString();
				System.out.println("Received ID: " + uid);  // 로그 추가
				
				sql = "SELECT jsonstr FROM user WHERE id = ?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, uid);
				rs = stmt.executeQuery();
				
				if (rs.next()) {
					String usrstr = rs.getString("jsonstr");
					JSONObject usrobj = (JSONObject) parser.parse(usrstr);
					usrobj.remove("password");
					usrobj.remove("ts");
					jsonobj.put("user", usrobj);
				}
				stmt.close(); rs.close();
				
				// phase 3. insert jsonobj to the table
				
				sql = "INSERT INTO feed(no, id, jsonstr) VALUES(?, ?, ?)";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, max + 1);
				stmt.setString(2, uid);
				System.out.println("Inserting ID: " + uid);
				stmt.setString(3, jsonobj.toJSONString());
				
				int count = stmt.executeUpdate(); 	
				return (count == 1) ? true : false;
			}
		} finally {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();		
				if (conn != null) conn.close();
			}
	}
	
	public boolean delete(int no, String currentUserId) throws SQLException, NamingException, ParseException {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    try {
	        // 게시글 작성자 ID를 가져오는 쿼리
	        String selectSql = "SELECT jsonstr, id FROM feed WHERE no = ?";
	        conn = ConnectionPool.get();
	        stmt = conn.prepareStatement(selectSql);
	        stmt.setInt(1, no); // 게시글 번호를 사용

	        rs = stmt.executeQuery();

	        // 게시글 존재 여부 확인
	        if (!rs.next()) {
	            System.out.println("게시글이 존재하지 않음");
	            return false;
	        }

	        String postId = rs.getString("id"); // 게시글 작성자 ID를 가져옴

	        // 작성자 ID와 현재 사용자 ID가 일치하는지 확인
	        if (!postId.equals(currentUserId)) {
	            System.out.println("삭제 권한이 없음");
	            return false; // 권한이 없으면 삭제하지 않음
	        }

	        // 삭제 쿼리 실행
	        String deleteSql = "DELETE FROM feed WHERE no = ?";
	        stmt = conn.prepareStatement(deleteSql);
	        stmt.setInt(1, no); // 게시글 번호를 사용

	        int count = stmt.executeUpdate();
	        return count == 1; // 삭제 성공 여부 반환

	    } finally {
	        // 자원 해제
	        if (rs != null) rs.close();
	        if (stmt != null) stmt.close();
	        if (conn != null) conn.close();
	    }
	}
	
	public boolean update(int no, String currentUserId, String newContent) throws SQLException, NamingException, ParseException {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    try {
	        conn = ConnectionPool.get();

	        // Phase 1: 게시글 존재 및 수정 권한 확인
	        String selectSql = "SELECT jsonstr, id FROM feed WHERE no = ?";
	        stmt = conn.prepareStatement(selectSql);
	        stmt.setInt(1, no);

	        rs = stmt.executeQuery();

	        if (!rs.next()) {
	            System.out.println("게시글이 존재하지 않습니다.");
	            return false;
	        }

	        String postId = rs.getString("id");

	        if (!postId.equals(currentUserId)) {
	            System.out.println("수정 권한이 없습니다. 작성자만 수정할 수 있습니다.");
	            return false;
	        }

	        // Phase 2: 기존 jsonstr 파싱 및 내용 업데이트
	        String jsonStr = rs.getString("jsonstr");
	        JSONParser parser = new JSONParser();
	        JSONObject jsonObj = (JSONObject) parser.parse(jsonStr);

	        jsonObj.put("content", newContent);  // 게시글 내용 수정
	        jsonObj.put("ts", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")));  // 수정 시간 업데이트

	        // Phase 3: 업데이트된 jsonstr로 DB 갱신
	        String updateSql = "UPDATE feed SET jsonstr = ? WHERE no = ?";
	        stmt.close();  // 이전 Statement 닫기
	        stmt = conn.prepareStatement(updateSql);
	        stmt.setString(1, jsonObj.toJSONString());
	        stmt.setInt(2, no);

	        int count = stmt.executeUpdate();
	        return count == 1;

	    } finally {
	        if (rs != null) rs.close();
	        if (stmt != null) stmt.close();
	        if (conn != null) conn.close();
	    }
	}

	
	public String getGroup(String frids, String maxNo) throws NamingException, SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {						
			String sql = "SELECT jsonstr FROM feed WHERE id IN (" + frids + ")";
			if (maxNo != null) {
				sql += " AND no < " + maxNo;
			}
			sql += " ORDER BY no DESC";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery(); 
			
			String str = "[";
			int cnt = 0;
			while(rs.next()) {
				if (cnt++ > 0) str += ", ";
				str += rs.getString("jsonstr");
			}
			return str + "]";
			
		} finally {
			if (rs != null) rs.close();		
			if (stmt != null) stmt.close();		
			if (conn != null) conn.close();
		}
	}
	
	public String getList() throws NamingException, SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {						
			String sql = "SELECT jsonstr FROM feed ORDER BY no DESC";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery(); 
			
			String str = "[";
			int cnt = 0;
			while(rs.next()) {
				if (cnt++ > 0) str += ", ";
				str += rs.getString("jsonstr");
			}
			return str + "]";
			
		} finally {
			if (rs != null) rs.close();		
			if (stmt != null) stmt.close();		
			if (conn != null) conn.close();
		}
	}
	
	public String getFeedByNo(int no) throws NamingException, SQLException, ParseException {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    String jsonResult = null;

	    try {
	        // 데이터베이스 연결
	        conn = ConnectionPool.get();

	        // 'no'와 'userId'를 모두 이용한 SQL 쿼리
	        String query = "SELECT jsonstr FROM feed WHERE no = ?";
	        stmt = conn.prepareStatement(query);
	        stmt.setInt(1, no);           // 게시글 번호 (no)

	        rs = stmt.executeQuery();

	        // 게시글이 존재하는지 확인
	        if (rs.next()) {
	            String jsonstr = rs.getString("jsonstr");

	            // JSON 문자열 파싱
	            JSONParser parser = new JSONParser();
	            JSONObject jsonObject = (JSONObject) parser.parse(jsonstr);

	            // JSON 객체를 문자열로 변환
	            jsonResult = jsonObject.toJSONString();
	        }
	    } finally {
	        // 자원 해제
	        if (rs != null) rs.close();
	        if (stmt != null) stmt.close();
	        if (conn != null) conn.close();
	    }

	    return jsonResult;
	}



}