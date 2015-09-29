package cn.ch.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class StringUtil {
	/*
	 * 字符串处理
	 */
	private final static Logger log = Logger.getLogger(StringUtil.class);
	/*
	 * 分解
	 */
	public static synchronized HashMap<String,String> getParameterMap(String source){
		HashMap<String,String> result = new HashMap<String, String>();
		try{
			String str  = source.substring(source.indexOf(SessionProtocol.SPLIT_SIGN)+1,source.length());
			String[] params = str.split("&");
			String[] entry;
			for(int i=0;i<params.length;i++){
				entry = params[i].split("=");
				result.put(entry[0].trim(), entry[1].trim());
			}
		}catch (Exception e) {
			log.error("参数传入不正确,无法进行提取,请检查!");
		}
		return result;
	}
}
