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

public class ModifyFile {
	public ModifyFile(String username, String ip, int port,String oldfile,String newfile,String oldfilename, String newfilename) {
		final FileConnect connectServer = new FileConnect(ip, port, username,oldfile,newfile,oldfilename,
				newfilename);
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
		private String oldfilename;
		private String newfilename;
		private String oldfile;
		private String newfile;
		private int flag = 0;// 0Ϊ���Ӳ��ɹ�,1Ϊ����������Ӧ

		public FileConnect(String ip, int port, String username,String oldfile,String newfile,String oldfilename, String newfilename) {
			this.ip = ip;
			this.port = port;
			this.username = username;
			this.oldfilename = oldfilename;
			this.newfilename = newfilename;
			this.oldfile = oldfile;
			this.newfile = newfile;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// ��ʱʱ��
				// ��ʼ����
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// ��ʼ����
				modifyfile(username, oldfile,newfile,oldfilename,newfilename);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("�޷����ӵ�������,�����������ӣ�");
				else if (flag == 1)
					System.out.println("������δ��Ӧ����");
			}
		}

		public void modifyfile(String username,String oldfile,String newfile,String oldfilename, String newfilename) {
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// �ļ����ƺʹ���������
				String line = null;
				// ������������û�����
				pst.println("ModifyFile" + SessionProtocol.SPLIT_SIGN
						+ "username=" + username + "&oldfile=" + oldfile+ "&newfile=" + newfile+ "&oldfilename=" + oldfilename+ "&newfilename=" + newfilename);
				line = br.readLine();
				if (line != null) {// ͨ����֤
					HashMap<String, String> param = StringUtil
							.getParameterMap(line);
					if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))) {
							JOptionPane.showMessageDialog(null, "�ļ��޸ĳɹ�");
						} else {
							JOptionPane.showMessageDialog(null, "�ļ��޸�ʧ��");
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
