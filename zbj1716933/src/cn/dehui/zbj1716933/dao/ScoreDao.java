package cn.dehui.zbj1716933.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cn.dehui.zbj1716933.entity.Course;
import cn.dehui.zbj1716933.entity.Score;
import cn.dehui.zbj1716933.entity.Student;

public class ScoreDao extends Dao {

    private static final String FIND_BY_STUDENT_SQL = "select student_id, course_id, score from score where student_id = '%s' order by %s";

    private static final String FIND_BY_COURSE_SQL  = "select student_id, course_id, score from score where course_id = '%s' order by %s";

    private static final String FIND_SQL            = "select student_id, course_id, score from score where student_id = '%s' and course_id = '%s'";

    private static final String INSERT_SQL          = "insert into score (student_id, course_id, score) values ('%s', '%s', %d)";

    private static final String UPDATE_SQL          = "update score set score = %d where student_id = '%s' and course_id = '%s'";

    private static final String DELETE_SQL          = "delete from score where student_id = '%s' and course_id = '%s'";

    public static final String  STUDENT_ID          = "student_id";

    public static final String  COURSE_ID           = "course_id";

    public static final String  SCORE               = "score";

    private StudentDao          studentDao          = new StudentDao();

    private CourseDao           courseDao           = new CourseDao();

    public void insertOrUpdate(Score score) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        try {
            if (find(score.getStudent().getId(), score.getCourse().getId()) == null) {
                stmt.executeUpdate(String.format(INSERT_SQL, score.getStudent().getId(), score.getCourse().getId(),
                        score.getScore()));
            } else {
                stmt.executeUpdate(String.format(UPDATE_SQL, score.getScore(), score.getStudent().getId(), score
                        .getCourse().getId()));
            }
        } finally {
            connection.close();
        }
    }

    public Score find(String studentId, String courseId) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_SQL, studentId, courseId));

            if (rs != null && rs.next()) {
                Student student = studentDao.find(studentId);
                Course course = courseDao.find(courseId);

                return new Score(student, course, rs.getInt("score"));
            }

            return null;
        } finally {
            connection.close();
        }
    }

    public void delete(String studentId, String courseId) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(String.format(DELETE_SQL, studentId, courseId));
        } finally {
            connection.close();
        }
    }

    public List<Score> findByStudentOrderBy(String studentId, String order) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_BY_STUDENT_SQL, studentId, order));

            List<Score> scoreList = new ArrayList<Score>();
            while (rs.next()) {
                Student student = studentDao.find(studentId);
                String courseId = rs.getString("course_id");
                Course course = courseDao.find(courseId);

                scoreList.add(new Score(student, course, rs.getInt("score")));
            }
            return scoreList;
        } finally {
            connection.close();
        }
    }

    public List<Score> findByCourseOrderBy(String courseId, String order) throws SQLException {
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(FIND_BY_COURSE_SQL, courseId, order));

            List<Score> scoreList = new ArrayList<Score>();
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                Student student = studentDao.find(studentId);
                Course course = courseDao.find(courseId);

                scoreList.add(new Score(student, course, rs.getInt("score")));
            }
            return scoreList;
        } finally {
            connection.close();
        }
    }

}
