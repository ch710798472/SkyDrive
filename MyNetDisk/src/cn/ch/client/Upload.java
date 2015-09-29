package cn.ch.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Upload {
	public Upload(String username, String ip, int port, File filename,String dir) {
		final FileConnect connectServer = new FileConnect(ip, port, username,
				filename,dir);
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
		private File filename;
		private String dir;
		private int flag = 0;// 0Ϊ���Ӳ��ɹ�,1Ϊ����������Ӧ

		public FileConnect(String ip, int port, String username, File filename,String dir) {
			this.ip = ip;
			this.port = port;
			this.username = username;
			this.filename = filename;
			this.dir = dir;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// ��ʱʱ��
				// ��ʼ����
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// ��ʼ����
				uploadfile(username, filename,dir);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("�޷����ӵ�������,�����������ӣ�");
				else if (flag == 1)
					System.out.println("������δ��Ӧ����");
			}
		}

		public void uploadfile(String username, File file,String dir) {

			String filedir = username;
			String filename = file.getName();
			File f = new File(dir);
			if (!f.exists()) {
				// ��ȡ�ļ�

				try {
					//double fileSize = file.length();
					// ������ȡ�ϴ��ļ��ܵ�
					InputStream is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					// ����д���ϴ��ļ��ܵ�
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(dir));
					int b = 0;
					byte[] buff = new byte[1024];
					while ((b = (bis.read(buff, 0, 1024))) != -1) {
						bos.write(buff, 0, b);
						System.out.write(b);
					}
					// ˢ�º͹رչܵ�
					bos.flush();
					bos.close();
					bis.close();
					JOptionPane.showMessageDialog(null, "�ϴ��ļ��ɹ���");
				} catch (FileNotFoundException a) {
					System.out.println(a.getMessage());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			} else {
				JOptionPane.showMessageDialog(null, "�ļ��Ѵ���");
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
