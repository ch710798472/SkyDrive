package cn.ch.server;

public class ServerThreadPoolConfig {
	/*
	 * �̳߳�����
	 */
	
	//�����ļ�·��
	public final static String PROPS_FILE_RSRC_PATH     = "threadPool.properties";
	//���������
	public final static String MAX_POOL_SIZE = "maxPoolSize";
	//��С������
	public final static String MIN_POOL_SIZE = "minPoolSize";
	//��ʼ��������
	public final static String INITIAL_POOL_SIZE= "initialPoolSize";
	//���ӵ�������ʱ��
	public final static String MAX_IDLE_TIME = "maxIdleTime";
	//�µ�������
	public final static String ACQUIRE_INCREMENT = "acquireIncrement";
	//�̵߳ȴ��û����������ʱ��
	public final static String MAX_WAIT_USER_TIME = "maxWaitUserTime";
	
}
