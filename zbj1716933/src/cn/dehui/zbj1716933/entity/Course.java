package cn.dehui.zbj1716933.entity;

public class Course {
    private String id;

    private String name;

    private String academy;

    public Course(String id) {
        this.id = id;
    }

    public Course(String id, String name, String academy) {
        this.id = id;
        this.name = name;
        this.academy = academy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademy() {
        return academy;
    }

    public void setAcademy(String academy) {
        this.academy = academy;
    }
}
