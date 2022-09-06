package com.xidian.cloud.mall.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBUtil {
	public static Connection getConn() {
	    String driver = "com.mysql.jdbc.Driver";
	    String url = "jdbc:mysql://localhost:3306/imooc_mall";
	    String username = "root";
	    String password = "2022LDBX";
	    Connection conn = null;
	    try {
	        Class.forName(driver); //classLoader,加载对应驱动
	        conn =DriverManager.getConnection(url, username, password);
	    } catch (ClassNotFoundException | SQLException e) {
	        e.printStackTrace();
	    }
		return conn;
	}
}
