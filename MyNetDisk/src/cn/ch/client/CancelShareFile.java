package cn.ch.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JOptionPane;

import cn.ch.util.SessionProtocol;
import cn.ch.util.StringUtil;

public class CancelShareFile {
	public CancelShareFile(String username, String ip, int port, String filename,String parentfilename) {
		final FileConnect connectServer = new FileConnect(ip, port, username,
				filename,parentfilename);
		final Thread fileconnectThread = new Thread(connectServer);
		fileconnectThread.start();
	}

	/*
	 * ���ӷ�����
	 */
	class FileConnect implements Runnable {
		private String ip;
		private int port;
		private String username;
		private Socket socket;
		private String filename;
		private String parentfilename;
		private int flag = 0;// 0Ϊ���Ӳ��ɹ�,1Ϊ����������Ӧ

		public FileConnect(String ip, int port, String username, String filename,String parentfilename) {
			this.ip = ip;
			this.port = port;
			this.username = username;
			this.filename = filename;
			this.parentfilename=parentfilename;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// ��ʱʱ��
				// ��ʼ����
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// ��ʼ����
				cancelsharefile(username,filename,parentfilename);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("�޷����ӵ�������,�����������ӣ�");
				else if (flag == 1)
					System.out.println("������δ��Ӧ����");
			}
		}

		public void cancelsharefile(String username, String filename,String parentfilename) {
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// �ļ����ƺʹ���������
				String line = null;
				// ������������û�����
				pst.println("CancelShareFile" + SessionProtocol.SPLIT_SIGN
						+ "username=" + username + "&filename=" + filename+ "&parentfilename=" + parentfilename);
				line = br.readLine();
				if (line != null) {// ͨ����֤
					HashMap<String, String> param = StringUtil
							.getParameterMap(line);
					if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))) {
							JOptionPane.showMessageDialog(null, "ȡ���ļ�����ɹ�");
						} else {
							JOptionPane.showMessageDialog(null, "ȡ���ļ�����ʧ��");
						}
					
					
				} else {
					JOptionPane.showMessageDialog(null,"socket failure!");
				}
			} catch (IOException c) {
				System.out.println(c.getMessage());
			}


		}

		public void distroy() {
			try {
				flag = 1;
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
