package cn.dehui.task.browser.search.uithread.controller.google2;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.util.Status;

public class ResultCountGoogleController2 extends GoogleController2 {

    private static final String lastPageJs    = "var tr=document.getElementById('pnnext').parentNode.parentNode; tr.children[tr.children.length-2].children[0].click();";

    private static final String resultStatsJs = "return document.getElementById('resultStats').innerHTML;";

    public ResultCountGoogleController2(ControllerManager controllerManager) {
        super(controllerManager);
    }

    @Override
    protected void handleSearchResult(final JWebBrowser webBrowser) {
        String resultStats;
        Object resultStatsObj;
        switch (status) {
            case KEYWORD_SEARCHING:
                if (!meetEnd()) {
                    webBrowser.executeJavascript(lastPageJs);
                    return;
                }

                long tens = 0;
                resultStatsObj = webBrowser.executeJavascriptWithResult(resultStatsJs);
                if (resultStatsObj != null) {
                    resultStats = resultStatsObj.toString();
                    if (resultStats.startsWith("Page ")) {
                        String[] parts = resultStats.split(" ", 3);
                        tens = (Long.parseLong(parts[1]) - 1) * 10;
                    }
                }
                int digit = collectUrls().size();

                searchContext.quoteResultCount = tens + digit;
                System.out.print(searchContext.quoteResultCount + ", ");

                status = Status.SITE_SEARCHING;

                String siteKeyword = "site:" + searchContext.keyword.substring(1, searchContext.keyword.length() - 1);
                searchKeywordInMainPage(siteKeyword);
                break;

            case SITE_SEARCHING:
                resultStatsObj = webBrowser.executeJavascriptWithResult(resultStatsJs);

                if (resultStatsObj != null) {
                    resultStats = resultStatsObj.toString().replaceAll("About ", "");

                    String[] parts = resultStats.split(" ", 2);

                    try {
                        searchContext.siteResultCount = Long.parseLong(parts[0].replaceAll(",", ""));
                    } catch (Exception e) {
                        System.err.println(resultStats);
                    }
                }
                System.out.print(searchContext.siteResultCount + ", ");
                System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                callback.execute();
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        String s = "\"en.wikipedia.org/wiki/Money_(That's_What_I_Want)\"";

        System.out.println(s.replaceAll("'", "\\\\'"));
    }
}
