package cn.dehui.zbj1984105;

import javax.swing.tree.DefaultMutableTreeNode;

public class CategoryTreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 4093594193031592432L;

    public String             id;

    public String             parentId;

    public String             title;

    public CategoryTreeNode(String id, String parentId, String title) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
