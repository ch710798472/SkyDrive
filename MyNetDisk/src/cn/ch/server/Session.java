package cn.ch.server;

import java.util.HashMap;

public class Session {
	/*
	 *���Լ� 
	 */
	HashMap<Object,Object> attributes = new HashMap<Object, Object>();
	
	/*
	 * ����key��ȡ����
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/*
	 * ��������
	 */
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	/*
	 * �Ƴ�����
	 */
	public void removeAttribute(String name){
		attributes.remove(name);
	}
}
