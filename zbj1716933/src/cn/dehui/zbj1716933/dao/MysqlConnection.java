package cn.dehui.zbj1716933.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {

    private String                       driver   = "com.mysql.jdbc.Driver";

    private String                       url      = "jdbc:mysql://localhost:3306/zbj1716933?useUnicode=true&characterEncoding=utf-8";

    private String                       username = "root";

    private String                       password = "root";

    private static MysqlConnection instance;

    public void init() {
        try {
            Class.forName(driver).newInstance();

            instance = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MysqlConnection getInstance() {
        return instance;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
