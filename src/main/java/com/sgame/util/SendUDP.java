package com.sgame.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sgame.conn.ConnectionFactory;

/**
 * 用户发送UDP数据包和客户端的IP地址
 * @author gdy
 */
public class SendUDP {
	
	/**
	 * 发送UDP数据包到客户端，需要客户端轮训接收
	 * @param sendStr 发送给客户端的字符串
	 * @param IP 目标机器IP地址
	 * @param port 协议端口号
	 * @throws Exception UDP包发送出错
	 */
	public void sendToClient(String sendStr, String IP, int port) throws Exception{
		DatagramSocket client = new DatagramSocket();
        byte[] sendBuf;
        sendBuf = sendStr.getBytes();
        InetAddress addr = InetAddress.getByName(IP);
        DatagramPacket sendPacket = new DatagramPacket(sendBuf ,sendBuf.length , addr , port);
        client.send(sendPacket);
        client.close();
	}
	
	/**
	 * 获取客户机的IP地址
	 * @param user_id 用户的ID，在用户登陆的时候将注册IP地址
	 * @return 返回客户端的IP地址
	 */
	public String clientIP(String user_id){
		Connection conn = ConnectionFactory.getConn();
		PreparedStatement pstmt;
		ResultSet rs;
		String sql = "SELECT clientIP FROM user_basic_info WHERE user_id=?";
		String targetIP = null;
		System.out.println("user_id:" + user_id);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			while(rs.next()){
				targetIP = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return targetIP;
	}
	
}
