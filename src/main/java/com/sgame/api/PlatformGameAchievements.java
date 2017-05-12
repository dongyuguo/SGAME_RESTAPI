package com.sgame.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;

import com.sgame.conn.ConnectionFactory;
import com.sgame.util.APIUtil;
import com.sgame.util.ParseJsonUtil;
import com.sgame.util.SendUDP;

/**
 * Root resource (exposed at "/platform/game/achievements" path)
 */
@Path("/platform/game/achievements")
public class PlatformGameAchievements {

	static Connection conn = null;	//定义数据库全局连接对象
	PreparedStatement pstmt = null;	//定义数据库全局语句对象
	ResultSet rs = null;			//数据库全局结果集
	ResultSetMetaData rsmd = null;	//读取数据取列名的必要结果集对象
	String dataJson = "";			//返回的data的JSON内容
	String json_code = null;		//返回JSON的code码
	String json_msg = null;			//返回JSON的提示消息内容
	String resultJson = null;		//返回的最终JSON字符串

	
	//静态函数，创建数据库连接以优化代码结构
	static{
		conn = ConnectionFactory.getConn();	
	}
	
	/**
	 * 用户查询一个成就的详细信息
	 * @param api_id 开发商id号
     * @param api_key 每个开发商注册验证成功后获得的api_key，用于调用API的验证
	 * @param achievement_id 成就id值
	 * @return 返回成就详细信息的JSON文本
	 */
	@GET
    @Path("/record")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOne(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("achievement_id") String achievement_id) {
		boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
		System.out.println(api_id + "," + api_key + "," + achievement_id);
		if(!confirmRight){
			dataJson = "null";
			json_code = "401";
			json_msg = "error:your api_id or api_key has an error,please check before next invoke";
		}else{
			//定义字符串参数、SQL查询参数以及JSON解析返回参数
			String sql = "SELECT * FROM achievements WHERE achievement_id=?";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, achievement_id);
	    		rs = pstmt.executeQuery();
	    		rsmd = rs.getMetaData();
	    		int colNum = rsmd.getColumnCount();
	    		HashMap<String, String> resultMap = new HashMap<String, String>();	//存储字符串映射，以便封装JSON 
				while(rs.next()){	//将结果集放入HsahMap中
					for(int i = 1; i <= colNum; i++){
						resultMap.put(rsmd.getColumnName(i), rs.getString(i));
					}
		    	}
				dataJson = ParseJsonUtil.convertResultsetToJson(resultMap, rsmd);
				if(!dataJson.equals("{}")){	//如果achievement_id符合，则正确返回；否则，返回失败
					json_code = "200";
					json_msg = "success";
				}else{
					dataJson = "null";
					json_code = "404";
					json_msg = "error:parameter achievement_id not registered,please with try a right achievement_id";
				}			
			} catch (SQLException | JSONException e) {
				e.printStackTrace();
			}
		}
    	resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}";	//封装为最终JSON
    	System.out.println(resultJson);
        return resultJson;
    }
	
	/**
	 * 用户批量查询成就的详细信息
	 * @param api_id 开发商id号
     * @param api_key 每个开发商注册验证成功后获得的api_key，用于调用API的验证
	 * @param from 查询起始id
	 * @param to 查询结束id
	 * @return 返回id包含在from,to之间的成就详细信息的JSON文本
	 */
    @GET
    @Path("/records")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFromTo(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("from") String from, @QueryParam("to") String to) {
    	boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
		if(!confirmRight){
			dataJson = "null";
			json_code = "401";
			json_msg = "error:your api_id or api_key has an error,please check before next invoke";
		}else{
			String sql = "SELECT * FROM achievements WHERE achievement_id between ? and ?";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, from);
	    		pstmt.setString(2, to);
	    		rs = pstmt.executeQuery();
	    		rsmd = rs.getMetaData();
	    		int colNum = rsmd.getColumnCount();
	    		HashMap<String, String> resultMap = new HashMap<String, String>();
				while(rs.next()){	//循环叠加字符串，使得JSONObjcet对象合成为统一的字符串
					for(int i = 1; i <= colNum; i++){
						resultMap.put(rsmd.getColumnName(i), rs.getString(i));
					}
					dataJson += ParseJsonUtil.convertResultsetToJson(resultMap, rsmd) + ",";	//字符串的叠加
		    	}
				if(!dataJson.equals("")){	//如果查询参数出错，则无法进行字符串操作
					dataJson = "[" + dataJson.substring(0, dataJson.length() - 1) + "]";	//由于添加字符串使得末尾多了一个逗号，因此需要去除，并封装为JSON数组
					json_code = "200";
					json_msg = "success";
				}else{
					dataJson = "null";
					json_code = "404";
					json_msg = "error:parameter id not registered or not in the search range,please try with a valid id";
				}
			} catch (SQLException | JSONException e) {
				e.printStackTrace();
			}
		}
    	resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}"; //封装为最终JSON
        return resultJson;
    }
    
    /**
     * 用户游戏开发商向服务器提交用户完成成就的记录
     * @param api_id 开发商id号
     * @param api_key 每个开发商注册验证成功后获得的api_key，用于调用API的验证
     * @param user_id 成功获得成就的用户id编号
     * @param achievement_id 提交的成就编号
     * @return 返回是否添加成功的JSON字符串
     */
    @POST
    @Path("/record")
    @Produces(MediaType.TEXT_PLAIN)
    public String postIt(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("user_id") String user_id, @QueryParam("achievement_id") String achievement_id) {
    	dataJson = "null";	//由于是更新操作，因此dataJson不存放任何有效数据，返回结果以json_code和json_msg为准
    	boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
    	System.out.println("insert a record:" + api_id + "," +api_key + "," + user_id);
		if(!confirmRight){
			json_code = "401";
			json_msg = "error:your api_id or api_key has an error,please check before next invoke";
		}else{
			String sql = "INSERT INTO user_achievements(user_id,achievement_id,unlock_time) VALUES(?,?,?)";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, user_id);
	    		pstmt.setString(2, achievement_id);
	    		pstmt.setString(3, APIUtil.getDateTime());
	    		int influencedrecord = pstmt.executeUpdate();
	    		if(influencedrecord == 1){	//如果服务器更新成功（成功插入），则返回成功；否则返回服务器错误
	    			json_code = "200";
	    			json_msg = "success:update user achievement successfully";
	    			System.out.println("insert achievement successfully");
	    			SendUDP udp_sender = new SendUDP();
	    			udp_sender.sendToClient(achievement_id, udp_sender.clientIP(user_id), 65534);//发送成就id编号给客户端
	    		}else{
	    			json_code = "500";
	    			json_msg = "error:server encountered a problem,please try after a while or email 348015319@qq.com";
	    		}   		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}"; //封装为最终JSON
    	return resultJson;
    }
    
}
