package cn.ch.dao;

import cn.ch.domain.User;


public interface UserDAO {
	public User checkUser(String name,String password);
	
}
