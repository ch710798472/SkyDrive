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
	 * 连接服务器
	 */
	class FileConnect implements Runnable {
		private String ip;
		private int port;
		private String username;
		private Socket socket;
		private File filename;
		private String dir;
		private int flag = 0;// 0为连接不成功,1为服务器不响应

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
				socket.setSoTimeout(10000);// 超时时间
				// 开始连接
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// 开始传输
				uploadfile(username, filename,dir);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("无法连接到服务器,请检查网络连接！");
				else if (flag == 1)
					System.out.println("服务器未响应请求！");
			}
		}

		public void uploadfile(String username, File file,String dir) {

			String filedir = username;
			String filename = file.getName();
			File f = new File(dir);
			if (!f.exists()) {
				// 获取文件

				try {
					//double fileSize = file.length();
					// 创建读取上传文件管道
					InputStream is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					// 创建写入上传文件管道
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(dir));
					int b = 0;
					byte[] buff = new byte[1024];
					while ((b = (bis.read(buff, 0, 1024))) != -1) {
						bos.write(buff, 0, b);
						System.out.write(b);
					}
					// 刷新和关闭管道
					bos.flush();
					bos.close();
					bis.close();
					JOptionPane.showMessageDialog(null, "上传文件成功！");
				} catch (FileNotFoundException a) {
					System.out.println(a.getMessage());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			} else {
				JOptionPane.showMessageDialog(null, "文件已存在");
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
