package cn.dehui.task.browser.search.uithread.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchContext {

    public SearchContext(String keyword) {
        this.keyword = keyword;
    }

    public String               keyword;

    public List<String>         urlList          = new ArrayList<String>();

    public Map<String, Integer> statisticMap     = new HashMap<String, Integer>();

    public long                 siteResultCount  = 0;

    public long                 quoteResultCount = 0;

    public void clear() {
        urlList.clear();
        statisticMap.clear();
        siteResultCount = 0;
        quoteResultCount = 0;
    }
}
