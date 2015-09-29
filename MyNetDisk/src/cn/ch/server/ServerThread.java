package cn.ch.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import cn.ch.client.MyFile;
import cn.ch.client.MyNetDisk;
import cn.ch.dao.JdbcUtil;
import cn.ch.dao.UserDAO;
import cn.ch.dao.impl.UserDAOImpl;
import cn.ch.util.FileType;
import cn.ch.util.FileUtil;
import cn.ch.util.SessionProtocol;
import cn.ch.util.SessionUtil;
import cn.ch.util.StringUtil;

public class ServerThread extends Thread {
	/**
	 * �����û������߳�
	 */
	private final static Logger log = Logger.getLogger(ServerThread.class);
	private Socket socket;// ����Ŀͻ���
	private boolean runningFlag = false;// �Ƿ�����
	private InputStream is;
	private InputStreamReader isr;

	public synchronized void startWork(Socket socket) {
		this.socket = socket;
		this.runningFlag = true;
		if (runningFlag)
			this.notify();
	}

	public synchronized void run() {
		BufferedReader br = null;
		while (true) {
			// ��û������,����еȴ�
			if (!runningFlag) {
				try {
					this.wait();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			System.out.println("�߳�:" + this.getName() + "�ṩ����");
			try {
				// ��ȡ�ͻ�������
				is = socket.getInputStream();
				int count = 0;
				// ��ʱ+������ ���жϿͻ����Ƿ�Ͽ�
				while (count == 0) {
					count = is.available();
				}
				while (true && is.available() > 0) {
					isr = new InputStreamReader(is);
					br = new BufferedReader(isr);
					PrintStream ps = new PrintStream(socket.getOutputStream());
					System.out.println("ch���̷���˿�ʼ��ȡ�ͻ������ݡ�����");
					String line = br.readLine();
					if (null != line && !"".equals(line)) {
						// �û���¼
						if (line.startsWith(SessionProtocol.LOGIN)) {
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							UserDAO userDAO = new UserDAOImpl();
							String username = param.get("username");
							String password = param.get("password");
							if (userDAO.checkUser(username, password) != null) {// ��½�ɹ�
								// ����Ự��ʶ
								String sessionID = SessionUtil.getSessionId();
								Server.sessionMap.put(sessionID, new Session());
								ps.println(SessionProtocol.LOGIN
										+ SessionProtocol.SPLIT_SIGN
										+ SessionProtocol.SESSIONID + "="
										+ sessionID + "&loginSign="
										+ SessionProtocol.SUCCESS);
								File[] files = FileUtil.getFolder(username);// �г��ļ�������
																			// ����socket
								File file;
								for (int i = 0; i < files.length; i++) {
									file = files[i];
									ps.println(file.getName()
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileUtil.getFileType(file));// ClientLogin
																			// br��������
								}
							} else {
								ps.println(SessionProtocol.LOGIN
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}
						}
						// ����������ҪsessionID��֤
						/*
						 * else{ //��ȡ�����б� HashMap<String, String> param =
						 * StringUtil.getParameterMap(line); //��ȡsessionID
						 * String sessionID =
						 * param.get(SessionProtocol.SESSIONID); if(null ==
						 * sessionID || "".equals(sessionID)) continue;
						 * if(Server.sessionMap.get(sessionID) !=
						 * null){//ͨ����֤,���в�������
						 * 
						 * } }
						 */
						
						if (line.startsWith("NewFolder")) {// �½��ļ���
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String filename = param.get("filename");
							File newfile = new File(username + "/" + filename);
							if (newfile.mkdir()) {
								// д�����ݿ�
								Connection conn = null;
								PreparedStatement pst = null;
								ResultSet rs = null;
								int r;
								try {
									conn = JdbcUtil.getConnection();
									pst = conn
											.prepareStatement("insert into sharefile(username,filename,isshared,fileparent) values(?,?,?,?)");
									pst.setString(1, username);
									pst.setString(2, filename);
									pst.setString(3, "no");
									pst.setString(4, "MyNetDisk");
									r = pst.executeUpdate();

								} catch (SQLException e2) {
									System.out.println(e2.getMessage());
								} finally {
									JdbcUtil.free(rs, pst, conn);
								}
								ps.println("NewFolder"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.SUCCESS);
							} else {
								ps.println("NewFolder"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}
						}
						
						if (line.startsWith("DeleteFile")) {// ɾ���ļ��У��ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String filename = param.get("filename");
							String file = param.get("file");
							File f = new File(filename);
							if (f.delete()) {
								// д�����ݿ�
								Connection conn = null;
								PreparedStatement pst = null;
								ResultSet rs = null;
								int r;
								try {
									conn = JdbcUtil.getConnection();
									pst = conn
											.prepareStatement("delete from sharefile where username=? and filename=?");
									pst.setString(1, username);
									pst.setString(2, file);
									r = pst.executeUpdate();
								    System.out.println(r);
								} catch (SQLException e2) {
									System.out.println(e2.getMessage());
								} finally {
									JdbcUtil.free(rs, pst, conn);
								}
								ps.println("DeleteFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.SUCCESS);
							} else {
								ps.println("DeleteFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}
						}
						
						if (line.startsWith("ModifyFile")) {// �޸��ļ��У��ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String oldfilename = param.get("oldfilename");
							String newfilename = param.get("newfilename");
							String oldfile = param.get("oldfile");
							String newfile = param.get("newfile");
							File oldf = new File(oldfile);
							File newf = new File(newfile);
							if (oldf.renameTo(newf)) {
								// д�����ݿ�
								Connection conn = null;
								PreparedStatement pst = null;
								ResultSet rs = null;
								int r;
								try {
									conn = JdbcUtil.getConnection();
									pst = conn
											.prepareStatement("update sharefile set filename=? where username=? and filename=?");
									pst.setString(1, newfilename);
									pst.setString(2, username);
									pst.setString(3, oldfilename);
									r = pst.executeUpdate();

								} catch (SQLException e2) {
									System.out.println(e2.getMessage());
								} finally {
									JdbcUtil.free(rs, pst, conn);
								}
								ps.println("ModifyFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.SUCCESS);
							} else {
								ps.println("ModifyFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}
						}
						
						if (line.startsWith("ShareFile")) {// ���ù����ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String filename = param.get("filename");
							Connection conn = null;
							PreparedStatement pst = null;
							ResultSet rs = null;
							int r;
							try {
								conn = JdbcUtil.getConnection();
								pst = conn
										.prepareStatement("update sharefile set isshared=? where username=? and filename=?");
								pst.setString(1, "yes");
								pst.setString(2, username);
								pst.setString(3, filename);
								r = pst.executeUpdate();
								if (r > 0){
									ps.println("ShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.SUCCESS);
								}
								else{
									ps.println("ShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.FAILURE);
								}
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
								//JOptionPane.showMessageDialog(null, "����ʧ��,�ļ��Ѵ��ڣ�");
							} finally {
								JdbcUtil.free(rs, pst, conn);
							}
						}
						
						if (line.startsWith("CancelShareFile")) {// ȡ�������ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String filename = param.get("filename");
							String parentfilename = param.get("parentfilename");
							Connection conn = null;
							PreparedStatement pst = null;
							ResultSet rs = null;
							int r;
							try {
								conn = JdbcUtil.getConnection();
								pst = conn
										.prepareStatement("update sharefile set isshared=? where username=? and filename=? and fileparent=?");
								pst.setString(1, "no");
								pst.setString(2,username);
								pst.setString(3, filename);
								pst.setString(4, parentfilename);
								r = pst.executeUpdate();
								if (r > 0){
									ps.println("CancelShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.SUCCESS);
								}
								else{
									ps.println("CancelShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.FAILURE);
								}
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
								JOptionPane.showMessageDialog(null, "ȡ���ļ�����ʧ��,�����ظ�ȡ����");
							} finally {
								JdbcUtil.free(rs, pst, conn);
							}
						}
						
						if (line.startsWith("SearchMyShareFile")) {// �鿴�ҵĹ����ļ�
							Boolean flg=false;
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							Connection conn = null;
							PreparedStatement pst = null;
							ResultSet rs = null;
							ArrayList<String> filenameString = new ArrayList<String>();
							ArrayList<String> fileparentString = new ArrayList<String>();
							try {
								conn = JdbcUtil.getConnection();
								pst = conn
										.prepareStatement("select * from sharefile where isshared=? and username=?");
								pst.setString(1, "yes");
								pst.setString(2, username);
								rs = pst.executeQuery();
								while (rs.next()) {
									flg=true;
									filenameString.add(rs.getString("filename"));
									fileparentString.add(rs.getString("fileparent"));
								}
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
							} finally {
								JdbcUtil.free(rs, pst, conn);
							}
							if(flg){
								ps.println("SearchMyShareFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.SUCCESS);
							}else{
								ps.println("SearchMyShareFile"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}
							
							String s1 = "";
							List<MyFile> f = new ArrayList<MyFile>();
							MyFile myFile = null;
							for (int i = 0; i < filenameString.size(); i++) {
								if (filenameString.get(i).toUpperCase()
										.endsWith(FileType.TXT))
									s1 = filenameString.get(i)
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileType.TXT;
								else if (filenameString.get(i).toUpperCase()
										.endsWith(FileType.WORD2003)
										|| filenameString.get(i).toUpperCase()
												.endsWith(FileType.WORD2007))
									s1 = filenameString.get(i)
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileType.WORD;
								else if (filenameString.get(i).toUpperCase()
										.endsWith(FileType.EXCEL2003)
										|| filenameString.get(i).toUpperCase()
												.endsWith(FileType.EXCEL2007))
									s1 = filenameString.get(i)
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileType.EXCEL;
								else if (filenameString.get(i).toUpperCase()
										.endsWith("PDF")
										|| filenameString.get(i).toUpperCase()
												.endsWith("AVI")
										|| filenameString.get(i).toUpperCase()
												.endsWith("MP3")
										|| filenameString.get(i).toUpperCase()
												.endsWith("MP4"))
									s1 = filenameString.get(i)
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileType.DEFAULT;
								else
									s1 = filenameString.get(i)
											+ SessionProtocol.FILE_SPLIT_FILETYPE
											+ FileType.FOLDER;
								ps.println(s1);
								myFile = FileUtil.getMyFile(s1);// ��ȡ�ļ���������
								if (null != myFile)
									f.add(myFile);
							}
						}
						
						if (line.startsWith("SearchShareFile")) {// ���������ļ�
							Boolean flg=false;
							Boolean flg1=false;
							Boolean flg2=false;
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String flag = param.get("flag");
							String getfilename = param.get("getfilename");
							if(flag.equals("all")){//�鿴�����ļ�
								Connection conn = null;
								PreparedStatement pst = null;
								ResultSet rs = null;
								ArrayList<String> filenameString = new ArrayList<String>();
								ArrayList<String> fileparentString = new ArrayList<String>();
								ArrayList<String> usernameString = new ArrayList<String>();
								try {
									conn = JdbcUtil.getConnection();
									pst = conn
											.prepareStatement("select * from sharefile where isshared=?");
									pst.setString(1, "yes");
									rs = pst.executeQuery();
									while (rs.next()) {
										filenameString.add(rs.getString("filename"));
										fileparentString.add(rs.getString("fileparent"));
										usernameString.add(rs.getString("username"));
										flg1=true;
									}
								} catch (SQLException e2) {
									System.out.println(e2.getMessage());
								} finally {
									JdbcUtil.free(rs, pst, conn);
								}
								if(flg1){
									ps.println("SearchShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.SUCCESS);
								}else{
									ps.println("SearchShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.FAILURE);
								}
								if (flg1) {
									String s1 = "";
									List<MyFile> f = new ArrayList<MyFile>();
									MyFile myFile = null;
									for (int i = 0; i < filenameString.size(); i++) {
										if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.TXT))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.TXT;
										else if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.WORD2003)
												|| filenameString.get(i).toUpperCase()
														.endsWith(FileType.WORD2007))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.WORD;
										else if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.EXCEL2003)
												|| filenameString.get(i).toUpperCase()
														.endsWith(FileType.EXCEL2007))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.EXCEL;
										else if (filenameString.get(i).toUpperCase()
												.endsWith("PDF")
												|| filenameString.get(i).toUpperCase()
														.endsWith("AVI")
												|| filenameString.get(i).toUpperCase()
														.endsWith("MP3")
												|| filenameString.get(i).toUpperCase()
														.endsWith("MP4"))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.DEFAULT;
										else
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.FOLDER;
										ps.println(s1);
										myFile = FileUtil.getMyFile(s1);// ��ȡ�ļ���������
										if (null != myFile)
											f.add(myFile);
									}	
								}
							}else{
								Connection conn = null;
								PreparedStatement pst = null;
								ResultSet rs = null;
								ArrayList<String> filenameString = new ArrayList<String>();
								ArrayList<String> fileparentString = new ArrayList<String>();
								ArrayList<String> usernameString = new ArrayList<String>();
								try {
									conn = JdbcUtil.getConnection();
									pst = conn
											.prepareStatement("select * from sharefile where isshared=? and filename like ?");
									pst.setString(1, "yes");
									pst.setString(2, "%" + getfilename + "%");
									rs = pst.executeQuery();
									while (rs.next()) {
										filenameString.add(rs.getString("filename"));
										fileparentString.add(rs.getString("fileparent"));
										usernameString.add(rs.getString("username"));
										flg2 = true;
									}
								} catch (SQLException e2) {
									System.out.println(e2.getMessage());
								} finally {
									JdbcUtil.free(rs, pst, conn);
								}
								if(flg2){
									ps.println("SearchShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.SUCCESS);
								}else{
									ps.println("SearchShareFile"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.FAILURE);
								}
								if (flg2) {
									String s1 = "";
									List<MyFile> f = new ArrayList<MyFile>();
									MyFile myFile = null;
									for (int i = 0; i < filenameString.size(); i++) {
										if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.TXT))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.TXT;
										else if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.WORD2003)
												|| filenameString.get(i).toUpperCase()
														.endsWith(FileType.WORD2007))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.WORD;
										else if (filenameString.get(i).toUpperCase()
												.endsWith(FileType.EXCEL2003)
												|| filenameString.get(i).toUpperCase()
														.endsWith(FileType.EXCEL2007))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.EXCEL;
										else if (filenameString.get(i).toUpperCase()
												.endsWith("PDF")
												|| filenameString.get(i).toUpperCase()
														.endsWith("AVI")
												|| filenameString.get(i).toUpperCase()
														.endsWith("MP3")
												|| filenameString.get(i).toUpperCase()
														.endsWith("MP4"))
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.DEFAULT;
										else
											s1 = usernameString.get(i) + "-"
													+ filenameString.get(i)
													+ SessionProtocol.FILE_SPLIT_FILETYPE
													+ FileType.FOLDER;
										ps.println(s1);
										myFile = FileUtil.getMyFile(s1);// ��ȡ�ļ���������
										if (null != myFile)
											f.add(myFile);
									}
								} 
							}	
						}
						
						if (line.startsWith("Upload")) {// �ϴ��ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							String filename = param.get("filename");
							String fileparent = param.get("fileparent");
							// д�����ݿ�
							Connection conn = null;
							PreparedStatement pst = null;
							ResultSet rs = null;
							int r;
							try {
								conn = JdbcUtil.getConnection();
								pst = conn
										.prepareStatement("insert into sharefile(username,filename,isshared,fileparent) values(?,?,?,?)");
								pst.setString(1, username);
								pst.setString(2,filename);
								pst.setString(3, "no");
								pst.setString(4, fileparent);
								r = pst.executeUpdate();
								// System.out.println(fileparent);
								if (r > 0){
									ps.println("Upload"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.SUCCESS);
								}
								else{
									ps.println("Upload"
											+ SessionProtocol.SPLIT_SIGN
											+ "loginSign="
											+ SessionProtocol.FAILURE);
								}
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
							} finally {
								JdbcUtil.free(rs, pst, conn);
							}
						}
						
						if (line.startsWith("download")) {// �ϵ������ļ�
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							ArrayList<String> fromdirString = new ArrayList<String>();
							ArrayList<String> todirString = new ArrayList<String>();
							ArrayList<String> usernameString = new ArrayList<String>();
							Connection conn1 = null;
							PreparedStatement ps1 = null;
							ResultSet rs1 = null;
							int r1;
							Boolean notdown = false;
							try {
								conn1 = JdbcUtil.getConnection();
								ps1 = conn1.prepareStatement("select * from download");
								rs1 = ps1.executeQuery();
								while (rs1.next()) {
									notdown = true;
									fromdirString.add(rs1.getString("fromdir"));
									todirString.add(rs1.getString("todir"));
									usernameString.add(rs1.getString("username"));
								}
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
							} finally {
								JdbcUtil.free(rs1, ps1, conn1);
							}
							if (notdown){
								ps.println("download"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.SUCCESS);
								ps.println(fromdirString.get(0));
								ps.println(todirString.get(0));
								ps.println(usernameString.get(0));
							}
							else{
								ps.println("download"
										+ SessionProtocol.SPLIT_SIGN
										+ "loginSign="
										+ SessionProtocol.FAILURE);
							}	
						}
						
						if (line.startsWith("not")) {// ɾ���ϵ��¼
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							String username = param.get("username");
							Connection conn1 = null;
							PreparedStatement ps1 = null;
							ResultSet rs1 = null;
							int r1;
							try {
								conn1 = JdbcUtil.getConnection();
								ps1 = conn1
										.prepareStatement("delete from download where username=?");
								ps1.setString(1, username);
								r1 = ps1.executeUpdate();
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
							} finally {
								JdbcUtil.free(rs1, ps1, conn1);
							}
							
						}
						
					}
				}
			} catch (SocketTimeoutException ste) {
				log.info("�ͻ��˳�ʱ��");
			} catch (Exception e) {
				log.error("��ȡ�ͻ��������������쳣!");
			} finally {
				if (null != br)
					try {
						br.close();
					} catch (IOException e) {
					}
				this.runningFlag = false;
				free(this);
			}
		}
	}

	public void free(ServerThread serverThread) {
		System.out.println("ch���̷�����߳�:" + this.getName() + "��ɷ���,��ʼ����");
		ServerThreadPool.threadPool.addLast(serverThread);
		System.out.println("��ǰ�̳߳�����:" + ServerThreadPool.threadPool.size());
	}

}
