package cn.dehui.task.keywordtool.selenium.controller;

public class AdwordsKeywordInfo {

    private static final String TPL     = "\"%s\",\"%s\",\"%s\"";

    public String               keyword = "";

    public String               competition;

    public String               avgMonthlySearches;

    @Override
    public String toString() {
        return String.format(TPL, keyword, competition, avgMonthlySearches);
    }
}