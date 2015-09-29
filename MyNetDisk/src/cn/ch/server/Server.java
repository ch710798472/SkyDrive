package cn.ch.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.ch.dao.JdbcUtil;

public class Server {
	/**
	 * ch 网盘 服务端
	 */
	private final static Logger log = Logger.getLogger(Server.class);
	private int listenPort = 8550;//监听端口
	private static ServerSocket ss;
	static LinkedList<Socket> taskQueue = new LinkedList<Socket>();//任务队列
	public static Map<String,Session> sessionMap = new HashMap<String,Session>();
	/*初始化线程池*/
	ServerThreadPool stp = new ServerThreadPool();
	/*数据库连接工具*/
	public JdbcUtil jdbcUtil = new JdbcUtil();
	private int maxWaitUserTime = ServerThreadPool.maxWaitUserTime;//最大等待操作时间
	
	public static void main(String[] args) {
		System.out.println("ch网盘服务端正在启动服务器...");
		Server server = new Server();
		new TaskHandle().start();
		server.init();
	}
	public void init(){
//		初始化数据库
		System.out.println("ch网盘服务端正在初始化数据库连接池...");
		try {
			System.out.println("---------------------------------");
			JdbcUtil.getConnection().close();
			System.out.println("---------------------------------");
			System.out.println("ch网盘服务端数据库连接池创建成功!");
		} catch (SQLException e1) {
			log.error("ch网盘服务端数据库连接池创建失败！");
			System.exit(1);
		}
		
		/*开启监听服务*/
		try {
			ss = new ServerSocket(listenPort);
			System.out.println("ch网盘服务器启动成功,正在监听端口:"+listenPort);
			while(true){
				Socket socket = ss.accept();
				socket.setSoTimeout(maxWaitUserTime);//设置最大连接时长
				System.out.println("ch网盘服务端发现客户端连接,IP:"+socket.getRemoteSocketAddress());
				process(socket);//转入线程池处理
			}
		} catch (IOException e) {
			System.out.println("ch网盘服务端服务器启动失败,请确保端口:"+listenPort+"不被其他程序占用!");
		}
	}
	
	/*
	 * 服务线程处理客户端请求
	 */
	public void process(Socket socket){
		if(ServerThreadPool.threadPool.size()>0){//如果池中还有服务线程
			ServerThreadPool.threadPool.removeFirst().startWork(socket);
		}
		else if(taskQueue.size()<1000){//若没有,并且队列长度小于1000,则加入任务队列
			taskQueue.add(socket);
		}
		else{
			try {
				socket.close();
			} catch (IOException e) {
				log.error("ch网盘服务端关闭客户端socket失败!");
			}
		}
	}
	
}

/*
 *开启定时器,处理任务队列,每隔 500 毫秒查看有没有空闲连接
 */
class TaskHandle extends Thread{
	static LinkedList<Socket> taskQueue =  Server.taskQueue;
	public TaskHandle(){
		System.out.println("ch网盘服务端队列任务处理器开启..");
	}
	public void run() {
		try {
			while(true){
				Thread.sleep(500);
				if(taskQueue.size()>0 && ServerThreadPool.threadPool.size()>0){//如果池中还有服务线程,则处理任务队列
					ServerThreadPool.threadPool.removeFirst().startWork(Server.taskQueue.removeFirst());
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
