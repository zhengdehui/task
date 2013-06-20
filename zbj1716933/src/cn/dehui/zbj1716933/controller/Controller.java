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
            printTitle("ѧ������ϵͳ");
            System.out.println("1. ѧ������");
            System.out.println("2. �γ̹���");
            System.out.println("3. �ɼ�����");
            System.out.println("0. �˳�");
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
                    System.out.println("�������");
                    break;
            }
        }
    }

    private void printScoreManage() {
        while (true) {
            printTitle("�ɼ����� ");
            System.out.println("1. ��ӳɼ���Ϣ");
            System.out.println("2. �޸ĳɼ���Ϣ");
            System.out.println("3. ɾ���ɼ���Ϣ");
            System.out.println("4. ��ѯĳ�γ̵����гɼ���ѧ������");
            System.out.println("5. ��ѯĳ�γ̵����гɼ�����������");
            System.out.println("6. ��ѯĳѧ�������гɼ����γ̺�����");
            System.out.println("7. ��ѯĳѧ�������гɼ�����������");
            System.out.println("0. ������һ��");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateScore("���", "��ӳɼ���Ϣ");
                    break;
                case 2:
                    printAddOrUpdateScore("�޸�", "�޸ĳɼ���Ϣ");
                    break;
                case 3:
                    printDeleteScore();
                    break;
                case 4:
                    printScoreByCourseOrderBy(ScoreDao.STUDENT_ID, "�γ̳ɼ���ѧ������");
                    break;
                case 5:
                    printScoreByCourseOrderBy(ScoreDao.SCORE, "�γ̳ɼ�����������");
                    break;
                case 6:
                    printScoreByStudentOrderBy(ScoreDao.COURSE_ID, "ѧ���ɼ����γ̺�����");
                    break;
                case 7:
                    printScoreByStudentOrderBy(ScoreDao.SCORE, "ѧ���ɼ�����������");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("�������");
                    break;
            }
        }

    }

    private void printScoreByStudentOrderBy(String orderColumn, String title) {
        printTitle(title);
        String studentId = doInput("ѧ��", scanner);
        String format = "%-10s";
        try {
            List<Score> scoreList = scoreDao.findByStudentOrderBy(studentId, orderColumn);
            System.out.printf(format, "ѧ��");
            System.out.printf(format, "����");
            System.out.printf(format, "�γ̺�");
            System.out.printf(format, "�γ���");
            System.out.printf(format, "����");
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
            System.out.println("��ѯ����");
        }
    }

    private void printScoreByCourseOrderBy(String orderColumn, String title) {
        printTitle(title);
        String courseId = doInput("�γ̺�", scanner);
        String format = "%-10s";
        try {
            List<Score> scoreList = scoreDao.findByCourseOrderBy(courseId, orderColumn);
            System.out.printf(format, "ѧ��");
            System.out.printf(format, "����");
            System.out.printf(format, "�γ̺�");
            System.out.printf(format, "�γ���");
            System.out.printf(format, "����");
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
            System.out.println("��ѯ����");
        }
    }

    private void printDeleteScore() {
        printTitle("ɾ���ɼ���Ϣ");
        String studentId = doInput("ѧ��", scanner);
        String courseId = doInput("�γ̺�", scanner);
        try {
            scoreDao.delete(studentId, courseId);
            System.out.println("ɾ���ɹ���");
        } catch (SQLException e) {
            System.out.println("ɾ��ʧ�ܣ�");
        }
    }

    private void printAddOrUpdateScore(String action, String title) {
        printTitle(title);
        String studentId = doInput("ѧ��", scanner);
        String courseId = doInput("�γ̺�", scanner);
        int score = doInputScore(scanner);

        try {
            scoreDao.insertOrUpdate(new Score(new Student(studentId), new Course(courseId), score));
            System.out.println(action + "�ɹ���");
        } catch (SQLException e) {
            System.out.println(action + "ʧ�ܣ�");
        }
    }

    private static int doInputScore(Scanner scanner) {
        while (true) {
            try {
                System.out.print("������");
                String tmp = scanner.next();
                int score=Integer.parseInt(tmp);

                if (score >= 0 && score <= 100) {
                    return score;
                }
                System.out.println("����������������룡");
            } catch (Exception e) {
                System.out.println("����������������룡");
            }
        }
    }

    private void printCourseManage() {
        while (true) {
            printTitle("�γ̹��� ");
            System.out.println("1. ��ӿγ���Ϣ");
            System.out.println("2. �޸Ŀγ���Ϣ");
            System.out.println("3. ɾ���γ���Ϣ");
            System.out.println("4. ���γ̺�����");
            System.out.println("5. ���γ�������");
            System.out.println("0. ������һ��");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateCourse("���", "��ӿγ���Ϣ");
                    break;
                case 2:
                    printAddOrUpdateCourse("�޸�", "�޸Ŀγ���Ϣ");
                    break;
                case 3:
                    printDeleteCourse();
                    break;
                case 4:
                    printCourseOrderBy(StudentDao.ID, "���γ̺�����");
                    break;
                case 5:
                    printCourseOrderBy(StudentDao.NAME, "���γ�������");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("�������");
                    break;
            }
        }
    }

    private void printCourseOrderBy(String orderColumn, String title) {
        printTitle(title);
        String format = "%-10s";
        try {
            List<Course> courseList = courseDao.findAll(orderColumn);
            System.out.printf(format, "�γ̺�");
            System.out.printf(format, "�γ���");
            System.out.printf(format, "ѧԺ");
            System.out.println();

            for (Course c : courseList) {
                System.out.printf(format, c.getId());
                System.out.printf(format, c.getName());
                System.out.printf(format, c.getAcademy());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("��ѯ����");
        }

    }

    private void printDeleteCourse() {
        printTitle("ɾ���γ���Ϣ");
        String id = doInput("�γ̺�", scanner);
        try {
            courseDao.delete(id);
            System.out.println("ɾ���ɹ���");
        } catch (SQLException e) {
            System.out.println("ɾ��ʧ�ܣ�");
        }
    }

    private void printAddOrUpdateCourse(String action, String title) {
        printTitle(title);
        String id = doInput("�γ̺�", scanner);
        String name = doInput("�γ���", scanner);
        String academy = doInput("ѧԺ", scanner);

        try {
            courseDao.insertOrUpdate(new Course(id, name, academy));
            System.out.println(action + "�ɹ���");
        } catch (SQLException e) {
            System.out.println(action + "ʧ�ܣ�");
        }
    }

    private void printStudentManage() {
        while (true) {
            printTitle("ѧ������ ");
            System.out.println("1. ���ѧ����Ϣ");
            System.out.println("2. �޸�ѧ����Ϣ");
            System.out.println("3. ɾ��ѧ����Ϣ");
            System.out.println("4. ��ѧ������");
            System.out.println("5. ����������");
            System.out.println("0. ������һ��");

            int choice = doChoice(scanner);
            switch (choice) {
                case 1:
                    printAddOrUpdateStudent("���", "���ѧ����Ϣ");
                    break;
                case 2:
                    printAddOrUpdateStudent("�޸�", "�޸�ѧ����Ϣ");
                    break;
                case 3:
                    printDeleteStudent();
                    break;
                case 4:
                    printStudentOrderBy(StudentDao.ID, "��ѧ������");
                    break;
                case 5:
                    printStudentOrderBy(StudentDao.NAME, "����������");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("�������");
                    break;
            }
        }
    }

    private void printStudentOrderBy(String orderColumn, String title) {
        printTitle(title);
        String format = "%-10s";
        try {
            List<Student> studentList = studentDao.findAll(orderColumn);
            System.out.printf(format, "ѧ��");
            System.out.printf(format, "����");
            System.out.println();

            for (Student s : studentList) {
                System.out.printf(format, s.getId());
                System.out.printf(format, s.getName());
                System.out.println();
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("��ѯ����");
        }

    }

    private void printDeleteStudent() {
        printTitle("ɾ��ѧ����Ϣ");
        String id = doInput("ѧ��", scanner);
        try {
            studentDao.delete(id);
            System.out.println("ɾ���ɹ���");
        } catch (SQLException e) {
            System.out.println("ɾ��ʧ�ܣ�");
        }
    }

    private void printAddOrUpdateStudent(String action, String title) {
        printTitle(title);
        String id = doInput("ѧ��", scanner);
        String name = doInput("����", scanner);

        try {
            studentDao.insertOrUpdate(new Student(id, name));
            System.out.println(action + "�ɹ���");
        } catch (SQLException e) {
            System.out.println(action + "ʧ�ܣ�");
        }
    }

    private static String doInput(String title, Scanner scanner) {
        System.out.print(title + "��");
        String tmp = scanner.next();
        //        System.out.println();
        return tmp;
    }

    private static int doChoice(Scanner scanner) {
        try {
            System.out.print("��ѡ��");
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
