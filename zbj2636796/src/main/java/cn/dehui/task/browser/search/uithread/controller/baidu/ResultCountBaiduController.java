package cn.dehui.task.browser.search.uithread.controller.baidu;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.util.Status;

public class ResultCountBaiduController extends BaiduController {

    private static final String getLastPageUrlJs = "var as=document.getElementById('page').getElementsByTagName('a'); return as[as.length-2].href;";

    private static final String resultStatsJs    = "var page=document.getElementById('page'); return page.children[page.children.length-1].innerHTML;";

    public ResultCountBaiduController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    @Override
    protected void handleSearchResult(final JWebBrowser webBrowser) {
        switch (status) {
            case KEYWORD_SEARCHING:
                if (searchContext.quoteProbableResultCount == 0) {
                    searchContext.quoteProbableResultCount = getNumberFromResultStats(webBrowser);
                    System.out.print(searchContext.quoteProbableResultCount + ", ");
                }

                if (!meetEnd()) {
                    lastPage();
                    return;
                }

                long tens = 0;
                tens = calculateTens(webBrowser, tens);
                int digit = collectUrls().size();

                searchContext.quoteResultCount = tens + digit;
                System.out.print(searchContext.quoteResultCount + ", ");

                status = Status.SITE_SEARCHING;

                String siteKeyword = "site:" + searchContext.keyword.substring(1, searchContext.keyword.length() - 1);
                searchKeywordInMainPage(siteKeyword);
                break;

            case SITE_SEARCHING:
                searchContext.siteResultCount = getNumberFromResultStats(webBrowser);
                System.out.print(searchContext.siteResultCount + ", ");
                System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                callback.execute();
                break;
            default:
                break;
        }
    }

    private long calculateTens(final JWebBrowser webBrowser, long tens) {
        String currentUrl = webBrowser.getResourceLocation();
        String paramStr = currentUrl.split("\\?", 2)[1];
        String[] params = paramStr.split("&");
        for (String param : params) {
            if (param.startsWith("pn=")) {
                return Long.parseLong(param.substring("pn=".length()));
            }
        }
        //        System.err.println("URL with no pn parameter: " + currentUrl);
        // 只有一页
        return 0;
    }

    private long getNumberFromResultStats(final JWebBrowser webBrowser) {
        String resultStats;
        Object resultStatsObj;

        resultStatsObj = webBrowser.executeJavascriptWithResult(resultStatsJs);

        if (resultStatsObj != null) {
            // 百度为您找到相关结果约500,000个
            // 百度为您找到相关结果18个
            resultStats = resultStatsObj.toString().substring("百度为您找到相关结果".length()).replaceAll("约", "")
                    .replaceAll("个", "");
            try {
                return Long.parseLong(resultStats.replaceAll(",", ""));
            } catch (Exception e) {
                System.err.println(resultStats);
            }
        }

        return 0;
    }

    private void lastPage() {
        JWebBrowser webBrowser = getWebBrowser();
        String url = (String) webBrowser.executeJavascriptWithResult(getLastPageUrlJs);
        if (url != null) {
            webBrowser.navigate(url);
        } else {
            webBrowser.executeJavascript(nextPageJs);
        }
    }

    public static void main(String[] args) {
        //        String s = "\"en.wikipedia.org/wiki/Money_(That's_What_I_Want)\"";
        //        System.out.println(s.replaceAll("'", "\\\\'"));

        String currentUrl = "http://www.baidu.com/s?wd=www.163.com&pn=20&ie=utf-8";
        String paramStr = currentUrl.split("\\?", 2)[1];
        String[] params = paramStr.split("&");
        for (String param : params) {
            if (param.startsWith("pn=")) {
                System.out.println(Long.parseLong(param.substring("pn=".length())));
            }
        }
    }
}
