package cn.dehui.zbj1716933.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cn.dehui.zbj1716933.entity.Student;

public class StudentDao extends Dao {

    private static final String FIND_SQL     = "select id, name from student where id = '%s'";

    private static final String FIND_ALL_SQL = "select id, name from student order by %s";

    private static final String INSERT_SQL   = "insert into student (id, name) values ('%s', '%s')";

    private static final String UPDATE_SQL   = "update student set name = '%s' where id = '%s'";

    private static final String DELETE_SQL   = "delete from student where id = '%s'";

    public static final String  ID           = "id";

    public static final String  NAME         = "name";

    public void insertOrUpdate(Student student) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        try {
            if (find(student.getId()) == null) {
                stmt.executeUpdate(String.format(INSERT_SQL, student.getId(), student.getName()));
            } else {
                stmt.executeUpdate(String.format(UPDATE_SQL, student.getName(), student.getId()));
            }
        } finally {
            connection.close();
        }
    }

    public Student find(String id) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_SQL, id));

            if (rs != null && rs.next()) {
                return new Student(rs.getString("id"), rs.getString("name"));
            }

            return null;
        } finally {
            connection.close();
        }
    }

    public void delete(String id) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(String.format(DELETE_SQL, id));
        } finally {
            connection.close();
        }
    }

    public List<Student> findAll(String order) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_ALL_SQL, order));

            List<Student> studentList = new ArrayList<Student>();
            while (rs.next()) {
                studentList.add(new Student(rs.getString("id"), rs.getString("name")));
            }
            return studentList;
        } finally {
            connection.close();
        }
    }

}
