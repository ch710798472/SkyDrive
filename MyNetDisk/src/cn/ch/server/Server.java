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
	 * ch ���� �����
	 */
	private final static Logger log = Logger.getLogger(Server.class);
	private int listenPort = 8550;//�����˿�
	private static ServerSocket ss;
	static LinkedList<Socket> taskQueue = new LinkedList<Socket>();//�������
	public static Map<String,Session> sessionMap = new HashMap<String,Session>();
	/*��ʼ���̳߳�*/
	ServerThreadPool stp = new ServerThreadPool();
	/*���ݿ����ӹ���*/
	public JdbcUtil jdbcUtil = new JdbcUtil();
	private int maxWaitUserTime = ServerThreadPool.maxWaitUserTime;//���ȴ�����ʱ��
	
	public static void main(String[] args) {
		System.out.println("ch���̷������������������...");
		Server server = new Server();
		new TaskHandle().start();
		server.init();
	}
	public void init(){
//		��ʼ�����ݿ�
		System.out.println("ch���̷�������ڳ�ʼ�����ݿ����ӳ�...");
		try {
			System.out.println("---------------------------------");
			JdbcUtil.getConnection().close();
			System.out.println("---------------------------------");
			System.out.println("ch���̷�������ݿ����ӳش����ɹ�!");
		} catch (SQLException e1) {
			log.error("ch���̷�������ݿ����ӳش���ʧ�ܣ�");
			System.exit(1);
		}
		
		/*������������*/
		try {
			ss = new ServerSocket(listenPort);
			System.out.println("ch���̷����������ɹ�,���ڼ����˿�:"+listenPort);
			while(true){
				Socket socket = ss.accept();
				socket.setSoTimeout(maxWaitUserTime);//�����������ʱ��
				System.out.println("ch���̷���˷��ֿͻ�������,IP:"+socket.getRemoteSocketAddress());
				process(socket);//ת���̳߳ش���
			}
		} catch (IOException e) {
			System.out.println("ch���̷���˷���������ʧ��,��ȷ���˿�:"+listenPort+"������������ռ��!");
		}
	}
	
	/*
	 * �����̴߳���ͻ�������
	 */
	public void process(Socket socket){
		if(ServerThreadPool.threadPool.size()>0){//������л��з����߳�
			ServerThreadPool.threadPool.removeFirst().startWork(socket);
		}
		else if(taskQueue.size()<1000){//��û��,���Ҷ��г���С��1000,������������
			taskQueue.add(socket);
		}
		else{
			try {
				socket.close();
			} catch (IOException e) {
				log.error("ch���̷���˹رտͻ���socketʧ��!");
			}
		}
	}
	
}

/*
 *������ʱ��,�����������,ÿ�� 500 ����鿴��û�п�������
 */
class TaskHandle extends Thread{
	static LinkedList<Socket> taskQueue =  Server.taskQueue;
	public TaskHandle(){
		System.out.println("ch���̷���˶���������������..");
	}
	public void run() {
		try {
			while(true){
				Thread.sleep(500);
				if(taskQueue.size()>0 && ServerThreadPool.threadPool.size()>0){//������л��з����߳�,�����������
					ServerThreadPool.threadPool.removeFirst().startWork(Server.taskQueue.removeFirst());
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
