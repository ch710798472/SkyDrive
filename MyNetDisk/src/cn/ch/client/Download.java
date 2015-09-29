package cn.ch.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import cn.ch.dao.JdbcUtil;

public class Download {
	public Download(String fromusername, String tousername, String ip,
			int port, String fromdir, String todir, int s) {
		final FileConnect connectServer = new FileConnect(ip, port,
				fromusername, tousername, fromdir, todir, s);
		final Thread fileconnectThread = new Thread(connectServer);
		fileconnectThread.start();
	}

	/*
	 * ���ӷ�����
	 */
	class FileConnect implements Runnable {
		private String ip;
		private int port;
		private String fromusername;
		private String tousername;
		private Socket socket;
		private String fromdir;
		private String todir;
		private int s;
		private int flag = 0;// 0Ϊ���Ӳ��ɹ�,1Ϊ����������Ӧ

		public FileConnect(String ip, int port, String fromusername,
				String tousername, String fromdir, String todir, int s) {
			this.ip = ip;
			this.port = port;
			this.fromusername = fromusername;
			this.tousername = tousername;
			this.fromdir = fromdir;
			this.todir = todir;
			this.s = s;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// ��ʱʱ��
				// ��ʼ����
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// ��ʼ����
				downloadfile(fromusername, tousername, fromdir, todir, s);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("�޷����ӵ�������,�����������ӣ�");
				else if (flag == 1)
					System.out.println("������δ��Ӧ����");
			}
		}

		public void downloadfile(String fromusername, String tousername,
				String fromdir, String todir, int s) {

			// �������ݿ⣬�Ƿ����û��������ļ�
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int r;

			// д�����ݿ�,�ɹ���ɾ����¼
			try {
				conn = JdbcUtil.getConnection();
				ps = conn
						.prepareStatement("insert into download(fromdir,todir,username) values(?,?,?)");
				ps.setString(1, "D:/MyEclipse/work/MyNetDisk/" + fromdir);
				ps.setString(2, todir);
				ps.setString(3, fromusername);
				r = ps.executeUpdate();
			} catch (SQLException e2) {
				System.out.println(e2.getMessage());
			} finally {
				JdbcUtil.free(rs, ps, conn);
			}
			String filedir = fromusername;
			File f = new File(fromdir);
			if (f.exists()) {
				// ��ȡ�ļ�
				try {
					double fileSize = f.length();
					InputStream is = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(is);
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(todir));
					int b = 0;
					byte[] buff = new byte[1024];
					while (true) {
						b = bis.read(buff, 0, 1024);
						if (b == -1)
							break;
						bos.write(buff, 0, b);
						// System.out.println(b);
						// ˯��Ϊ�˲��Զϵ�����������ʹ��ʱ��ע�͵�

						/*try {
							Thread.sleep(100); // System.out.println("100");
						} catch (InterruptedException x) {
							System.out.println(x.getMessage());
						}*/

					}
					// ˢ�º͹رչܵ�
					bos.flush();
					bos.close();
					bis.close();
					// д�����ݿ�,�ɹ���ɾ����¼
					try {
						conn = JdbcUtil.getConnection();
						ps = conn
								.prepareStatement("delete from download where username=? and todir=?");
						ps.setString(1, fromusername);
						ps.setString(2, todir);
						r = ps.executeUpdate();
					} catch (SQLException e2) {
						System.out.println(e2.getMessage());
					} finally {
						JdbcUtil.free(rs, ps, conn);
					}
					if (s == 1)
						JOptionPane.showMessageDialog(null, "�����ļ��ɹ���");
					else
						JOptionPane.showMessageDialog(null, "�����ļ��ɹ���");
				} catch (FileNotFoundException a) {
					System.out.println(a.getMessage());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			} else {
				JOptionPane.showMessageDialog(null, "�Բ��𣬼������ص��ļ��Ҳ����ˣ�");
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
