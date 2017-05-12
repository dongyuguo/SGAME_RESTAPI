package com.sgame.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;

import com.sgame.conn.ConnectionFactory;
import com.sgame.util.APIUtil;
import com.sgame.util.ParseJsonUtil;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/platform/ghostVM")
public class GhoVMHostInfo {
	private static Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private ResultSetMetaData rsmd = null;
    private String dataJson = null;
    private String resultJson = null;
	private String json_code = null;
	private String json_msg = null;
	
	static{
		conn = ConnectionFactory.getConn();	
	}

	/**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
	/**
	 * 用于返回目标游戏的GHO镜像
	 * @param api_id API开发者Id
	 * @param api_key API开发者Key
	 * @param game_name 目标游戏名
	 * @return
	 */
	@GET
    @Path("/information")
    @Produces(MediaType.TEXT_PLAIN)
    public String getInfo(@QueryParam("api_id") String api_id, @QueryParam("api_key") String api_key, @QueryParam("game_name") String game_name) {
		boolean confirmRight = APIUtil.apiAccessConfirm(api_id, api_key);	//验证用户调用API权限
		System.out.println(game_name);
		if(!confirmRight){
			dataJson = "null";
			json_code = "401";
			json_msg = "error:your publisher id or api_key has an error,please check before next invoke";
		}else{
			//定义字符串参数、SQL查询参数以及JSON解析返回参数
			String sql = "SELECT * FROM gho_info WHERE game_name=?";
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	    		pstmt.setString(1, game_name);
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
					json_msg = "error:game_name not registered,please with try a right game_name";
				}			
			} catch (SQLException | JSONException e) {
				e.printStackTrace();
			}
		}
    	resultJson = "{\"data\":" + dataJson + ",\"code\":" + json_code + ",\"msg\":\"" + json_msg + "\"}";	//封装为最终JSON
    	System.out.println(resultJson);
        return resultJson;
    }
}
