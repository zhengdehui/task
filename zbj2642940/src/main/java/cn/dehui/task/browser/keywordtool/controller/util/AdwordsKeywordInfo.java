package cn.dehui.task.browser.keywordtool.controller.util;

public class AdwordsKeywordInfo {

    private static final String TPL = "\"%s\",\"%s\",\"%s\",\"%s\"";

    public String               keyword = "";

    public String               competition;

    public String               globalMonthlySearches;

    public String               localMonthlySearches;

    @Override
    public String toString() {
        return String.format(TPL, keyword, competition, globalMonthlySearches, localMonthlySearches);
    }
}
