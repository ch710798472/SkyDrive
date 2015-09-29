package cn.ch.util;

public interface SessionProtocol {
	/*
	 * 回话协议,指定操作方法
	 * 通信协议规则:	协议类型+分割号+参数列表(param1=value1&param2=value2)
	 */
	
	//分隔字符
	String SPLIT_SIGN = ": ";
	
	//登陆
	String LOGIN = "LOGIN";
	//登陆成功
	String SUCCESS = "SUCCESS";
	//登录失败
	String FAILURE = "FAILURE";
	//会话ID
	String SESSIONID = "SESSIONID";
	
	//文件名与文件类型分割符
	String FILE_SPLIT_FILETYPE = "/";
	
}
