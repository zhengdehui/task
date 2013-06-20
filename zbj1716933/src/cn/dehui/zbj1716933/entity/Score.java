package cn.dehui.zbj1716933.entity;

public class Score {

    private Course  course;

    private Student student;

    private int     score;

    public Score(Student student, Course course, int score) {
        this.course = course;
        this.student = student;
        this.score = score;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

}
