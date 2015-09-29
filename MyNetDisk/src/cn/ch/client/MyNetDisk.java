package cn.ch.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import cn.ch.dao.JdbcUtil;
import cn.ch.util.FileType;
import cn.ch.util.FileUtil;
import cn.ch.util.SessionProtocol;
import cn.ch.util.StringUtil;

@SuppressWarnings("serial")
public class MyNetDisk extends JFrame {
	/**
	 * ch����
	 */
	private static String sessionID;
	private static String username;
	private static String dialogfilename;
	private static String parentfilename;
	private JTextField searchfilename;
	// ��ȡ��Ļ��С
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public MyNetDisk(String username, String sessionID, List<MyFile> files) {
		MyNetDisk.sessionID = sessionID;
		MyNetDisk.username = username;
		MyNetDisk.dialogfilename = "MyNetDisk";
		MyNetDisk.parentfilename = "MyNetDisk";
		final JFrame frame = this;
		this.setTitle("ch����");
		this.setSize(800, 600);
		this.setLocation((int) ((screenSize.getWidth() - this.getWidth()) / 2),
				(int) (screenSize.getHeight() - this.getHeight()) / 2);
		this.setLayout(new BorderLayout(10, 10));// �߽粼��
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ����
		JPanel topPanel = new JPanel();
		JPanel topRightPanel = new JPanel();
		// ��ӵ�½��Ϣ��
		topRightPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 5));// ��ʽ����
		topRightPanel.add(new JLabel("��ӭ����" + username));
		try {
			topRightPanel.add(new JLabel("��ǰIP��"
					+ InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			topRightPanel.add(new JLabel("��ǰIP����ȡʧ��"));
		}
		// ��ť
		JButton newBtn = new JButton("�½��ļ���");
		topRightPanel.add(newBtn);
		JButton uploadBtn = new JButton("�ϴ�");
		JButton dnloadBtn = new JButton("���� ");
		JButton refreshBtn = new JButton("ˢ��");
		topRightPanel.add(uploadBtn);
		topRightPanel.add(dnloadBtn);
		topRightPanel.add(refreshBtn);

		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		topPanel.add(new JLabel(new ImageIcon("images/logo.jpg")));
		topPanel.add(topRightPanel);

		JPanel buttomPanel = new JPanel();// �ײ����
		final JPanel mainPanel = new JPanel();// �в����
		final JPanel rightPanel = new JPanel();// ���Ҳ����
		// ���Ŀ¼
		final JTree jt = getFolderTree(files);
		// Ŀ¼����
		jt.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		jt.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// TODO �Զ����ɵķ������
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt
						.getLastSelectedPathComponent();
				if (node == null)
					return;
				Object nodeInfo = node.getUserObject();
				String nodeString = String.valueOf(nodeInfo);
				// System.out.println(nodeString);
				// ȡ��ѡ�еĽڵ���
				if (nodeString.equals("MyNetDisk"))
					MyNetDisk.dialogfilename = "MyNetDisk";
				else {
					int x = nodeString.indexOf(" ");// ��ȡ�ո�֮ǰ���ļ����ַ���
					MyNetDisk.dialogfilename = nodeString.substring(0, x);
				}
				// System.out.println(MyNetDisk.dialogfilename);

				// ȡ��ѡ�нڵ㸸�ڵ����������ж��Ƿ��ڸ�Ŀ¼��
				DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) node
						.getParent();
				if (parentnode == null)
					return;
				Object parentnodeInfo = parentnode.getUserObject();
				String parentnodeString = String.valueOf(parentnodeInfo);
				if (parentnodeString.equals("MyNetDisk"))
					MyNetDisk.parentfilename = "MyNetDisk";
				else {
					int y = parentnodeString.indexOf(" ");// ��ȡ�ո�֮ǰ���ļ����ַ���
					MyNetDisk.parentfilename = parentnodeString.substring(0, y);
				}
				// System.out.println(MyNetDisk.parentfilename);

			}
		});

		final JScrollPane JS = new JScrollPane(jt);
		final JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, JS, rightPanel);
		splitPane.setOneTouchExpandable(true);
		mainPanel.add(splitPane);

		rightPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));
		rightPanel.add(new JLabel("��ӭʹ�� ch ����"));
		rightPanel.add(new JLabel("���ô�С/ȫ����С��0"
				+ getDirSizeByString(getDirSize(new File(MyNetDisk.username)))
				+ "/100MB"));

		JLabel hide1JLabel = new JLabel("                ");// ��������
		rightPanel.add(hide1JLabel);
		JButton deleteBtn = new JButton("ɾ���ļ�");
		rightPanel.add(deleteBtn);
		JButton modifyBtn = new JButton("�޸��ļ���");
		rightPanel.add(modifyBtn);
		JLabel hide2JLabel = new JLabel("                                   ");// ��������
		rightPanel.add(hide2JLabel);
		JButton shareBtn = new JButton("�����ļ�");
		rightPanel.add(shareBtn);
		JButton cancelshareBtn = new JButton("ȡ���ļ�����");
		rightPanel.add(cancelshareBtn);
		JButton searchshareBtn = new JButton("�鿴�ҵĹ����ļ�");
		rightPanel.add(searchshareBtn);
		JLabel hide3JLabel = new JLabel("            ");// ��������
		rightPanel.add(hide3JLabel);
		searchfilename = new JTextField(20);// ��ߵ�Ŀ¼�������ƶ���
		rightPanel.add(new InputPanel(new JLabel("����"), searchfilename));
		JButton searchBtn = new JButton("�����ļ�");
		rightPanel.add(searchBtn);

		buttomPanel.add(new JLabel("������� �ƿ�1104�� ch ����,�ɱ�������µ����ݶ�ʧ�������Ը�"));
		topPanel.setPreferredSize(new Dimension(800, 40));
		rightPanel.setPreferredSize(new Dimension(550, 480));
		mainPanel.setPreferredSize(new Dimension(800, 480));
		buttomPanel.setPreferredSize(new Dimension(800, 20));
		mainPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
				Color.BLUE));// �ֽ���

		// ��ť�¼�����
		newBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �½��ļ���
				String filename = JOptionPane.showInputDialog(null, "�������½��ļ���");
				new NewFolder(MyNetDisk.username, "127.0.0.1", 8550, filename);
			}
		});

		uploadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �ϴ��ļ�
				String fileparent = "";
				// ��ʼ���ļ�ѡ���
				JFileChooser fDialog = new JFileChooser();
				fDialog.setDialogTitle("��ѡ���ϴ����ļ�");
				int returnVal = fDialog.showOpenDialog(null);
				// �����ѡ�����ļ�
				if (JFileChooser.APPROVE_OPTION == returnVal) {
					// System.out.println(fDialog.getSelectedFile());
					if ((getDirSize(new File(MyNetDisk.username)) + getDirSize(fDialog
							.getSelectedFile())) <= 102400.00) {// �жϿռ��С
						// ѡ����Ҫ�ϴ������ļ���
						if (checkfilename(fDialog.getSelectedFile().getName()) == null) {// û�������ļ�
							String dir = "";
							if (MyNetDisk.dialogfilename.equals("MyNetDisk")) {
								dir = "D:/MyEclipse/work/MyNetDisk/"
										+ MyNetDisk.username + "/"
										+ fDialog.getSelectedFile().getName();
								fileparent = "MyNetDisk";
							} else {
								File f = new File(
										"D:/MyEclipse/work/MyNetDisk/"
												+ MyNetDisk.username + "/"
												+ MyNetDisk.dialogfilename);
								if (f.isDirectory()) {
									dir = "D:/MyEclipse/work/MyNetDisk/"
											+ MyNetDisk.username
											+ "/"
											+ MyNetDisk.dialogfilename
											+ "/"
											+ fDialog.getSelectedFile()
													.getName();
									fileparent = MyNetDisk.dialogfilename;
								} else {
									JOptionPane.showMessageDialog(null,
											"����ѡ����ļ��н����ϴ�");
								}
							}
							new Upload(MyNetDisk.username, "127.0.0.1", 8550,
									fDialog.getSelectedFile(), dir);
							Boolean up = false;
							Socket socket = new Socket();
							try {
								socket.setSoTimeout(10000);// ��ʱʱ��
								// ��ʼ����
								socket.connect(new InetSocketAddress(
										"127.0.0.1", 8550), 10000);
								// ��ʼ����
								try {
									PrintStream pst = new PrintStream(socket
											.getOutputStream());
									BufferedReader br = new BufferedReader(
											new InputStreamReader(socket
													.getInputStream()));// �ļ����ƺʹ���������
									String line = null;
									// ������������û�����
									pst.println("Upload"
											+ SessionProtocol.SPLIT_SIGN
											+ "username="
											+ MyNetDisk.username
											+ "&filename="
											+ fDialog.getSelectedFile()
													.getName() + "&fileparent="
											+ fileparent);
									line = br.readLine();
									if (line != null) {// ͨ����֤
										HashMap<String, String> param = StringUtil
												.getParameterMap(line);
										if (SessionProtocol.SUCCESS
												.equals(param.get("loginSign"))) {
											up = true;
										}
									} else {
										JOptionPane.showMessageDialog(null,
												"socket failure!");
									}
								} catch (IOException c) {
									System.out.println(c.getMessage());
								}
							} catch (Exception e3) {
								System.out.println(e3.getMessage());
							}
							if (!up) {
								System.out.println("�ϴ�д�����ݿ�ʧ��");
							}
							try {
								Thread.sleep(1000);// �ӳ�ˢ�½���
							} catch (InterruptedException x) {
								System.out.println(x.getMessage());
							}
							frame.dispose();
							newMyNetDisk();
						} else {
							JOptionPane.showMessageDialog(null, "�ϴ��ļ�ʧ�ܣ��ļ��Ѵ���");
						}

					} else {
						JOptionPane.showMessageDialog(null, "�ϴ��ļ�ʧ�ܣ��ռ䲻��");
					}
				}
			}
		});

		dnloadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �����ļ�
				String fromdir = checkfilename(MyNetDisk.dialogfilename);
				File a = new File("D:/MyEclipse/work/MyNetDisk/"
						+ MyNetDisk.username + "/" + MyNetDisk.dialogfilename);
				if (!a.isDirectory()) {// �������������ļ���
					JFileChooser chooser = new JFileChooser();
					JTextField text;
					text = getTextField(chooser);
					text.setText(MyNetDisk.dialogfilename);// ����Ĭ�������ļ���
					int returnVal = chooser.showSaveDialog(chooser);
					String path = "";
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						path = chooser.getSelectedFile().getPath();
						new Download(MyNetDisk.username, MyNetDisk.username,
								"127.0.0.1", 8550, fromdir, path, 1);
					}
				} else {
					JOptionPane.showMessageDialog(null, "����ʧ�ܣ���ѡ����ļ��н������أ�");
				}
			}
		});

		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ˢ�´���
				frame.dispose();
				newMyNetDisk();
			}
		});

		deleteBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ɾ���ļ�
				if (MyNetDisk.dialogfilename != null) {// ɾ���ļ�
					if (checkfilename(MyNetDisk.dialogfilename) != null) {
						new DeleteFile(MyNetDisk.username, "127.0.0.1", 8550,
								MyNetDisk.dialogfilename,
								checkfilename(MyNetDisk.dialogfilename));
					}
				}
			}
		});

		modifyBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �޸��ļ���
				String filename = JOptionPane.showInputDialog(null,
						"�������޸ĺ���ļ���");
				String f;
				if (MyNetDisk.dialogfilename != null) {
					if (MyNetDisk.parentfilename.equals("MyNetDisk")) {
						f = MyNetDisk.username + "/" + filename;
					} else {
						f = MyNetDisk.username + "/" + MyNetDisk.parentfilename
								+ "/" + filename;
					}
					if (checkfilename(MyNetDisk.dialogfilename) != null) {
						new ModifyFile(MyNetDisk.username, "127.0.0.1", 8550,
								checkfilename(MyNetDisk.dialogfilename), f,
								MyNetDisk.dialogfilename, filename);
					}
				}
			}
		});

		shareBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �����ļ�
				File f = new File(MyNetDisk.username + "/"
						+ MyNetDisk.dialogfilename);
				if (!f.isDirectory()) {
					new ShareFile(MyNetDisk.username, "127.0.0.1", 8550,
							MyNetDisk.dialogfilename);
				} else {
					JOptionPane.showMessageDialog(null, "����ʧ��,�ļ��в�������");
				}
			}
		});

		cancelshareBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ȡ���ļ�����
				new CancelShareFile(MyNetDisk.username, "127.0.0.1", 8550,
						MyNetDisk.dialogfilename, MyNetDisk.parentfilename);
			}
		});

		searchshareBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �鿴�ҵĹ����ļ�
				Socket socket = new Socket();
				List<MyFile> files = new ArrayList<MyFile>();
				String node = null;
				MyFile myFile = null;
				try {
					socket.setSoTimeout(10000);// ��ʱʱ��
					// ��ʼ����
					socket.connect(new InetSocketAddress("127.0.0.1", 8550),
							10000);
					// ��ʼ����
					try {
						PrintStream pst = new PrintStream(socket
								.getOutputStream());
						BufferedReader br = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));// �ļ����ƺʹ���������
						String line = null;
						// ������������û�����
						pst.println("SearchMyShareFile"
								+ SessionProtocol.SPLIT_SIGN + "username="
								+ MyNetDisk.username);
						line = br.readLine();
						if (line != null) {// ͨ����֤
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							if (SessionProtocol.SUCCESS.equals(param
									.get("loginSign"))) {
								// ��ȡ�ļ�(��)�б�
								while ((node = br.readLine()) != null) {
									// System.out.println(node);
									myFile = FileUtil.getMyFile(node);// ��ȡ�ļ���������
									if (null != myFile)
										files.add(myFile);
								}
							} else {
								JOptionPane.showMessageDialog(null, "�鿴ʧ�ܣ�");
							}

						} else {
							JOptionPane.showMessageDialog(null,
									"socket failure!");
						}
					} catch (IOException c) {
						System.out.println(c.getMessage());
					}
				} catch (Exception e3) {
					System.out.println(e3.getMessage());
				}
				new DialogShowTree(files).jd.setVisible(true);// �����Ի���
			}
		});

		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// �����ļ�
				Socket socket = new Socket();
				List<MyFile> files = new ArrayList<MyFile>();
				String node = null;
				MyFile myFile = null;
				String getfilename = searchfilename.getText().trim();// ȡ���������ļ���
				// System.out.println(searchfilename.getText().trim());
				Boolean flag = false;
				if (getfilename.equals("")) {
					try {
						socket.setSoTimeout(10000);// ��ʱʱ��
						// ��ʼ����
						socket.connect(
								new InetSocketAddress("127.0.0.1", 8550), 10000);
						// ��ʼ����
						try {
							PrintStream pst = new PrintStream(socket
									.getOutputStream());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(socket
											.getInputStream()));// �ļ����ƺʹ���������
							String line = null;
							// ������������û�����
							pst.println("SearchShareFile"
									+ SessionProtocol.SPLIT_SIGN + "flag="
									+ "all");
							line = br.readLine();
							if (line != null) {// ͨ����֤
								HashMap<String, String> param = StringUtil
										.getParameterMap(line);
								if (SessionProtocol.SUCCESS.equals(param
										.get("loginSign"))) {
									// ��ȡ�ļ�(��)�б�
									while ((node = br.readLine()) != null) {
										// System.out.println(node);
										myFile = FileUtil.getMyFile(node);// ��ȡ�ļ���������
										if (null != myFile)
											files.add(myFile);
									}
									new DialogShowSearchTree(files).jd
											.setVisible(true);// �����Ի���
								} else {
									JOptionPane
											.showMessageDialog(null, "����ʧ�ܣ�");
								}
							} else {
								JOptionPane.showMessageDialog(null,
										"socket failure!");
							}
						} catch (IOException c) {
							System.out.println(c.getMessage());
						}
					} catch (Exception e3) {
						System.out.println(e3.getMessage());
					}
				} else {
					try {
						socket.setSoTimeout(10000);// ��ʱʱ��
						// ��ʼ����
						socket.connect(
								new InetSocketAddress("127.0.0.1", 8550), 10000);
						// ��ʼ����
						try {
							PrintStream pst = new PrintStream(socket
									.getOutputStream());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(socket
											.getInputStream()));// �ļ����ƺʹ���������
							String line = null;
							// ������������û�����
							pst.println("SearchShareFile"
									+ SessionProtocol.SPLIT_SIGN + "flag="
									+ "match" + "&getfilename=" + getfilename);
							line = br.readLine();
							if (line != null) {// ͨ����֤
								HashMap<String, String> param = StringUtil
										.getParameterMap(line);
								if (SessionProtocol.SUCCESS.equals(param
										.get("loginSign"))) {
									// ��ȡ�ļ�(��)�б�
									while ((node = br.readLine()) != null) {
										// System.out.println(node);
										myFile = FileUtil.getMyFile(node);// ��ȡ�ļ���������
										if (null != myFile)
											files.add(myFile);
									}
									new DialogShowSearchTree(files).jd
											.setVisible(true);// �����Ի���
								} else {
									JOptionPane
											.showMessageDialog(null, "����ʧ�ܣ�");
								}
							} else {
								JOptionPane.showMessageDialog(null,
										"socket failure!");
							}
						} catch (IOException c) {
							System.out.println(c.getMessage());
						}
					} catch (Exception e3) {
						System.out.println(e3.getMessage());
					}

				}

			}
		});

		// �������ݿ⣬�Ƿ����û��������ļ�
		ArrayList<String> fromdirString = new ArrayList<String>();
		ArrayList<String> todirString = new ArrayList<String>();
		ArrayList<String> usernameString = new ArrayList<String>();
		Boolean notdown = false;
		Socket socket = new Socket();
		try {
			socket.setSoTimeout(10000);// ��ʱʱ��
			// ��ʼ����
			socket.connect(new InetSocketAddress("127.0.0.1", 8550), 10000);
			// ��ʼ����
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// �ļ����ƺʹ���������
				String line = null;
				// ������������û�����
				pst.println("download" + SessionProtocol.SPLIT_SIGN + "username="
						+ MyNetDisk.username);
				line = br.readLine();
				String node = null;
				if (line != null) {// ͨ����֤
					HashMap<String, String> param = StringUtil
							.getParameterMap(line);
					if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))) {
						notdown = true;
						if ((node = br.readLine()) != null) {
							fromdirString.add(node);
						}
						
						if ((node = br.readLine()) != null) {
							todirString.add(node);
						}
						
						if ((node = br.readLine()) != null) {
							usernameString.add(node);
						}
						
					}
				} 
			} catch (IOException c) {
				System.out.println(c.getMessage());
			}
		} catch (Exception e3) {
			System.out.println(e3.getMessage());
		}
		
		if (notdown) {// ��ʼ����δ��ɵ��ļ�
			for (int i = 0; i < usernameString.size(); i++) {
				if (usernameString.get(i).equals(MyNetDisk.username)) {
					JOptionPane.showMessageDialog(null, "����δ��������ļ���");
					File fromfile = new File(fromdirString.get(i));
					File tofile = new File(todirString.get(i));
					if (fromfile.length() > tofile.length()) {
						File tof = new File(todirString.get(i));
						File fromf = new File(fromdirString.get(i));
						if (tof.exists()) {
							try {
								long fileSize = tof.length();
								InputStream is = new FileInputStream(fromf);
								BufferedInputStream bis = new BufferedInputStream(
										is);
								BufferedOutputStream bos = new BufferedOutputStream(
										new FileOutputStream(
												todirString.get(i), true));// ����true��������
								is.skip(fileSize);
								int b = 0;
								byte[] buff = new byte[1024];
								while (true) {
									b = bis.read(buff, 0, 1024);
									if (b == -1)
										break;
									bos.write(buff, 0, b);
									// System.out.println(b);
								}
								// ˢ�º͹رչܵ�
								bos.flush();
								bos.close();
								bis.close();
								// System.out.println(tof.length());
								Socket socket1 = new Socket();
								try {
									socket1.setSoTimeout(10000);// ��ʱʱ��
									// ��ʼ����
									socket1.connect(new InetSocketAddress("127.0.0.1", 8550), 10000);
									// ��ʼ����
									try {
										PrintStream pst = new PrintStream(socket1.getOutputStream());
										BufferedReader br = new BufferedReader(new InputStreamReader(
												socket1.getInputStream()));// �ļ����ƺʹ���������
										// ������������û�����
										pst.println("not" + SessionProtocol.SPLIT_SIGN + "username="
												+ MyNetDisk.username);
									} catch (IOException c) {
										System.out.println(c.getMessage());
									}
								} catch (Exception e3) {
									System.out.println(e3.getMessage());
								}
								JOptionPane.showMessageDialog(null,
										"�ϴ�δ������ɵ��ļ��������سɹ���");
							} catch (FileNotFoundException a) {
								System.out.println(a.getMessage());
							} catch (IOException e) {
								System.out.println(e.getMessage());
							}
						} else {
							JOptionPane.showMessageDialog(null,
									"�Բ���δ��������ļ�������ɾ�������������أ�");
						}

					}

				}
			}

		}

		this.add(topPanel, BorderLayout.NORTH);
		this.add(mainPanel, BorderLayout.WEST);
		this.add(buttomPanel, BorderLayout.SOUTH);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	// ��������class MyNetDisk
	// <----------------------------------------------------------------------------------------->

	class DialogShowTree {// ������ʾѡ���ļ�����
		JDialog jd = new JDialog();

		public DialogShowTree(List<MyFile> files) {
			// TODO �Զ����ɵĹ��캯�����
			jd.setSize(300, 600);
			jd.setLocation(400, 70);
			Container c2 = jd.getContentPane();
			c2.setLayout(null);
			// Ŀ¼
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 10, 300, 450);
			c2.add(scrollPane);
			final JTree JT = getFileTree(files);
			scrollPane.setViewportView(JT);
			setVisible(true);
			// ������Ŀ¼�ڵ��¼��ļ���
			JT.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			JT.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					// TODO �Զ����ɵķ������
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) JT
							.getLastSelectedPathComponent();
					if (node == null)
						return;
					Object nodeInfo = node.getUserObject();
					String nodeString = String.valueOf(nodeInfo);
					MyNetDisk.dialogfilename = nodeString;
					// System.out.println(nodeString);

				}
			});

			JButton jbb1 = new JButton("�����ļ�");
			jbb1.setBounds(50, 500, 100, 30);
			c2.add(jbb1);
			JButton jbb2 = new JButton("ȡ    ��");
			jbb2.setBounds(150, 500, 100, 30);
			c2.add(jbb2);

			jbb1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String fromdir = checkfilename(MyNetDisk.dialogfilename);
					File a = new File("D:/MyEclipse/work/MyNetDisk/"
							+ MyNetDisk.username + "/"
							+ MyNetDisk.dialogfilename);
					if (!a.isDirectory()) {// �������������ļ���
						JFileChooser chooser = new JFileChooser();
						JTextField text;
						text = getTextField(chooser);
						text.setText(MyNetDisk.dialogfilename);// ����Ĭ�������ļ���
						int returnVal = chooser.showSaveDialog(chooser);
						String path = "";
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							path = chooser.getSelectedFile().getPath();
						}
						// System.out.println(path);
						new Download(MyNetDisk.username, MyNetDisk.username,
								"127.0.0.1", 8550, fromdir, path, 1);
					} else {
						JOptionPane
								.showMessageDialog(null, "����ʧ�ܣ���ѡ����ļ��н������أ�");
					}
				}
			});

			jbb2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jd.dispose();
				}
			});

			jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		}
	}

	class DialogShowSearchTree {// ������ʾ��������ļ�����
		JDialog jd = new JDialog();

		public DialogShowSearchTree(List<MyFile> files) {
			// TODO �Զ����ɵĹ��캯�����
			jd.setSize(300, 600);
			jd.setLocation(400, 70);
			Container c2 = jd.getContentPane();
			c2.setLayout(null);
			// Ŀ¼
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 10, 300, 450);
			c2.add(scrollPane);
			final JTree JT = getFileTree(files);
			scrollPane.setViewportView(JT);
			setVisible(true);
			// ������Ŀ¼�ڵ��¼��ļ���
			JT.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			JT.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					// TODO �Զ����ɵķ������
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) JT
							.getLastSelectedPathComponent();
					if (node == null)
						return;
					Object nodeInfo = node.getUserObject();
					String nodeString = String.valueOf(nodeInfo);
					MyNetDisk.dialogfilename = nodeString;
					// System.out.println(nodeString);
				}
			});

			JButton jbb1 = new JButton("�����ļ�");
			jbb1.setBounds(10, 500, 80, 30);
			c2.add(jbb1);
			JButton jbb3 = new JButton("�� ��");
			jbb3.setBounds(100, 500, 80, 30);
			c2.add(jbb3);
			JButton jbb2 = new JButton("ȡ ��");
			jbb2.setBounds(190, 500, 80, 30);
			c2.add(jbb2);

			jbb1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// ����ڵ��ϵ��û������ļ���
					int x = MyNetDisk.dialogfilename.indexOf("-");
					String getusername = MyNetDisk.dialogfilename.substring(0,
							x);
					int y = MyNetDisk.dialogfilename.length();
					String getfilename = MyNetDisk.dialogfilename.substring(
							x + 1, y);
					MyNetDisk.username = getusername;// ���ü���ļ��������ĸ��û�
					String fromdir = checkfilename(getfilename);
					JFileChooser chooser = new JFileChooser();
					JTextField text;
					text = getTextField(chooser);
					text.setText(MyNetDisk.dialogfilename);// ����Ĭ�������ļ���
					int returnVal = chooser.showSaveDialog(chooser);
					String path = "";
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						path = chooser.getSelectedFile().getPath();
					}
					// System.out.println(path);
					new Download(MyNetDisk.username, MyNetDisk.username,
							"127.0.0.1", 8550, fromdir, path, 1);
				}
			});

			jbb3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// ����ڵ��ϵ��û������ļ���
					String us = MyNetDisk.username;
					String path = "D:/MyEclipse/work/MyNetDisk/"
							+ MyNetDisk.username + "/";// ���浱ǰ�û�·��
					int x = MyNetDisk.dialogfilename.indexOf("-");
					String getusername = MyNetDisk.dialogfilename.substring(0,
							x);
					int y = MyNetDisk.dialogfilename.length();
					String getfilename = MyNetDisk.dialogfilename.substring(
							x + 1, y);
					MyNetDisk.username = getusername;// ���ü���ļ��������ĸ��û�
					String fromdir = checkfilename(getfilename);
					MyNetDisk.username = us;
					if (checkfilename(getfilename) == null) {// ������
						path = path + getfilename;
						new Download(MyNetDisk.username, MyNetDisk.username,
								"127.0.0.1", 8550, fromdir, path, 0);
						// д�����ݿ�
						Connection conn = null;
						PreparedStatement ps = null;
						ResultSet rs = null;
						int r;
						try {
							conn = JdbcUtil.getConnection();
							ps = conn
									.prepareStatement("insert into sharefile(username,filename,isshared,fileparent) values(?,?,?,?)");
							ps.setString(1, MyNetDisk.username);
							ps.setString(2, getfilename);
							ps.setString(3, "no");
							ps.setString(4, "MyNetDisk");
							r = ps.executeUpdate();
						} catch (SQLException e2) {
							System.out.println(e2.getMessage());
						} finally {
							JdbcUtil.free(rs, ps, conn);
						}
					} else {
						String filename = JOptionPane.showInputDialog(null,
								"������Ҫ������ļ���");
						if (filename != null) {
							path = path + filename;
							new Download(MyNetDisk.username,
									MyNetDisk.username, "127.0.0.1", 8550,
									fromdir, path, 0);
							// д�����ݿ�
							Connection conn = null;
							PreparedStatement ps = null;
							ResultSet rs = null;
							int r;
							try {
								conn = JdbcUtil.getConnection();
								ps = conn
										.prepareStatement("insert into sharefile(username,filename,isshared,fileparent) values(?,?,?,?)");
								ps.setString(1, MyNetDisk.username);
								ps.setString(2, filename);
								ps.setString(3, "no");
								ps.setString(4, "MyNetDisk");
								r = ps.executeUpdate();
							} catch (SQLException e2) {
								System.out.println(e2.getMessage());
							} finally {
								JdbcUtil.free(rs, ps, conn);
							}
						} else {
							JOptionPane.showMessageDialog(null, "�������ļ�����");
						}

					}
				}
			});

			jbb2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jd.dispose();
				}
			});

			jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		}
	}

	public JTextField getTextField(Container c) {// ���ؿ�����Ĭ��������
		JTextField textField = null;
		for (int i = 0; i < c.getComponentCount(); i++) {
			Component cnt = c.getComponent(i);
			if (cnt instanceof JTextField) {
				return (JTextField) cnt;
			}
			if (cnt instanceof Container) {
				textField = getTextField((Container) cnt);
				if (textField != null) {
					return textField;
				}
			}
		}
		return textField;
	}

	/*
	 * ����Ŀ¼
	 */
	public JTree getFolderTree(List<MyFile> files) {// ���ļ���С��Ŀ¼��
		// ������û���Ŀ¼������,����д���
		File homeFolder = new File(MyNetDisk.username);
		if (!homeFolder.exists())
			homeFolder.mkdir();
		// ������
		JTree tree;
		DefaultMutableTreeNode home = new DefaultMutableTreeNode(
				new TreeNodeData(FileType.HOME, "MyNetDisk"));
		DefaultMutableTreeNode li = null;
		for (MyFile file : files) {
			// �г����ļ����Լ��ļ���
			li = new DefaultMutableTreeNode(new TreeNodeData(
					file.getFileType(), file.getName()
							+ "   0"
							+ getDirSizeByString(getDirSize(new File(
									"D:/MyEclipse/work/MyNetDisk/"
											+ MyNetDisk.username + "/"
											+ file.getName())))));
			// �г��ļ��������ļ���ֹ������Ŀ¼
			String a = file.getFileType();
			if (a.equals(FileType.FOLDER)) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// ·��������Ҫ���ܴ���
				for (File fc : f.listFiles()) {
					li.add(new DefaultMutableTreeNode(new TreeNodeData(FileUtil
							.getFileType(fc), fc.getName()
							+ "   0"
							+ getDirSizeByString(getDirSize(new File(
									"D:/MyEclipse/work/MyNetDisk/"
											+ MyNetDisk.username + "/"
											+ file.getName() + "/"
											+ fc.getName()))))));
				}
			}
			home.add(li);
		}
		// ���ÿռ��СKB
		DecimalFormat df = new DecimalFormat(".00");
		String count = df.format(getDirSize(new File(MyNetDisk.username)));
		// System.out.println(count + "KB");
		tree = new JTree(home);
		tree.setCellRenderer(new MyRenderer());// ����TreeNodeData�������
		tree.setPreferredSize(new Dimension(220, 480));
		return tree;
	}

	public JTree getFileTree(List<MyFile> files) {// ��Ŀ¼��
		JTree tree;
		DefaultMutableTreeNode home = new DefaultMutableTreeNode(
				new TreeNodeData(FileType.HOME, "MySharefile"));
		DefaultMutableTreeNode li = null;
		for (MyFile file : files) {
			// �г����ļ����Լ��ļ���
			li = new DefaultMutableTreeNode(new TreeNodeData(
					file.getFileType(), file.getName()));
			// �г��ļ��������ļ���ֹ������Ŀ¼
			String a = file.getFileType();
			if (a.equals(FileType.FOLDER)) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// ·��������Ҫ���ܴ���
				if (f.listFiles() != null) {// ���жϻᵼ��ʱ��ʱ���׳���ָ���쳣
					for (File fc : f.listFiles()) {
						li.add(new DefaultMutableTreeNode(new TreeNodeData(
								FileUtil.getFileType(fc), fc.getName())));
					}
				}
			}
			home.add(li);
		}
		tree = new JTree(home);
		tree.setCellRenderer(new MyRenderer());// ����TreeNodeData�������
		tree.setPreferredSize(new Dimension(220, 480));
		return tree;
	}

	public static double getDirSize(File file) {
		// �ж��ļ��Ƿ����
		if (file.exists()) {
			// �����Ŀ¼��ݹ���������ݵ��ܴ�С
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				double size = 0;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {// ������ļ���ֱ�ӷ������С,��KBΪ��λ
				double size = (double) file.length() / 1024;
				return size;
			}
		} else {
			System.out.println("�ļ������ļ��в����ڣ�����·���Ƿ���ȷ��");
			return 0.0;
		}
	}

	public static String getDirSizeByString(double s) {// ��ʾ�ļ���С�ַ��� �Ѵ����
		DecimalFormat df = new DecimalFormat(".00");// ���ÿռ侫ȷ��С�������λ
		String size = "";
		if (s > 1024 * 1024) {
			size = df.format(s / (1024 * 1024)) + "G";
		} else if (s > 1024) {
			size = df.format(s / 1024) + "M";
		} else {
			size = df.format(s) + "KB";
		}
		return size;
	}

	public static String checkfilename(String filename) {// ����ļ��Ƿ����
		File homeFolder = new File(MyNetDisk.username);
		File[] files = homeFolder.listFiles();
		// ������
		for (File file : files) {
			// �г����ļ����Լ��ļ���
			// �г��ļ��������ļ���ֹ������Ŀ¼
			String a = file.getName();
			if (file.isDirectory()) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// ·��������Ҫ���ܴ���
				for (File fc : f.listFiles()) {
					if (filename.equals(fc.getName()))
						return MyNetDisk.username + "/" + file.getName() + "/"
								+ filename;
				}
			}
			if (filename.equals(file.getName()))
				return MyNetDisk.username + "/" + filename;
		}
		return null;
	}

	public static void newMyNetDisk() {// ˢ��Ŀ¼������
		File[] files = FileUtil.getFolder(MyNetDisk.username);// �г��ļ�������
		File file;
		String s1 = "";
		List<MyFile> f = new ArrayList<MyFile>();
		MyFile myFile = null;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			s1 = file.getName() + SessionProtocol.FILE_SPLIT_FILETYPE
					+ FileUtil.getFileType(file);
			myFile = FileUtil.getMyFile(s1);// ��ȡ�ļ���������
			if (null != myFile)
				f.add(myFile);
		}
		// �ػ�����
		new MyNetDisk(MyNetDisk.username, MyNetDisk.sessionID, f);
	}

	public static void main(String[] args) {
		new MyNetDisk("ch", "OJDSGKDF65DSHF4S78DH4F2SDSD34FH", null);
	}

}
