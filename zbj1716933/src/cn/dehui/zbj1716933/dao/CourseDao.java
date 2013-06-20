package cn.dehui.zbj1716933.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cn.dehui.zbj1716933.entity.Course;

public class CourseDao extends Dao {

    private static final String FIND_SQL     = "select id, name, academy from course where id = '%s'";

    private static final String FIND_ALL_SQL = "select id, name, academy from course order by %s";

    private static final String INSERT_SQL   = "insert into course (id, name, academy) values ('%s', '%s', '%s')";

    private static final String UPDATE_SQL   = "update course set name = '%s', academy = '%s' where id = '%s'";

    private static final String DELETE_SQL   = "delete from course where id = '%s'";

    public static final String  ID           = "id";

    public static final String  NAME         = "name";

    public void insertOrUpdate(Course course) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        try {
            if (find(course.getId()) == null) {
                stmt.executeUpdate(String.format(INSERT_SQL, course.getId(), course.getName(), course.getAcademy()));
            } else {
                stmt.executeUpdate(String.format(UPDATE_SQL, course.getName(), course.getAcademy(), course.getId()));
            }
        } finally {
            connection.close();
        }
    }

    public Course find(String id) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_SQL, id));

            if (rs != null && rs.next()) {
                return new Course(rs.getString("id"), rs.getString("name"), rs.getString("academy"));
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

    public List<Course> findAll(String order) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_ALL_SQL, order));

            List<Course> courseList = new ArrayList<Course>();
            while (rs.next()) {
                courseList.add(new Course(rs.getString("id"), rs.getString("name"), rs.getString("academy")));
            }
            return courseList;
        } finally {
            connection.close();
        }
    }

}
