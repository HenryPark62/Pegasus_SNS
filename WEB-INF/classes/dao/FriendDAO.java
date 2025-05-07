package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import util.ConnectionPool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FriendDAO {
    
    public String insert(String uid, String frid) throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {        
            String sql = "SELECT id FROM friend WHERE id = ? AND frid = ?";
            
            conn = ConnectionPool.get();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uid);
            stmt.setString(2, frid);
            
            rs = stmt.executeQuery();  
            if (rs.next()) return "EX"; 
            
            stmt.close();
            
            sql = "INSERT INTO friend (id, frid) VALUES(?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uid);
            stmt.setString(2, frid);
            
            int count = stmt.executeUpdate(); 
            return (count == 1) ? "OK" : "ER";
            
        } finally {
            if (rs != null) rs.close(); 
            if (stmt != null) stmt.close(); 
            if (conn != null) conn.close();
        }
    }
    
    public String remove(String uid, String frid) throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {	
            String sql = "DELETE FROM friend WHERE id = ? AND frid = ?";
            
            conn = ConnectionPool.get();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uid);
            stmt.setString(2, frid);
            
            int count = stmt.executeUpdate(); 
            return (count == 1) ? "OK" : "ER";
        } finally {	
            if (stmt != null) stmt.close(); 
            if (conn != null) conn.close();
        }
    }
    
    public String getList(String uid) throws NamingException, SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT frid FROM friend WHERE id = ?";
            
            conn = ConnectionPool.get();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uid);
            rs = stmt.executeQuery();
            
            List<String> fridList = new ArrayList<>();
            while (rs.next()) {
                fridList.add("'" + rs.getString("frid") + "'"); // 각 frid에 따옴표 추가
            }
            
            if (fridList.isEmpty()) return "[]"; // 친구가 없으면 빈 배열 반환
            
            // fridList를 SQL 쿼리에 사용하기 위한 문자열로 변환
            String str = String.join(",", fridList);
            
            sql = "SELECT jsonstr FROM user WHERE id IN (" + str + ")";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            JSONArray userArray = new JSONArray(); // 사용자 정보를 담을 JSON 배열
            while (rs.next()) {
                userArray.add(rs.getString("jsonstr"));
            }
            
            return userArray.toJSONString(); // JSON 배열 문자열 반환
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close(); 
            if (conn != null) conn.close();
        }
    }
}