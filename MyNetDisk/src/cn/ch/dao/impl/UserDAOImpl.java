package cn.ch.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import cn.ch.dao.JdbcUtil;
import cn.ch.dao.UserDAO;
import cn.ch.domain.User;

public class UserDAOImpl implements UserDAO {

	private final static Logger log = Logger.getLogger(UserDAO.class);
	/*
	 * 登陆
	 */
	public User checkUser(String name, String password) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User user = null;
		try {
			conn = JdbcUtil.getConnection();
			ps = conn.prepareStatement("select * from m_user where name=? and password=?");
			ps.setString(1, name);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if(rs.next()){
				user = new User();
				user.setName(rs.getString("name"));
			}
		} catch (SQLException e) {
			log.error("检查用户名密码出错！",e);
		}
		finally{
			JdbcUtil.free(rs, ps, conn);
		}
		return user;

	}

}
