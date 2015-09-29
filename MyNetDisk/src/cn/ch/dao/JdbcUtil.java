package cn.ch.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcUtil {
	/**
	 * ���ݿ�����
	 */
	/*��ʼ�����ݿ����ӳ�*/
	private static DataSource dataSource = new ComboPooledDataSource();
	
	/*��ȡ����Դ*/
	public DataSource getDataSource(){
		return dataSource;
	}
	
	/*��ȡ����*/
	public static Connection getConnection() throws SQLException{
		return dataSource.getConnection();
	}
	
	/*�ͷ�����*/
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
