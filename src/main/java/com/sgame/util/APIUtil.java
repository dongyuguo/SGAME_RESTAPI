package com.sgame.util;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.sgame.conn.ConnectionFactory;

public class APIUtil {
	
	/**
	 * 用于测试用户是否拥有调用API的权限
	 * @param api_id api的id值
	 * @param api_key api的key值
	 * @return 若成功，返回true；否则返回false
	 */
	public static boolean apiAccessConfirm(String api_id, String api_key){
		Connection conn = ConnectionFactory.getConn();
		String sql = "SELECT id FROM game_publisher WHERE api_id=? and api_key=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, api_id);
			pstmt.setString(2, api_key);
			rs = pstmt.executeQuery();
			while(rs.next()){
				System.out.println("API authorlization success");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("API authorlization fail");
		return false;
	}
	
	public static String getDateTime(){
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}
}
