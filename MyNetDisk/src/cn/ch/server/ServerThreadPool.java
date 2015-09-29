package cn.ch.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ServerThreadPool {
	/**
	 * 服务端线程池
	 */
	private final static Logger log = Logger.getLogger(ServerThreadPool.class);
	
	public static LinkedList<ServerThread> threadPool = new LinkedList<ServerThread>();
	private static int maxPoolSize;//最大连接数
	private static int minPoolSize;//最小连接数
	private static int initialPoolSize;//初始化连接数
	private static int maxIdleTime;//连接的最大空闲时间
	private static int acquireIncrement;//获取的新的连接数
	static int maxWaitUserTime;//线程等待用户操作的最大时间
	
	public ServerThreadPool(){
		initProperties();
		initThreadPool();
	}
	
	/*
	 * 初始化配置
	 */
	public void initProperties(){
		System.out.println("ch网盘服务端正在启动线程池...");
		System.out.println("ch网盘服务端正在加载线程池配置文件...");
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
			log.error("ch网盘服务端线程池配置文件加载出错,请确保文件threadPool.properties存在,并正确配置!");
			System.exit(1);
		}
		System.out.println("ch网盘服务端线程池配置加载成功,配置信息如下:");
		System.out.println("---------------------------------");
		System.out.println("最大连接数:"+ServerThreadPool.maxPoolSize);
		System.out.println("最小连接数:"+ServerThreadPool.minPoolSize);
		System.out.println("初始化连接数:"+ServerThreadPool.initialPoolSize);
		System.out.println("连接的最大空闲时间:"+ServerThreadPool.maxIdleTime+" 秒");
		System.out.println("在当前连接数耗尽的时候,一次获取的新的连接数:"+ServerThreadPool.acquireIncrement);
		System.out.println("线程等待用户操作的最大时间:"+ServerThreadPool.maxWaitUserTime+" 毫秒");
		System.out.println("---------------------------------");
	}
	/*
	 * 初始化
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
