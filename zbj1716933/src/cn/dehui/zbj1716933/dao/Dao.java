package cn.dehui.zbj1716933.dao;

import java.sql.Connection;

public class Dao {

    protected Connection getConnection() {
        return MysqlConnection.getInstance().getConnection();
    }
}
