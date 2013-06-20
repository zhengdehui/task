package cn.dehui.zbj1716933.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import cn.dehui.zbj1716933.dao.CourseDao;
import cn.dehui.zbj1716933.dao.MysqlConnection;
import cn.dehui.zbj1716933.dao.ScoreDao;
import cn.dehui.zbj1716933.dao.StudentDao;
import cn.dehui.zbj1716933.entity.Course;
import cn.dehui.zbj1716933.entity.Score;
import cn.dehui.zbj1716933.entity.Student;

public class Controller {

    private Scanner    scanner    = new Scanner(System.in);

    private StudentDao studentDao = new StudentDao();

    private CourseDao  courseDao  = new CourseDao();

    private ScoreDao   scoreDao   = new ScoreDao();

    public void run() {
        printMain();
    }

    private void printMain() {
        while (true) {
            printTitle("学籍管理系统");
            System.out.println("1. 学生管理");
            System.out.println("2. 课程管理");
            System.out.println("3. 成绩管理");
            System.out.println("0. 退出");
            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printStudentManage();
                    break;
                case 2:
                    printCourseManage();
                    break;
                case 3:
                    printScoreManage();
                    break;
                case 0:
                    return;

                default:
                    System.out.println("输入错误！");
                    break;
            }
        }
    }

    private void printScoreManage() {
        while (true) {
            printTitle("成绩管理 ");
            System.out.println("1. 添加成绩信息");
            System.out.println("2. 修改成绩信息");
            System.out.println("3. 删除成绩信息");
            System.out.println("4. 查询某课程的所有成绩按学号排序");
            System.out.println("5. 查询某课程的所有成绩按分数排序");
            System.out.println("6. 查询某学生的所有成绩按课程号排序");
            System.out.println("7. 查询某学生的所有成绩按分数排序");
            System.out.println("0. 返回上一级");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateScore("添加", "添加成绩信息");
                    break;
                case 2:
                    printAddOrUpdateScore("修改", "修改成绩信息");
                    break;
                case 3:
                    printDeleteScore();
                    break;
                case 4:
                    printScoreByCourseOrderBy(ScoreDao.STUDENT_ID, "课程成绩按学号排序");
                    break;
                case 5:
                    printScoreByCourseOrderBy(ScoreDao.SCORE, "课程成绩按分数排序");
                    break;
                case 6:
                    printScoreByStudentOrderBy(ScoreDao.COURSE_ID, "学生成绩按课程号排序");
                    break;
                case 7:
                    printScoreByStudentOrderBy(ScoreDao.SCORE, "学生成绩按分数排序");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("输入错误！");
                    break;
            }
        }

    }

    private void printScoreByStudentOrderBy(String orderColumn, String title) {
        printTitle(title);
        String studentId = doInput("学号", scanner);
        String format = "%-10s";
        try {
            List<Score> scoreList = scoreDao.findByStudentOrderBy(studentId, orderColumn);
            System.out.printf(format, "学号");
            System.out.printf(format, "姓名");
            System.out.printf(format, "课程号");
            System.out.printf(format, "课程名");
            System.out.printf(format, "分数");
            System.out.println();

            for (Score s : scoreList) {
                System.out.printf(format, s.getStudent().getId());
                System.out.printf(format, s.getStudent().getName());
                System.out.printf(format, s.getCourse().getId());
                System.out.printf(format, s.getCourse().getName());
                System.out.printf("%10d", s.getScore());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("查询错误！");
        }
    }

    private void printScoreByCourseOrderBy(String orderColumn, String title) {
        printTitle(title);
        String courseId = doInput("课程号", scanner);
        String format = "%-10s";
        try {
            List<Score> scoreList = scoreDao.findByCourseOrderBy(courseId, orderColumn);
            System.out.printf(format, "学号");
            System.out.printf(format, "姓名");
            System.out.printf(format, "课程号");
            System.out.printf(format, "课程名");
            System.out.printf(format, "分数");
            System.out.println();

            for (Score s : scoreList) {
                System.out.printf(format, s.getStudent().getId());
                System.out.printf(format, s.getStudent().getName());
                System.out.printf(format, s.getCourse().getId());
                System.out.printf(format, s.getCourse().getName());
                System.out.printf("%10d", s.getScore());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("查询错误！");
        }
    }

    private void printDeleteScore() {
        printTitle("删除成绩信息");
        String studentId = doInput("学号", scanner);
        String courseId = doInput("课程号", scanner);
        try {
            scoreDao.delete(studentId, courseId);
            System.out.println("删除成功！");
        } catch (SQLException e) {
            System.out.println("删除失败！");
        }
    }

    private void printAddOrUpdateScore(String action, String title) {
        printTitle(title);
        String studentId = doInput("学号", scanner);
        String courseId = doInput("课程号", scanner);
        int score = doInputScore(scanner);

        try {
            scoreDao.insertOrUpdate(new Score(new Student(studentId), new Course(courseId), score));
            System.out.println(action + "成功！");
        } catch (SQLException e) {
            System.out.println(action + "失败！");
        }
    }

    private static int doInputScore(Scanner scanner) {
        while (true) {
            try {
                System.out.print("分数：");
                String tmp = scanner.next();
                int score=Integer.parseInt(tmp);

                if (score >= 0 && score <= 100) {
                    return score;
                }
                System.out.println("输入错误，请重新输入！");
            } catch (Exception e) {
                System.out.println("输入错误，请重新输入！");
            }
        }
    }

    private void printCourseManage() {
        while (true) {
            printTitle("课程管理 ");
            System.out.println("1. 添加课程信息");
            System.out.println("2. 修改课程信息");
            System.out.println("3. 删除课程信息");
            System.out.println("4. 按课程号排序");
            System.out.println("5. 按课程名排序");
            System.out.println("0. 返回上一级");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateCourse("添加", "添加课程信息");
                    break;
                case 2:
                    printAddOrUpdateCourse("修改", "修改课程信息");
                    break;
                case 3:
                    printDeleteCourse();
                    break;
                case 4:
                    printCourseOrderBy(StudentDao.ID, "按课程号排序");
                    break;
                case 5:
                    printCourseOrderBy(StudentDao.NAME, "按课程名排序");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("输入错误！");
                    break;
            }
        }
    }

    private void printCourseOrderBy(String orderColumn, String title) {
        printTitle(title);
        String format = "%-10s";
        try {
            List<Course> courseList = courseDao.findAll(orderColumn);
            System.out.printf(format, "课程号");
            System.out.printf(format, "课程名");
            System.out.printf(format, "学院");
            System.out.println();

            for (Course c : courseList) {
                System.out.printf(format, c.getId());
                System.out.printf(format, c.getName());
                System.out.printf(format, c.getAcademy());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("查询错误！");
        }

    }

    private void printDeleteCourse() {
        printTitle("删除课程信息");
        String id = doInput("课程号", scanner);
        try {
            courseDao.delete(id);
            System.out.println("删除成功！");
        } catch (SQLException e) {
            System.out.println("删除失败！");
        }
    }

    private void printAddOrUpdateCourse(String action, String title) {
        printTitle(title);
        String id = doInput("课程号", scanner);
        String name = doInput("课程名", scanner);
        String academy = doInput("学院", scanner);

        try {
            courseDao.insertOrUpdate(new Course(id, name, academy));
            System.out.println(action + "成功！");
        } catch (SQLException e) {
            System.out.println(action + "失败！");
        }
    }

    private void printStudentManage() {
        while (true) {
            printTitle("学生管理 ");
            System.out.println("1. 添加学生信息");
            System.out.println("2. 修改学生信息");
            System.out.println("3. 删除学生信息");
            System.out.println("4. 按学号排序");
            System.out.println("5. 按名字排序");
            System.out.println("0. 返回上一级");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateStudent("添加", "添加学生信息");
                    break;
                case 2:
                    printAddOrUpdateStudent("修改", "修改学生信息");
                    break;
                case 3:
                    printDeleteStudent();
                    break;
                case 4:
                    printStudentOrderBy(StudentDao.ID, "按学号排序");
                    break;
                case 5:
                    printStudentOrderBy(StudentDao.NAME, "按名字排序");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("输入错误！");
                    break;
            }
        }
    }

    private void printStudentOrderBy(String orderColumn, String title) {
        printTitle(title);
        String format = "%-10s";
        try {
            List<Student> studentList = studentDao.findAll(orderColumn);
            System.out.printf(format, "学号");
            System.out.printf(format, "姓名");
            System.out.println();

            for (Student s : studentList) {
                System.out.printf(format, s.getId());
                System.out.printf(format, s.getName());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("查询错误！");
        }

    }

    private void printDeleteStudent() {
        printTitle("删除学生信息");
        String id = doInput("学号", scanner);
        try {
            studentDao.delete(id);
            System.out.println("删除成功！");
        } catch (SQLException e) {
            System.out.println("删除失败！");
        }
    }

    private void printAddOrUpdateStudent(String action, String title) {
        printTitle(title);
        String id = doInput("学号", scanner);
        String name = doInput("姓名", scanner);

        try {
            studentDao.insertOrUpdate(new Student(id, name));
            System.out.println(action + "成功！");
        } catch (SQLException e) {
            System.out.println(action + "失败！");
        }
    }

    private static String doInput(String title, Scanner scanner) {
        System.out.print(title + "：");
        String tmp = scanner.next();
        //        System.out.println();
        return tmp;
    }

    private static int doChoice(Scanner scanner) {
        try {
            System.out.print("请选择：");
            String tmp = scanner.next();
            //            System.out.println();
            return Integer.parseInt(tmp);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void printTitle(String title) {
        System.out.printf("********** %s **********", title);
        System.out.println();
    }

    public static final void main(String[] args) throws FileNotFoundException, IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(new File("jdbc.properties")));

        MysqlConnection conn=new MysqlConnection();
        conn.setDriver(p.getProperty("driver.class"));
        conn.setUrl(p.getProperty("jdbc.url"));
        conn.setUsername(p.getProperty("jdbc.username"));
        conn.setPassword(p.getProperty("jdbc.password"));
        conn.init();

        new Controller().run();
    }
}
