package com.sgame.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONStringer;

public class ParseJsonUtil {
	
	/**
	 * 将HashMap结果集转换为JSON文本
	 * @param resultMap 结果集素材
	 * @param rsmd 作为结果集的key顺序索引
	 * @return JSON目标字符串
	 * @throws JSONException JSON解析出错
	 * @throws SQLException 参数rsmd为空，导致报出数据库异常错误
	 */
	public static String convertResultsetToJson(HashMap<String,String> resultMap,ResultSetMetaData rsmd) throws JSONException, SQLException{
		JSONStringer resultJsonObject = new JSONStringer(); 
		resultJsonObject.object();	//开启JSONStringer序列化
		for(int i = 1; i <= resultMap.size(); i++){	//使用JSONStringer进行序列化key-value
			resultJsonObject.key(rsmd.getColumnName(i));
			resultJsonObject.value(resultMap.get(rsmd.getColumnName(i)));		
		}
		resultJsonObject.endObject(); //完成JSONStringer序列化
		return resultJsonObject.toString();	
	}
}
