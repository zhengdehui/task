package cn.dehui.zbj1984105;

public class KeyValueEntry {

    public String id;

    public String title;

    public KeyValueEntry(String id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
