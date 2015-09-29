package cn.ch.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cn.ch.dao.JdbcUtil;
import cn.ch.util.FileUtil;
import cn.ch.util.MD5;
import cn.ch.util.SessionProtocol;
import cn.ch.util.StringUtil;

public class ClientLogin {

	/**
	 * ch 网盘
	 */
	public static void main(String[] args) {
		new LoginFrame("ch 网盘");
	}

}

@SuppressWarnings("serial")
class LoginFrame extends JFrame {
	/*
	 * 登陆窗口
	 */
	private JTextField name;
	private JPasswordField password;
	private JTextField ip;
	private JTextField port;
	private JButton loginBtn;
	private JButton regBtn;

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public LoginFrame(String frameTitle) {
		this.setTitle(frameTitle);
		this.setSize(390, 285);
		this.setLocation((int) ((screenSize.getWidth() - this.getWidth()) / 2),
				(int) (screenSize.getHeight() - this.getHeight()) / 2);

		JPanel btnPanel = new JPanel();

		Box box = Box.createVerticalBox();

		ImageIcon background = new ImageIcon("images/clientLoginLogo.jpg");

		JLabel nameLabel = new JLabel("用户名:");
		JLabel passwordLabel = new JLabel("密    码:");
		JLabel ipLabel = new JLabel("服务器IP:");
		JLabel portLabel = new JLabel("端口:");
		name = new JTextField(20);
		password = new JPasswordField(20);
		ip = new JTextField(10);
		port = new JTextField(5);
		ip.setText("127.0.0.1");
		port.setText("8550");

		loginBtn = new JButton("登    陆");
		regBtn = new JButton("注    册 ");
		btnPanel.add(loginBtn);
		btnPanel.add(regBtn);

		box.add(new InputPanel(nameLabel, name));
		box.add(new InputPanel(passwordLabel, password));
		box.add(new InputPanel(ipLabel, ip));
		box.add(new InputPanel(ipLabel, ip, portLabel, port));
		box.add(btnPanel);

		this.add(new JLabel(background), BorderLayout.NORTH);
		this.add(box);

		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

		// 登陆按钮
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkInputData(ip.getText(), port.getText())) {
					if (!"".equals(name.getText())
							&& !"".equals(password.getPassword()))// 用户名密码不为空
					{
						// 登陆验证
						login(name.getText(), new MD5().calcMD5(String
								.valueOf(password.getPassword())),
								ip.getText(), Integer.valueOf(port.getText()));
					} else {
						JOptionPane.showMessageDialog(null, "请输入不为空的用户名或密码");
					}
				}
			}
		});

		// 注册按钮
		regBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reg(name.getText(), new MD5().calcMD5(String.valueOf(password
						.getPassword())));
			}
		});
	}

	/*
	 * 检查输入
	 */
	public boolean checkInputData(String ip, String port) {
		// if(!Pattern.matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}", ip)){
		// JOptionPane.showMessageDialog(this, "IP地址格式不正确！");
		// return false;
		// }
		// else if(!Pattern.matches("[0-9]{1,5}", port)){
		// JOptionPane.showMessageDialog(this, "端口不正确,范围为: 1-65535");
		// return false;
		// }
		return true;
	}

	/*
	 * 登陆
	 */
	public void login(String name, String password, String ip, int port) {
		final JFrame frame = this;
		/*
		 * 连接提示框
		 */
		final JDialog dialog = new JDialog(this, "正在连接", false);
		dialog.setModal(false);
		JButton cancel = new JButton("取消");
		// 提示信息
		JPanel panel = new JPanel();
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 0));
		p1.add(new JLabel("请稍候..."));
		p1.add(cancel);
		panel.setLayout(new GridLayout(4, 1));
		panel.setLocation(100, 100);
		panel.add(new JLabel("正在连接到 :"));
		panel.add(new JLabel(ip + " : " + port));
		panel.add(p1);

		JLabel icon = new JLabel(new ImageIcon("images/connect.png"));
		icon.setPreferredSize(new Dimension(50, 50));

		dialog.setSize(290, 150);
		dialog.setLocation(
				(int) ((screenSize.getWidth() - dialog.getWidth()) / 2),
				(int) (screenSize.getHeight() - dialog.getHeight()) / 2);
		dialog.add(icon, BorderLayout.WEST);
		dialog.add(panel);
		dialog.setVisible(true);
		this.setEnabled(false);

		final ConnectServer connectServer = new ConnectServer(this, ip, port,
				dialog, name, password);
		final Thread connectThread = new Thread(connectServer);
		connectThread.start();

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectServer.distroy();
				frame.setEnabled(true);
				dialog.dispose();
			}
		});
	}

	/*
	 * 注册
	 */
	public void reg(String name, String password) {
		final JFrame frame = this;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String strname = "";
		String strpassword = "d41d8cd98f00b204e9800998ecf8427e";
		Socket socket = new Socket();
		try{
		socket.setSoTimeout(10000);// 超时时间
		socket.connect(new InetSocketAddress("127.0.0.1", 8550), 10000);
		}catch(Exception e){
			JOptionPane.showMessageDialog(frame, "服务器未响应请求！");
		}
		try {
			if (!strname.equals(name) && !strpassword.equals(password))// 用户名密码不为空
			{
				conn = JdbcUtil.getConnection();//有点小问题
				ps = conn
						.prepareStatement("insert into m_user(name,password) values(?,?) ");
				ps.setString(1, name);
				ps.setString(2, password);
				ps.executeUpdate();
				JOptionPane.showMessageDialog(frame, "注册新用户成功！");
			} else {
				JOptionPane.showMessageDialog(frame, "用户名或密码不允许为空，请重新输入！");
			}
		} catch (SQLException e) {
			// System.out.println("注册新用户出错！");
			JOptionPane.showMessageDialog(frame, "注册新用户出错！");
		} finally {
			JdbcUtil.free(rs, ps, conn);
		}

	}
}


