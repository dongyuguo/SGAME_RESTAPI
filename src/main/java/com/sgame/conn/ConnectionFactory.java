package com.sgame.conn;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionFactory {
	
	private static 	Connection conn;

	private static String driverName = null;

	private static String url = null;

	private static String loginname = null;

	private static String password = null;
	
	static{
		driverName = "com.mysql.jdbc.Driver";
		url = "jdbc:mysql://127.0.0.1/sgame?useUnicode=true&characterEncoding=utf-8";
		loginname = "root";
		password = "123456";
	}
	
	private ConnectionFactory(){}
	

	public static Connection getConn(){
		
		if(conn==null){
			
			try {
				Class.forName(driverName);
				conn = DriverManager.getConnection(url,loginname,password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return conn;
	}
	
}
