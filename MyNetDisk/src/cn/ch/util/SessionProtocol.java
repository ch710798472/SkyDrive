package cn.ch.util;

public interface SessionProtocol {
	/*
	 * �ػ�Э��,ָ����������
	 * ͨ��Э�����:	Э������+�ָ��+�����б�(param1=value1&param2=value2)
	 */
	
	//�ָ��ַ�
	String SPLIT_SIGN = ": ";
	
	//��½
	String LOGIN = "LOGIN";
	//��½�ɹ�
	String SUCCESS = "SUCCESS";
	//��¼ʧ��
	String FAILURE = "FAILURE";
	//�ỰID
	String SESSIONID = "SESSIONID";
	
	//�ļ������ļ����ͷָ��
	String FILE_SPLIT_FILETYPE = "/";
	
}
