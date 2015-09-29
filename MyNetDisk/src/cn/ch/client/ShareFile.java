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

public class ShareFile {
	public ShareFile(String username, String ip, int port, String filename) {
		final FileConnect connectServer = new FileConnect(ip, port, username,
				filename);
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
		private int flag = 0;// 0Ϊ���Ӳ��ɹ�,1Ϊ����������Ӧ

		public FileConnect(String ip, int port, String username, String filename) {
			this.ip = ip;
			this.port = port;
			this.username = username;
			this.filename = filename;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// ��ʱʱ��
				// ��ʼ����
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// ��ʼ����
				sharefile(username,filename);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("�޷����ӵ�������,�����������ӣ�");
				else if (flag == 1)
					System.out.println("������δ��Ӧ����");
			}
		}

		public void sharefile(String username, String filename) {
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// �ļ����ƺʹ���������
				String line = null;
				// ������������û�����
				pst.println("ShareFile" + SessionProtocol.SPLIT_SIGN
						+ "username=" + username + "&filename=" + filename);
				line = br.readLine();
				if (line != null) {// ͨ����֤
					HashMap<String, String> param = StringUtil
							.getParameterMap(line);
					if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))) {
							JOptionPane.showMessageDialog(null, "�ļ�����ɹ�");
						} else {
							JOptionPane.showMessageDialog(null, "�ļ�����ʧ��");
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
