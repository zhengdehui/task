package cn.dehui.task.keywordtool.browser.controller.util;

public class BaiduKeywordInfo {

    private static final String TPL     = "\"%s\",\"%s\",\"%s\"";

    public String               keyword = "";

    public String               dailySearches;

    public String               reason="";

    @Override
    public String toString() {
        return String.format(TPL, keyword, dailySearches, reason.isEmpty()?"其他":reason);
    }
}