@SuppressWarnings("serial")
class InputPanel extends JPanel {
	public InputPanel(JLabel label, JTextField textField) {
		this.add(label);
		this.add(textField);
	}

	public InputPanel(JLabel label1, JTextField textField1, JLabel label2,
			JTextField textField2) {
		this.setPreferredSize(new Dimension(390, 100));
		this.add(label1);
		this.add(textField1);
		this.add(label2);
		this.add(textField2);
	}
}

/*
 * 连接线程
 */
class ConnectServer implements Runnable {
	private JFrame frame;
	private String ip;
	private int port;
	private String username;
	private String password;
	private JDialog dialog;
	private Socket socket;
	private int flag = 0;// 0为连接不成功,1为用户取消,2为服务器不响应

	public ConnectServer(JFrame frame, String ip, int port, JDialog dialog,
			String username, String password) {
		this.frame = frame;
		this.ip = ip;
		this.port = port;
		this.dialog = dialog;
		this.username = username;
		this.password = password;
	}

	public void run() {
		socket = new Socket();
		try {
			socket.setSoTimeout(10000);// 超时时间
			// 开始连接
			socket.connect(new InetSocketAddress(ip, port), 10000);
			dialog.dispose();// 连接成功后销毁
			flag = 2;
			checkUser(socket);// 检查用户登陆名和密码
		} catch (Exception e) {
			frame.setEnabled(true);
			dialog.dispose();
			if (flag == 0)
				JOptionPane.showMessageDialog(frame, "无法连接到服务器,请检查网络连接！");
			else if (flag == 2)
				JOptionPane.showMessageDialog(frame, "服务器未响应请求！");
		}
	}

	// 用户验证
	public boolean checkUser(Socket socket) throws Exception {
		PrintStream ps = new PrintStream(socket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));// 文件名称和处理后的类型
		String line = null;
		// 向服务器发送用户数据
		ps.println(SessionProtocol.LOGIN + SessionProtocol.SPLIT_SIGN
				+ "username=" + username + "&password=" + password);
		// System.out.println("---------->"+SessionProtocol.LOGIN+SessionProtocol.SPLIT_SIGN+"username="+username+"&password="+password);
		// 验证是否通过
		line = br.readLine();
		if (line != null) {// 通过验证
			HashMap<String, String> param = StringUtil.getParameterMap(line);
			String sessionID = param.get(SessionProtocol.SESSIONID);
			if (SessionProtocol.SUCCESS.equals(param.get("loginSign"))
					&& null != sessionID && !"".equals(sessionID)) {
				// 获取文件(夹)列表
				List<MyFile> files = new ArrayList<MyFile>();
				String node = null;
				MyFile myFile = null;
				while ((node = br.readLine()) != null) {
					// System.out.println(node);
					myFile = FileUtil.getMyFile(node);// 获取文件名跟类型
					if (null != myFile)
						files.add(myFile);
				}
				frame.dispose();
				new MyNetDisk(username, param.get(SessionProtocol.SESSIONID),
						files);// 登陆后界面的生成
			} else if (SessionProtocol.FAILURE.equals(param.get("loginSign"))) {
				frame.setEnabled(true);
				JOptionPane.showMessageDialog(frame, "用户名或密码错误！");
			}
		} else {
			frame.setEnabled(true);
			JOptionPane.showMessageDialog(frame, "无法读取服务器参数,可能客户端已经损坏,请重新下载！");
		}
		return false;
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