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
	 * 连接服务器
	 */
	class FileConnect implements Runnable {
		private String ip;
		private int port;
		private String username;
		private Socket socket;
		private String filename;
		private int flag = 0;// 0为连接不成功,1为服务器不响应

		public FileConnect(String ip, int port, String username, String filename) {
			this.ip = ip;
			this.port = port;
			this.username = username;
			this.filename = filename;
		}

		public void run() {
			socket = new Socket();
			try {
				socket.setSoTimeout(10000);// 超时时间
				// 开始连接
				socket.connect(new InetSocketAddress(ip, port), 10000);
				flag = 1;
				// 开始传输
				sharefile(username,filename);
			} catch (Exception e) {
				if (flag == 0)
					System.out.println("无法连接到服务器,请检查网络连接！");
				else if (flag == 1)
					System.out.println("服务器未响应请求！");
			}
		}

		public void sharefile(String username, String filename) {
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// 文件名称和处理后的类型
				String line = null;
				// 向服务器发送用户数据
				pst.println("ShareFile" + SessionProtocol.SPLIT_SIGN
						+ "username=" + username + "&filename=" + filename);
				line = br.readLine();
				if (line != null) {// 通过验证
					HashMap<String, String> param = StringUtil
							.getParameterMap(line);
					if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))) {
							JOptionPane.showMessageDialog(null, "文件共享成功");
						} else {
							JOptionPane.showMessageDialog(null, "文件共享失败");
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
