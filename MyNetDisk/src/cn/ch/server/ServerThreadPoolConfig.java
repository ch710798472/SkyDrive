package cn.ch.server;

public class ServerThreadPoolConfig {
	/*
	 * 线程池配置
	 */
	
	//配置文件路径
	public final static String PROPS_FILE_RSRC_PATH     = "threadPool.properties";
	//最大连接数
	public final static String MAX_POOL_SIZE = "maxPoolSize";
	//最小连接数
	public final static String MIN_POOL_SIZE = "minPoolSize";
	//初始化连接数
	public final static String INITIAL_POOL_SIZE= "initialPoolSize";
	//连接的最大空闲时间
	public final static String MAX_IDLE_TIME = "maxIdleTime";
	//新的连接数
	public final static String ACQUIRE_INCREMENT = "acquireIncrement";
	//线程等待用户操作的最大时间
	public final static String MAX_WAIT_USER_TIME = "maxWaitUserTime";
	
}
