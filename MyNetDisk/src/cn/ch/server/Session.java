package cn.ch.server;

import java.util.HashMap;

public class Session {
	/*
	 *属性集 
	 */
	HashMap<Object,Object> attributes = new HashMap<Object, Object>();
	
	/*
	 * 根据key获取属性
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/*
	 * 放入属性
	 */
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	/*
	 * 移除属性
	 */
	public void removeAttribute(String name){
		attributes.remove(name);
	}
}
