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
	 * ch网盘
	 */
	private static String sessionID;
	private static String username;
	private static String dialogfilename;
	private static String parentfilename;
	private JTextField searchfilename;
	// 获取屏幕大小
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public MyNetDisk(String username, String sessionID, List<MyFile> files) {
		MyNetDisk.sessionID = sessionID;
		MyNetDisk.username = username;
		MyNetDisk.dialogfilename = "MyNetDisk";
		MyNetDisk.parentfilename = "MyNetDisk";
		final JFrame frame = this;
		this.setTitle("ch网盘");
		this.setSize(800, 600);
		this.setLocation((int) ((screenSize.getWidth() - this.getWidth()) / 2),
				(int) (screenSize.getHeight() - this.getHeight()) / 2);
		this.setLayout(new BorderLayout(10, 10));// 边界布局
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 顶部
		JPanel topPanel = new JPanel();
		JPanel topRightPanel = new JPanel();
		// 添加登陆信息等
		topRightPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 5));// 流式布局
		topRightPanel.add(new JLabel("欢迎您：" + username));
		try {
			topRightPanel.add(new JLabel("当前IP："
					+ InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			topRightPanel.add(new JLabel("当前IP：获取失败"));
		}
		// 按钮
		JButton newBtn = new JButton("新建文件夹");
		topRightPanel.add(newBtn);
		JButton uploadBtn = new JButton("上传");
		JButton dnloadBtn = new JButton("下载 ");
		JButton refreshBtn = new JButton("刷新");
		topRightPanel.add(uploadBtn);
		topRightPanel.add(dnloadBtn);
		topRightPanel.add(refreshBtn);

		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		topPanel.add(new JLabel(new ImageIcon("images/logo.jpg")));
		topPanel.add(topRightPanel);

		JPanel buttomPanel = new JPanel();// 底部面板
		final JPanel mainPanel = new JPanel();// 中部面板
		final JPanel rightPanel = new JPanel();// 中右部面板
		// 左侧目录
		final JTree jt = getFolderTree(files);
		// 目录监听
		jt.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		jt.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// TODO 自动生成的方法存根
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt
						.getLastSelectedPathComponent();
				if (node == null)
					return;
				Object nodeInfo = node.getUserObject();
				String nodeString = String.valueOf(nodeInfo);
				// System.out.println(nodeString);
				// 取出选中的节点名
				if (nodeString.equals("MyNetDisk"))
					MyNetDisk.dialogfilename = "MyNetDisk";
				else {
					int x = nodeString.indexOf(" ");// 提取空格之前的文件名字符串
					MyNetDisk.dialogfilename = nodeString.substring(0, x);
				}
				// System.out.println(MyNetDisk.dialogfilename);

				// 取出选中节点父节点名，用于判断是否在根目录下
				DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) node
						.getParent();
				if (parentnode == null)
					return;
				Object parentnodeInfo = parentnode.getUserObject();
				String parentnodeString = String.valueOf(parentnodeInfo);
				if (parentnodeString.equals("MyNetDisk"))
					MyNetDisk.parentfilename = "MyNetDisk";
				else {
					int y = parentnodeString.indexOf(" ");// 提取空格之前的文件名字符串
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
		rightPanel.add(new JLabel("欢迎使用 ch 网盘"));
		rightPanel.add(new JLabel("已用大小/全部大小：0"
				+ getDirSizeByString(getDirSize(new File(MyNetDisk.username)))
				+ "/100MB"));

		JLabel hide1JLabel = new JLabel("                ");// 布局所需
		rightPanel.add(hide1JLabel);
		JButton deleteBtn = new JButton("删除文件");
		rightPanel.add(deleteBtn);
		JButton modifyBtn = new JButton("修改文件名");
		rightPanel.add(modifyBtn);
		JLabel hide2JLabel = new JLabel("                                   ");// 布局所需
		rightPanel.add(hide2JLabel);
		JButton shareBtn = new JButton("共享文件");
		rightPanel.add(shareBtn);
		JButton cancelshareBtn = new JButton("取消文件共享");
		rightPanel.add(cancelshareBtn);
		JButton searchshareBtn = new JButton("查看我的共享文件");
		rightPanel.add(searchshareBtn);
		JLabel hide3JLabel = new JLabel("            ");// 布局所需
		rightPanel.add(hide3JLabel);
		searchfilename = new JTextField(20);// 左边的目录树不能移动了
		rightPanel.add(new InputPanel(new JLabel("搜索"), searchfilename));
		JButton searchBtn = new JButton("搜索文件");
		rightPanel.add(searchBtn);

		buttomPanel.add(new JLabel("本软件由 计科1104班 ch 制作,由本软件导致的数据丢失的责任自负"));
		topPanel.setPreferredSize(new Dimension(800, 40));
		rightPanel.setPreferredSize(new Dimension(550, 480));
		mainPanel.setPreferredSize(new Dimension(800, 480));
		buttomPanel.setPreferredSize(new Dimension(800, 20));
		mainPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
				Color.BLUE));// 分界线

		// 按钮事件监听
		newBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 新建文件夹
				String filename = JOptionPane.showInputDialog(null, "请输入新建文件名");
				new NewFolder(MyNetDisk.username, "127.0.0.1", 8550, filename);
			}
		});

		uploadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 上传文件
				String fileparent = "";
				// 初始化文件选择框
				JFileChooser fDialog = new JFileChooser();
				fDialog.setDialogTitle("请选择上传的文件");
				int returnVal = fDialog.showOpenDialog(null);
				// 如果是选择了文件
				if (JFileChooser.APPROVE_OPTION == returnVal) {
					// System.out.println(fDialog.getSelectedFile());
					if ((getDirSize(new File(MyNetDisk.username)) + getDirSize(fDialog
							.getSelectedFile())) <= 102400.00) {// 判断空间大小
						// 选择需要上传到的文件夹
						if (checkfilename(fDialog.getSelectedFile().getName()) == null) {// 没有重名文件
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
											"请勿选择非文件夹进行上传");
								}
							}
							new Upload(MyNetDisk.username, "127.0.0.1", 8550,
									fDialog.getSelectedFile(), dir);
							Boolean up = false;
							Socket socket = new Socket();
							try {
								socket.setSoTimeout(10000);// 超时时间
								// 开始连接
								socket.connect(new InetSocketAddress(
										"127.0.0.1", 8550), 10000);
								// 开始传输
								try {
									PrintStream pst = new PrintStream(socket
											.getOutputStream());
									BufferedReader br = new BufferedReader(
											new InputStreamReader(socket
													.getInputStream()));// 文件名称和处理后的类型
									String line = null;
									// 向服务器发送用户数据
									pst.println("Upload"
											+ SessionProtocol.SPLIT_SIGN
											+ "username="
											+ MyNetDisk.username
											+ "&filename="
											+ fDialog.getSelectedFile()
													.getName() + "&fileparent="
											+ fileparent);
									line = br.readLine();
									if (line != null) {// 通过验证
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
								System.out.println("上传写入数据库失败");
							}
							try {
								Thread.sleep(1000);// 延迟刷新界面
							} catch (InterruptedException x) {
								System.out.println(x.getMessage());
							}
							frame.dispose();
							newMyNetDisk();
						} else {
							JOptionPane.showMessageDialog(null, "上传文件失败，文件已存在");
						}

					} else {
						JOptionPane.showMessageDialog(null, "上传文件失败，空间不足");
					}
				}
			}
		});

		dnloadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 下载文件
				String fromdir = checkfilename(MyNetDisk.dialogfilename);
				File a = new File("D:/MyEclipse/work/MyNetDisk/"
						+ MyNetDisk.username + "/" + MyNetDisk.dialogfilename);
				if (!a.isDirectory()) {// 下载名不能是文件夹
					JFileChooser chooser = new JFileChooser();
					JTextField text;
					text = getTextField(chooser);
					text.setText(MyNetDisk.dialogfilename);// 设置默认下载文件名
					int returnVal = chooser.showSaveDialog(chooser);
					String path = "";
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						path = chooser.getSelectedFile().getPath();
						new Download(MyNetDisk.username, MyNetDisk.username,
								"127.0.0.1", 8550, fromdir, path, 1);
					}
				} else {
					JOptionPane.showMessageDialog(null, "下载失败，请选择非文件夹进行下载！");
				}
			}
		});

		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 刷新窗口
				frame.dispose();
				newMyNetDisk();
			}
		});

		deleteBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 删除文件
				if (MyNetDisk.dialogfilename != null) {// 删除文件
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
				// 修改文件名
				String filename = JOptionPane.showInputDialog(null,
						"请输入修改后的文件名");
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
				// 共享文件
				File f = new File(MyNetDisk.username + "/"
						+ MyNetDisk.dialogfilename);
				if (!f.isDirectory()) {
					new ShareFile(MyNetDisk.username, "127.0.0.1", 8550,
							MyNetDisk.dialogfilename);
				} else {
					JOptionPane.showMessageDialog(null, "共享失败,文件夹不允许共享！");
				}
			}
		});

		cancelshareBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 取消文件共享
				new CancelShareFile(MyNetDisk.username, "127.0.0.1", 8550,
						MyNetDisk.dialogfilename, MyNetDisk.parentfilename);
			}
		});

		searchshareBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 查看我的共享文件
				Socket socket = new Socket();
				List<MyFile> files = new ArrayList<MyFile>();
				String node = null;
				MyFile myFile = null;
				try {
					socket.setSoTimeout(10000);// 超时时间
					// 开始连接
					socket.connect(new InetSocketAddress("127.0.0.1", 8550),
							10000);
					// 开始传输
					try {
						PrintStream pst = new PrintStream(socket
								.getOutputStream());
						BufferedReader br = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));// 文件名称和处理后的类型
						String line = null;
						// 向服务器发送用户数据
						pst.println("SearchMyShareFile"
								+ SessionProtocol.SPLIT_SIGN + "username="
								+ MyNetDisk.username);
						line = br.readLine();
						if (line != null) {// 通过验证
							HashMap<String, String> param = StringUtil
									.getParameterMap(line);
							if (SessionProtocol.SUCCESS.equals(param
									.get("loginSign"))) {
								// 获取文件(夹)列表
								while ((node = br.readLine()) != null) {
									// System.out.println(node);
									myFile = FileUtil.getMyFile(node);// 获取文件名跟类型
									if (null != myFile)
										files.add(myFile);
								}
							} else {
								JOptionPane.showMessageDialog(null, "查看失败！");
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
				new DialogShowTree(files).jd.setVisible(true);// 弹出对话框
			}
		});

		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 搜索文件
				Socket socket = new Socket();
				List<MyFile> files = new ArrayList<MyFile>();
				String node = null;
				MyFile myFile = null;
				String getfilename = searchfilename.getText().trim();// 取得搜索的文件名
				// System.out.println(searchfilename.getText().trim());
				Boolean flag = false;
				if (getfilename.equals("")) {
					try {
						socket.setSoTimeout(10000);// 超时时间
						// 开始连接
						socket.connect(
								new InetSocketAddress("127.0.0.1", 8550), 10000);
						// 开始传输
						try {
							PrintStream pst = new PrintStream(socket
									.getOutputStream());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(socket
											.getInputStream()));// 文件名称和处理后的类型
							String line = null;
							// 向服务器发送用户数据
							pst.println("SearchShareFile"
									+ SessionProtocol.SPLIT_SIGN + "flag="
									+ "all");
							line = br.readLine();
							if (line != null) {// 通过验证
								HashMap<String, String> param = StringUtil
										.getParameterMap(line);
								if (SessionProtocol.SUCCESS.equals(param
										.get("loginSign"))) {
									// 获取文件(夹)列表
									while ((node = br.readLine()) != null) {
										// System.out.println(node);
										myFile = FileUtil.getMyFile(node);// 获取文件名跟类型
										if (null != myFile)
											files.add(myFile);
									}
									new DialogShowSearchTree(files).jd
											.setVisible(true);// 弹出对话框
								} else {
									JOptionPane
											.showMessageDialog(null, "搜索失败！");
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
						socket.setSoTimeout(10000);// 超时时间
						// 开始连接
						socket.connect(
								new InetSocketAddress("127.0.0.1", 8550), 10000);
						// 开始传输
						try {
							PrintStream pst = new PrintStream(socket
									.getOutputStream());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(socket
											.getInputStream()));// 文件名称和处理后的类型
							String line = null;
							// 向服务器发送用户数据
							pst.println("SearchShareFile"
									+ SessionProtocol.SPLIT_SIGN + "flag="
									+ "match" + "&getfilename=" + getfilename);
							line = br.readLine();
							if (line != null) {// 通过验证
								HashMap<String, String> param = StringUtil
										.getParameterMap(line);
								if (SessionProtocol.SUCCESS.equals(param
										.get("loginSign"))) {
									// 获取文件(夹)列表
									while ((node = br.readLine()) != null) {
										// System.out.println(node);
										myFile = FileUtil.getMyFile(node);// 获取文件名跟类型
										if (null != myFile)
											files.add(myFile);
									}
									new DialogShowSearchTree(files).jd
											.setVisible(true);// 弹出对话框
								} else {
									JOptionPane
											.showMessageDialog(null, "搜索失败！");
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

		// 访问数据库，是否存在没下载完的文件
		ArrayList<String> fromdirString = new ArrayList<String>();
		ArrayList<String> todirString = new ArrayList<String>();
		ArrayList<String> usernameString = new ArrayList<String>();
		Boolean notdown = false;
		Socket socket = new Socket();
		try {
			socket.setSoTimeout(10000);// 超时时间
			// 开始连接
			socket.connect(new InetSocketAddress("127.0.0.1", 8550), 10000);
			// 开始传输
			try {
				PrintStream pst = new PrintStream(socket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));// 文件名称和处理后的类型
				String line = null;
				// 向服务器发送用户数据
				pst.println("download" + SessionProtocol.SPLIT_SIGN + "username="
						+ MyNetDisk.username);
				line = br.readLine();
				String node = null;
				if (line != null) {// 通过验证
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
		
		if (notdown) {// 开始下载未完成的文件
			for (int i = 0; i < usernameString.size(); i++) {
				if (usernameString.get(i).equals(MyNetDisk.username)) {
					JOptionPane.showMessageDialog(null, "你有未下载完的文件！");
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
												todirString.get(i), true));// 设置true继续下载
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
								// 刷新和关闭管道
								bos.flush();
								bos.close();
								bis.close();
								// System.out.println(tof.length());
								Socket socket1 = new Socket();
								try {
									socket1.setSoTimeout(10000);// 超时时间
									// 开始连接
									socket1.connect(new InetSocketAddress("127.0.0.1", 8550), 10000);
									// 开始传输
									try {
										PrintStream pst = new PrintStream(socket1.getOutputStream());
										BufferedReader br = new BufferedReader(new InputStreamReader(
												socket1.getInputStream()));// 文件名称和处理后的类型
										// 向服务器发送用户数据
										pst.println("not" + SessionProtocol.SPLIT_SIGN + "username="
												+ MyNetDisk.username);
									} catch (IOException c) {
										System.out.println(c.getMessage());
									}
								} catch (Exception e3) {
									System.out.println(e3.getMessage());
								}
								JOptionPane.showMessageDialog(null,
										"上次未下载完成的文件继续下载成功！");
							} catch (FileNotFoundException a) {
								System.out.println(a.getMessage());
							} catch (IOException e) {
								System.out.println(e.getMessage());
							}
						} else {
							JOptionPane.showMessageDialog(null,
									"对不起，未完成下载文件本地已删除，请重新下载！");
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

	// 以上属于class MyNetDisk
	// <----------------------------------------------------------------------------------------->

	class DialogShowTree {// 弹框显示选择文件窗口
		JDialog jd = new JDialog();

		public DialogShowTree(List<MyFile> files) {
			// TODO 自动生成的构造函数存根
			jd.setSize(300, 600);
			jd.setLocation(400, 70);
			Container c2 = jd.getContentPane();
			c2.setLayout(null);
			// 目录
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 10, 300, 450);
			c2.add(scrollPane);
			final JTree JT = getFileTree(files);
			scrollPane.setViewportView(JT);
			setVisible(true);
			// 处理单击目录节点事件的监听
			JT.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			JT.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					// TODO 自动生成的方法存根
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

			JButton jbb1 = new JButton("下载文件");
			jbb1.setBounds(50, 500, 100, 30);
			c2.add(jbb1);
			JButton jbb2 = new JButton("取    消");
			jbb2.setBounds(150, 500, 100, 30);
			c2.add(jbb2);

			jbb1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String fromdir = checkfilename(MyNetDisk.dialogfilename);
					File a = new File("D:/MyEclipse/work/MyNetDisk/"
							+ MyNetDisk.username + "/"
							+ MyNetDisk.dialogfilename);
					if (!a.isDirectory()) {// 下载名不能是文件夹
						JFileChooser chooser = new JFileChooser();
						JTextField text;
						text = getTextField(chooser);
						text.setText(MyNetDisk.dialogfilename);// 设置默认下载文件名
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
								.showMessageDialog(null, "下载失败，请选择非文件夹进行下载！");
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

	class DialogShowSearchTree {// 弹框显示搜索结果文件窗口
		JDialog jd = new JDialog();

		public DialogShowSearchTree(List<MyFile> files) {
			// TODO 自动生成的构造函数存根
			jd.setSize(300, 600);
			jd.setLocation(400, 70);
			Container c2 = jd.getContentPane();
			c2.setLayout(null);
			// 目录
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 10, 300, 450);
			c2.add(scrollPane);
			final JTree JT = getFileTree(files);
			scrollPane.setViewportView(JT);
			setVisible(true);
			// 处理单击目录节点事件的监听
			JT.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			JT.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					// TODO 自动生成的方法存根
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

			JButton jbb1 = new JButton("下载文件");
			jbb1.setBounds(10, 500, 80, 30);
			c2.add(jbb1);
			JButton jbb3 = new JButton("保 存");
			jbb3.setBounds(100, 500, 80, 30);
			c2.add(jbb3);
			JButton jbb2 = new JButton("取 消");
			jbb2.setBounds(190, 500, 80, 30);
			c2.add(jbb2);

			jbb1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// 分离节点上的用户名和文件名
					int x = MyNetDisk.dialogfilename.indexOf("-");
					String getusername = MyNetDisk.dialogfilename.substring(0,
							x);
					int y = MyNetDisk.dialogfilename.length();
					String getfilename = MyNetDisk.dialogfilename.substring(
							x + 1, y);
					MyNetDisk.username = getusername;// 设置检查文件名来自哪个用户
					String fromdir = checkfilename(getfilename);
					JFileChooser chooser = new JFileChooser();
					JTextField text;
					text = getTextField(chooser);
					text.setText(MyNetDisk.dialogfilename);// 设置默认下载文件名
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
					// 分离节点上的用户名和文件名
					String us = MyNetDisk.username;
					String path = "D:/MyEclipse/work/MyNetDisk/"
							+ MyNetDisk.username + "/";// 保存当前用户路径
					int x = MyNetDisk.dialogfilename.indexOf("-");
					String getusername = MyNetDisk.dialogfilename.substring(0,
							x);
					int y = MyNetDisk.dialogfilename.length();
					String getfilename = MyNetDisk.dialogfilename.substring(
							x + 1, y);
					MyNetDisk.username = getusername;// 设置检查文件名来自哪个用户
					String fromdir = checkfilename(getfilename);
					MyNetDisk.username = us;
					if (checkfilename(getfilename) == null) {// 不重名
						path = path + getfilename;
						new Download(MyNetDisk.username, MyNetDisk.username,
								"127.0.0.1", 8550, fromdir, path, 0);
						// 写入数据库
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
								"请输入要保存的文件名");
						if (filename != null) {
							path = path + filename;
							new Download(MyNetDisk.username,
									MyNetDisk.username, "127.0.0.1", 8550,
									fromdir, path, 0);
							// 写入数据库
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
							JOptionPane.showMessageDialog(null, "请输入文件名！");
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

	public JTextField getTextField(Container c) {// 下载框设置默认下载名
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
	 * 网盘目录
	 */
	public JTree getFolderTree(List<MyFile> files) {// 带文件大小的目录树
		// 如果该用户根目录不存在,则进行创建
		File homeFolder = new File(MyNetDisk.username);
		if (!homeFolder.exists())
			homeFolder.mkdir();
		// 创建树
		JTree tree;
		DefaultMutableTreeNode home = new DefaultMutableTreeNode(
				new TreeNodeData(FileType.HOME, "MyNetDisk"));
		DefaultMutableTreeNode li = null;
		for (MyFile file : files) {
			// 列出非文件夹以及文件夹
			li = new DefaultMutableTreeNode(new TreeNodeData(
					file.getFileType(), file.getName()
							+ "   0"
							+ getDirSizeByString(getDirSize(new File(
									"D:/MyEclipse/work/MyNetDisk/"
											+ MyNetDisk.username + "/"
											+ file.getName())))));
			// 列出文件夹下子文件，止于两级目录
			String a = file.getFileType();
			if (a.equals(FileType.FOLDER)) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// 路径名很重要不能错了
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
		// 已用空间大小KB
		DecimalFormat df = new DecimalFormat(".00");
		String count = df.format(getDirSize(new File(MyNetDisk.username)));
		// System.out.println(count + "KB");
		tree = new JTree(home);
		tree.setCellRenderer(new MyRenderer());// 用了TreeNodeData里的树类
		tree.setPreferredSize(new Dimension(220, 480));
		return tree;
	}

	public JTree getFileTree(List<MyFile> files) {// 纯目录树
		JTree tree;
		DefaultMutableTreeNode home = new DefaultMutableTreeNode(
				new TreeNodeData(FileType.HOME, "MySharefile"));
		DefaultMutableTreeNode li = null;
		for (MyFile file : files) {
			// 列出非文件夹以及文件夹
			li = new DefaultMutableTreeNode(new TreeNodeData(
					file.getFileType(), file.getName()));
			// 列出文件夹下子文件，止于两级目录
			String a = file.getFileType();
			if (a.equals(FileType.FOLDER)) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// 路径名很重要不能错了
				if (f.listFiles() != null) {// 不判断会导致时不时的抛出空指针异常
					for (File fc : f.listFiles()) {
						li.add(new DefaultMutableTreeNode(new TreeNodeData(
								FileUtil.getFileType(fc), fc.getName())));
					}
				}
			}
			home.add(li);
		}
		tree = new JTree(home);
		tree.setCellRenderer(new MyRenderer());// 用了TreeNodeData里的树类
		tree.setPreferredSize(new Dimension(220, 480));
		return tree;
	}

	public static double getDirSize(File file) {
		// 判断文件是否存在
		if (file.exists()) {
			// 如果是目录则递归计算其内容的总大小
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				double size = 0;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {// 如果是文件则直接返回其大小,以KB为单位
				double size = (double) file.length() / 1024;
				return size;
			}
		} else {
			System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
			return 0.0;
		}
	}

	public static String getDirSizeByString(double s) {// 显示文件大小字符串 已处理好
		DecimalFormat df = new DecimalFormat(".00");// 已用空间精确到小数点后两位
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

	public static String checkfilename(String filename) {// 检查文件是否存在
		File homeFolder = new File(MyNetDisk.username);
		File[] files = homeFolder.listFiles();
		// 创建树
		for (File file : files) {
			// 列出非文件夹以及文件夹
			// 列出文件夹下子文件，止于两级目录
			String a = file.getName();
			if (file.isDirectory()) {
				File f = new File(MyNetDisk.username + "/" + file.getName());// 路径名很重要不能错了
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

	public static void newMyNetDisk() {// 刷新目录树界面
		File[] files = FileUtil.getFolder(MyNetDisk.username);// 列出文件夹名称
		File file;
		String s1 = "";
		List<MyFile> f = new ArrayList<MyFile>();
		MyFile myFile = null;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			s1 = file.getName() + SessionProtocol.FILE_SPLIT_FILETYPE
					+ FileUtil.getFileType(file);
			myFile = FileUtil.getMyFile(s1);// 获取文件名跟类型
			if (null != myFile)
				f.add(myFile);
		}
		// 重画界面
		new MyNetDisk(MyNetDisk.username, MyNetDisk.sessionID, f);
	}

	public static void main(String[] args) {
		new MyNetDisk("ch", "OJDSGKDF65DSHF4S78DH4F2SDSD34FH", null);
	}

}
