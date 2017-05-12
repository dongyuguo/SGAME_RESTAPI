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

/**
 * 请求访问的根节点：/platform/server
 */
@Path("/platform/server")
public class ServerInfo {
	private static Connection conn;		//数据库连接对象
	private PreparedStatement pstmt;	//语句对象，可防止SQL注入
	private ResultSet rs;				//存储查询出来的结果集
	private ResultSetMetaData rsmd;		//防止JSON乱序使用的MetaData对象
    private String dataJson;			//返回的JSON数据部分
	private String json_code;			//返回的JSON状态码
	private String json_msg;			//返回的JSON提示消息
	private String resultJson;			//返回的最终JSON结果
	
	static{
		conn = ConnectionFactory.getConn();	
	}

	/**
	 * 用于返回目标游戏的GHO镜像
	 * @param api_id API开发者Id
	 * @param api_key API开发者Key
	 * @param game_name 目标游戏名
	 * @return
	 */
	@GET
    @Path("/server_info")
    @Produces(MediaType.TEXT_PLAIN)
    public String getInfo(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("user_id") String user_id, @QueryParam("game_name") String game_name) {
		boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
		if(!confirmRight){
			dataJson = "null";
			json_code = "401";
			json_msg = "error:your publisher id or api_key has an error,please check before next invoke";
		}else{
			//定义字符串参数、SQL查询参数以及JSON解析返回参数
			String sql = "SELECT * FROM server_info WHERE user_id=? AND game_name=?";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, user_id);
	    		pstmt.setString(2, game_name);
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
				if(!dataJson.equals("{}")){	//如果id符合，则正确返回；否则，返回失败
					json_code = "200";
					json_msg = "success";
				}else{
					dataJson = "null";
					json_code = "404";
					json_msg = "information:user has not register this game";
				}		
			} catch (SQLException | JSONException e) {
				e.printStackTrace();
			}
		}
    	resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}";	//封装为最终JSON
    	System.out.println(resultJson);
        return resultJson;
    }
	
	@POST
    @Path("/server_info")
    @Produces(MediaType.TEXT_PLAIN)
    public String postInfo(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("user_id") String user_id, @QueryParam("game_name") String game_name, @QueryParam("host") String host) {
		boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
		System.out.println("api_id:" + api_id + ";api_key:" + api_key + ";game_name:" + game_name + ";user_id:" + user_id + ";host:" + host);
		if(!confirmRight){
			dataJson = "null";
			json_code = "401";
			json_msg = "error:your publisher id or api_key has an error,please check before next invoke";
		}else{
			//定义字符串参数、SQL查询参数以及JSON解析返回参数
			String sql = "INSERT INTO server_info(user_id,game_name,host) VALUES(?,?,?)";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, user_id);
	    		pstmt.setString(2, game_name);
	    		pstmt.setString(3, host);
	    		int influenced_col = pstmt.executeUpdate();
	    		if(influenced_col == 1){	//如果服务器更新成功（成功插入），则返回成功；否则返回服务器错误
	    			json_code = "200";
	    			json_msg = "success:insert into DB successfully";
	    			System.out.println("insert " + user_id + " instance IP successfully");
	    		}else{
	    			json_code = "500";
	    			json_msg = "error:server encountered a problem,please try after a while or email 348015319@qq.com";
	    		}   		
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}"; //封装为最终JSON
    	return resultJson;
	}
}
