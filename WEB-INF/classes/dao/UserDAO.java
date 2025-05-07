package dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import util.ConnectionPool;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserDAO {
	public boolean insert(String uid, String jsonstr) throws SQLException, NamingException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {									//try-catch-finally 구문 사용 -> NullPointException Error 처리 가능 
			String sql = "INSERT INTO user(id, jsonstr) VALUES(?, ?)";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, uid);
			stmt.setString(2, jsonstr);
			
			int count = stmt.executeUpdate(); 	//INSERT 구문에서는 executeUpdate() 함수 사용 =>update된 레코드 수를 반환. 
			return (count == 1) ? true : false;
			
		} finally {
			if (stmt != null) stmt.close();		//자원반납  
			if (conn != null) conn.close();
		}
	}

	public boolean exists(String uid) throws SQLException, NamingException {	//boolean -> true or false 반환 
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id FROM user WHERE id =?";
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, uid);		
			
			rs = stmt.executeQuery(); 	//DB에서 id check. SELECT 구문에서는 executeQuery() 함수 사용. 
			return rs.next(); 			//record가 있으면 true 반환. 
			
		} finally {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public boolean delete(String uid, String upass) throws SQLException, NamingException {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    try {
	        String selectSql = "SELECT jsonstr FROM user WHERE id = ?";
	        conn = ConnectionPool.get();
	        stmt = conn.prepareStatement(selectSql);
	        stmt.setString(1, uid);
	        
	        rs = stmt.executeQuery();
	        if (!rs.next()) return false; // 사용자가 존재하지 않으면 false 반환
	        
	        // JSON에서 비밀번호 추출
	        String jsonstr = rs.getString("jsonstr");
	        JSONObject obj = (JSONObject) (new JSONParser()).parse(jsonstr);
	        String storedPass = obj.get("password").toString();
	        
	        // 비밀번호 확인
	        if (!upass.equals(storedPass)) return false;
	        
	        // 비밀번호가 일치하면 삭제 진행
	        String deleteSql = "DELETE FROM user WHERE id = ?";
	        stmt = conn.prepareStatement(deleteSql);
	        stmt.setString(1, uid);
	        
	        int count = stmt.executeUpdate();
	        return count == 1;
	        
	    } catch (ParseException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        if (rs != null) rs.close();
	        if (stmt != null) stmt.close();
	        if (conn != null) conn.close();
	    }
	}
	
	public int login(String uid, String upass) throws SQLException, NamingException, ParseException {	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT jsonstr FROM user WHERE id = ?";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1,uid);
			
			rs = stmt.executeQuery(); 
			if (!rs.next()) return 1;
			
			String jsonstr = rs.getString("jsonstr");
			JSONObject obj = (JSONObject) (new JSONParser()).parse(jsonstr);
			String pass = obj.get("password").toString();
			
			if (!upass.equals(pass)) return 2;
			
			return 0;
			
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
			String sql = "SELECT jsonstr FROM user";
			
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
	
	public String get(String uid) throws NamingException, SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT jsonstr FROM user WHERE id = ?";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, uid);
			
			
			rs = stmt.executeQuery();
			return rs.next() ? rs.getString("jsonstr") : "{}";
			
		} finally {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public boolean update(String uid, String jsonstr) throws NamingException, SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE user SET jsonstr = ? WHERE id = ?";
			
			conn = ConnectionPool.get();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, jsonstr);
			stmt.setString(2, uid);
			
			
			int count = stmt.executeUpdate();
			return (count == 1) ? true : false;
			
		} finally {
			if (stmt != null) stmt.close(); 
			if (conn != null) conn.close();
		}
	}
}