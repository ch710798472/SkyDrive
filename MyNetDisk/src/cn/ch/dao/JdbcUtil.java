package cn.ch.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcUtil {
	/**
	 * 数据库连接
	 */
	/*初始化数据库连接池*/
	private static DataSource dataSource = new ComboPooledDataSource();
	
	/*获取数据源*/
	public DataSource getDataSource(){
		return dataSource;
	}
	
	/*获取连接*/
	public static Connection getConnection() throws SQLException{
		return dataSource.getConnection();
	}
	
	/*释放连接*/
	public static void free(ResultSet rs,PreparedStatement ps,Connection conn){
		if(null != rs){
			try {
				rs.close();
			} catch (SQLException e) {}
		}
		if(null != ps){
			try {
				ps.close();
			} catch (SQLException e) {}
		}
		if(null != conn){
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}
	public static void free(PreparedStatement ps,Connection conn){
		if(null != ps){
			try {
				ps.close();
			} catch (SQLException e) {}
		}
		if(null != conn){
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}
}
