package cn.ch.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ServerThreadPool {
	/**
	 * ������̳߳�
	 */
	private final static Logger log = Logger.getLogger(ServerThreadPool.class);
	
	public static LinkedList<ServerThread> threadPool = new LinkedList<ServerThread>();
	private static int maxPoolSize;//���������
	private static int minPoolSize;//��С������
	private static int initialPoolSize;//��ʼ��������
	private static int maxIdleTime;//���ӵ�������ʱ��
	private static int acquireIncrement;//��ȡ���µ�������
	static int maxWaitUserTime;//�̵߳ȴ��û����������ʱ��
	
	public ServerThreadPool(){
		initProperties();
		initThreadPool();
	}
	
	/*
	 * ��ʼ������
	 */
	public void initProperties(){
		System.out.println("ch���̷�������������̳߳�...");
		System.out.println("ch���̷�������ڼ����̳߳������ļ�...");
		Properties pro = new Properties();
		HashMap<String, String> propertiesMap = new HashMap<String, String>();
		try {
			pro.load(ServerThreadPool.class.getClassLoader().getResourceAsStream(ServerThreadPoolConfig.PROPS_FILE_RSRC_PATH));
			propertiesMap.put(ServerThreadPoolConfig.MAX_POOL_SIZE, pro.getProperty(ServerThreadPoolConfig.MAX_POOL_SIZE));
			propertiesMap.put(ServerThreadPoolConfig.MIN_POOL_SIZE, pro.getProperty(ServerThreadPoolConfig.MIN_POOL_SIZE));
			propertiesMap.put(ServerThreadPoolConfig.INITIAL_POOL_SIZE, pro.getProperty(ServerThreadPoolConfig.INITIAL_POOL_SIZE));
			propertiesMap.put(ServerThreadPoolConfig.MAX_IDLE_TIME, pro.getProperty(ServerThreadPoolConfig.MAX_IDLE_TIME));
			propertiesMap.put(ServerThreadPoolConfig.ACQUIRE_INCREMENT, pro.getProperty(ServerThreadPoolConfig.ACQUIRE_INCREMENT));
			propertiesMap.put(ServerThreadPoolConfig.MAX_WAIT_USER_TIME, pro.getProperty(ServerThreadPoolConfig.MAX_WAIT_USER_TIME));
			if(null != propertiesMap.get(ServerThreadPoolConfig.MAX_POOL_SIZE)){
				ServerThreadPool.maxPoolSize = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.MAX_POOL_SIZE));
			}else{
				ServerThreadPool.maxPoolSize = 100;
			}
			
			if(null != propertiesMap.get(ServerThreadPoolConfig.MIN_POOL_SIZE)){
				ServerThreadPool.minPoolSize = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.MIN_POOL_SIZE));
			}else{
				ServerThreadPool.minPoolSize = 5;
			}
			
			if(null != propertiesMap.get(ServerThreadPoolConfig.INITIAL_POOL_SIZE)){
				ServerThreadPool.initialPoolSize = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.INITIAL_POOL_SIZE));
			}else{
				ServerThreadPool.initialPoolSize = 5;
			}
			
			if(null != propertiesMap.get(ServerThreadPoolConfig.MAX_IDLE_TIME)){
				ServerThreadPool.maxIdleTime = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.MAX_IDLE_TIME));
			}else{
				ServerThreadPool.maxIdleTime = 10;
			}
			
			if(null != propertiesMap.get(ServerThreadPoolConfig.ACQUIRE_INCREMENT)){
				ServerThreadPool.acquireIncrement = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.ACQUIRE_INCREMENT));
			}else{
				ServerThreadPool.acquireIncrement = 1;
			}
			if(null != propertiesMap.get(ServerThreadPoolConfig.MAX_WAIT_USER_TIME)){
				ServerThreadPool.maxWaitUserTime = Integer.parseInt(propertiesMap.get(ServerThreadPoolConfig.MAX_WAIT_USER_TIME));
			}else{
				ServerThreadPool.maxWaitUserTime = 60000;
			}
			
		} catch (Exception e) {
			log.error("ch���̷�����̳߳������ļ����س���,��ȷ���ļ�threadPool.properties����,����ȷ����!");
			System.exit(1);
		}
		System.out.println("ch���̷�����̳߳����ü��سɹ�,������Ϣ����:");
		System.out.println("---------------------------------");
		System.out.println("���������:"+ServerThreadPool.maxPoolSize);
		System.out.println("��С������:"+ServerThreadPool.minPoolSize);
		System.out.println("��ʼ��������:"+ServerThreadPool.initialPoolSize);
		System.out.println("���ӵ�������ʱ��:"+ServerThreadPool.maxIdleTime+" ��");
		System.out.println("�ڵ�ǰ�������ľ���ʱ��,һ�λ�ȡ���µ�������:"+ServerThreadPool.acquireIncrement);
		System.out.println("�̵߳ȴ��û����������ʱ��:"+ServerThreadPool.maxWaitUserTime+" ����");
		System.out.println("---------------------------------");
	}
	/*
	 * ��ʼ��
	 */
	public void initThreadPool(){
		for(int i=0;i<ServerThreadPool.initialPoolSize;i++){
			ServerThread st = new ServerThread();
			st.start();
			threadPool.add(st);
		}
	}
	
	public void poolAdjust(){
		
	}
}
